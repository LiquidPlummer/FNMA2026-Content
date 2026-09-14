# The Configuration Language

Terraform configurations are written in **HCL**, the **HashiCorp Configuration Language**. We aren't aiming to write it here. The aim is to read it: to look at a configuration and say what it would build.

---

## HCL Basics

Configuration lives in files ending in `.tf`. Terraform reads every `.tf` file in a folder together, as if it were one big file, so the split between files is just for organization. By convention, we'll often see:

- `main.tf`: the resources themselves
- `variables.tf`: the inputs
- `outputs.tf`: the values the configuration exposes

HCL is built from two things: **blocks** and **arguments**.

```hcl
# A block: a keyword, one or more labels, and a body in braces
block_type "label_one" "label_two" {
  argument_name = "a string value"   # an argument: name = value
  another_one   = 42
}
```

*The general shape of HCL: blocks contain arguments, and `#` starts a comment.*

That's nearly all the syntax there is. Every construct in this topic is a block of this shape, and the difference between them is the keyword at the front.

---

## The Resource Block

The **resource block** is the unit of everything in Terraform. Each one describes a single piece of infrastructure that should exist.

```hcl
resource "aws_instance" "web" {
  ami           = "ami-0123456789abcdef0"
  instance_type = "t3.micro"
}
```

*A resource block describing one EC2 virtual server, built from a specific machine image and sized as a `t3.micro`.*

Every resource block has three parts:

- **The resource type** (`aws_instance`). This says what kind of thing it is. The prefix names the provider responsible for it: `aws_` means the AWS provider. Each provider documents the types it offers.
- **The local name** (`web`). This is a name *we* choose so we can refer to this resource elsewhere in the configuration. It only exists inside Terraform. AWS never sees it, and it doesn't name the server.
- **The arguments** (everything inside the braces). These configure the resource. Which arguments are available, and which are required, depends on the resource type.

Together, the type and local name form the resource's **address**: `aws_instance.web`. That address is how Terraform identifies the resource, and it must be unique within a configuration.

---

## References Between Resources

Real infrastructure is connected. A subnet lives inside a network, and a server lives inside a subnet. In HCL, we express those connections by having one resource refer to another's attributes.

```hcl
resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"
}

resource "aws_subnet" "app" {
  vpc_id     = aws_vpc.main.id
  cidr_block = "10.0.1.0/24"
}
```

*A network (VPC) and a subnet inside it. The subnet finds its VPC by referencing `aws_vpc.main.id`.*

The expression `aws_vpc.main.id` means "the `id` of the resource at address `aws_vpc.main`." That ID doesn't exist when we write the file; AWS assigns it when the VPC is created. Terraform fills it in at the right moment.

That reference does more than pass a value. **References are how Terraform knows the order to build things in.** Because the subnet needs the VPC's ID, Terraform knows the VPC has to be created first. It reads every reference in the configuration and builds a **dependency graph**, a map of what depends on what. From that graph:

- Resources are created in dependency order: the VPC, then the subnet.
- Resources that don't depend on each other are created at the same time, which speeds things up.
- When tearing down, the order reverses: the subnet is deleted before the VPC.

The order of blocks in the file doesn't matter. We could put the subnet above the VPC, and Terraform would still build the VPC first.

---

## Variables and Outputs

### Variables

Hard-coded values make a configuration usable in only one situation. **Variables** are the inputs that let the same configuration serve different environments, such as a small server in development and a large one in production.

```hcl
variable "instance_type" {
  type    = string
  default = "t3.micro"
}

resource "aws_instance" "web" {
  ami           = "ami-0123456789abcdef0"
  instance_type = var.instance_type
}
```

*A variable declaring an input with a default value, and a resource using it through `var.instance_type`.*

A variable is declared with a `variable` block and used with `var.<name>`. If nobody supplies a value, the default is used. Values are usually supplied through a variables file per environment:

```hcl
# prod.tfvars
instance_type = "m5.large"
```

*A variables file that overrides the default for a production environment.*

Same configuration, different inputs, different result.

### Outputs

**Outputs** go the other direction. They expose values from the configuration for something else to consume.

```hcl
output "web_public_ip" {
  value = aws_instance.web.public_ip
}
```

*An output that reports the server's public IP address once it's been created.*

Terraform prints outputs after it finishes building, so a person can see them. More importantly, other tools and other Terraform configurations can read them. A deployment script might need the server's IP, and a separate configuration might need the ID of a network this one created.

Variables are a configuration's inputs; outputs are its return values.

---

## Modules

Suppose we need the same network setup (a VPC, subnets, route tables) in development, staging, and production. Copying the same resource blocks three times would work, but every future fix would have to be made three times too.

A **module** solves this. A module is simply a folder of Terraform configuration, with variables as its inputs and outputs as its return values. Other configurations can call it as many times as they like, with different inputs each time.

```hcl
module "dev_network" {
  source     = "./modules/network"
  cidr_block = "10.0.0.0/16"
}

module "prod_network" {
  source     = "./modules/network"
  cidr_block = "10.1.0.0/16"
}
```

*The same network module called twice with different address ranges, producing two separate, identical-shaped networks.*

Each `module` block names the folder to use (`source`) and supplies values for that module's variables. Everything the module defines is created once per call. The calling configuration can read the module's outputs, such as `module.prod_network.vpc_id`.

If modules sound like functions, that's the right intuition: define the logic once, call it with different arguments. In fact, every Terraform configuration is technically a module. The folder we run Terraform in is called the **root module**, and anything it calls is a **child module**.

---

## Reading a Configuration End to End

Here's a short but complete configuration. We'll read it the way we'd read one in a real repository.

```hcl
provider "aws" {
  region = "us-east-1"
}

variable "environment" {
  type    = string
  default = "dev"
}

resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"

  tags = {
    Name = "${var.environment}-vpc"
  }
}

resource "aws_subnet" "app" {
  vpc_id     = aws_vpc.main.id
  cidr_block = "10.0.1.0/24"
}

resource "aws_instance" "app_server" {
  ami           = "ami-0123456789abcdef0"
  instance_type = "t3.micro"
  subnet_id     = aws_subnet.app.id

  tags = {
    Name = "${var.environment}-app-server"
  }
}

output "app_server_private_ip" {
  value = aws_instance.app_server.private_ip
}
```

*A configuration that builds a network, a subnet, and one server in AWS, then reports the server's private IP address.*

Reading it top to bottom:

1. **`provider "aws"`**: this configuration talks to AWS, in the `us-east-1` region.
2. **`variable "environment"`**: one input, defaulting to `dev`. The `${var.environment}` syntax inside a string inserts its value, so the tags below become `dev-vpc` and `dev-app-server`.
3. **`aws_vpc.main`**: a VPC with the address range `10.0.0.0/16`.
4. **`aws_subnet.app`**: a subnet inside that VPC (it references `aws_vpc.main.id`).
5. **`aws_instance.app_server`**: a `t3.micro` server inside that subnet (it references `aws_subnet.app.id`).
6. **`output "app_server_private_ip"`**: once built, report the server's private IP.

So it creates **three resources**, in the order VPC → subnet → server, and prints one value when done.

Just as important is what it *doesn't* create. There's no internet gateway and no route to the internet, so this server can't be reached from outside the network. Terraform builds exactly what's written and nothing more, so reading a configuration means noticing what's missing as well as what's there.

---

## Key Takeaways

- Terraform configurations are written in HCL, in `.tf` files; every `.tf` file in a folder is read together.
- A resource block has a type (which also names the provider), a local name we choose, and arguments that configure it. Type plus name forms the address, like `aws_instance.web`.
- References such as `aws_vpc.main.id` pass values between resources and tell Terraform the build order. Block order in the file doesn't matter.
- Variables are inputs that change between environments; outputs expose values for people and other tools to consume.
- A module is a folder of configuration that can be called repeatedly with different inputs, which is how duplication is avoided.
- To read a configuration: identify the provider, list the resources, follow the references to find the build order, and note what's missing as well as what's there.
