# Connection Draining & Lifecycle Hooks

Terminating an instance immediately drops whatever it was doing. Two mechanisms create a window to finish work: **deregistration delay** on the load balancer, and **lifecycle hooks** on the Auto Scaling group.

---

## Deregistration Delay

Also called **connection draining**. When a target is deregistered — because it is being terminated or has failed a health check — the load balancer stops sending it *new* requests but allows in-flight ones to complete for a configured period.

```bash
aws elbv2 modify-target-group-attributes \
  --target-group-arn arn:aws:elasticloadbalancing:...:targetgroup/app/abc \
  --attributes Key=deregistration_delay.timeout_seconds,Value=60
```

*Gives in-flight requests 60 seconds to finish before the target is fully removed. The default is 300 seconds.*

Setting it well:

**Slightly longer than the longest normal request.** For a typical API, 30 seconds is generous. For a service with long uploads or streaming responses, longer.

**Not too long.** The delay applies to every deployment and every scale-in, so a 300-second default makes a rolling deployment across ten instances take much longer than necessary.

Note that the target enters `draining` state during this period. An instance that appears stuck in `draining` is usually holding long-lived connections — WebSockets in particular, which do not finish on their own.

---

## Lifecycle Hooks

A lifecycle hook pauses an instance in a transitional state and waits for a signal, giving time for work the load balancer knows nothing about.

Two points where a hook can pause:

**`autoscaling:EC2_INSTANCE_LAUNCHING`** — the instance is running but not yet in service. Used to complete configuration, warm caches, or register with an external system before traffic arrives.

**`autoscaling:EC2_INSTANCE_TERMINATING`** — the instance is being terminated but not yet gone. Used to finish work, upload logs, deregister from external systems, or drain a queue.

```bash
aws autoscaling put-lifecycle-hook \
  --auto-scaling-group-name app-servers \
  --lifecycle-hook-name drain-work \
  --lifecycle-transition autoscaling:EC2_INSTANCE_TERMINATING \
  --heartbeat-timeout 300 \
  --default-result CONTINUE
```

*Pauses termination for up to 300 seconds. `CONTINUE` means proceed with termination if the timeout expires without a signal — `ABANDON` would terminate immediately instead.*

The instance signals when it is finished:

```bash
aws autoscaling complete-lifecycle-action \
  --auto-scaling-group-name app-servers \
  --lifecycle-hook-name drain-work \
  --lifecycle-action-result CONTINUE \
  --instance-id i-0abc123
```

*Releases the hook early rather than waiting out the full timeout, which keeps scale-in and deployments responsive.*

Hooks also emit EventBridge events, so an external Lambda function can perform the work and signal completion — useful when the work should not depend on the instance being healthy.

---

## The Full Termination Sequence

```
Scale-in decision
   │
   ├─► Deregister from target group
   │      └─► Deregistration delay: in-flight requests finish
   │
   ├─► Lifecycle hook: EC2_INSTANCE_TERMINATING
   │      └─► Instance drains queues, flushes logs, deregisters externally
   │      └─► complete-lifecycle-action  (or timeout)
   │
   └─► Instance terminated
```

*Two independent windows: the load balancer's, covering HTTP requests, and the lifecycle hook's, covering everything else.*

They handle different things. Deregistration delay only knows about load balancer traffic. A worker consuming from SQS is not behind a load balancer at all, so only a lifecycle hook gives it time to finish.

---

## Spot Interruptions

Spot instances get **two minutes' notice**, delivered through instance metadata and as an EventBridge event. That is a hard limit and cannot be extended.

Two minutes is enough to deregister from a target group, finish short requests, and checkpoint work — but only if something is listening for the notice. **EC2 Auto Scaling handles the load balancer deregistration automatically** when it receives the interruption notice; application-level work still needs a handler.

For SQS-based workers, the interruption often needs no special handling at all: an in-flight message whose visibility timeout expires is redelivered to another consumer automatically. Designing for at-least-once delivery makes interruption a non-event.

---

## Practical Notes

**Handle `SIGTERM`.** The instance's shutdown sends it. An application that ignores it is killed with `SIGKILL` shortly after, losing whatever it was doing regardless of any configured delay.

**Do not rely on hooks for correctness.** A hook is best-effort — a host failure runs no hook at all. Anything that must not be lost needs a mechanism that survives sudden death, such as a queue with redelivery.

**Watch the timeout.** An instance held in `Terminating:Wait` for the full timeout on every scale-in makes deployments slow. Signalling completion promptly matters.

**Test it.** Terminate an instance under load and watch for dropped requests. Draining configuration is easy to get wrong and only shows up under real traffic.

---

## Key Takeaways

- Deregistration delay lets in-flight load balancer requests finish before a target is removed; the default of 300 seconds is usually longer than needed.
- Set it slightly above the longest normal request, since it applies to every deployment and scale-in.
- Lifecycle hooks pause instances at launch or termination for work the load balancer knows nothing about.
- Signal completion with `complete-lifecycle-action` rather than waiting out the timeout.
- The two mechanisms are independent — queue workers need a hook because they are not behind a load balancer.
- Spot instances get a fixed two-minute notice; Auto Scaling handles deregistration, but application work needs a handler.
- Handle `SIGTERM`, and treat hooks as best-effort — durable work needs a queue that redelivers.
