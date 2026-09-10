# Docker Desktop & the CLI

Docker Desktop's dashboard and the `docker` command are two clients of the same daemon. Anything visible in one is visible in the other, and an action taken in either shows up immediately in the other. This is an orientation tour: what each pane shows, which command corresponds to it, and when to reach for which.

We haven't covered images, volumes, or networks yet — those are the next several lessons. The goal here is only to know where things live, so the dashboard is familiar rather than mysterious when we start using it.

> **A note on versions:** Docker Desktop's interface changes regularly, and panes get renamed, reorganized, or added between releases. The mapping below describes what each area does rather than exactly where it sits, so expect small differences from whatever version is installed.

---

## The Containers Pane

The default view, and the one most used. It lists every container the daemon knows about, running or stopped, with its name, the image it came from, its status, and any published ports.

Selecting a container opens a detail view with its logs, a terminal into the container, a live resource graph, and its full configuration.

| In the dashboard | On the command line |
|---|---|
| The container list | `docker ps -a` |
| Toggling "only running" | `docker ps` |
| Start / Stop / Restart buttons | `docker start`, `docker stop`, `docker restart` |
| Delete button | `docker rm` |
| Logs tab | `docker logs`, `docker logs -f` |
| Terminal / Exec tab | `docker exec -it <name> sh` |
| Inspect tab | `docker inspect <name>` |
| Stats / resource graph | `docker stats` |
| The clickable port link | the `-p` flag used when starting it |

The clickable port link is genuinely useful — the dashboard turns a published port into a hyperlink that opens the right `localhost` address in a browser, which saves guessing which port ended up mapped where.

---

## The Images Pane

Lists images stored locally, with repository, tag, size, and when they were created. It usually flags images not currently used by any container, which makes it easy to see what's safe to delete.

| In the dashboard | On the command line |
|---|---|
| The image list | `docker images` |
| Pull | `docker pull <image>` |
| Run (with the optional settings dialog) | `docker run` with flags |
| Delete | `docker rmi <image>` |
| Clean up unused | `docker image prune` |
| Hub / search tab | `docker search`, or the Docker Hub website |

The **Run** dialog is worth knowing about early. Clicking Run on an image opens a form with fields for a container name, published ports, volumes, and environment variables — which is exactly the set of `docker run` flags we're about to learn. Filling in that form and comparing it to the equivalent command is a good way to make the flags stick.

---

## The Volumes Pane

Lists Docker-managed volumes — the mechanism for keeping data alive after a container is removed. Shows each volume's name, size, and which containers are using it, and allows browsing the files inside without starting a container.

| In the dashboard | On the command line |
|---|---|
| The volume list | `docker volume ls` |
| Create | `docker volume create <name>` |
| Delete | `docker volume rm <name>` |
| Clean up unused | `docker volume prune` |
| File browser | no direct equivalent |

That file browser is one of the few places the GUI does something the CLI genuinely can't do as easily. Inspecting a volume's contents from the command line means starting a throwaway container with the volume attached; the dashboard just shows the files.

---

## The Builds Pane

Shows a history of image builds, including which succeeded, how long each took, and the full output log for each one. Useful for reviewing a build that failed a few minutes ago without scrolling back through terminal history.

| In the dashboard | On the command line |
|---|---|
| Build history | the output of past `docker build` runs |
| Build detail / logs | scrollback, or `docker build` output |

---

## Settings

The settings dialog covers things with no everyday CLI equivalent, because they configure the daemon and its VM rather than any container:

- **Resources** — how much CPU, memory, and disk the Linux VM is allowed. The most common reason to open settings at all: the default memory limit can be too low for a container running a database or a JVM build.
- **General** — whether Docker Desktop starts with the machine, and which backend it uses.
- **Docker Engine** — the daemon's raw JSON configuration, for things like registry mirrors.
- **Resource Saver / auto-pause** — lets the VM idle down when nothing is running.

If containers are being killed unexpectedly during memory-heavy work, the Resources page is the first place to look.

---

## Choosing Between Them

Neither interface is the "correct" one, but they're good at different things.

**The dashboard is better for:**

- Seeing what currently exists at a glance, especially early on when `docker ps -a` output is hard to read.
- Reading logs, since it scrolls and searches comfortably.
- Browsing files inside a volume.
- Adjusting the VM's CPU and memory limits.
- Cleaning up when several containers and images have accumulated.

**The CLI is better for:**

- Anything we need to do more than once. A command can be re-run from history, saved in a script, or pasted to a colleague; a sequence of clicks cannot.
- Anything that has to run somewhere without a GUI — a server, a CI pipeline, a remote host.
- Precision. Every option is a documented flag, whereas the GUI exposes a curated subset.
- Sharing and documentation. Instructions written as commands are unambiguous and don't go stale when the UI is redesigned.

The practical habit worth building: **use the CLI as the default, and the dashboard for looking around.** The rest of this module is written in commands, both because they're what transfers to a server and because they're what we'll actually be able to reproduce six months from now.

---

## Key Takeaways

- The dashboard and the CLI are both clients of the same daemon; they show the same state and neither hides anything from the other.
- Containers, Images, and Volumes panes correspond to `docker ps -a`, `docker images`, and `docker volume ls`.
- The Images pane's Run dialog is the same set of options as `docker run`'s flags, in a form.
- The volume file browser and the VM resource limits are the two things meaningfully easier in the GUI.
- Settings → Resources controls the Linux VM's CPU and memory, and is the first place to check when containers die during heavy work.
- Default to the CLI for anything repeatable, scriptable, or destined for a machine without a GUI.
