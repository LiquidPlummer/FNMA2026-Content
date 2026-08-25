# Defining IaaS, PaaS, and SaaS

Cloud services aren't all the same "shape." Some hand us raw infrastructure and let us build anything on top; others hand us a finished product we just log into. This spectrum is usually described with three terms: **IaaS**, **PaaS**, and **SaaS**.

## Infrastructure as a Service (IaaS)

**IaaS** provides the fundamental building blocks — virtual machines, storage, and networking — without any application-level structure on top. We get a blank server and decide everything from the operating system up: what OS to install, what software to run, how to configure it. AWS's **EC2** is the textbook example of IaaS: it hands us a virtual machine and stops there. Everything above the operating system is our responsibility.

IaaS gives maximum flexibility — we can run literally anything a computer can run — at the cost of maximum responsibility. We're the ones patching the OS, configuring the runtime, and managing the application.

## Platform as a Service (PaaS)

**PaaS** provides a managed environment for running applications, without us having to manage the underlying servers. We provide our code (or a container), and the platform handles provisioning, scaling, and operating system maintenance. AWS's **Elastic Beanstalk** and **Lambda** both fall into this category — with Lambda in particular, we write a function, and AWS handles literally everything about running it, including scaling it to zero when it's not in use.

PaaS trades away some low-level control in exchange for AWS taking on more operational burden. We generally can't SSH into the underlying server, because there isn't one we manage — but we also never have to patch it.

## Software as a Service (SaaS)

**SaaS** provides a complete, ready-to-use application delivered over the internet — we don't manage infrastructure, a platform, or even code. We just use the software. Common examples outside AWS include Gmail or Salesforce; within the AWS ecosystem, a service like **Amazon Chime** (a fully built video-conferencing application) is a SaaS offering. There's no server to configure and no code to deploy — we simply use the product.

## Comparing the Three

| | IaaS | PaaS | SaaS |
|---|---|---|---|
| **What we manage** | OS, runtime, app, data | App and data | Nothing — just use it |
| **What the provider manages** | Physical hardware, virtualization | Everything below the app layer | Everything |
| **AWS example** | EC2 | Lambda, Elastic Beanstalk | Amazon Chime |
| **Flexibility** | Highest | Moderate | Lowest |
| **Operational burden** | Highest | Lower | Lowest |

## Why This Framework Matters

This spectrum isn't just trivia — it's a useful lens for evaluating *any* cloud service, including ones we haven't encountered yet. When we meet a new AWS service, asking "where does this fall on the IaaS-to-SaaS spectrum?" immediately tells us a lot: how much control we'll have, how much we'll be responsible for maintaining, and how quickly we can get something running. That same spectrum is also the backbone of the next topic — the **shared responsibility model** — which describes exactly where AWS's job ends and ours begins at each of these levels.
