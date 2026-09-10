# Container Networking

A container has its own network namespace — its own interfaces, its own IP address, its own set of ports. That isolation is why publishing a port was necessary in lesson 004, and it's also what we have to work through when an application container needs to reach a database container.

---

## The Default Bridge

Every container attaches to a network. Without any configuration, that's a built-in network called `bridge`.

```bash
docker network ls
```

*Lists networks. A fresh install shows three: `bridge`, `host`, and `none`.*

On the default bridge, each container gets a private IP address on a virtual network, and outbound traffic is translated through the host — so a container can reach the internet without any setup at all.

```bash
docker run --rm alpine ping -c 2 example.com
```

*Works with no configuration. Outbound access is available by default.*

Inbound is the opposite. Nothing on the host can reach a container's ports unless they're published with `-p`, which is the asymmetry from lesson 004.

### The Default Bridge Has No Name Resolution

Here's the part that matters, and it's a real limitation rather than a quirk:

> **Containers on the default bridge cannot find each other by name.**

They're on the same network and can reach each other by IP address, but there's no DNS. Container `api` has no way to resolve the hostname `db`.

```bash
docker run -d --name db postgres:16.4
docker run --rm alpine ping -c 2 db
# ping: bad address 'db'
```

*Both containers are on the default bridge and can reach each other by IP, but the name doesn't resolve.*

Using IP addresses instead is not a workaround. Container IPs are assigned at start time and change whenever a container is recreated — which happens constantly. Hard-coding one guarantees a broken configuration by tomorrow.

The fix is a user-defined network.

---

## User-Defined Bridge Networks

Creating our own network gets us automatic DNS, and this is the single most useful thing in this lesson.

```bash
docker network create app-net
```

*Creates a user-defined bridge network named `app-net`.*

```bash
docker run -d --name db --network app-net \
  -e POSTGRES_PASSWORD=dev postgres:16.4

docker run -d --name api --network app-net \
  -e DATABASE_URL=postgres://postgres:dev@db:5432/postgres \
  -p 3000:3000 my-api:1.0
```

*Both containers join `app-net`. The `api` container reaches the database at the hostname `db` — its container name — with no IP addresses anywhere.*

On a user-defined network, **Docker runs an embedded DNS server that resolves container names to their current IP addresses**. A container recreated with a new IP is found automatically at the same name, because the DNS entry follows the name rather than the address.

That single difference is why the guidance is unambiguous: **always create a user-defined network for containers that talk to each other.** The default bridge exists mainly for backward compatibility.

User-defined networks also give better isolation. Containers on different networks cannot reach each other at all, so an unrelated container isn't sitting on the same network as the database.

Managing them:

```bash
docker network ls
docker network inspect app-net
docker network connect app-net some-container
docker network disconnect app-net some-container
docker network rm app-net
docker network prune
```

*Lists networks, shows a network's details including every attached container and its IP, attaches and detaches a running container, removes a network (refused while containers are attached), and removes all unused networks.*

`docker network connect` is worth noting: a container can be on several networks at once, which is how a service that needs to talk to two otherwise-isolated groups is wired up.

---

## Publishing vs Connecting

These are two different problems, and conflating them causes a specific and common mistake.

**Publishing (`-p`) exposes a container to the host.** It's for traffic originating outside Docker — a browser, a REST client, another machine.

**A shared network lets containers reach each other.** It's for traffic between containers, and it does not involve the host at all.

```
   Browser
      │
      │  localhost:3000        ← needs -p 3000:3000
      ▼
  ┌───────────────────────────────────────────┐
  │  Docker network "app-net"                 │
  │                                           │
  │   ┌──────────┐   db:5432    ┌──────────┐  │
  │   │   api    │ ───────────► │    db    │  │
  │   │  :3000   │              │  :5432   │  │
  │   └──────────┘              └──────────┘  │
  │                                           │
  └───────────────────────────────────────────┘
      Only `api` is published. `db` is reachable
      from `api`, but not from the host.
```

*Publishing is for outside-in traffic; the shared network handles container-to-container traffic on its own.*

The mistake this prevents: publishing the database's port and then configuring the API to connect to `localhost:5432`. Inside the API container, `localhost` is the API container itself — where nothing is listening on 5432. The connection fails even though the database is running and its port is published.

**Inside a container, `localhost` always means that container.** Reaching a different container means using its name on a shared network.

There's a second reason not to publish a database in the first place: publishing puts it on the host's network interface. Anything that can reach the host can reach the database. If only other containers need it, leave it unpublished — the shared network is enough, and it's the more secure arrangement.

The exception is development convenience. Publishing `5432` so a local database GUI can connect is entirely reasonable on a laptop; it's the production habit to avoid.

### Which Port to Use

On a shared network, containers connect to the **container's own port**, not any published host port:

```bash
docker run -d --name db --network app-net -p 15432:5432 postgres:16.4
```

*Postgres listens on 5432 inside the container, published to 15432 on the host.*

- From the host: `localhost:15432`
- From another container on `app-net`: `db:5432`

*The published port is irrelevant to container-to-container traffic — that path goes directly over the network at the container's real port.*

---

## Reaching the Host from a Container

Sometimes a container needs to reach a service running on the host machine — a database installed natively, or an API server running in an IDE.

`localhost` won't do it, for the same reason as before: inside the container that's the container.

Docker Desktop provides a special hostname:

```bash
docker run --rm alpine ping -c 2 host.docker.internal
```

*`host.docker.internal` resolves to the host machine from inside a container. Available on Docker Desktop for Windows and macOS.*

```bash
docker run -d --name api \
  -e DATABASE_URL=postgres://host.docker.internal:5432/appdb \
  my-api:1.0
```

*Connects to a Postgres instance installed natively on the Windows host rather than one in a container.*

Two caveats:

- **On native Linux this doesn't exist by default.** It has to be added explicitly with `--add-host=host.docker.internal:host-gateway`. Since the audience here is on Docker Desktop this mostly won't come up, but a script that works on a laptop and fails on a Linux CI runner has usually hit this.
- **The host service must be listening on an interface the container can reach.** A service on the host bound to `127.0.0.1` is unreachable from a container — the same `0.0.0.0` problem from lesson 004, in the opposite direction.

---

## Other Network Modes

Two other modes exist. Neither is common in development, but both appear in documentation.

**`--network host`** removes network isolation entirely — the container shares the host's network stack, so a container port *is* a host port and `-p` becomes meaningless.

```bash
docker run -d --network host nginx:1.27
```

*Nginx binds directly to the host's port 80. Fast, and with no isolation. On Docker Desktop the behavior is limited, because the "host" is the Linux VM rather than Windows.*

**`--network none`** gives the container no network access at all, beyond loopback. Occasionally useful for running something untrusted.

The default bridge or, better, a user-defined network covers nearly everything.

---

## Key Takeaways

- Each container has its own network namespace — its own interfaces, IP, and ports.
- Outbound internet access works by default; inbound from the host requires `-p`.
- Containers on the **default** bridge cannot resolve each other by name, and IPs change on recreation.
- User-defined networks provide automatic DNS: a container is reachable at its container name.
- Always create a user-defined network for containers that need to talk to each other.
- Publishing exposes a container to the host; a shared network handles container-to-container traffic and needs no publishing.
- Inside a container, `localhost` is that container — never the host and never another container.
- Container-to-container traffic uses the container's real port, not any published host port.
- Databases used only by other containers don't need publishing, and are safer without it.
- `host.docker.internal` reaches the host from a container on Docker Desktop; native Linux needs `--add-host`.
