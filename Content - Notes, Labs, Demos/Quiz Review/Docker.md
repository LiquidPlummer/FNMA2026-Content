# Docker — Quiz Review

This page reviews the Docker ideas the quiz checks: what Docker is for, how images and containers relate, the pieces of Docker's architecture (including the daemon and registries), and what a Dockerfile does. Each section has a short summary, a few questions to test ourselves with, and links to the notes that explain the idea in full.

This is a study guide, not an answer key. The answers to the check-yourself questions are in the linked notes.

---

## What Docker Is For

An application is never just its source code. It also needs a specific runtime version, libraries, configuration, and a few system packages. The differences in those things between one machine and the next are called **environment drift**, and drift is behind most "it works on my machine" failures.

Containers fix drift by packaging the application **together with its entire environment** as one artifact, which then runs the same way everywhere. Docker is the tool that made containers practical. The kernel features behind containers existed before Docker. What Docker added was a simple image format, registries for sharing images, and one command-line tool for building and running them.

A container is **an isolated process running on the host**, not a small virtual machine:

| | Virtual machine | Container |
|---|---|---|
| What it virtualizes | Hardware, with a full guest OS on top | The operating system; shares the host's kernel |
| Typical size | Gigabytes | Megabytes |
| Startup time | Seconds to minutes | Milliseconds |

**Check yourself**
- What is environment drift? Think of an example from a project you've worked on.
- Setup documentation and configuration scripts tend to drift over time. Why doesn't a container image?
- Which two Linux kernel features turn an ordinary process into a container, and what does each one control?

**Go deeper**
- Containers & Why Docker: [The Problem: Environment Drift](../Docker/001%20-%20Docker%20Fundamentals/001%20-%20Containers%20%26%20Why%20Docker/Notes/001%20-%20Containers%20%26%20Why%20Docker.md#the-problem-environment-drift), [What a Container Actually Is](../Docker/001%20-%20Docker%20Fundamentals/001%20-%20Containers%20%26%20Why%20Docker/Notes/001%20-%20Containers%20%26%20Why%20Docker.md#what-a-container-actually-is), [Containers vs Virtual Machines](../Docker/001%20-%20Docker%20Fundamentals/001%20-%20Containers%20%26%20Why%20Docker/Notes/001%20-%20Containers%20%26%20Why%20Docker.md#containers-vs-virtual-machines), [Where Docker Fits](../Docker/001%20-%20Docker%20Fundamentals/001%20-%20Containers%20%26%20Why%20Docker/Notes/001%20-%20Containers%20%26%20Why%20Docker.md#where-docker-fits)

---

## Images and Containers

These two words get used loosely, but they mean different things:

- An **image** is the packaged artifact: a read-only stack of filesystem layers, plus settings that say how to start a process. Nothing in an image is running. Images are built once, stored, and shared.
- A **container** is an instance created from an image, and it can be running or stopped. Starting a container adds a thin *writable layer* on top of the image's read-only layers.

One image can back any number of containers, and each container gets its own writable layer. The relationship works like a class and its objects, or a program on disk and a running process.

The writable layer belongs to the container, not the image. It's deleted when the container is removed.

**Check yourself**
- We start three containers from the same image. How many writable layers are there? If one container changes a file, do the others see the change?
- We write a file inside a container and then remove the container. What happened to the file?
- Why doesn't deleting a file in a later layer make an image smaller, or remove a secret that leaked into an earlier layer?

**Go deeper**
- Containers & Why Docker: [Images and Containers](../Docker/001%20-%20Docker%20Fundamentals/001%20-%20Containers%20%26%20Why%20Docker/Notes/001%20-%20Containers%20%26%20Why%20Docker.md#images-and-containers)
- Images & Docker Hub: [An Image Is a Stack of Layers](../Docker/001%20-%20Docker%20Fundamentals/003%20-%20Images%20%26%20Docker%20Hub/Notes/001%20-%20Images%20%26%20Docker%20Hub.md#an-image-is-a-stack-of-layers)

---

## Docker's Architecture: Client, Daemon, Registry

When we type `docker run`, the `docker` command itself does very little. Docker is a client/server system, plus a remote registry that stores images:

| Piece | What it is | What it does |
|---|---|---|
| **Client** | The `docker` command | Turns what we typed into an API request and sends it to the daemon. That's all it does |
| **Daemon** (`dockerd`) | A long-running background service | Does all the real work: pulls images, creates and runs containers, manages networks and volumes. It also holds all of Docker's state |
| **Registry** | A remote server that stores images | Supplies images the daemon doesn't have locally, and receives the images we `push`. Docker Hub is the default |

Three consequences worth knowing:

- If the daemon isn't running, every command fails with "cannot connect to the Docker daemon." On Windows, that almost always means Docker Desktop hasn't been started.
- On Windows and macOS, the daemon runs inside a lightweight Linux VM (WSL 2 on Windows), because containers need a Linux kernel.
- A full image name is `registry/repository:tag`. If the registry is left off, Docker uses Docker Hub. If the tag is left off, it uses `latest`, which means "the default tag," not "the newest version."

**Check yourself**
- Trace `docker run hello-world` on a fresh install. Which piece handles each of the six steps?
- Are `nginx`, `nginx:latest`, and `docker.io/library/nginx:latest` different images? Why or why not?
- What three steps does it take to publish our own image so that a teammate can pull it?

**Go deeper**
- Docker Architecture & Installation: [Three Pieces](../Docker/001%20-%20Docker%20Fundamentals/002%20-%20Docker%20Architecture%20%26%20Installation/Notes/001%20-%20Docker%20Architecture%20%26%20Installation.md#three-pieces), [Docker Desktop vs Docker Engine](../Docker/001%20-%20Docker%20Fundamentals/002%20-%20Docker%20Architecture%20%26%20Installation/Notes/001%20-%20Docker%20Architecture%20%26%20Installation.md#docker-desktop-vs-docker-engine), [Verifying the Install](../Docker/001%20-%20Docker%20Fundamentals/002%20-%20Docker%20Architecture%20%26%20Installation/Notes/001%20-%20Docker%20Architecture%20%26%20Installation.md#verifying-the-install)
- Images & Docker Hub: [Image Names](../Docker/001%20-%20Docker%20Fundamentals/003%20-%20Images%20%26%20Docker%20Hub/Notes/001%20-%20Images%20%26%20Docker%20Hub.md#image-names), [Docker Hub](../Docker/001%20-%20Docker%20Fundamentals/003%20-%20Images%20%26%20Docker%20Hub/Notes/001%20-%20Images%20%26%20Docker%20Hub.md#docker-hub)
- Building & Tagging Images: [Publishing to Docker Hub](../Docker/001%20-%20Docker%20Fundamentals/007%20-%20Building%20%26%20Tagging%20Images/Notes/001%20-%20Building%20%26%20Tagging%20Images.md#publishing-to-docker-hub)

---

## The Dockerfile

A **Dockerfile** is how we build our own images. It's a plain text file that lists the steps for assembling an image. When we run `docker build`, the daemon carries out those steps in order, top to bottom. By convention the file is named `Dockerfile`, with no extension, and it sits at the root of the project it builds.

Here's the example from the notes:

```dockerfile
FROM node:22-slim
WORKDIR /app
COPY package*.json ./
RUN npm ci --omit=dev
COPY . .
ENV NODE_ENV=production
EXPOSE 3000
CMD ["node", "server.js"]
```

| Instruction | What it does |
|---|---|
| `FROM` | Sets the base image that everything else builds on. Every Dockerfile starts with it |
| `WORKDIR` | Sets the working directory for the instructions that follow |
| `COPY` | Copies files from the build context into the image |
| `RUN` | Runs a command *during the build* and saves the result as a new layer |
| `ENV` | Sets an environment variable that's also present when the container runs |
| `EXPOSE` | Documents which port the app listens on. It publishes nothing; that still takes `-p` |
| `CMD` | The default command a container runs when it starts |

Each instruction that changes the filesystem creates a layer. Changing one instruction rebuilds its layer and every layer after it. So instructions should run **from least likely to change to most likely to change**: copy the dependency list and install dependencies before copying the source code.

**Check yourself**
- What's the difference between `RUN` and `CMD`? Which one happens at build time?
- Why does the example copy `package*.json` and run `npm ci` *before* `COPY . .`?
- Why does `FROM node`, with no tag, make builds unreproducible?
- Why must a `.env` file never be copied into an image, even if a later instruction deletes it?

**Go deeper**
- Writing a Dockerfile: [The Shape of a Dockerfile](../Docker/001%20-%20Docker%20Fundamentals/006%20-%20Writing%20a%20Dockerfile/Notes/001%20-%20Writing%20a%20Dockerfile.md#the-shape-of-a-dockerfile), [One Instruction, One Layer](../Docker/001%20-%20Docker%20Fundamentals/006%20-%20Writing%20a%20Dockerfile/Notes/001%20-%20Writing%20a%20Dockerfile.md#one-instruction-one-layer), [The Instructions](../Docker/001%20-%20Docker%20Fundamentals/006%20-%20Writing%20a%20Dockerfile/Notes/001%20-%20Writing%20a%20Dockerfile.md#the-instructions), [Never Copy Secrets Into an Image](../Docker/001%20-%20Docker%20Fundamentals/006%20-%20Writing%20a%20Dockerfile/Notes/001%20-%20Writing%20a%20Dockerfile.md#never-copy-secrets-into-an-image)
- Building & Tagging Images: [The Build Command](../Docker/001%20-%20Docker%20Fundamentals/007%20-%20Building%20%26%20Tagging%20Images/Notes/001%20-%20Building%20%26%20Tagging%20Images.md#the-build-command)

---

## More Practice

The [Docker Command Cheat Sheet](../Docker/001%20-%20Docker%20Fundamentals/Review/Docker%20Command%20Cheat%20Sheet.md) lists every command taught in the module, grouped by task.
