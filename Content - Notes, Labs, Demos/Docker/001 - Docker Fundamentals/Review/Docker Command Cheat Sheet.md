# Docker Command Cheat Sheet

A recall aid for the commands covered in this module, grouped by what we're trying to do rather than alphabetically. Every command here appears in the lesson notes — this is not a condensed copy of `docker --help`, and anything not taught in the module is deliberately absent.

Placeholders: `<name>` is a container name or ID, `<image>` is an image reference like `nginx:1.27`.

---

## Setup & Daemon

| Command | What it does |
|---|---|
| `docker version` | Client and server versions — if the server section is missing, the daemon isn't running |
| `docker info` | Daemon state: container and image counts, storage driver, available CPU and memory |
| `docker run hello-world` | End-to-end check — proves the client reaches the daemon, the daemon reaches a registry, and containers can run |

> "Cannot connect to the Docker daemon" almost always means Docker Desktop isn't started.

---

## Images

| Command | What it does |
|---|---|
| `docker pull <image>` | Download an image from a registry |
| `docker images` | List local images |
| `docker rmi <image>` | Delete an image — fails if any container, even stopped, still references it |
| `docker tag <src> <new>` | Add another name to an existing image; creates no new image |
| `docker history <image>` | Show the layers an image is made of, and the instruction that created each |
| `docker search <term>` | Search Docker Hub from the command line |

```bash
docker pull postgres:16.4
docker images
docker rmi nginx:1.27
```

**Image naming:** `registry/repository:tag`. Omitting the registry means Docker Hub; omitting the tag means `latest`. `latest` is just the default tag, **not** the newest version — pin real versions.

```bash
nginx                          # → docker.io/library/nginx:latest
postgres:16.4                  # official image, explicit version
myusername/my-app:1.0          # a Docker Hub user's namespace
ghcr.io/my-org/my-app:1.0      # a different registry
```

---

## Running Containers

```bash
docker run [flags] <image> [command]
```

`run` = pull if needed + create + start. **Most flags are fixed at creation** — to change a port or environment variable, remove the container and run a new one.

| Flag | What it does |
|---|---|
| `-d` | Detached — run in the background |
| `--name <name>` | Give it a readable name instead of a generated one |
| `-p <host>:<container>` | Publish a port — **host port on the left** |
| `-P` | Publish all `EXPOSE`d ports to random host ports |
| `-e KEY=value` | Set an environment variable |
| `--env-file <file>` | Read environment variables from a file |
| `-v <source>:<target>` | Mount a volume or bind mount |
| `-it` | Interactive terminal — needed for a shell or REPL |
| `--rm` | Delete the container when it exits |
| `--network <name>` | Attach to a specific network |
| `--restart on-failure` | Restart automatically if it exits with an error |

```bash
# a background web server, reachable at localhost:8080
docker run -d --name web -p 8080:80 nginx:1.27

# a throwaway interactive shell
docker run -it --rm alpine sh

# a database with configuration and persistent storage
docker run -d --name db \
  -e POSTGRES_PASSWORD=devpassword \
  -e POSTGRES_DB=appdb \
  -v pgdata:/var/lib/postgresql/data \
  -p 5432:5432 \
  postgres:16.4
```

---

## Container Lifecycle

| Command | What it does |
|---|---|
| `docker ps` | List **running** containers |
| `docker ps -a` | List **all** containers, including stopped ones |
| `docker stop <name>` | Graceful shutdown — SIGTERM, then SIGKILL after ~10s |
| `docker start <name>` | Restart a stopped container with its original configuration |
| `docker restart <name>` | Stop then start |
| `docker kill <name>` | Immediate SIGKILL, no grace period |
| `docker rm <name>` | Delete a stopped container, its writable layer, and its logs |
| `docker rm -f <name>` | Kill and delete in one step |

```bash
docker ps -a
docker ps -a --filter "status=exited"
docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
docker ps -aq                                  # IDs only, for piping
```

Containers can be referenced by name, full ID, or any unambiguous short ID prefix — `docker stop a3f9` works.

Prefer `stop` over `kill` for anything holding state. A stopped container still holds its name, logs, and disk space.

---

## Inspecting & Debugging

| Command | What it does |
|---|---|
| `docker logs <name>` | Everything the container has written to stdout/stderr |
| `docker logs -f <name>` | Follow the log live (`Ctrl+C` to stop following) |
| `docker logs --tail 50 <name>` | Last 50 lines only |
| `docker logs --since 10m <name>` | Last 10 minutes only |
| `docker logs -t <name>` | Prefix each line with a timestamp |
| `docker exec -it <name> sh` | Open a shell inside a **running** container |
| `docker exec <name> <cmd>` | Run one command inside a container and return |
| `docker inspect <name>` | Full configuration and state as JSON |
| `docker stats` | Live CPU, memory, network, and I/O per container |
| `docker stats --no-stream` | One snapshot instead of a live view |

```bash
docker logs --tail 100 -f api
docker exec -it web sh
docker exec web env                            # what environment did it actually get?
docker exec web ls -la /app                    # did the files land where expected?
```

`docker inspect` alone is too long to read. Extract single fields:

```bash
docker inspect --format '{{.State.Status}}' web
docker inspect --format '{{.State.ExitCode}}' web
docker inspect --format '{{.NetworkSettings.IPAddress}}' web
docker inspect --format '{{json .Config.Env}}' web
```

**Try `sh` before `bash`** — minimal images (Alpine especially) have no `bash`, no `apt`, and often no `curl`. Logs work on stopped containers, which is exactly where they're most useful.

---

## Volumes

| Command | What it does |
|---|---|
| `docker volume create <name>` | Create a named volume |
| `docker volume ls` | List volumes |
| `docker volume inspect <name>` | Details, including where it lives on disk |
| `docker volume rm <name>` | Delete a volume — refused while a container uses it |

```bash
# named volume — Docker-managed, for data the application owns
docker run -d -v pgdata:/var/lib/postgresql/data postgres:16.4

# bind mount — a host directory, for files a human edits
docker run -d -v C:/Users/me/project/src:/app/src my-app:1.0

# read-only bind mount
docker run -d -v C:/Users/me/config:/app/config:ro my-app:1.0

# current directory, portably
docker run -d -v ${PWD}:/app my-app:1.0        # PowerShell
docker run -d -v "$(pwd)":/app my-app:1.0      # bash / WSL
```

The `--mount` long form is more explicit and **errors on a missing host path** instead of silently creating an empty directory:

```bash
docker run -d --mount type=volume,source=pgdata,target=/var/lib/postgresql/data postgres:16.4
docker run -d --mount type=bind,source=C:/Users/me/project,target=/app my-app:1.0
```

**Volumes for data the application owns; bind mounts for files a human edits.** A volume named in `-v` that doesn't exist is created automatically — so a typo produces an empty volume, not an error.

**Windows paths:** use PowerShell with forward slashes. In Git Bash, paths get rewritten — use `//app` or prefix with `MSYS_NO_PATHCONV=1`.

---

## Networks

| Command | What it does |
|---|---|
| `docker network create <name>` | Create a user-defined bridge network |
| `docker network ls` | List networks |
| `docker network inspect <name>` | Details, including every attached container and its IP |
| `docker network connect <net> <name>` | Attach a running container to a network |
| `docker network disconnect <net> <name>` | Detach it |
| `docker network rm <name>` | Delete a network |

```bash
docker network create app-net

docker run -d --name db --network app-net \
  -e POSTGRES_PASSWORD=devpassword postgres:16.4

docker run -d --name api --network app-net \
  -e DATABASE_URL=postgres://postgres:devpassword@db:5432/postgres \
  -p 3000:3000 my-api:1.0
```

**Always create a user-defined network for containers that talk to each other.** On one, containers resolve each other by container name; on the default bridge they cannot.

Container-to-container traffic uses the **container's own port** (`db:5432`), not any published host port, and needs no `-p` at all. To reach the host machine from inside a container, use `host.docker.internal`.

---

## Building & Publishing

| Command | What it does |
|---|---|
| `docker build -t <name>:<tag> .` | Build an image; the `.` is the **build context** |
| `docker build -f <path> ...` | Use a differently named or located Dockerfile |
| `docker build --no-cache ...` | Rebuild every layer from scratch |
| `docker build --pull ...` | Re-pull the base image before building |
| `docker login` | Authenticate to a registry |
| `docker push <name>:<tag>` | Upload an image to a registry |

```bash
docker build -t my-app:1.0 .
docker build -f docker/Dockerfile.prod -t my-app:1.0 .
docker build -t my-app:1.4.2 -t my-app:1.4 -t my-app:latest .
```

Publishing needs a **namespaced** name:

```bash
docker login
docker tag my-app:1.0 myusername/my-app:1.0
docker push myusername/my-app:1.0
```

`CACHED` in the build output means a layer was reused. The first uncached step is where the rebuild's cost starts — if dependency installation is rebuilding every time, the instruction order is wrong.

---

## Docker Compose

Run from the directory containing `compose.yaml`. Commands take the **service name**, not the container name.

| Command | What it does |
|---|---|
| `docker compose up -d` | Create networks and volumes, then start every service |
| `docker compose up` | Same, in the foreground with interleaved logs |
| `docker compose up -d --build` | Rebuild images first — **`up` alone does not rebuild** |
| `docker compose down` | Stop and remove containers and the network; **keeps volumes** |
| `docker compose down -v` | Also delete named volumes and the data in them |
| `docker compose ps` | List the project's services and their state |
| `docker compose logs` | Logs from all services |
| `docker compose logs -f <service>` | Follow one service's logs |
| `docker compose exec <service> sh` | Shell into a running service |
| `docker compose build` | Rebuild images without starting anything |
| `docker compose restart <service>` | Restart one service |
| `docker compose stop` | Stop everything without removing it |

```bash
docker compose up -d
docker compose logs -f api
docker compose exec api sh
docker compose down
```

The command is `docker compose`; the standalone `docker-compose` is the deprecated older tool.

---

## Cleanup & Disk Space

| Command | What it does |
|---|---|
| `docker system df` | What's actually using disk, and how much is reclaimable |
| `docker container prune` | Delete all stopped containers |
| `docker image prune` | Delete dangling (untagged) images |
| `docker volume prune` | Delete volumes not attached to any container |
| `docker network prune` | Delete unused networks |
| `docker builder prune` | Delete cached build layers |
| `docker system prune` | Stopped containers, unused networks, dangling images, build cache |

```bash
docker system df                    # always start here
docker system prune                 # safe periodic cleanup
```

⚠️ **The aggressive forms delete real things:**

```bash
docker system prune -a              # also every image not used by a RUNNING container
docker system prune -a --volumes    # also volumes — including database data
```

`docker volume prune` deletes the volume belonging to a database whose container was removed. This is the most common way people lose real data with Docker. Read the confirmation prompt.

---

## Gotchas Worth Memorizing

| Symptom | Cause |
|---|---|
| "Cannot connect to the Docker daemon" | Docker Desktop isn't running |
| Container runs, browser can't connect | Missing `-p`, **or** the app is bound to `127.0.0.1` instead of `0.0.0.0` |
| Port mapping seems backwards | `-p` is `host:container` — host on the left |
| `EXPOSE` in the Dockerfile but still unreachable | `EXPOSE` is documentation only; it publishes nothing |
| "Container name already in use" | A **stopped** container still holds the name — `docker rm` it or use `--rm` |
| Container exits immediately | Its main process finished; the container's life is the process's life |
| Container "randomly died", exit code 137 | Killed for exceeding its memory limit |
| `exec: "bash": executable file not found` | Minimal image — use `sh` |
| "no such file or directory" for a file that exists | Missing interpreter — usually CRLF line endings in a script |
| Another container unreachable by name | Containers are on the default bridge; create a user-defined network |
| App can't reach the DB at `localhost` | Inside a container, `localhost` is that container — use the service or container name |
| Bind mount is mysteriously empty | Typo in the host path, and `-v` created it as an empty directory |
| `node_modules` missing after a bind mount | The mount hid the directory the build populated |
| Code changes not taking effect under Compose | `up` doesn't rebuild — use `up -d --build` |
| Service starts before its database is ready | `depends_on` is start order only, not readiness |
