# What Terraform Is

**Terraform** is a tool for creating and managing infrastructure (servers, networks, databases, storage, DNS records, and so on) by describing it in text files. It was built by HashiCorp and is the most widely used tool of its kind. We write down what infrastructure should exist, and Terraform makes the real world match.

---

## Infrastructure as Code

**Infrastructure as code (IaC)** means treating infrastructure the way we already treat application code. Instead of building a network by clicking through a cloud console, we describe it in files. Those files live in version control, often in the same repository as the application that runs on that infrastructure.

```hcl
resource "aws_s3_bucket" "reports" {
  bucket = "acme-quarterly-reports"
}
```

*A complete Terraform description of one piece of infrastructure: an S3 bucket named `acme-quarterly-reports`.*

We don't need to read that syntax closely yet (the next topic covers it). What matters is that the bucket now exists as text. That means it can be committed, reviewed, versioned, and rebuilt, the same as any other code.

---

## The Problem It Solves

Almost everyone starts by building infrastructure through the cloud console. It's approachable, and for a one-off experiment it's fine. As soon as the infrastructure matters, clicking through a console runs into three problems:

- **It's unrepeatable.** Building a staging environment that matches production means redoing dozens of screens by hand and hoping nothing gets missed. Rebuilding after a disaster means remembering what was there.
- **It's unreviewable.** Nobody approves a click before it happens. A mistyped setting in production goes live immediately, and nobody else saw it coming.
- **It's undocumented.** The only record of how the environment was built is the environment itself, plus whatever the person who built it remembers. When they leave, that knowledge leaves with them.

Infrastructure as code fixes all three at once. The files *are* the documentation. Changes go through the same pull-request review as application code. And building a second identical environment means running the same files again.

---

## Declarative, Not Procedural

There are two ways to tell a computer to build something.

A **procedural** approach lists steps: create a network, then create a subnet inside it, then launch a server in the subnet. A script like that has a problem when we run it a second time: it follows the steps again and creates a second network, a second subnet, and a second server. It only knows how to *do* things, not what the result should look like.

Terraform is **declarative**. We describe the end state: *there should be a network, a subnet inside it, and a server in that subnet.* Terraform compares that description with what currently exists and works out the steps itself:

- If nothing exists yet, it creates all three, in the right order.
- If everything already matches, it does nothing. Running it twice is safe.
- If we change one setting on the server, it changes just that setting and leaves the rest alone.
- If we remove the server from the description, it deletes the server.

This is the most important idea in Terraform. We never write "create" or "delete" commands for individual resources. We edit the description, and Terraform figures out what has to happen to make reality match it.

---

## The Provider Model

Terraform itself knows nothing about AWS. Or Azure, or GitHub, or any other platform.

The core of Terraform is a general-purpose engine. It reads configuration files, works out what depends on what, tracks what it has built, and calculates what needs to change. The platform-specific knowledge lives in **providers**: plugins that each understand one platform. A provider knows which kinds of resources that platform offers and how to create, read, update, and delete each one through the platform's API.

When Terraform decides an S3 bucket needs to exist, it hands that job to the AWS provider. The AWS provider turns it into the actual AWS API call that creates the bucket.

```text
Configuration file  →  Terraform core  →  AWS provider     →  AWS API
                        (what changes?)    (how, on AWS?)      (does it)
```

*How a resource in a configuration file becomes a real object in AWS. Terraform core decides what should happen, and the provider carries it out.*

Providers are downloaded from the **Terraform Registry**, a public catalog with thousands of them. Some are maintained by HashiCorp, many by the platform vendors themselves, and many more by the community.

### Why That Matters

Because the platform knowledge lives in plugins, one configuration can manage resources on completely different platforms, using the same language and the same workflow:

```hcl
resource "aws_s3_bucket" "site_assets" {
  bucket = "acme-marketing-site-assets"
}

resource "github_repository" "site" {
  name = "marketing-site"
}
```

*One configuration managing two platforms: an S3 bucket through the AWS provider and a GitHub repository through the GitHub provider.*

The first word of each resource type (`aws_`, `github_`) tells us which provider is responsible for it. A team can manage its AWS accounts, Azure subscriptions, GitHub repositories, DNS in Cloudflare, and monitoring in Datadog with one tool. Learn the tool once, and the skills carry across every platform it touches.

---

## How Terraform Compares

Terraform's job only makes sense next to the other tools we'll hear it compared with. Some do the same job differently, and some do a different job entirely.

### Terraform vs. CloudFormation

**AWS CloudFormation** is AWS's own infrastructure-as-code service. It does the same job as Terraform: describe infrastructure in files (YAML or JSON, called **templates**), and CloudFormation creates and updates it.

The key difference is scope. CloudFormation is built for AWS and manages AWS resources. Terraform manages AWS just as well, plus everything else that has a provider. A team that's all-in on AWS might reasonably pick either one. A team with anything outside AWS usually picks Terraform.

### Terraform vs. Ansible

**Ansible** is often mentioned in the same breath as Terraform, but it does a different job:

- **Provisioning** is creating the infrastructure itself: the servers, networks, load balancers, and databases. This is Terraform's job.
- **Configuration management** is setting up what runs *on* that infrastructure: installing packages, writing config files, starting services, creating OS user accounts. This is Ansible's job.

The two are commonly used together rather than as alternatives. Terraform creates three servers and a load balancer. Ansible then connects to those servers and installs and configures the web application on them. There is some overlap (Ansible can create cloud resources, and Terraform can run startup scripts), but each tool is at its best doing its own half.

### Pulumi and the AWS CDK

Terraform configurations are written in a purpose-built configuration language. **Pulumi** and the **AWS Cloud Development Kit (CDK)** take the same idea and apply it in a different way: infrastructure is defined in a general-purpose programming language like TypeScript, Python, Java, or Go.

That brings real loops, conditionals, classes, and the testing tools developers already know. The tradeoff is that infrastructure code can become as complex as any other program. Pulumi works across many platforms, like Terraform. The AWS CDK is AWS-only; under the hood, it generates CloudFormation templates.

### Summary

| Tool | What it does | Written in | Platforms |
|---|---|---|---|
| Terraform | Provisioning | HCL (a configuration language) | Anything with a provider |
| CloudFormation | Provisioning | YAML or JSON templates | AWS |
| Pulumi | Provisioning | General-purpose languages | Many |
| AWS CDK | Provisioning (via CloudFormation) | General-purpose languages | AWS |
| Ansible | Configuration management | YAML playbooks | Servers that already exist |

---

## The Category and the Tool

"Infrastructure as code" is the category: the practice of defining infrastructure in files. Terraform is the most widely used tool in that category, but it's one tool among several. When someone says "we do infrastructure as code," Terraform is a good guess, not a certainty. Everything that makes IaC valuable (repeatability, review, documentation) comes from the practice, whichever tool delivers it.

---

## Key Takeaways

- Infrastructure as code describes servers, networks, and databases in files kept in version control, instead of building them by hand in a console.
- Hand-built infrastructure is unrepeatable, unreviewable, and undocumented; IaC fixes all three.
- Terraform is declarative: we describe the end state, and Terraform works out what to create, change, or delete to get there.
- Terraform core knows nothing about any specific platform. Providers are plugins that translate configuration into each platform's API calls.
- The provider model gives one language and one workflow across AWS, Azure, GitHub, and anything else with a provider.
- CloudFormation does the same job but only for AWS; Pulumi and the AWS CDK use general-purpose programming languages instead of a configuration language.
- Terraform provisions infrastructure, and Ansible configures what runs on it. The two are often used together.
