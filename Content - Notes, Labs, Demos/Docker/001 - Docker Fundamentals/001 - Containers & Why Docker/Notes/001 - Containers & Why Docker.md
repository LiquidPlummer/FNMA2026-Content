# Containers & Why Docker

Every developer has hit some version of this: the application runs locally, gets handed to QA, and immediately fails. The code is identical, so the difference is everything *around* the code — a different Java version, a missing environment variable, a library the local machine happened to have installed two years ago. Containers exist to make that surrounding environment part of what we ship.

---

## The Problem: Environment Drift

An application is never just its source code. To run, it needs a runtime of a specific version, a set of libraries, configuration, file system layout, and often a few system packages. On a developer laptop, all of that accumulated over months and nobody wrote it down.

**Environment drift** is what we call the gap between two machines that are supposed to be running the same thing. It shows up everywhere:

- A laptop has Node 22; the server has Node 18.
- A build works because a developer installed a native library last spring.
- Test passes with an environment variable that production doesn't set.
- Two developers on the same team have subtly different local databases.

The traditional answers were documentation ("here are the fourteen setup steps") and configuration management tools that bring a machine to a desired state. Both help, and both drift, because they describe the environment rather than *being* it.

A container takes a different approach: package the application together with its entire environment into one artifact, and run that artifact identically everywhere.

---

## What a Container Actually Is

Here is the part that trips people up, so let's be precise:

> **A container is a process running on the host machine, isolated so that it sees its own filesystem, its own network interfaces, and its own process list.**

It is not a small computer. There is no virtual hardware and no second operating system booting up inside it. When we run a container on a Linux host, the processes inside it are ordinary Linux processes on that host — we could see them in the host's process list. What makes them a "container" is that the kernel has been told to lie to them about what they can see.

Two Linux kernel features do the work:

- **Namespaces** control *visibility*. A process in its own PID namespace sees itself as process 1 and cannot see any other process on the machine. Its own mount namespace gives it a completely different root filesystem. Its own network namespace gives it its own interfaces and ports.
- **Control groups (cgroups)** control *consumption*. They cap how much CPU, memory, and I/O the container's processes are allowed to use.

Isolation plus resource limits, applied to a normal process. That's a container.

---

## Containers vs Virtual Machines

This comparison is the fastest way to make the idea concrete, because VMs solve a similar problem in a fundamentally different way.

A **virtual machine** virtualizes *hardware*. A hypervisor presents what looks like a physical machine — CPU, disk, network card — and a complete guest operating system boots on top of it, with its own kernel, its own init system, its own device drivers. Running three applications in three VMs means running three entire operating systems.

A **container** virtualizes the *operating system*. There is no guest OS and no second kernel. All containers on a host share that host's single kernel, and each one just gets an isolated view of it.

```
        VIRTUAL MACHINES                        CONTAINERS

  ┌──────┐ ┌──────┐ ┌──────┐            ┌──────┐ ┌──────┐ ┌──────┐
  │ App  │ │ App  │ │ App  │            │ App  │ │ App  │ │ App  │
  │ Libs │ │ Libs │ │ Libs │            │ Libs │ │ Libs │ │ Libs │
  ├──────┤ ├──────┤ ├──────┤            └──────┘ └──────┘ └──────┘
  │Guest │ │Guest │ │Guest │            ┌────────────────────────┐
  │  OS  │ │  OS  │ │  OS  │            │   Container Runtime    │
  └──────┘ └──────┘ └──────┘            └────────────────────────┘
  ┌────────────────────────┐            ┌────────────────────────┐
  │      Hypervisor        │            │      Host OS Kernel    │
  ├────────────────────────┤            ├────────────────────────┤
  │       Hardware         │            │       Hardware         │
  └────────────────────────┘            └────────────────────────┘

  Three kernels running.                One kernel, shared.
```

*The structural difference: a VM stack repeats the operating system for every workload, while containers share one kernel underneath all of them.*

### What That Difference Buys

Removing the guest OS from the picture has consequences that show up immediately in day-to-day work:

| | Virtual Machine | Container |
|---|---|---|
| **Size** | Gigabytes — includes a full OS | Megabytes — just the app and its dependencies |
| **Startup** | Seconds to minutes; a real boot sequence | Milliseconds; it's starting a process |
| **Density on one host** | Tens | Hundreds or thousands |
| **Isolation boundary** | Hardware, enforced by the hypervisor | Kernel features, enforced by the OS |

Startup time is the one that changes how we work. Because a container starts as fast as a process, it becomes reasonable to create and destroy them constantly — spin one up for a single test run, throw it away, spin up forty in a CI pipeline. That is simply not practical when each one needs to boot an operating system.

### When a VM Is Still the Right Answer

Containers are not strictly better, and it's worth knowing where the line is:

- **We need a different kernel or OS.** Containers share the host kernel, so a Linux host runs Linux containers. Needing a Windows kernel, a specific kernel version, or a custom kernel module means a VM.
- **We need a harder security boundary.** Namespace isolation is good, but every container is talking to the same kernel. A kernel vulnerability is a shared risk in a way it isn't across hypervisor boundaries. For genuinely untrusted or hostile workloads sharing hardware, VM isolation is stronger.
- **We're managing whole machines, not applications.** If the unit of work really is "a server," a VM models that directly.

In practice these coexist constantly — containers running inside VMs is the normal arrangement in cloud environments.

---

## Images and Containers

Two words get used loosely and mean different things.

An **image** is the packaged artifact: a read-only bundle containing a filesystem and the metadata describing how to start a process in it. Images are built once, stored, and shared. Nothing in an image is running.

A **container** is a running (or stopped) instance created from an image. Starting a container takes the image's read-only filesystem, adds a thin writable layer on top, and launches the specified process inside the isolation described above.

The relationship is the familiar one between a class and an object, or a program on disk and a process:

```
  nginx:1.27  (image, read-only)
        │
        ├──→ container "web-1"   (running, own writable layer)
        ├──→ container "web-2"   (running, own writable layer)
        └──→ container "web-3"   (running, own writable layer)
```

*One image can back any number of containers; each gets its own writable layer, so changes inside one never affect the others or the image.*

That writable layer has a consequence we'll return to repeatedly: it belongs to the container, not the image, and it is destroyed when the container is removed. Anything written inside a running container is gone unless we deliberately arrange otherwise.

---

## Where Docker Fits

Containers are a capability of the Linux kernel, and the kernel features behind them predate Docker by years. What Docker did in 2013 was make them usable: a simple format for packaging an image, a registry for sharing images, and a single command-line tool that made the whole thing approachable.

It worked well enough that "container" and "Docker" became nearly synonymous in conversation, but they aren't the same thing. The ecosystem standardized:

- The **Open Container Initiative (OCI)** publishes the image format and runtime specifications that Docker and its competitors implement.
- **containerd** and **runc** are the lower-level components that actually create and run containers — Docker uses them under the hood.
- Other tools (Podman, Buildah, and the container runtimes inside Kubernetes) work with the same OCI images.

The practical upshot: an image built with Docker is not locked to Docker. It will run on any OCI-compatible runtime, which is most of them. We're learning Docker because it's the most common tool and has the best developer experience, but the concepts and the artifacts transfer.

---

## Key Takeaways

- A container is an isolated process, not a small machine — the kernel restricts what it can see (namespaces) and consume (cgroups).
- Containers exist to eliminate environment drift by shipping the application and its environment as a single artifact.
- A VM virtualizes hardware and runs a full guest OS; a container virtualizes the OS and shares the host kernel.
- Sharing the kernel is what makes containers small and fast to start, which is what makes disposable, high-density workloads practical.
- VMs remain the right choice for a different kernel or OS, and for a stronger isolation boundary between untrusted workloads.
- An image is the read-only blueprint; a container is a running instance with its own writable layer that disappears when the container is removed.
- Docker is one implementation of an open standard (OCI) — images built with it run on other compatible runtimes.
