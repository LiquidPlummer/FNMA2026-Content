# Cluster Architecture & Serverless

How Redshift is deployed, and the two forms it takes.

---

## Provisioned Clusters

A Redshift cluster has one **leader node** and one or more **compute nodes**.

```
Client ──SQL──► Leader node
                    │  parses, optimizes, generates code, distributes
                    ├──► Compute node 1  (slices 0, 1, 2, 3)
                    ├──► Compute node 2  (slices 4, 5, 6, 7)
                    └──► Compute node 3  (slices 8, 9, 10, 11)
                              │
                         results back to leader, aggregated, returned
```

*The leader plans and coordinates; compute nodes do the work in parallel on their own data.*

**The leader node** parses queries, builds execution plans, distributes compiled code to compute nodes, and aggregates results. It stores no user data and is not separately billed.

**Compute nodes** hold the data and execute in parallel. Each is divided into **slices**, and each slice processes a portion of the node's data — so parallelism is the number of slices, not the number of nodes.

**A single-node cluster** has one node serving both roles, suitable only for development.

---

## Node Types

**RA3 nodes** separate compute from storage. Data lives in **Redshift Managed Storage** backed by S3, with local SSD as a cache. Compute and storage scale independently, which means a cluster can be sized for query load rather than for data volume. This is the current default.

**DC2 nodes** use local SSD storage, coupling the two — more storage requires more nodes. Older, and appropriate only for small datasets needing high performance per dollar.

The RA3 separation matters practically: growing data no longer forces adding compute that is not needed, and cross-cluster data sharing becomes possible because storage is not node-local.

---

## Redshift Serverless

**Serverless** removes cluster management entirely. Capacity is measured in **Redshift Processing Units (RPUs)**, scaling automatically with workload, and billing is per second while queries run.

```bash
aws redshift-serverless create-workgroup \
  --workgroup-name analytics \
  --namespace-name analytics-ns \
  --base-capacity 32
```

*A workgroup holds compute settings; a namespace holds the data and its credentials. The two are separate so compute can change without touching data.*

**Serverless suits:**

- Intermittent or unpredictable query patterns
- Development and test environments
- Workloads with long idle periods
- Teams that do not want to size a cluster

**Provisioned suits:**

- Steady, predictable, high-volume workloads
- Cases where reserved-instance pricing meaningfully reduces cost
- Workloads needing specific tuning that serverless abstracts away

The decisive factor is usually utilization. **Serverless bills only while queries run**, so a cluster queried a few hours a day is much cheaper serverless. A cluster running continuously at high utilization is cheaper provisioned, especially with a reservation.

---

## Concurrency Scaling

A provisioned cluster has fixed capacity, so concurrent queries compete and queue. **Concurrency scaling** adds transient capacity automatically during bursts, with credits accruing that cover a period of use each day before charges begin.

It addresses the specific problem of a dashboard refresh or a reporting window producing many simultaneous queries against a cluster sized for normal load.

---

## Pausing and Snapshots

**Provisioned clusters can be paused**, which stops compute billing while retaining storage. For a cluster used only during business hours, pausing it overnight and at weekends is a substantial saving, and it can be scheduled.

**Snapshots** are automated and incremental, retained for a configurable period, and can be copied to another region. Restoring creates a new cluster.

---

## Sizing

Two practical points.

**Start smaller than expected.** Resizing is straightforward — elastic resize takes minutes for supported changes — and over-provisioning tends to persist because nobody revisits it.

**Watch for disk-based queries.** When a query's intermediate results exceed available memory, Redshift spills to disk and performance collapses. `SVL_QUERY_SUMMARY` shows which queries did this, and it is usually a sign of a query needing optimization rather than a cluster needing to be larger.

---

## Key Takeaways

- A cluster has a leader node that plans and coordinates, and compute nodes divided into slices that execute in parallel.
- Parallelism follows slice count, not node count, and the leader node stores no data.
- RA3 nodes separate compute from storage so each scales independently; DC2 couples them.
- Serverless scales capacity automatically and bills per second while queries run, suiting intermittent workloads.
- Provisioned clusters suit steady, high-utilization workloads, especially with reserved pricing.
- Concurrency scaling adds transient capacity during query bursts.
- Pausing a provisioned cluster stops compute billing and can be scheduled around working hours.
- Disk-based queries indicate memory exhaustion and usually point to a query problem rather than a sizing one.
