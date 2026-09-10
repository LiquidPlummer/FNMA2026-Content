# Health Checks

A **health check** determines whether a target receives traffic. It is a small piece of configuration with an outsized effect: a badly configured health check can take a working application offline, and a badly written health endpoint can keep a broken one in rotation.

---

## How They Work

The load balancer periodically requests a configured path from each target and evaluates the response:

| Setting | Meaning | Typical |
|---|---|---|
| `HealthCheckPath` | What to request | `/health` |
| `HealthCheckIntervalSeconds` | How often | 30 |
| `HealthCheckTimeoutSeconds` | How long to wait | 5 |
| `HealthyThresholdCount` | Successes to mark healthy | 2 |
| `UnhealthyThresholdCount` | Failures to mark unhealthy | 2 |
| `Matcher` | Acceptable status codes | `200` |

A target starts `unhealthy`, becomes `healthy` after enough consecutive successes, and returns to `unhealthy` after enough consecutive failures. Only healthy targets receive traffic.

The arithmetic matters: with a 30-second interval and a threshold of 2, a failed instance keeps receiving traffic for up to a minute. Tightening to a 10-second interval detects failure in 20 seconds, at the cost of more check traffic and less tolerance for a slow response.

---

## How a Health Check Takes an Application Offline

The failure that catches people: the health check fails for every instance simultaneously, the load balancer marks them all unhealthy, and it stops sending traffic anywhere. The application is running fine and serving nothing.

Common causes:

**The path requires authentication.** A `/health` endpoint behind auth returns 401, which does not match `200`.

**The security group blocks the check.** The load balancer's checks come from the ALB's security group; if targets only allow application traffic on a different port, checks fail.

**The timeout is too short.** A health endpoint that queries a database can exceed a 2-second timeout under load — precisely when instances are most needed.

**The endpoint checks too much.** This is the most damaging case, and it deserves its own section.

**The path does not exist.** A 404 does not match `200`, and the application is otherwise working perfectly.

---

## What a Health Endpoint Should Test

The principle: **a health check answers "should this instance receive traffic?", not "is the entire system working?"**

Consider an endpoint that checks the database, a cache, and three downstream APIs, returning 500 if any is unavailable. When the database has a brief problem, *every* instance reports unhealthy at once, the load balancer removes all of them, and a recoverable database blip becomes a total outage. Worse, the Auto Scaling group may then terminate and replace every instance, which does not fix the database and destroys any warm state.

A shared dependency failing should degrade the service, not remove all capacity.

**A shallow check** verifies the process is running and able to respond:

```python
@app.route("/health")
def health():
    return {"status": "ok"}, 200
```

*Returns 200 whenever the process can serve a request. It answers exactly the load balancer's question and nothing more.*

**A deep check** verifies dependencies and belongs at a different endpoint, used by monitoring rather than by the load balancer:

```python
@app.route("/health/deep")
def deep_health():
    checks = {
        "database": check_database(),
        "cache": check_cache(),
    }
    ok = all(checks.values())
    return {"checks": checks}, (200 if ok else 503)
```

*Reports dependency status for alarms and dashboards. Pointing a load balancer at this endpoint is what causes correlated removal of all targets.*

The useful middle ground is a shallow check that also returns unhealthy for conditions genuinely specific to that instance — a full disk, an exhausted connection pool, a failed startup — because those are cases where removing this instance actually helps.

---

## Auto Scaling Health Checks

An Auto Scaling group has its own notion of health, and it can use two sources:

**EC2 status checks** (default) — the instance is unhealthy only if EC2 reports a system or instance status failure. An instance whose application has crashed still passes.

**ELB health checks** — the group also treats the load balancer's assessment as authoritative, so an instance failing its application health check is replaced.

**Enabling ELB health checks on the Auto Scaling group is what makes automatic recovery work.** Without it, a crashed application on a running instance is never replaced, because EC2 sees a healthy virtual machine.

The **health check grace period** is the companion setting: how long after launch before health checks count. Too short, and instances are terminated while still starting, producing a launch-terminate loop that never converges. It should exceed the application's realistic worst-case startup time.

---

## Key Takeaways

- Health checks determine which targets receive traffic, using interval, timeout, and consecutive-success thresholds.
- A misconfigured check can mark every target unhealthy and take a working application offline.
- Common causes: authenticated paths, security groups blocking checks, short timeouts, and missing endpoints.
- Load balancer health checks should be shallow — answering whether this instance can serve, not whether the whole system is well.
- Deep dependency checks belong on a separate endpoint used by monitoring, since a shared dependency failing would otherwise remove all capacity at once.
- Enable ELB health checks on the Auto Scaling group, or crashed applications on healthy instances are never replaced.
- Set the grace period longer than realistic startup time to avoid a launch-terminate loop.
