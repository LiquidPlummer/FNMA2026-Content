# Docker Demo — Instructor Working Directory

Everything the demo needs. Nothing here is for students; they watch.

```
docker-demo/
├── app/                 <- the build target for topic 005
│   ├── public/index.html
│   ├── server.js
│   ├── package.json
│   └── .dockerignore    <- present; the Dockerfile is NOT
├── site/                <- static files for the bind-mount demo, topic 006
│   └── index.html
├── instructor/          <- reference copies, keep off screen
│   ├── Dockerfile.reference
│   ├── Dockerfile.bad-order
│   └── compose.reference.yaml
└── commands.md          <- the scratch command list, in demo order
```

## The app

A minimal Express service. `GET /` serves a page showing the container's
**hostname**, platform, Node version, PID, and database status; it re-fetches
every five seconds. Hostname is the point — inside a container it is the
container ID, which makes "this is a different machine" visible without
saying it.

The database connection is lazy and failure-tolerant. The app runs fine with
no Postgres anywhere, so topics 001–005 never need one. In topic 007 the page
flips to green the moment Compose brings `db` up alongside it.

Environment variables it reads: `PORT` (3000), `GREETING`, `DB_HOST`,
`DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`.

## Before you present

```bash
cd docker-demo/app && npm install        # so the host-run baseline works
docker pull hello-world
docker pull alpine
docker pull nginx:alpine
docker pull nginx:latest
docker pull node:22-alpine
docker pull postgres:16-alpine
```

Then confirm the clean slate:

```bash
docker ps -a                             # empty
docker images | grep myapp               # nothing
docker volume ls | grep pgdata           # nothing
```

`app/` must have **no Dockerfile** when you start. Topic 005 writes it live;
`instructor/Dockerfile.reference` is your safety net if typing goes wrong.
Same for `compose.yaml` and topic 007.

## Reset between runs

```bash
docker rm -f web app db 2>/dev/null
docker volume rm pgdata 2>/dev/null
docker rmi myapp:1 2>/dev/null
rm app/Dockerfile compose.yaml 2>/dev/null
git checkout site/index.html app/public/index.html 2>/dev/null
```
