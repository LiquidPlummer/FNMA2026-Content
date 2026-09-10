# Running Containers

`docker run` is the command we'll use more than any other. It takes an image and produces a running container, and almost everything interesting about how that container behaves is decided by the flags we pass here.

---

## What `docker run` Actually Does

`docker run` is a convenience that bundles three separate operations:

1. **Pull** the image, if the daemon doesn't already have it locally.
2. **Create** a container from it — allocate the writable layer, apply the configuration, but start nothing.
3. **Start** that container, launching its process.

Those steps exist as their own commands (`docker pull`, `docker create`, `docker start`), and knowing that clears up a recurring confusion: **most `docker run` flags configure the container at creation time, so they cannot be changed later.** Restarting a container with `docker start` reuses the configuration it was created with. To change a published port or an environment variable, we remove the container and run a new one.

That sounds wasteful and isn't. Containers are meant to be disposable — the image is the durable thing, and the container is a cheap instance of it.

---

## The Flags That Matter

Roughly six flags cover the overwhelming majority of everyday use.

```bash
docker run -d --name web -p 8080:80 nginx:1.27
```

*Starts nginx in the background, names the container `web`, and makes it reachable at `http://localhost:8080`.*

### `-d` — Detached

Without `-d`, the container runs in the foreground: its output streams to the terminal and the terminal is blocked until it exits. With `-d`, the daemon starts it in the background and prints the container ID.

Foreground is useful while getting something working, because we see the logs immediately. Detached is normal for anything long-running.

### `--name` — A Readable Name

Without a name, Docker generates one like `nostalgic_hopper`. Names must be unique among existing containers — including stopped ones — which is a common irritation:

```
docker: Error response from daemon: Conflict. The container name "/web" is
already in use by container "a3f9...".
```

*A stopped container still holds its name. Either remove it with `docker rm web`, or use `--rm` so it cleans up automatically.*

### `-p` — Publish a Port

The most important flag to understand properly, covered in its own section below.

### `-e` — Environment Variable

Passes configuration into the container. Also covered in its own section.

### `-it` — Interactive Terminal

Two flags almost always used together: `-i` keeps standard input open, `-t` allocates a pseudo-terminal. Together they let us interact with a shell or a REPL inside the container.

```bash
docker run -it --rm python:3.12-slim python
```

*Starts a Python REPL inside a throwaway container. Exiting the REPL ends the container, and `--rm` deletes it.*

Without `-it`, an interactive program has no terminal and typically exits immediately.

### `--rm` — Clean Up On Exit

Automatically removes the container once it stops. Excellent for anything short-lived — one-off commands, experiments, quick tests — because it prevents the pile of stopped containers that otherwise accumulates.

Do not use it for anything whose logs we might want after it stops. Removing the container removes its logs.

---

## Publishing Ports

A container has its own network namespace, and therefore its own set of ports. A process listening on port 80 *inside* a container is listening on that container's port 80, which is not the host's port 80 and is not reachable from the host by default.

**Publishing** a port maps a host port to a container port:

```bash
docker run -d -p 8080:80 nginx:1.27
```

*The mapping reads `host:container`. Traffic arriving at port 8080 on the host is forwarded to port 80 inside the container.*

The order is the single most common mistake, so it's worth a mnemonic: **the host is on the outside, and it's written on the outside (left).**

```
   Browser                Host                    Container
   ────────                ────                    ─────────
   localhost:8080  ──►  port 8080  ──────────►  port 80
                                                (nginx listening)
```

*We choose the host port freely; the container port is whatever the application inside is actually listening on.*

A few consequences:

- **Host ports must be free.** Two containers can't both publish to host port 8080. Both can use container port 80, because those are in separate namespaces — publish them to 8080 and 8081.
- **Without `-p`, the app is unreachable from the host**, even though it's running perfectly well.
- **`-p 8080:80` is not the same as `-p 80:8080`.** Reversing it forwards host port 80 to container port 8080, where nothing is listening.
- **Container-to-container traffic doesn't need publishing at all.** Publishing exposes a container to the *host*. Containers talking to each other is a networking topic we'll get to later.

To let Docker pick a free host port, give only the container port:

```bash
docker run -d -P nginx:1.27
docker ps
```

*`-P` publishes all exposed ports to random available host ports; `docker ps` shows which ones were assigned.*

---

## The Application Must Listen on `0.0.0.0`

This one blocks nearly everyone once, and the symptom is maddening: the container is running, the logs look healthy, `-p` is correct, and the browser still can't connect.

The cause is inside the application. A server binds to an address as well as a port:

- **`127.0.0.1` (localhost)** means "accept connections originating from this machine only." Inside a container, "this machine" is the container. Traffic forwarded in from the host arrives on the container's network interface, which is *not* the loopback interface — so the application never sees it.
- **`0.0.0.0`** means "accept connections on every interface." This is what a containerized application needs.

Many frameworks default to localhost for safety during local development, which is exactly wrong inside a container:

```javascript
app.listen(3000, '127.0.0.1');   // unreachable from outside the container
app.listen(3000, '0.0.0.0');     // correct
app.listen(3000);                // usually defaults to 0.0.0.0 — verify
```

*The bind address is part of the application's configuration, not Docker's — no `docker run` flag can compensate for a process bound to loopback.*

The same issue in other stacks:

```bash
# Python / Flask
flask run --host=0.0.0.0

# Spring Boot (application.properties)
server.address=0.0.0.0

# Vite dev server
vite --host 0.0.0.0
```

*Each framework has its own way of setting the bind address; all of them need it set to `0.0.0.0` when containerized.*

**How to tell this is the problem:** if `docker ps` shows the port mapping and the logs show the server started, get a shell into the container and check what it's bound to. A server reporting `Listening on 127.0.0.1:3000` has diagnosed itself.

---

## Configuration at Run Time

Here is the mental shift that makes containers work properly, and it's worth stating directly:

> **The image is built once and runs unchanged in every environment. Everything that differs between environments is injected when the container starts.**

Database URLs, API keys, feature flags, log levels — none of these belong in the image. If they were baked in, we'd need a separate image per environment, and the image we tested would not be the image we deployed. The entire value proposition collapses.

Environment variables are the standard mechanism:

```bash
docker run -d --name api \
  -e DATABASE_URL=postgres://db:5432/app \
  -e LOG_LEVEL=debug \
  -p 3000:3000 \
  my-api:1.4.0
```

*Each `-e` sets one variable inside the container. The application reads them exactly as it would read variables set any other way.*

The same image with different values is a different environment:

```bash
# staging
docker run -d -e DATABASE_URL=postgres://staging-db/app -e LOG_LEVEL=debug my-api:1.4.0

# production
docker run -d -e DATABASE_URL=postgres://prod-db/app   -e LOG_LEVEL=warn  my-api:1.4.0
```

*One tested artifact, two behaviors, decided at start time.*

For more than a couple of variables, put them in a file:

```bash
docker run -d --env-file ./app.env -p 3000:3000 my-api:1.4.0
```

*Reads `KEY=value` lines from the file. The file stays out of the image and out of shell history — and belongs in `.gitignore`.*

Two cautions worth internalizing now:

- **Anything passed with `-e` is visible in `docker inspect`** and to anyone who can reach the daemon. It keeps secrets out of the *image*, which is the big win, but it is not strong secret management. Production systems use dedicated secret stores.
- **Never bake secrets into the image instead.** We'll cover this again when writing Dockerfiles, but the short version: a credential copied into a layer is in that layer permanently, and shipping it to a registry ships the credential with it.

Official images use environment variables heavily for their own configuration:

```bash
docker run -d --name db \
  -e POSTGRES_PASSWORD=devpassword \
  -e POSTGRES_DB=appdb \
  -p 5432:5432 \
  postgres:16.4
```

*A working Postgres instance, configured entirely through environment variables. The image's Docker Hub page documents which ones it supports.*

---

## A Container Lives as Long as Its Main Process

A container is not a machine that stays up until shut down. It runs exactly one main process, and **when that process exits, the container stops.** That's the whole lifecycle rule.

This explains several confusing behaviors:

- `docker run hello-world` "immediately stopped" because it printed a message and finished. That's correct.
- `docker run ubuntu` exits instantly, because the default command is `bash`, and `bash` with no terminal attached and nothing to read has nothing to do. `docker run -it ubuntu` stays alive because now there's a terminal.
- A web server container keeps running because the server process doesn't return.
- A container "dies for no reason" — the process crashed. `docker logs` will usually say why.

The exit code is preserved and visible:

```bash
docker ps -a
```

*Shows stopped containers with a status like `Exited (0) 2 minutes ago`. Exit code 0 means a clean finish; anything else means a failure worth investigating in the logs.*

Because the main process defines the container's lifetime, running two things in one container is awkward and generally discouraged. The convention is one concern per container — a web server in one, a database in another — connected together, which is where Compose eventually comes in.

---

## Key Takeaways

- `docker run` = pull (if needed) + create + start; most flags are fixed at creation and require a new container to change.
- `-d` detaches, `--name` gives a readable name, `--rm` cleans up on exit, `-it` provides an interactive terminal.
- `-p host:container` publishes a port — the host side is written on the left, and without it the app is unreachable from the host.
- Two containers can share a container port but not a host port.
- The application must bind `0.0.0.0`, not `127.0.0.1`; no Docker flag can fix a process listening only on loopback.
- One image runs everywhere; environment-specific configuration is injected at run time with `-e` or `--env-file`.
- Values passed with `-e` are visible via `docker inspect` — better than baking them into an image, but not real secret management.
- A container stops when its main process exits; `docker ps -a` shows the exit code and `docker logs` usually shows the reason.
