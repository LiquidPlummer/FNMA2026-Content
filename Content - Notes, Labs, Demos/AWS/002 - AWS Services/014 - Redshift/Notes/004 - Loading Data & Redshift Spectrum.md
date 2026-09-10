# Loading Data & Redshift Spectrum

How data gets into Redshift efficiently, and how to query data that never enters it at all.

---

## COPY, Not INSERT

**`INSERT` is the wrong way to load data into Redshift.** Row-by-row insertion into a columnar store is slow, produces poorly compressed blocks, and creates work for `VACUUM`.

**`COPY` loads in parallel from S3**, and it is what Redshift is built for:

```sql
COPY orders
FROM 's3://data-lake/orders/2026/09/'
IAM_ROLE 'arn:aws:iam::123456789012:role/RedshiftLoadRole'
FORMAT AS PARQUET;
```

*Every compute slice reads a portion of the files simultaneously — the source of the performance difference.*

The parallelism is the point. A single large file is read by one slice; many files are read by many. The guidance follows:

**Split input into multiple files**, ideally a multiple of the slice count, so every slice has work.

**Aim for roughly 100 MB per file after compression.** Very small files add per-file overhead; very large ones limit parallelism.

**Use a columnar format.** Parquet loads faster than CSV and is smaller.

**Compress the files.** gzip, bzip2, or Parquet's built-in compression reduces transfer time.

---

## Access and Errors

`COPY` needs permission to read from S3, supplied by an IAM role attached to the cluster:

```sql
COPY orders FROM 's3://data-lake/orders/'
IAM_ROLE 'arn:aws:iam::123456789012:role/RedshiftLoadRole'
FORMAT AS CSV
IGNOREHEADER 1
MAXERROR 100;
```

*`MAXERROR` allows a number of bad rows before the load fails, which is useful for imperfect source data and dangerous if set high without checking what was rejected.*

Load errors are recorded rather than only reported:

```sql
SELECT * FROM stl_load_errors ORDER BY starttime DESC LIMIT 10;
```

*Shows the file, line, column, and reason for each rejected row — the first place to look when a load fails or silently drops data.*

---

## Other Load Paths

**`UNLOAD`** writes query results back to S3 in parallel, which is how data leaves Redshift for other tools.

**Auto-copy from S3** loads new files as they arrive, without external orchestration.

**Zero-ETL integrations** replicate from Aurora and RDS into Redshift continuously, removing a pipeline that many teams would otherwise build.

**Data sharing** gives another Redshift cluster read access to data without copying it — how a producer cluster serves several consumer clusters on RA3.

---

## Redshift Spectrum

**Spectrum** queries data in S3 directly, without loading it. External tables are defined in the AWS Glue Data Catalog and joined against Redshift tables in the same query:

```sql
CREATE EXTERNAL SCHEMA spectrum_data
FROM DATA CATALOG DATABASE 'datalake'
IAM_ROLE 'arn:aws:iam::123456789012:role/RedshiftSpectrumRole';

SELECT c.segment, SUM(a.amount)
FROM spectrum_data.archived_orders a
JOIN customers c ON a.customer_id = c.customer_id
WHERE a.order_date >= '2020-01-01'
GROUP BY c.segment;
```

*Archived orders stay in S3 while customers live in Redshift, and the query joins across both.*

Spectrum is billed **per terabyte scanned**, separately from cluster cost. Three practices control that:

**Partition the data in S3** by a filtered column — typically date. A partitioned table scans only relevant prefixes, which is the largest single saving available.

**Use Parquet or ORC.** Columnar formats let Spectrum read only the referenced columns, often reducing scanned bytes by an order of magnitude against CSV.

**Compress.** Less data scanned is less data billed.

### When to use it

**Use Spectrum for** infrequently queried historical data, very large datasets not worth loading, and exploring data before deciding to load it.

**Load into Redshift for** frequently queried data, anything needing fast joins, and anything where consistent query performance matters. Local columnar storage with sort keys and zone maps is faster than scanning S3.

The common arrangement: recent data loaded into Redshift, older data aged out to S3 and queried through Spectrum, with a view spanning both so queries do not need to know where data lives.

---

## Athena and Spectrum

Both query S3 through the Glue Data Catalog. The difference is that **Spectrum runs on a Redshift cluster and can join to Redshift tables**, while **Athena is fully serverless and independent**.

Use Athena when there is no Redshift cluster or no need to join to one. Use Spectrum when the query combines S3 data with data already in Redshift.

---

## Key Takeaways

- Use `COPY` rather than `INSERT`; it loads in parallel across slices and is what Redshift is designed for.
- Split input into many files of roughly 100 MB compressed so every slice participates.
- Prefer Parquet and compression for faster, smaller loads.
- Check `stl_load_errors` when a load fails or drops rows, and be careful with high `MAXERROR` values.
- `UNLOAD` exports in parallel; auto-copy, zero-ETL integrations, and data sharing cover other paths.
- Spectrum queries S3 directly and can join external tables to Redshift tables in one query.
- Spectrum is billed per terabyte scanned — partition by date and use columnar formats to control it.
- Load frequently queried data; leave archival data in S3 behind Spectrum, optionally unified by a view.
