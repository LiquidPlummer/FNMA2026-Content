# Terraform — Quiz Review

The quiz doesn't ask about Terraform directly. It does ask seven questions about the cloud and AWS, covering cloud computing, EC2, Security Groups, AMIs, and API Gateway. Those topics are reviewed here, because the Terraform notes are where AWS resources appear in the course. The last section is a quick map of Terraform's core ideas, for revisiting the module itself.

Each section has a short summary, a few questions to test ourselves with, and links for going deeper. This is a study guide, not an answer key.

> **Heads-up:** most of the cloud and AWS material below isn't in the course notes yet. Those sections are marked, and they link to AWS's own documentation. The summaries here cover what the quiz expects us to know.

---

## Cloud Computing

> **Not yet in the course notes.**

**Cloud computing** means renting computing resources from a provider such as AWS, **on demand and over the internet**, and paying only for what we use. Those resources include servers, storage, databases, networking, and higher-level services. The alternative is buying, installing, and running our own hardware in our own data center.

What that gets us:

- **No up-front hardware purchase.** We pay as we go, instead of buying capacity in advance and hoping we guessed right.
- **Elasticity.** We can add capacity in minutes when demand grows, then remove it (and stop paying for it) when demand drops.
- **Speed.** A new environment takes minutes to create, instead of weeks of ordering and installing hardware.
- **Less to run ourselves.** The provider operates the physical data centers.
- **Global reach.** We can run in regions around the world.

The Terraform notes show the cost side directly. A test environment can be destroyed while it's idle and rebuilt when it's needed, because "cloud resources that don't exist don't cost anything."

**Check yourself**
- A team needs fifty servers for a two-day load test. Compare buying them with renting them from a cloud provider.
- What does it mean for a cloud workload to be "elastic"?

**Go deeper**
- The Workflow: [destroy: Tear It All Down](../Terraform/001%20-%20Terraform%20Intro/004%20-%20The%20Workflow/Notes/001%20-%20The%20Workflow.md#destroy-tear-it-all-down)
- AWS: [What is cloud computing?](https://aws.amazon.com/what-is-cloud-computing/)

---

## EC2 Instances and AMIs

> **Partly covered.** The Terraform notes create EC2 instances from AMIs but don't define either term.

**Amazon EC2 (Elastic Compute Cloud)** is AWS's service for renting virtual servers. AWS calls each server an **instance**. When we launch an instance, we choose:

- **An AMI (Amazon Machine Image).** This is the template the instance boots from. It contains the operating system plus any preinstalled software and configuration. Every instance is launched from an AMI.
- **An instance type.** This sets how much CPU and memory the server gets, for example `t3.micro` or `m5.large`.
- **Network settings.** These are the VPC and subnet the instance runs in, and the security groups that control its traffic (see the next section).
- **A key pair** (optional), for logging in to the instance over SSH.

An AMI relates to an instance the way a Docker image relates to a container. The AMI is the reusable template, the instance is a running server created from it, and one AMI can launch any number of identical instances.

In Terraform, those launch choices are arguments on an `aws_instance` resource:

```hcl
resource "aws_instance" "web" {
  ami           = "ami-0123456789abcdef0"
  instance_type = "t3.micro"
}
```

An existing instance can't switch to a different AMI. If we change the `ami` argument, Terraform has to destroy the server and build a new one. Its plan marks that change with `# forces replacement`.

**Check yourself**
- What's the difference between an AMI and an instance? How many instances can one AMI launch?
- In the resource block above, which argument sets the server's size, and which sets what software it boots with?
- Why does changing `ami` force Terraform to replace the server instead of updating it in place?

**Go deeper**
- The Configuration Language: [The Resource Block](../Terraform/001%20-%20Terraform%20Intro/002%20-%20The%20Configuration%20Language/Notes/001%20-%20The%20Configuration%20Language.md#the-resource-block)
- The Workflow: [An Unexpected Destroy Means Stop](../Terraform/001%20-%20Terraform%20Intro/004%20-%20The%20Workflow/Notes/001%20-%20The%20Workflow.md#an-unexpected-destroy-means-stop)
- Docker, for the image/instance comparison: [Images and Containers](../Docker/001%20-%20Docker%20Fundamentals/001%20-%20Containers%20%26%20Why%20Docker/Notes/001%20-%20Containers%20%26%20Why%20Docker.md#images-and-containers)
- AWS: [What is Amazon EC2?](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/concepts.html), [Amazon Machine Images](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AMIs.html)

---

## Security Groups

> **Not yet in the course notes.**

A **security group** acts as a **virtual firewall** for EC2 instances. Its rules control which network traffic may reach an instance (**inbound**) and which traffic may leave it (**outbound**). Each rule names a protocol, a port or range of ports, and a source or destination. That source or destination can be an IP address range or another security group.

Behaviors worth knowing:

- Rules can only **allow** traffic. There are no "deny" rules, and any inbound traffic that no rule allows is blocked.
- Security groups are **stateful**. When an inbound request is allowed, its response is allowed back out automatically.
- An instance can have several security groups, and one security group can protect many instances.
- Rule changes take effect immediately. The instance doesn't need a restart.

In Terraform, a security group is just another resource (`aws_security_group`). An instance refers to it the same way the notes' subnet refers to its VPC.

**Check yourself**
- A web server should accept HTTP (port 80) from anywhere, but SSH (port 22) only from the office network. What inbound rules does its security group need?
- Why don't we need an outbound rule to send responses to inbound requests we've allowed?
- An instance is running, but nothing can reach it. What's the first thing to check in its security group?

**Go deeper**
- The Configuration Language: [References Between Resources](../Terraform/001%20-%20Terraform%20Intro/002%20-%20The%20Configuration%20Language/Notes/001%20-%20The%20Configuration%20Language.md#references-between-resources)
- AWS: [Amazon EC2 security groups](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-security-groups.html)

---

## API Gateway

> **Not yet in the course notes.**

An **API gateway** is a single front door for an application's APIs. Clients send their requests to the gateway, and it routes each one to the backend service that handles it. Along the way, the gateway takes care of jobs every backend would otherwise have to do for itself: authentication, rate limiting, caching, and monitoring.

**Amazon API Gateway** is AWS's managed service for this. It forwards requests to backends such as Lambda functions, applications running on EC2, or other HTTP endpoints. Building and deploying a REST API with it follows a sequence:

1. Define **resources**, which are URL paths such as `/orders`, and **methods** on them, such as `GET` and `POST`.
2. **Integrate** each method with the backend that does the work.
3. **Deploy** the API to a **stage**: a named version of the API, such as `dev` or `prod`. Each stage gets its own URL for clients to call.

Changes to the API aren't visible to clients until it's deployed again. Each deployment is a snapshot of the API, and stages let several versions be live at once. That's the same idea as promoting one artifact through dev, staging, and production in the DevOps notes.

**Check yourself**
- Why put a gateway in front of backend services instead of letting clients call them directly? Name two jobs it takes off the backends.
- We changed a method's integration, but clients still see the old behavior. What step did we miss?
- How are a deployment and a stage related?

**Go deeper**
- DevOps Overview: [Core Vocabulary](../DevOps/Notes/001%20-%20DevOps%20Overview.md#core-vocabulary), for environments and promotion
- AWS: [What is Amazon API Gateway?](https://docs.aws.amazon.com/apigateway/latest/developerguide/welcome.html), [Deploy REST APIs in API Gateway](https://docs.aws.amazon.com/apigateway/latest/developerguide/how-to-deploy-api.html)

---

## Terraform Core Ideas at a Glance

No quiz questions target these directly, but they're the core of the Terraform module:

| Idea | In one line | Notes |
|---|---|---|
| Infrastructure as code | Infrastructure is described in version-controlled files instead of being clicked together in a console | [What Terraform Is](../Terraform/001%20-%20Terraform%20Intro/001%20-%20What%20Terraform%20Is/Notes/001%20-%20What%20Terraform%20Is.md#infrastructure-as-code) |
| Declarative | We describe the end state, and Terraform works out what to create, change, or delete | [Declarative, Not Procedural](../Terraform/001%20-%20Terraform%20Intro/001%20-%20What%20Terraform%20Is/Notes/001%20-%20What%20Terraform%20Is.md#declarative-not-procedural) |
| Providers | Plugins that turn configuration into each platform's API calls | [The Provider Model](../Terraform/001%20-%20Terraform%20Intro/001%20-%20What%20Terraform%20Is/Notes/001%20-%20What%20Terraform%20Is.md#the-provider-model) |
| Resource blocks | A type, a local name, and arguments. The type plus the name form an address, such as `aws_instance.web` | [The Resource Block](../Terraform/001%20-%20Terraform%20Intro/002%20-%20The%20Configuration%20Language/Notes/001%20-%20The%20Configuration%20Language.md#the-resource-block) |
| State | Terraform's record linking each resource address to a real object | [The State File](../Terraform/001%20-%20Terraform%20Intro/003%20-%20State/Notes/001%20-%20State.md#the-state-file) |
| Drift | Changes made outside Terraform, which the next run undoes | [When Reality and State Disagree](../Terraform/001%20-%20Terraform%20Intro/003%20-%20State/Notes/001%20-%20State.md#when-reality-and-state-disagree) |
| Workflow | `init`, then `plan`, then `apply`, and `destroy` when finished | [The Workflow](../Terraform/001%20-%20Terraform%20Intro/004%20-%20The%20Workflow/Notes/001%20-%20The%20Workflow.md) |

---

## More Practice

The [Terraform Module Review Reading Questions](../Terraform/001%20-%20Terraform%20Intro/Review/Module%20Review%20-%20Reading%20Questions.md) cover the whole module in more depth.
