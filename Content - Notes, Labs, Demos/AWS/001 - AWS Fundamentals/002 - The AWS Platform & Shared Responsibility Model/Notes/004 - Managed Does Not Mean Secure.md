# Managed Does Not Mean Secure

Moving to a managed service transfers operational work. It transfers very little security responsibility, and the parts it does not transfer are the parts that cause most real incidents.

---

## What Stays Ours in Every Case

No matter which service we choose — EC2, RDS, Lambda, S3, or anything else — these remain ours:

**Who can access it.** IAM policies, resource policies, and credentials are ours in every case. AWS has no opinion about which identity in our account should be able to delete a table.

**What network can reach it.** Security groups, subnet placement, and whether something has a public endpoint are configuration we supply. RDS will happily accept `0.0.0.0/0` on port 5432 if we tell it to.

**Whether the data is encrypted.** Most services offer encryption; several require us to turn it on, and some require it at creation time or not at all.

**The data itself.** Whether it should be stored, how long it is kept, whether it is sensitive, and whether it is being logged somewhere it should not be — all ours.

**Application-level correctness.** SQL injection is still SQL injection on RDS. A Lambda function that returns another customer's records is our bug on our fully managed platform.

---

## The Failure Mode

Nearly every widely reported "cloud breach" is a configuration failure on the customer's side of the line, not a failure of the service:

- An S3 bucket made public by a bucket policy, exposing exactly what the policy permitted.
- An RDS instance placed in a public subnet with a security group allowing the whole internet.
- Long-lived access keys committed to a public repository and used within minutes.
- An over-broad IAM policy — often `Action: "*"` on `Resource: "*"` — attached because a narrower one was harder to write.

In each case the service performed correctly. It did what its configuration said. The gap was not in AWS's software; it was in the settings we supplied.

---

## Where the Intuition Goes Wrong

The mistake is treating "managed" as a property of security rather than of operations. It is worth separating the two questions:

| Question | Answered by |
|---|---|
| Who patches the database engine? | Whether it is managed |
| Who can connect to the database? | Us, always |
| Who takes the backups? | Whether it is managed |
| Whether backups are encrypted | Us, always |
| Who scales the service? | Whether it is managed |
| Who may invoke it | Us, always |

*The left column splits cleanly into operational questions, which management answers, and access questions, which it never does.*

Another way to put it: a managed service reliably does what we configured. Security depends on whether what we configured is what we meant.

---

## The Practical Habit

For any service we adopt, answer four questions before it holds anything real:

1. **Who can reach it over the network?** Which security group, which subnet, is there a public endpoint?
2. **Which IAM identities can call it, and with what actions?**
3. **Is the data encrypted at rest, and can that still be changed later?** For several services the answer to the second half is no.
4. **Where do its logs go, and does anything sensitive end up in them?**

None of these are answered by choosing a managed service, and all four are answerable before deployment.

---

## Key Takeaways

- Managed services transfer operational work — patching, backups, scaling — and almost no security responsibility.
- Access control, network exposure, encryption settings, data handling, and application correctness stay with us on every service.
- Most publicized cloud breaches are customer misconfiguration, not provider failure; the service did exactly what it was configured to do.
- "Managed" answers operational questions, never access questions.
- Before a service holds real data, settle network reach, IAM access, encryption, and logging.
