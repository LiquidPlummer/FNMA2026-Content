# Docker Compose

By the end of the last lesson, running a two-container application meant creating a network and issuing two long `docker run` commands with the right flags in the right order. That works, and it doesn't survive contact with a real team.

**Docker Compose** replaces those commands with a file.

---

## The Problem

Here's a realistic setup, using everything from the previous lessons:

```bash
docker network create app-net

docker volume create pgdata

docker run -d --name db --network app-net \
  -e POSTGRES_PASSWORD=devpassword \
  -e POSTGRES_DB=appdb \
  -v pgdata:/var/lib/postgresql/data \
  postgres:16.4

docker run -d --name api --network app-net \
  -e DATABASE_URL=postgres://postgres:devpassword@db:5432/appdb \
  -p 3000:3000 \
  my-api:1.0
```

*Four commands, an exact order, and a set of flags that must agree with each other — the network name in two places, the credentials in two places, the database hostname matching the container name.*

Everything wrong with this is a maintenance problem rather than a technical one. It lives in someone's shell history or a README that drifts. Onboarding means pasting commands and hoping. Changing the Postgres password means remembering it appears twice. Nothing is reviewable, and nothing is in version control.

Compose puts all of it in one file that lives with the code.

---

## The Same Thing in Compose

```yaml
services:
  db:
    image: postgres:16.4
    environment:
      POSTGRES_PASSWORD: devpassword
      POSTGRES_DB: appdb
    volumes:
      - pgdata:/var/lib/postgresql/data

  api:
    image: my-api:1.0
    environment:
      DATABASE_URL: postgres://postgres:devpassword@db:5432/appdb
    ports:
      - "3000:3000"
    depends_on:
      - db

volumes:
  pgdata:
```

*The complete equivalent of all four commands. Saved as `compose.yaml` in the project root.*

```bash
docker compose up -d
```

*Creates the network, creates the volume, starts both containers in dependency order, and returns.*

The file is declarative — it describes the desired state rather than the steps. That means it can be committed, reviewed in a pull request, and run identically by anyone who clones the repository.

Two conveniences are already visible: the network is never mentioned because Compose creates one automatically, and the volume is declared once and referenced by name.

> **A note on names:** the current filename is `compose.yaml`; older projects use `docker-compose.yml`, which still works. The command is `docker compose` (a subcommand of Docker); the older standalone `docker-compose` is a deprecated separate tool. Also, a `version:` key at the top of the file is obsolete — modern Compose ignores it and warns if it's present.

---

## The File, Piece by Piece

### `services`

Each key under `services` defines one container. The key is the **service name**, and it becomes both the container's hostname on the network and the name used in commands.

### `image` vs `build`

A service either uses a pre-built image or builds one from a Dockerfile.

```yaml
services:
  db:
    image: postgres:16.4        # pull this image

  api:
    build: .                    # build from the Dockerfile in this directory
```

*`image` pulls; `build` builds. A service uses one or the other.*

The longer form of `build` gives more control:

```yaml
  api:
    build:
      context: .
      dockerfile: docker/Dockerfile.dev
    image: my-api:dev
```

*Builds using an explicitly named Dockerfile and tags the result, so it's identifiable in `docker images`.*

### `ports`

Publishes ports, exactly like `-p`:

```yaml
    ports:
      - "3000:3000"
      - "8080:80"
```

*Same `host:container` ordering as the CLI. Quote these — YAML interprets some unquoted colon-separated numbers as sexagesimal values, which produces genuinely strange results.*

The same reasoning from lesson 009 applies: publish only what needs reaching from outside. The `db` service in the example has no `ports` because only `api` talks to it, and `api` doesn't go through the host to do it.

### `environment` and `env_file`

Two ways to set environment variables:

```yaml
    environment:
      POSTGRES_PASSWORD: devpassword
      LOG_LEVEL: debug
```

*Inline values, visible to anyone reading the file.*

```yaml
    env_file:
      - .env
```

*Reads `KEY=value` lines from a file that isn't committed.*

That distinction matters, because `compose.yaml` goes into version control. Any credential written inline is a credential in the repository, in its history, visible to everyone with read access — and still there after being edited out, exactly like a secret in an image layer.

The normal pattern is to keep the file committed and the values out of it:

```yaml
services:
  db:
    image: postgres:16.4
    environment:
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      POSTGRES_DB: appdb
```

*`${POSTGRES_PASSWORD}` is substituted from a `.env` file in the project directory, or from the shell environment.*

```
# .env — listed in .gitignore, never committed
POSTGRES_PASSWORD=devpassword
```

*A `.env` alongside `compose.yaml` is read automatically for variable substitution. Commit a `.env.example` with the keys and no values so others know what to provide.*

For a local development database with a throwaway password, an inline value is defensible. The habit worth building is that a real credential never goes in a committed file.

### `volumes`

Referenced per service, and named volumes declared at the bottom of the file:

```yaml
services:
  db:
    volumes:
      - pgdata:/var/lib/postgresql/data     # named volume
  api:
    volumes:
      - ./src:/app/src                      # bind mount, relative to the file

volumes:
  pgdata:
```

*Named volumes must also appear in the top-level `volumes:` block, which is where Compose is told to create them. Bind mounts don't, and relative paths resolve from the compose file's directory — which sidesteps the Windows absolute-path problems from lesson 008 entirely.*

### `depends_on`

Controls startup order — and this is the field most often misunderstood:

```yaml
    depends_on:
      - db
```

*Starts `db` before `api`.*

> **`depends_on` guarantees start order, not readiness.**

Compose starts the database container and immediately proceeds to the API container. It does not wait for Postgres to finish initializing and begin accepting connections. On a first run, where the database has to create its data directory, the API will very likely start and fail to connect.

Three ways to handle it, in increasing order of correctness:

**Restart policy** — let the API crash and retry until the database is up:

```yaml
    restart: on-failure
```

*Crude but effective for local development, and requires no other changes.*

**A healthcheck plus a condition** — the proper mechanism:

```yaml
services:
  db:
    image: postgres:16.4
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 3s
      retries: 5

  api:
    image: my-api:1.0
    depends_on:
      db:
        condition: service_healthy
```

*The database declares how to check whether it's ready, and `api` waits for that check to pass before starting.*

**Retry logic in the application** — the most robust, because databases also become temporarily unavailable long after startup. An application that reconnects on failure needs no orchestration help at all.

---

## The Network Compose Creates

Compose creates a network for the project automatically and attaches every service to it. That's why the example never mentions a network and why `db` resolves as a hostname.

**Each service is reachable at its service name.** From `api`, the database is at `db:5432` — service name, container's own port, exactly as in lesson 009.

Rename the service to `database`, and the connection string has to become `database:5432`. The service name *is* the hostname.

The network is named after the project (the directory name by default), so two projects in different directories are isolated from each other and can both have a service called `db` without conflict.

---

## The Commands

```bash
docker compose up -d
```

*Creates networks and volumes, builds images where `build` is specified and none exists, then creates and starts every service. `-d` detaches, as with `docker run`.*

```bash
docker compose up
```

*Same, but in the foreground with interleaved, colour-coded logs from every service. Excellent while getting a stack working. `Ctrl+C` stops everything.*

```bash
docker compose down
```

*Stops and removes the containers and the network. **Named volumes are kept**, so data survives.*

```bash
docker compose down -v
```

*Also deletes the named volumes declared in the file — and the data in them. This is the deliberate "start completely fresh" command, and it's destructive.*

```bash
docker compose ps
docker compose logs
docker compose logs -f api
```

*Lists the project's services and their state; shows logs from all services, or follows one.*

```bash
docker compose exec api sh
```

*Opens a shell in the running `api` service — the Compose equivalent of `docker exec -it`. Note it takes the service name, not the container name.*

```bash
docker compose build
docker compose up -d --build
```

*Rebuilds images. The second form is the one to remember: `up` alone will not rebuild after a Dockerfile or source change, which causes a lot of confusion about changes not taking effect.*

```bash
docker compose restart api
docker compose stop
```

*Restarts a single service, or stops everything without removing it.*

One thing that makes Compose pleasant: `up` is idempotent and incremental. Change one service's configuration and re-run `docker compose up -d`, and Compose recreates only what changed, leaving the rest running.

---

## Compose Is a Development Tool

Compose runs containers on **one machine**. That's its scope, and it's an excellent fit for:

- Local development stacks — the application plus its dependencies, started with one command.
- Integration tests that need a real database.
- Demos and single-machine deployments.

It does not do the things a production system needs at scale: spreading containers across multiple hosts, restarting them elsewhere when a machine dies, rolling out a new version without downtime, or scaling automatically under load. Those are orchestration concerns, and they're where Kubernetes and the managed container services come in — a separate topic, and out of scope here.

That's not a limitation to work around. A Compose file that makes the whole stack start with one command is enormously valuable on its own, and it's the format most developers actually use every day.

---

## Key Takeaways

- Compose replaces a sequence of `docker run` commands with one declarative file that lives in version control.
- The file is `compose.yaml`, the command is `docker compose`; `docker-compose` and the `version:` key are both legacy.
- Each key under `services` is a container; the service name becomes its hostname.
- A service uses either `image` (pull) or `build` (build from a Dockerfile).
- Quote port mappings, and publish only what needs reaching from outside Docker.
- Compose creates a project network automatically and every service is reachable at its service name.
- Credentials belong in an uncommitted `.env` referenced by `${VAR}` substitution, not inline in a committed file.
- Named volumes are referenced per service and declared in the top-level `volumes:` block.
- `depends_on` controls start order only — use a healthcheck with `condition: service_healthy`, or application retry logic, for readiness.
- `down` keeps volumes; `down -v` deletes them and the data in them.
- `up` does not rebuild after code changes — use `up -d --build`.
- Compose runs on a single machine; multi-host scheduling, rolling updates, and autoscaling are orchestration concerns beyond it.
