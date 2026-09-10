# Docker Architecture & Installation

When we type `docker run`, very little of the actual work happens in the program we just invoked. Understanding the pieces involved explains several things that are otherwise confusing — why Docker needs a background service running, why the first command after a reboot fails, and why "Docker on Windows" involves a Linux virtual machine.

---

## Three Pieces

Docker is a client/server system with a third component for distribution.

**The client** is the `docker` command. It parses what we typed, turns it into an HTTP request, and sends it to the daemon. It does essentially nothing else — it doesn't create containers, doesn't build images, and doesn't store anything.

**The daemon** (`dockerd`) is a long-running background service. It receives requests from the client and does all the real work: pulling images, creating containers, managing networks and volumes, and maintaining all the state. Everything Docker "has" — images we've pulled, containers that exist, volumes holding data — lives with the daemon.

**The registry** is a remote server that stores images. Docker Hub is the default one. When we reference an image the daemon doesn't have locally, the daemon reaches out to the registry and pulls it.

```
    ┌────────────┐      REST API       ┌─────────────────┐
    │   docker   │ ──────────────────► │     dockerd     │
    │  (client)  │ ◄────────────────── │    (daemon)     │
    └────────────┘                     └────────┬────────┘
                                                │
                                    ┌───────────┴───────────┐
                                    │                       │
                              ┌─────▼─────┐         ┌───────▼────────┐
                              │  Images   │         │    Registry    │
                              │Containers │  pull   │  (Docker Hub)  │
                              │ Volumes   │ ◄────── │                │
                              │ Networks  │         └────────────────┘
                              └───────────┘
```

*The client only sends requests; the daemon holds all state and does all work; the registry supplies images the daemon doesn't have locally.*

### Why This Matters in Practice

The split isn't academic — it explains real behavior:

- **The daemon must be running.** If it isn't, every command fails with a message about not being able to connect to the Docker daemon. This is the single most common "Docker is broken" moment, and the fix is usually just starting Docker Desktop.
- **The CLI is not the only client.** Anything that can speak the API can drive the daemon — IDE integrations, the Docker Desktop dashboard, CI systems, and testing libraries all talk to the same daemon we do.
- **The daemon can be remote.** Because it's an API, the client doesn't have to be on the same machine. This is normal in some workflows, and it's also why the daemon socket is a security-sensitive thing: access to it is effectively root access on the host.

---

## Docker Desktop vs Docker Engine

These names get used interchangeably and shouldn't be.

**Docker Engine** is the daemon, the CLI, and the underlying runtime. It runs natively on Linux, where the kernel already has everything containers need.

**Docker Desktop** is an application for Windows and macOS that bundles the Engine along with a dashboard GUI, a virtual machine to run the Engine in, and some conveniences like Kubernetes and extensions.

That virtual machine is the important detail. Containers share the host kernel, and they need a *Linux* kernel — but Windows and macOS don't have one. So Docker Desktop runs a lightweight Linux VM in the background, and the Docker daemon lives inside it. The `docker` command runs natively on Windows, but it's sending requests across into that VM.

```
  Windows Host
  ┌──────────────────────────────────────────────┐
  │  docker.exe  (CLI, runs on Windows)          │
  │       │                                      │
  │       ▼                                      │
  │  ┌────────────────────────────────────────┐  │
  │  │  WSL 2 — Linux VM                      │  │
  │  │    dockerd + containerd                │  │
  │  │    ┌─────────┐ ┌─────────┐             │  │
  │  │    │container│ │container│             │  │
  │  │    └─────────┘ └─────────┘             │  │
  │  └────────────────────────────────────────┘  │
  └──────────────────────────────────────────────┘
```

*On Windows the CLI is a native program, but the daemon and every container actually run inside a Linux VM managed by WSL 2.*

So "containers are lightweight because there's no VM" is true on Linux and a simplification on Windows and macOS. There is exactly one VM, shared by every container, rather than one per container — which preserves most of the benefit but is worth knowing about when reasoning about file performance and networking later.

---

## Installing on Windows

Docker Desktop on Windows uses **WSL 2** (Windows Subsystem for Linux, version 2) as its backend — that's the Linux VM from the diagram above.

The setup:

1. **Enable WSL 2.** On current Windows versions, `wsl --install` from an elevated PowerShell prompt handles enabling the required Windows features and installing a Linux kernel. A reboot is typically required.
2. **Install Docker Desktop** from the Docker website. The installer defaults to the WSL 2 backend and will offer to enable anything still missing.
3. **Start Docker Desktop.** The daemon starts with it. The whale icon in the system tray reports status — it animates while starting and settles when the daemon is ready.

```powershell
wsl --install
wsl --status
```

*Installs WSL 2 and confirms the installed version and default distribution; Docker Desktop expects version 2, not version 1.*

Docker Desktop needs to launch before any `docker` command will work. It can be configured to start with Windows, which is worth doing to avoid the confusing "cannot connect to the daemon" error on a fresh boot.

---

## Verifying the Install

Three commands, in increasing order of how much they prove.

```bash
docker version
```

*Prints version information for both the client and the server. If the server section is missing or reports an error, the CLI is installed but the daemon isn't reachable — Docker Desktop is not running.*

```bash
docker info
```

*Reports the daemon's full state: how many containers and images exist, the storage driver, available CPU and memory, and the operating system it's running on. Note that the OS reported here is the Linux VM's, not Windows.*

```bash
docker run hello-world
```

*Pulls a tiny official image and runs it. It prints a message and exits.*

That last one is worth slowing down on, because the output looks trivial and the operation isn't. Running it exercises the entire architecture end to end:

1. The client sent a request to the daemon.
2. The daemon looked for the `hello-world` image locally and didn't find it.
3. The daemon contacted Docker Hub and pulled the image.
4. The daemon created a container from that image.
5. The daemon started the container, which printed its message.
6. The process finished, so the container exited.

If `hello-world` works, then the client can reach the daemon, the daemon has network access to a registry, and it can create and run containers. Everything downstream in this module builds on those three things.

Note that the container still exists after exiting — it stopped, but it wasn't removed. That's a detail we'll deal with directly when we get to managing containers.

---

## The Dashboard and the CLI

Docker Desktop includes a graphical dashboard alongside the CLI. Both are clients talking to the same daemon over the same API, so neither is more "real" than the other and nothing done in one is hidden from the other.

The two are covered side by side in the next notes file, which maps each part of the dashboard to its equivalent command.

---

## Key Takeaways

- The `docker` CLI is a thin client; `dockerd` holds all state and does all the work; a registry supplies images.
- "Cannot connect to the Docker daemon" almost always means Docker Desktop isn't running.
- Docker Engine runs natively on Linux; Docker Desktop bundles the Engine with a GUI and a Linux VM for Windows and macOS.
- On Windows that VM is WSL 2 — the CLI is native, the daemon and all containers are inside Linux.
- `docker version` checks the client and daemon, `docker info` reports daemon state, and `docker run hello-world` proves the whole path including registry access.
- The dashboard and the CLI are two clients of the same daemon, showing the same state.
