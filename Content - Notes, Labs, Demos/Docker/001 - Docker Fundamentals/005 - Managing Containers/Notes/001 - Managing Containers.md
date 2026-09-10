# Managing Containers

Starting a container is the easy part. Most of the time we spend with Docker goes to the rest of it: finding out what exists, reading what a container is doing, getting inside one that's misbehaving, and clearing out what's accumulated.

---

## Seeing What Exists

```bash
docker ps
```

*Lists only **running** containers, with ID, image, command, uptime, published ports, and name.*

```bash
docker ps -a
```

*Lists **every** container the daemon knows about, including stopped ones.*

The difference matters more than it looks. A stopped container hasn't gone anywhere — it still occupies disk space, still holds its name, and still holds its logs. `docker ps` showing nothing does not mean nothing exists.

This is why a container name can be "already in use" when nothing appears to be running, and why disk usage climbs during a debugging session. When something is missing, `-a` is almost always the next command.

Useful variations:

```bash
docker ps -a --filter "status=exited"
docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
docker ps -aq
```

*Filter by status, reshape the columns, or print only IDs. The last one is designed to be piped into other commands.*

---

## Referring to a Container

Three ways to name a container in any command:

- **The name** — `web`, whether we chose it or Docker generated one.
- **The full ID** — a 64-character hex string.
- **A short ID prefix** — the first few characters, which is what `docker ps` displays.

The prefix only needs to be long enough to be unambiguous, so in practice three or four characters is plenty:

```bash
docker stop a3f9
docker stop web
```

*Both work. Names are better for anything we'll type more than once or put in a script; short IDs are convenient for one-off commands against a container we can see in `docker ps`.*

---

## The Lifecycle Commands

```bash
docker stop web
```

*Sends `SIGTERM` to the main process and waits — 10 seconds by default — for it to shut down. If it hasn't exited by then, sends `SIGKILL`. This is the graceful option: it gives the application a chance to close connections and finish writing.*

```bash
docker start web
```

*Restarts a stopped container using its original configuration. Ports, environment variables, and mounts are all as they were — they cannot be changed here.*

```bash
docker restart web
```

*Stop followed by start.*

```bash
docker kill web
```

*Sends `SIGKILL` immediately with no grace period. The process gets no chance to clean up. Reserve it for containers that won't respond to `stop`.*

```bash
docker rm web
```

*Deletes a stopped container — its writable layer and its logs. Refuses to touch a running container unless forced with `-f`, which kills and removes in one step.*

The `stop` versus `kill` distinction matters for anything holding state. A database given `SIGKILL` mid-write may need recovery on next start. Prefer `stop`, and reach for `kill` when `stop` has already failed.

---

## Reading What a Container Is Doing

### Logs

Docker captures whatever the main process writes to standard output and standard error.

```bash
docker logs web
```

*Prints everything the container has written since it started.*

```bash
docker logs -f web
```

*Follows the log, streaming new lines as they arrive. `Ctrl+C` stops following without affecting the container.*

```bash
docker logs --tail 50 -f web
docker logs --since 10m web
docker logs -t web
```

*Start from the last 50 lines and follow; show only the last 10 minutes; prefix each line with a timestamp.*

Logs work on stopped containers too, which is exactly where they're most valuable — a container that exited immediately usually explains why in its final lines. This is also the reason to think twice about `--rm` on anything that might fail: removing the container removes the evidence.

One implication of Docker capturing stdout: **a containerized application should log to standard output, not to a file inside the container.** A log file written inside the container is invisible to `docker logs` and disappears when the container is removed.

### Inspect

```bash
docker inspect web
```

*Dumps the container's complete configuration and state as JSON — every setting, its IP address, mounts, environment variables, exit code, and more. Comprehensive and much too long to read whole.*

The format flag makes it practical:

```bash
docker inspect --format '{{.State.Status}}' web
docker inspect --format '{{.State.ExitCode}}' web
docker inspect --format '{{.NetworkSettings.IPAddress}}' web
docker inspect --format '{{json .Config.Env}}' web
```

*Extracts a single field. These are worth knowing when a container has exited and we need the exit code, or when we need the environment it actually received rather than the one we think we passed.*

### Stats

```bash
docker stats
```

*A live, continuously updating view of CPU, memory, network, and disk I/O per running container. `Ctrl+C` exits.*

```bash
docker stats --no-stream
```

*Takes one snapshot and returns, which is what we want in a script or when we just need a number.*

The memory column is the one to watch. A container hitting its limit gets killed by the kernel, which shows up as a container that "randomly died" with exit code 137.

---

## Getting a Shell Inside a Container

`docker exec` runs an additional command inside an already-running container. With `-it`, that command can be a shell.

```bash
docker exec -it web sh
```

*Opens an interactive shell inside the running container. Exiting leaves the container running — we're only ending the extra process we started.*

This is the primary debugging tool. Inside, we can check whether a file was copied where we expected, read the environment the process actually got, verify what's listening, and try to reach another service.

```bash
docker exec web ls -la /app
docker exec web env
docker exec web cat /etc/hosts
```

*Runs one command and returns, without an interactive session. Useful for a quick check or in a script.*

Note that `exec` requires a **running** container. A container that crashed on startup can't be exec'd into — for that, `docker logs` is the tool, or starting a new container from the same image with a shell as its command.

### What's Actually In There

Here's where the base image choice from lesson 003 becomes concrete, and it surprises people:

```bash
docker exec -it web bash
# OCI runtime exec failed: exec: "bash": executable file not found in $PATH
```

*A perfectly normal result on an Alpine-based image, which ships BusyBox `sh` and no `bash` at all.*

A container is not a Linux machine with the usual tools. It contains whatever the image put there and nothing else. On a minimal base image, expect:

| Expectation | Reality on Alpine |
|---|---|
| `bash` | Not installed — use `sh` |
| `apt-get` | Not present — the package manager is `apk` |
| `curl`, `wget` | Often absent; `wget` sometimes exists via BusyBox |
| `ps`, `top` | BusyBox versions with fewer options |
| `vim`, `nano` | Not installed |
| `ping`, `dig`, `netstat` | Usually absent |

The habit that saves time: **try `sh` first.** It exists on essentially every Linux image, including Debian-based ones where `bash` is also available.

```bash
docker exec -it web sh
```

*Works on Alpine, Debian, Ubuntu, and nearly everything else.*

If a tool is genuinely needed for debugging, it can be installed temporarily inside the running container:

```bash
docker exec -it web sh
apk add --no-cache curl        # Alpine
apt-get update && apt-get install -y curl   # Debian / Ubuntu
```

*Installs into the container's writable layer only. It affects nothing else, and it vanishes when the container is removed — which is the right outcome. Do not add debugging tools to the image to make this easier.*

This is also the honest trade-off of minimal base images: smaller and less exposed, but noticeably harder to poke at when something goes wrong.

---

## Cleaning Up

Docker accumulates. Stopped containers, images from abandoned experiments, orphaned volumes, and build cache all consume disk, and none of it is removed automatically.

Start by finding out what's actually using space:

```bash
docker system df
```

*Breaks down disk usage across images, containers, volumes, and build cache, with a reclaimable column for each.*

Targeted cleanup:

```bash
docker container prune
docker image prune
docker volume prune
docker builder prune
```

*Removes, in order: all stopped containers; dangling images (untagged leftovers from rebuilds); volumes not attached to any container; cached build layers.*

Or all at once:

```bash
docker system prune
```

*Removes stopped containers, unused networks, dangling images, and build cache in one pass. It prompts for confirmation and reports what it will delete.*

Two warnings about the aggressive variants:

```bash
docker system prune -a           # also removes every image not used by a running container
docker system prune -a --volumes # also removes volumes — including ones holding real data
```

*`-a` will delete images we intended to keep and force re-downloading them. `--volumes` can delete database contents permanently. Read the confirmation prompt rather than accepting reflexively.*

`docker system prune` without flags is safe and worth running periodically. The `-a --volumes` combination is not something to run out of habit.

---

## Key Takeaways

- `docker ps` shows running containers; `docker ps -a` shows all of them — stopped containers keep their name, logs, and disk space.
- Containers can be referenced by name, full ID, or an unambiguous short ID prefix.
- `stop` is graceful (SIGTERM, then SIGKILL after a grace period); `kill` is immediate; prefer `stop` for anything holding state.
- `docker logs` works on stopped containers and is the first place to look when one exited unexpectedly.
- Containerized applications should log to stdout, not to a file inside the container.
- `docker inspect --format` extracts single fields — exit code, status, IP, environment — from an otherwise unreadable JSON dump.
- `docker exec -it <name> sh` opens a shell in a running container; try `sh` before `bash`, because minimal images often have no `bash`.
- A container has only what its image installed — expect no `curl`, no editors, and `apk` instead of `apt` on Alpine.
- `docker system df` finds what's using disk; `docker system prune` is a safe periodic cleanup, while `-a --volumes` can destroy real data.
