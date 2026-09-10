# One API, Three Clients

Every AWS service is an HTTPS API. The console, the CLI, and the SDKs are three clients that call it. Understanding this collapses a lot of apparent complexity, because it means there is only one thing to learn and three ways to reach it.

---

## The Underlying API

An AWS API call is an HTTPS request to a regional service endpoint, carrying an action name, parameters, and a signature proving who is making it. The signing process (**SigV4**) uses our credentials to sign the request without ever transmitting the secret key.

```
POST https://ec2.us-east-1.amazonaws.com/
Action=RunInstances&ImageId=ami-0abcd1234&InstanceType=t3.micro&...
Authorization: AWS4-HMAC-SHA256 Credential=AKIA.../20260115/us-east-1/ec2/aws4_request, ...
```

*The raw shape of an EC2 API call: a regional endpoint, an action, parameters, and a SigV4 signature identifying the caller.*

We almost never construct these by hand — that is what the clients are for — but knowing the shape explains why every client needs credentials and a region.

---

## The Three Clients

**The console** is a web application that makes API calls on our behalf. It is good for exploring an unfamiliar service, reading state, and one-off investigation. Its weakness is that a console action leaves no artifact describing what was done.

**The CLI** is a command-line program that maps commands to API calls almost one to one:

```bash
aws ec2 run-instances --image-id ami-0abcd1234 --instance-type t3.micro
```

*The CLI's `aws <service> <operation>` shape corresponds directly to the `RunInstances` API call above.*

Its strength is that commands are text: they can be saved, reviewed, version-controlled, and re-run.

**The SDKs** are libraries for Python (`boto3`), Java, JavaScript, Go, and others, used when application code needs to call AWS:

```python
import boto3

ec2 = boto3.client("ec2", region_name="us-east-1")
response = ec2.run_instances(
    ImageId="ami-0abcd1234",
    InstanceType="t3.micro",
    MinCount=1,
    MaxCount=1,
)
```

*The same `RunInstances` call from Python; note that the SDK method name and parameters mirror the API directly.*

---

## Why This Matters

**No client is privileged.** The console cannot do anything the CLI cannot. If an action is possible by clicking, it is possible by command, and the reverse — some API operations have no console UI at all.

**IAM applies identically to all three.** Permissions are granted on API actions (`ec2:RunInstances`), not on tools. Denying someone the ability to launch instances denies it in the console, the CLI, and the SDK at once. There is no "console-only" permission.

**CloudTrail logs all three identically.** Every call is recorded with the identity, the action, the parameters, and the source. A console click and a CLI command produce comparable log entries, distinguished mainly by the recorded user agent.

**Error messages are API errors.** When the console shows a permissions error, it is surfacing the API's response. The same call from the CLI produces the same error, often with more detail — which makes the CLI a good debugging tool even for console-driven work.

---

## Choosing Between Them

| Task | Client |
|---|---|
| Exploring a service, reading state | Console |
| Anything that should be repeatable or reviewed | CLI, or infrastructure as code |
| Calls from application code | SDK |
| Debugging a confusing console error | CLI, for the fuller message |

---

## Key Takeaways

- Every AWS action is an HTTPS API call, signed with SigV4 and sent to a regional endpoint.
- The console, CLI, and SDKs are three clients for that one API, with no capability difference between them.
- IAM permissions apply to API actions, not to tools, so a permission grant covers all three clients equally.
- CloudTrail records calls from all three the same way.
- Console errors are API errors; re-running the call in the CLI is a fast way to get a fuller message.
