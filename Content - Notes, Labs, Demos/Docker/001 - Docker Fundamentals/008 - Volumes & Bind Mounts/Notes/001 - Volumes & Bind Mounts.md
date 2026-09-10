# Volumes & Bind Mounts

Everything a container writes goes into its writable layer, and that layer is destroyed when the container is removed. For a stateless web service that's fine and even desirable. For a database it's a disaster. This lesson covers the two mechanisms for putting data somewhere that outlives the container.

---

## The Container Filesystem Is Ephemeral

Recall the structure from lesson 003: an image is read-only layers, and starting a container adds a thin writable layer on top. Every file the container creates or modifies lives in that writable layer.

That layer belongs to the container, not the image, and it dies with the container:

```bash
docker run -d --name db -e POSTGRES_PASSWORD=dev postgres:16.4
# ... create tables, insert data ...
docker rm -f db
```

*Every byte of that database is now gone. Not archived, not recoverable — deleted with the container's writable layer.*

Worth being precise about what does and doesn't destroy data:

| Action | Writable layer |
|---|---|
| `docker stop` | Preserved — the container still exists |
| `docker start` | Same layer, data intact |
| `docker restart` | Same layer, data intact |
| `docker rm` | **Destroyed** |
| `docker run` (a new container) | A brand new empty layer |

Stopping is safe. Removing is not. And since we remove and recreate containers constantly — that's the whole model, since configuration can't be changed on an existing container — anything that must survive has to live outside the writable layer.

Docker offers two ways to do that, and choosing correctly between them is most of this lesson.

---

## Named Volumes

A **volume** is storage managed by Docker, living outside any container, that can be mounted into one at a path we choose.

```bash
docker volume create pgdata
```

*Creates a volume named `pgdata`. Docker decides where it physically lives — on Windows, inside the WSL 2 VM's filesystem.*

```bash
docker run -d --name db \
  -e POSTGRES_PASSWORD=dev \
  -v pgdata:/var/lib/postgresql/data \
  postgres:16.4
```

*Mounts the `pgdata` volume at `/var/lib/postgresql/data` inside the container — the directory Postgres uses for its data files. Everything the database writes there goes to the volume.*

Now the container is genuinely disposable:

```bash
docker rm -f db
docker run -d --name db \
  -e POSTGRES_PASSWORD=dev \
  -v pgdata:/var/lib/postgresql/data \
  postgres:16.4
```

*Destroys the container and creates a fresh one. The data is still there, because it was never in the container. This is also how a database gets upgraded — remove the container, start a new one from a newer image, same volume.*

Managing them:

```bash
docker volume ls
docker volume inspect pgdata
docker volume rm pgdata
docker volume prune
```

*Lists volumes, shows one volume's details including its location on disk, deletes one (refused while a container is using it), and removes all volumes not attached to any container.*

A volume named in `-v` that doesn't exist yet is **created automatically**. Convenient, and also the source of a common surprise: a typo in a volume name silently produces a new empty volume rather than an error, and the data appears to have vanished. It hasn't — `docker volume ls` will show both.

### Reading What's Inside

Volumes live inside the Docker VM, so they aren't browsable from Windows Explorer. Two ways in:

The Docker Desktop dashboard's Volumes pane shows the files directly — one of the few places the GUI is meaningfully easier.

Or mount the volume into a throwaway container:

```bash
docker run --rm -it -v pgdata:/data alpine sh
```

*Starts a temporary Alpine container with the volume mounted at `/data`, giving a shell to inspect it. The container is deleted on exit; the volume is untouched.*

---

## Bind Mounts

A **bind mount** maps a directory from the host machine directly into the container. Unlike a volume, we specify the exact host path, and Docker manages nothing — it's the host's directory, appearing inside the container.

```bash
docker run -d --name web \
  -v C:\Users\me\project\src:/app/src \
  -p 3000:3000 \
  my-app:1.0
```

*Mounts the host's `src` directory at `/app/src` inside the container. The container sees the real files on the host, and edits on either side are immediately visible to the other.*

The defining property: **the host directory replaces whatever was at that path in the image.** If the image had files at `/app/src`, they're hidden while the mount is in place. This is deliberate and is exactly what makes the development use case work — the image's copy of the source is replaced by the live one.

That same behavior causes the most common bind-mount accident. Mounting over a directory the image populated during the build hides the built content:

```bash
# Mounts the whole project over /app, hiding the node_modules
# that `npm ci` installed during the build
docker run -v C:\Users\me\project:/app my-app:1.0
```

*The container now sees the host's `/app`, which has no `node_modules` — and the application fails to start with missing-module errors.*

The usual workaround is to mount only the source subdirectory, or to add an anonymous volume over the path that must keep the image's version:

```bash
docker run -v C:\Users\me\project:/app -v /app/node_modules my-app:1.0
```

*The second `-v`, with no host path, tells Docker to keep the image's `/app/node_modules` rather than letting the bind mount hide it.*

---

## Host Paths on Windows

Bind mounts need an absolute host path, and on Windows there are three ways to write one plus a shell that rewrites them. This is a genuine time sink, so here it is directly.

**PowerShell and CMD** take native Windows paths:

```powershell
docker run -v C:\Users\me\project:/app my-app:1.0
docker run -v C:/Users/me/project:/app my-app:1.0
```

*Both work. Forward slashes are accepted and avoid backslash-escaping problems, so they're the safer habit.*

**Paths with spaces need quoting**, and the quotes go around the whole `-v` argument:

```powershell
docker run -v "C:/Users/me/My Project:/app" my-app:1.0
```

*Quoting only the host portion breaks the argument — the colon separators must be inside the quotes.*

**WSL and Linux-style paths** use the `/mnt/c/` form:

```bash
docker run -v /mnt/c/Users/me/project:/app my-app:1.0
```

*Correct when running the Docker CLI from inside a WSL distribution. Note that files on `/mnt/c` are on the Windows filesystem, accessed across the WSL boundary — noticeably slower than files kept inside the WSL filesystem.*

**Git Bash rewrites paths**, which is the one that produces genuinely baffling errors. Git Bash's MSYS layer converts anything that looks like a Unix path into a Windows path before the program sees it — so `/app` silently becomes something like `C:/Program Files/Git/app`:

```bash
# In Git Bash: the container path gets mangled
docker run -v /c/Users/me/project:/app my-app:1.0

# Fix: a leading double slash disables the rewriting
docker run -v /c/Users/me/project://app my-app:1.0

# Or disable it for the command
MSYS_NO_PATHCONV=1 docker run -v /c/Users/me/project:/app my-app:1.0
```

*The symptom is an error mentioning a path containing `Program Files` or `Git` that we never typed. If that appears, the shell is rewriting paths.*

The simplest way to avoid all of this: **use PowerShell with forward slashes**, or use `${PWD}` for the current directory:

```powershell
docker run -v ${PWD}:/app my-app:1.0        # PowerShell
docker run -v "$(pwd)":/app my-app:1.0      # bash / WSL
```

*Portable across machines and avoids typing absolute paths at all.*

Docker Desktop also needs permission to share the drive. If a bind mount produces a permissions error or an unexpectedly empty directory, check Settings → Resources → File Sharing.

---

## `-v` vs `--mount`

Two syntaxes do the same job.

**`-v`** is compact, positional, and colon-separated:

```bash
-v pgdata:/var/lib/postgresql/data
-v C:/Users/me/project:/app
-v C:/Users/me/project:/app:ro
```

*Volume, bind mount, and a read-only bind mount. Docker infers the type from whether the first part looks like a path.*

**`--mount`** is explicit and key-value based:

```bash
--mount type=volume,source=pgdata,target=/var/lib/postgresql/data
--mount type=bind,source=C:/Users/me/project,target=/app
--mount type=bind,source=C:/Users/me/project,target=/app,readonly
```

*Verbose, but every part is named and the type is stated rather than inferred.*

Functionally they're near-identical, with one behavioral difference worth knowing: if the host path in a `-v` bind mount doesn't exist, Docker **creates it as an empty directory**; `--mount` **errors instead**. So a typo in a `-v` host path produces a container mounting an empty directory it just created, while `--mount` says the source doesn't exist. When a bind mount appears mysteriously empty, a typo plus `-v`'s helpfulness is a likely explanation.

`-v` is what most documentation and most people use, and it's fine. `--mount` is worth preferring in scripts and Compose files, where clarity and a real error message beat brevity.

---

## Choosing Between Them

Both put data outside the writable layer, but they're for different jobs.

| | Named volume | Bind mount |
|---|---|---|
| **Location** | Managed by Docker | A path we specify on the host |
| **Created by** | Docker, automatically if needed | Must already exist (or `-v` creates it empty) |
| **Host visibility** | Not directly browsable on Windows | Ordinary files in a folder |
| **Performance on Windows/macOS** | Fast — inside the VM | Slower — crosses the VM boundary |
| **Portability** | Works identically anywhere | Depends on host paths existing |
| **Typical use** | Databases, uploads, persistent app state | Source code during development, config files |

The rule of thumb:

> **Volumes for data the application owns. Bind mounts for files a human edits.**

A database's storage is the application's data — we never open those files by hand, and we want Docker to manage them. That's a volume, and the performance and portability both favor it.

Source code during development is the opposite: we edit it in an editor on the host and want the container to see changes immediately. That's a bind mount, and it's the reason the mechanism exists.

Configuration files sit in the middle and usually go either way depending on whether they're edited by hand.

One more caution on volumes: `docker volume prune` and `docker system prune --volumes` delete volumes not currently attached to a container. A database whose container was removed has an unattached volume — and it will be deleted, permanently, with the data in it. This is the most common way people lose real data with Docker.

---

## Key Takeaways

- A container's writable layer is destroyed by `docker rm`; stopping and starting preserve it, removing does not.
- Named volumes are Docker-managed storage that outlives any container and is mounted at a chosen path.
- A volume named in `-v` is created automatically if missing — so a typo silently produces an empty volume rather than an error.
- Volumes live inside the VM on Windows; inspect them via the dashboard or by mounting into a throwaway container.
- Bind mounts map a specific host directory in, and the host's contents **replace** whatever the image had at that path.
- Mounting over a directory the build populated (like `node_modules`) hides it — mount a subdirectory or shadow it with an anonymous volume.
- On Windows, prefer PowerShell with forward slashes or `${PWD}`; Git Bash rewrites paths and needs `//` or `MSYS_NO_PATHCONV=1`.
- `-v` creates a missing bind-mount host path as an empty directory; `--mount` errors, which is usually more helpful.
- Volumes for data the application owns; bind mounts for files a human edits.
- `docker volume prune` deletes unattached volumes — including the one holding a database whose container was removed.
