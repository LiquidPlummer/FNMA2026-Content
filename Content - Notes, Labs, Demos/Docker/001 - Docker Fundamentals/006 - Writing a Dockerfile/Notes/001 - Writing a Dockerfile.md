# Writing a Dockerfile

So far we've run images other people built. A **Dockerfile** is how we build our own: a plain text file listing the steps to assemble an image, executed top to bottom by the daemon.

It reads like a shell script, and that comparison is useful right up to the point where it isn't. Each instruction produces a new layer, and layers are what make builds fast, images shareable — and secrets permanent.

---

## The Shape of a Dockerfile

A minimal but complete example, which we'll take apart instruction by instruction:

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

*A complete Dockerfile for a Node service: start from a Node base image, install dependencies, copy the source, and declare how to start it.*

The file is conventionally named `Dockerfile` with no extension, and lives at the root of the project it builds.

---

## One Instruction, One Layer

Each instruction that changes the filesystem creates a layer capturing that change. The Dockerfile above produces roughly one layer per `COPY` and `RUN`, stacked on top of the base image's layers.

This has a direct, practical consequence: **the layer cache**.

When rebuilding, Docker walks the instructions in order and reuses the cached layer for each one as long as nothing has changed. The moment an instruction's inputs differ, that layer is rebuilt — **and so is every layer after it**, because each layer depends on the state left by the one before.

Which brings us to the single most important thing about writing Dockerfiles:

> **Order instructions from least likely to change to most likely to change.**

Look again at the middle of that example:

```dockerfile
COPY package*.json ./
RUN npm ci --omit=dev

COPY . .
```

*Dependencies are copied and installed **before** the application source is copied.*

That ordering is deliberate. Source code changes constantly; the dependency list changes rarely. By copying only `package.json` first, the expensive `npm ci` layer stays cached through every source edit. Rebuilding after changing a source file re-runs only the final `COPY`, taking seconds.

Written the naive way, it looks tidier and behaves much worse:

```dockerfile
COPY . .
RUN npm ci --omit=dev
```

*Any change to any file invalidates the `COPY`, which invalidates the `RUN`, which reinstalls every dependency from scratch. Every build. This is the difference between a five-second rebuild and a three-minute one.*

The same pattern applies everywhere: `pom.xml` before Java source, `requirements.txt` before Python source, `go.mod` before Go source.

---

## The Instructions

### `FROM`

Every Dockerfile starts here. It sets the base image that everything else builds on.

```dockerfile
FROM node:22-slim
```

*Starts from the Node 22 slim image. Pin a specific version — `FROM node` uses `latest` and makes builds non-reproducible.*

### `WORKDIR`

Sets the working directory for every instruction that follows, creating it if needed.

```dockerfile
WORKDIR /app
```

*Subsequent `COPY`, `RUN`, and `CMD` instructions operate relative to `/app`.*

Use this rather than `RUN cd /app`. Each `RUN` executes in its own shell, so a `cd` in one has no effect on the next — a genuinely confusing failure. `WORKDIR` persists.

### `COPY`

Copies files from the build context (covered below) into the image.

```dockerfile
COPY package*.json ./
COPY src/ ./src/
COPY . .
```

*Copies matching files, a directory, or everything, into the current `WORKDIR`.*

There's also `ADD`, which additionally unpacks archives and fetches URLs. The convention is to use `COPY` unless one of those extras is specifically needed — `COPY` does exactly one predictable thing.

### `RUN`

Executes a command *during the build* and captures the resulting filesystem changes as a layer.

```dockerfile
RUN npm ci --omit=dev
```

*Installs dependencies at build time. The installed files become part of the image.*

Chaining related commands into a single `RUN` keeps layers fewer and smaller:

```dockerfile
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*
```

*Updates the package index, installs curl, and deletes the package lists — all in one layer, so the deleted lists never occupy space in the final image. Split across three `RUN` instructions, the lists would be added in one layer and merely hidden by another, still shipping in the image.*

That's worth pausing on, because it's the same immutability rule from lesson 003: deleting a file in a later layer hides it, it doesn't remove it. Cleanup only saves space when it happens in the same layer as the creation.

### `ENV`

Sets an environment variable that persists into the running container.

```dockerfile
ENV NODE_ENV=production
```

*Available both to later build instructions and to the process at runtime. Can be overridden at run time with `-e`.*

Use `ENV` for defaults that are genuinely part of the image. Anything that varies by environment belongs in `-e` at run time, not here.

### `EXPOSE`

```dockerfile
EXPOSE 3000
```

*Documents that the application listens on port 3000. **It publishes nothing.***

This is the most commonly misunderstood instruction in Docker, so let's be blunt: `EXPOSE` has essentially no runtime effect. It does not open a port, does not make the container reachable, and does not substitute for `-p`. A container built from an image with `EXPOSE 3000` is still unreachable from the host unless started with `-p 3000:3000`.

What it actually does is record metadata. Tools read it — `docker run -P` uses it to decide what to publish to random ports, and the Docker Desktop dashboard uses it to show a link. Mostly it's documentation for humans reading the Dockerfile.

Include it, because it's genuinely useful documentation. Just don't expect it to do anything.

### `CMD` and `ENTRYPOINT`

Both specify what runs when the container starts, and the relationship between them confuses everyone at first.

**`CMD`** provides the default command, and it is **replaced** by any command given on the `docker run` line.

```dockerfile
CMD ["node", "server.js"]
```

*Runs `node server.js` by default. `docker run my-app npm test` runs `npm test` instead — the CMD is discarded.*

**`ENTRYPOINT`** sets a fixed executable that always runs, and arguments from `docker run` are **appended** to it.

```dockerfile
ENTRYPOINT ["node", "server.js"]
```

*Always runs `node server.js`. `docker run my-app --port=4000` runs `node server.js --port=4000`.*

Used together, `ENTRYPOINT` is the command and `CMD` supplies default arguments:

```dockerfile
ENTRYPOINT ["node", "server.js"]
CMD ["--port=3000"]
```

*Runs `node server.js --port=3000` by default; `docker run my-app --port=8080` swaps in the different argument while keeping the fixed command.*

Which to use:

- **`CMD` alone** for most application images. It's flexible — anyone can override it to get a shell or run a one-off command, which is convenient for debugging.
- **`ENTRYPOINT` + `CMD`** when the image *is* a specific tool and arguments should behave like that tool's flags.

### Shell Form vs Exec Form

Both `CMD` and `ENTRYPOINT` accept two syntaxes, and the difference is not cosmetic.

```dockerfile
CMD ["node", "server.js"]     # exec form  — a JSON array
CMD node server.js            # shell form — a plain string
```

*Exec form runs the executable directly. Shell form wraps it: Docker runs `/bin/sh -c "node server.js"`.*

That wrapper causes a real problem. In shell form, the shell becomes PID 1 — the container's main process — and the application is a child of it. When `docker stop` sends `SIGTERM`, it goes to PID 1, and `sh` does not forward signals to its children.

So the application never receives the shutdown signal. It keeps running, oblivious, until the 10-second grace period expires and Docker sends `SIGKILL`. Every stop takes ten seconds, and the application never gets to shut down cleanly — no closing connections, no finishing writes, no flushing buffers.

**Use exec form.** The JSON array syntax, with double quotes:

```dockerfile
CMD ["node", "server.js"]
```

*The application is PID 1 and receives signals directly, so it can shut down gracefully and `docker stop` returns immediately.*

Note that exec form is genuinely JSON — double quotes are required, single quotes will fail.

---

## The Build Context and `.dockerignore`

When a build starts, the CLI packages up a directory — the **build context** — and sends it to the daemon. `COPY` can only read from that context, which is why `COPY ../something` doesn't work.

The context is usually the project directory, and sending it wholesale is often a mistake. A typical Node project's `node_modules` is hundreds of megabytes; a Java project's `target` directory holds build output; `.git` holds the entire repository history.

A **`.dockerignore`** file excludes paths from the context, exactly like `.gitignore`:

```
node_modules
.git
.env
*.log
dist
target
.vscode
```

*Excluded paths are never sent to the daemon and can never be copied into the image, even by `COPY . .`.*

This does three things at once:

1. **Speeds up builds**, because less data is transferred.
2. **Improves cache behavior**, because irrelevant file changes no longer invalidate `COPY . .`.
3. **Prevents accidents.** `COPY . .` with a `.env` file in the directory copies that `.env` into the image. `.dockerignore` is what stops it.

Excluding `node_modules` is not optional, incidentally. Dependencies installed on a Windows or macOS host can include platform-specific binaries that don't work in a Linux container. Copying them in produces failures that make no sense; the `RUN npm ci` inside the build is what should populate that directory.

---

## Never Copy Secrets Into an Image

This deserves its own section, because it's easy to do, easy to miss in review, and cannot be undone.

```dockerfile
# WRONG — do not do this
COPY .env .
RUN ./configure.sh
RUN rm .env
```

*The `rm` looks like it cleans up. It does not. The `.env` file exists permanently in the layer created by `COPY`, and the `rm` only adds a marker hiding it in the final filesystem.*

Anyone with the image can extract that file — the layers are right there, and `docker history` shows exactly where to look. Push the image to a registry, and the credential is now wherever the registry is, for everyone with pull access, in every copy anyone made.

The same applies to any secret written into a layer: an API key in an `ENV` instruction, a token in a `RUN` command's arguments, an SSH key copied in to fetch a private dependency.

What to do instead:

- **Runtime configuration** — inject with `-e` or `--env-file` when the container starts, as covered in lesson 004. This is the answer for the overwhelming majority of cases.
- **Build-time secrets** — for a credential genuinely needed *during* the build, such as a private registry token, Docker provides `--secret`, which mounts a file for one instruction without writing it to any layer. Worth knowing exists; rarely needed at this stage.
- **`.dockerignore`** — list `.env` and any key files, so `COPY . .` cannot pick them up by accident.

The rule to carry forward: **if it shouldn't be in a public git repository, it shouldn't be in a layer.**

---

## A Windows Gotcha: Line Endings

This one costs people an afternoon, and the error message is actively misleading.

Windows tools write text files with CRLF line endings (`\r\n`); Linux uses LF (`\n`). A shell script written on Windows and copied into an image carries those carriage returns, so its first line reads:

```
#!/bin/sh\r
```

Linux takes that literally — it looks for an interpreter named `/bin/sh\r`, which doesn't exist. The result:

```
exec ./entrypoint.sh: no such file or directory
```

*The file plainly exists and `ls` will show it. The missing file is the interpreter, not the script.*

Any of these fixes it:

```
# .gitattributes — the durable fix, applied at checkout
*.sh text eol=lf
```

*Forces LF endings for shell scripts regardless of platform. This is the right answer because it fixes the file for everyone on the team.*

```dockerfile
RUN sed -i 's/\r$//' entrypoint.sh && chmod +x entrypoint.sh
```

*Strips carriage returns during the build. A reliable safety net, though it treats the symptom.*

Editors can also be set to LF for a project — VS Code's setting is `files.eol`, and most editors have an equivalent.

The general shape of this problem is worth remembering: **"no such file or directory" about a file that exists usually means a missing interpreter or a missing shared library**, not a missing file.

---

## Building and Running It

We've written a file. Let's make it real — the mechanics get a full treatment in the next lesson, but a Dockerfile that has never produced a container is just text.

Given the Node example above, in a directory containing `server.js` and `package.json`:

```bash
docker build -t my-app:1.0 .
```

*Builds an image from the `Dockerfile` in the current directory and tags it `my-app:1.0`. The trailing `.` is the build context — the directory sent to the daemon.*

Watch the output as it runs. Each instruction is reported in order with its own step, and the layers get built one at a time.

```bash
docker images my-app
```

*Confirms the image now exists locally, with its tag and size.*

```bash
docker run -d --name my-app-test -p 3000:3000 my-app:1.0
```

*Starts a container from our image, detached, publishing container port 3000 to host port 3000.*

```bash
docker ps
docker logs my-app-test
```

*Confirms it's running, then shows what the application printed on startup.*

At this point everything from earlier lessons applies unchanged — this is an ordinary image and an ordinary container, and nothing about it being ours makes it different. Visit `http://localhost:3000`, and if it doesn't respond, the checklist from lesson 004 is the place to start: is the port published, and is the application bound to `0.0.0.0`?

Now change one line of `server.js` and rebuild:

```bash
docker build -t my-app:1.0 .
```

*Notice how much of the output reports cached layers. The dependency install is reused; only the final `COPY` and everything after it rebuilds. That's the instruction ordering from the top of this lesson paying off.*

Clean up when finished:

```bash
docker rm -f my-app-test
```

*Stops and removes the test container in one step.*

---

## Key Takeaways

- A Dockerfile is an ordered list of instructions; each filesystem-changing instruction produces a layer.
- Changing one instruction invalidates its layer and every layer after it — order from least to most frequently changing.
- Copy dependency manifests and install dependencies *before* copying source code; this is the single biggest build-speed win.
- `WORKDIR` sets the directory for later instructions; `RUN cd` does not persist between instructions.
- Prefer `COPY` over `ADD` unless archive extraction or URL fetching is specifically needed.
- Chain related commands in one `RUN` so cleanup actually reduces image size.
- `EXPOSE` is documentation only — it publishes nothing, and `-p` is still required.
- `CMD` is a replaceable default; `ENTRYPOINT` is fixed and receives run arguments as parameters.
- Always use exec form (`CMD ["node", "server.js"]`) — shell form makes `sh` PID 1, which swallows shutdown signals.
- The build context is what gets sent to the daemon; `.dockerignore` keeps it small and prevents accidental copies.
- A secret copied into a layer stays in that layer permanently; deleting it later only hides it.
- On Windows, CRLF line endings in shell scripts cause "no such file or directory" errors about files that exist.
