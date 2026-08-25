# Redshift Query App (Java)

We'll stand up a small Redshift Serverless data warehouse, then write a Java app that creates a table, seeds it with sample sales data, and runs an aggregate query across it — total sales by region. Where the DynamoDB lab was all about fast, exact lookups by key, this one is the OLAP side of the comparison from the Redshift notes: scanning and summarizing a whole table at once, not fetching one record at a time.

We'll query Redshift through the **Redshift Data API** rather than a direct JDBC connection. That's a deliberate choice: the Data API lets our app run SQL over ordinary, IAM-signed AWS API calls, so there's no database password to manage, no need to make the cluster reachable from our laptop's network, and no separate JDBC driver dependency — the same AWS credentials we already use for the CLI are enough.

## Prerequisites

| Software | Required Version |
|---|---|
| JDK | 21 or later (LTS) |
| Maven | 3.9.x or later |
| AWS CLI v2 | 2.27 or later — used to provision Redshift Serverless |
| AWS account credentials | Configured locally via `aws configure`, with permissions to manage Redshift Serverless and call the Redshift Data API (see step 2 below) |

The AWS SDK dependency (`software.amazon.awssdk:redshiftdata`, pinned via the SDK's BOM at version 2.49.6) is already wired up in `pom.xml`.

`cd` into this lab's directory before starting.

## Guided Walkthrough

### 1. Create a Redshift Serverless namespace and workgroup

A **namespace** holds the actual data (databases, tables, users); a **workgroup** is the compute that runs queries against it. We'll use your account's default VPC subnets — no public accessibility setting is needed, since the Data API never connects to the database port directly.

```bash
export AWS_REGION=us-east-1
export NAMESPACE_NAME=redshift-lab-ns
export WORKGROUP_NAME=redshift-lab-wg
export DATABASE_NAME=dev

aws redshift-serverless create-namespace \
  --namespace-name $NAMESPACE_NAME \
  --db-name $DATABASE_NAME \
  --region $AWS_REGION

SUBNET_IDS=$(aws ec2 describe-subnets \
  --filters Name=default-for-az,Values=true \
  --query 'Subnets[].SubnetId' --output text --region $AWS_REGION)

aws redshift-serverless create-workgroup \
  --namespace-name $NAMESPACE_NAME \
  --workgroup-name $WORKGROUP_NAME \
  --base-capacity 8 \
  --subnet-ids $SUBNET_IDS \
  --region $AWS_REGION
```
*Creates the namespace (with a `dev` database inside it, matching `DATABASE_NAME`) and a workgroup sized at 8 RPUs — Redshift Serverless's unit of compute capacity, and a broadly-supported minimum at the time of writing. (Some regions now support as low as 4 RPUs — if `create-workgroup` rejects 8, check the current minimum for your region and adjust.) Notice there's no cluster size or node type to choose here at all — the serverless model handles that the same way Lambda does for compute, just for a data warehouse instead of a function.*

Wait for it to become available:

```bash
aws redshift-serverless get-workgroup --workgroup-name $WORKGROUP_NAME --region $AWS_REGION \
  --query 'workgroup.status' --output text
```
*Re-run this until it prints `AVAILABLE` — typically a few minutes.*

### 2. Make sure your credentials can use the Data API

Querying through the Data API with no password requires permission to mint temporary database credentials on your behalf. If the identity you configured with `aws configure` doesn't already have broad Redshift access, attach a policy scoped to just what this lab needs:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "redshift-serverless:GetCredentials",
        "redshift-data:ExecuteStatement",
        "redshift-data:DescribeStatement",
        "redshift-data:GetStatementResult"
      ],
      "Resource": "*"
    }
  ]
}
```
*The same least-privilege instinct from the IAM lesson applies here — this grants exactly the four actions the Data API workflow uses, nothing broader.*

### 3. Get oriented in `App.java`

Open `src/main/java/com/revature/labs/redshift/App.java`. Three SQL statements are already defined as constants: one to create a `sales` table, one to seed it with eight sample rows across three regions, and one to aggregate total sales per region. `main()` runs all three in order, printing the aggregate results at the end.

Two methods are left as `TODO`s: `executeAndWait`, which every one of those three SQL statements flows through, and `printResults`, which turns the final query's output into something readable.

### 4. Submit a statement and wait for it to finish

The Redshift Data API is **asynchronous** — `ExecuteStatement` hands back a statement ID immediately, before the query has actually run. Finding out whether (and when) it finished means polling `DescribeStatement` separately. Find the TODO in `executeAndWait` and replace it with:

```java
ExecuteStatementResponse submitted = client.executeStatement(ExecuteStatementRequest.builder()
        .workgroupName(workgroupName)
        .database(database)
        .sql(sql)
        .build());
String id = submitted.id();

DescribeStatementResponse status;
do {
    try {
        Thread.sleep(500);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
    }
    status = client.describeStatement(DescribeStatementRequest.builder().id(id).build());
} while (status.status() == StatusString.SUBMITTED
        || status.status() == StatusString.PICKED
        || status.status() == StatusString.STARTED);

if (status.status() != StatusString.FINISHED) {
    throw new RuntimeException("Statement failed: " + status.error());
}

return id;
```
*Submits the SQL, then loops — checking every half-second — until the statement leaves the "in progress" states (`SUBMITTED`, `PICKED`, `STARTED`). If it lands on `FAILED` or `ABORTED` instead of `FINISHED`, we surface Redshift's own error message rather than silently continuing.*

### 5. Fetch and print the result set

Find the TODO in `printResults` and replace it with:

```java
GetStatementResultResponse result = client.getStatementResult(
        GetStatementResultRequest.builder().id(statementId).build());

List<String> columnNames = result.columnMetadata().stream()
        .map(ColumnMetadata::name)
        .collect(Collectors.toList());
System.out.println("  " + columnNames);

for (List<Field> row : result.records()) {
    List<String> values = row.stream().map(App::fieldToString).collect(Collectors.toList());
    System.out.println("  " + values);
}
```
*`columnMetadata()` gives us the column names once; `records()` gives us the rows, each one a list of `Field` values in the same order as those columns. The `fieldToString` helper already provided in the file handles the fact that `Field` is a tagged union — a string column and a numeric column come back through different accessor methods, so we branch on `field.type()` to read the right one.*

### 6. Run it

```bash
mvn -q exec:java
```
*You should see the table get created, the eight sample rows seeded, and finally the aggregate result: three rows, one per region, each with its `total_sales`, ordered highest to lowest.*

### 7. Clean up

```bash
aws redshift-serverless delete-workgroup --workgroup-name $WORKGROUP_NAME --region $AWS_REGION
aws redshift-serverless delete-namespace --namespace-name $NAMESPACE_NAME --region $AWS_REGION
```
*The workgroup has to go first — Redshift Serverless won't delete a namespace that still has a workgroup attached to it.*

## Exercises

Keep the namespace and workgroup running for these (or recreate them) — each one means editing the SQL constants or adding a new one in `App.java`, then re-running with `mvn -q exec:java`.

1. **Filter before aggregating.** Add a `WHERE category = 'electronics'` clause to a copy of `AGGREGATE_QUERY_SQL` and run it as a new statement. Confirm the totals shrink to reflect only electronics sales per region.

2. **A second grouping dimension.** Write a new query that groups by *both* `region` and `category` (`GROUP BY region, category`), so the result has one row per region/category combination instead of one row per region. Notice this needs no changes to `executeAndWait` or `printResults` at all — only the SQL and the column list change.

3. **Find the top region.** Write a query that returns just the single highest-grossing region (`ORDER BY total_sales DESC LIMIT 1`), run it through the same `executeAndWait` / `printResults` pair, and print a one-line message naming that region rather than a full result table. This is the same underlying plumbing put to a slightly different, more specific question — which is exactly the point of writing `executeAndWait` as reusable, SQL-agnostic infrastructure rather than something hardcoded to one query.
