# Images & Docker Hub

An image is the artifact we actually ship. Before writing one, it pays to understand how they're structured, because the layer model explains why builds are fast, why images share disk space, and why a careless instruction can permanently embed a secret.

---

## An Image Is a Stack of Layers

An image is not a single archive file. It's an ordered stack of **layers**, each one a set of filesystem changes, plus a small **configuration** object describing how to start a process.

Each layer records a diff from the layer below it: files added, files modified, files deleted. Stacking them produces the final filesystem the container sees. A **union filesystem** presents that stack as one coherent directory tree, where a file in an upper layer hides a file with the same path in a lower one.

```
  ┌─────────────────────────────────┐
  │ Layer 4:  COPY app.jar /app/     │  ← our application
  ├─────────────────────────────────┤
  │ Layer 3:  RUN apt-get install…   │  ← extra packages
  ├─────────────────────────────────┤
  │ Layer 2:  the JDK                │  ← from the base image
  ├─────────────────────────────────┤
  │ Layer 1:  Debian base filesystem │  ← from the base image
  └─────────────────────────────────┘
              ▲
              │  all read-only
  ┌─────────────────────────────────┐
  │ Container writable layer         │  ← added at runtime, per container
  └─────────────────────────────────┘
```

*Every layer in the image is read-only; starting a container adds a thin writable layer on top that belongs only to that container.*

Two properties follow from this, and both matter constantly.

**Layers are shared.** A layer is identified by a hash of its contents, so identical layers are stored once no matter how many images use them. Pulling five images that all build on `eclipse-temurin:21` downloads that base once. The disk cost of the fifth image is only what's unique to it — which is why `docker images` can list 12 GB of images on a disk with far less than 12 GB used.

**Layers are immutable, which means nothing is ever really deleted.** Removing a file in layer 4 doesn't remove it from layer 2 — it adds a marker that hides it. The bytes are still in the image and can be recovered by anyone who has it. This is exactly why a credentials file copied in and deleted later is still a leaked credential, a point we'll return to when writing Dockerfiles.

---

## Image Names

A full image reference has three parts, and most of them are usually left off:

```
registry / repository : tag
```

- **Registry** — the host storing the image. Omitted, it defaults to Docker Hub.
- **Repository** — the image's name, optionally with a namespace like an organization or username.
- **Tag** — which version within that repository. Omitted, it defaults to `latest`.

So these are all the same image:

```bash
docker pull nginx
docker pull nginx:latest
docker pull docker.io/library/nginx:latest
```

*The short form expands to the full form; `library` is the namespace Docker Hub uses for its official images.*

And these are different registries entirely:

```bash
docker pull ghcr.io/some-org/some-app:2.1     # GitHub Container Registry
docker pull public.ecr.aws/some-org/tool:v3   # Amazon ECR Public
```

*Any reference containing a hostname before the first slash targets that registry instead of Docker Hub.*

### What `latest` Actually Means

`latest` is the tag applied when no tag is specified. That's the whole definition.

It does **not** mean the newest version. It's an ordinary tag with no special behavior — a publisher has to deliberately point it at a release, and many either forget or deliberately don't. It is also **mutable**: the same `latest` tag can point to different images over time, so `docker pull nginx:latest` today and in six months can produce entirely different software.

The consequences are the familiar reproducibility problems. A build that worked last week fails today because a base image moved. Two developers on the same code get different behavior. Production and staging drift apart despite identical configuration.

The habit to build now: **pin a real version tag.**

```bash
docker pull postgres:16.4        # a specific version
docker pull node:22-alpine       # a major version plus a variant
```

*Explicit tags make a pull reproducible; `latest` does not.*

Even a major-version tag like `node:22` is a large improvement, though it still moves as patches land. For genuinely locked builds, images can be referenced by their content hash (`nginx@sha256:...`), which can never change — worth knowing exists, though version tags are the normal practice.

---

## Docker Hub

**Docker Hub** is Docker's public registry and the default when no other is specified. It hosts a very large number of images of very mixed provenance, and telling them apart matters.

**Official Images** are curated in partnership with Docker and the upstream projects. They have no namespace prefix — `postgres`, `nginx`, `python`, `node` — which is a reliable signal, because only Docker can publish a name with no namespace. They follow common conventions, are documented well, get security updates, and are the correct default choice.

**Verified Publisher** images come from a confirmed commercial vendor. Namespaced, with a badge, and appropriate when we specifically want that vendor's build.

**Everything else** is any account that signed up. Some of it is excellent, and none of it is reviewed by anyone. An image named `postgres-optimized` from an unfamiliar account is running arbitrary code with whatever access we give it. Treat it as we'd treat a dependency from an unknown source.

The practical rule: prefer an official image; if one doesn't exist, prefer the project's own published image from its documentation; only then consider community images, and read the Dockerfile first.

---

## Working with Images Locally

```bash
docker pull nginx:1.27
```

*Downloads the image and its layers to the local daemon. Layers already present from other images are skipped, which is why a second pull of a related image is often much faster.*

```bash
docker images
```

*Lists local images with repository, tag, image ID, age, and size. The sizes shown are per-image and count shared layers in each — they do not sum to actual disk usage.*

```bash
docker rmi nginx:1.27
```

*Removes an image. It fails if any container — including a stopped one — still references it, in which case remove the container first.*

```bash
docker image prune
```

*Deletes dangling images: layers left behind by rebuilds that no longer have a tag pointing at them. These accumulate quickly and are safe to remove.*

```bash
docker system df
```

*Reports actual disk usage across images, containers, volumes, and build cache, including how much is reclaimable. This is the honest answer to "why is my disk full."*

---

## Choosing a Base Image

Most images start `FROM` something else, and that choice determines size, available tools, and security surface. Official images usually publish several variants of the same version:

| Variant | Base | Approximate size | Notes |
|---|---|---|---|
| `node:22` | Debian, full | ~1.1 GB | Complete toolchain, compilers, shell utilities |
| `node:22-slim` | Debian, trimmed | ~200 MB | Same OS and libc, docs and extras removed |
| `node:22-alpine` | Alpine Linux | ~130 MB | Much smaller, different underlying libc |

*Sizes are illustrative and shift between releases — the ordering is the stable part.*

**Alpine** deserves a specific warning, because it's popular and it has a real catch. Alpine uses **musl** libc rather than the **glibc** that Debian and Ubuntu use. Most software doesn't care. Some does — precompiled native binaries and libraries built against glibc may fail in ways that are hard to diagnose, and certain runtimes have historically shown performance differences under musl.

Alpine is also minimal in ways that surprise people. There's no `bash` (it uses BusyBox `sh`), the package manager is `apk` rather than `apt`, and common tools like `curl` may simply not be installed. That becomes very concrete the first time we open a shell inside an Alpine container, which is exactly what happens in the next lesson.

A reasonable default: **start with `-slim`.** It captures most of the size reduction without changing libc, and it's a much smaller step from a familiar Debian environment. Move to Alpine when image size genuinely matters and after confirming the application runs correctly on it.

---

## Key Takeaways

- An image is an ordered stack of read-only layers plus configuration; each layer is a filesystem diff.
- Identical layers are stored once and shared across images, so listed image sizes overstate real disk usage.
- Layers are immutable — a file deleted in a later layer is hidden, not removed, and remains recoverable.
- A full reference is `registry/repository:tag`; the defaults are Docker Hub and `latest`.
- `latest` means "the default tag," not "the newest version," and it can change under us — pin explicit versions.
- Official images have no namespace prefix; anything else is either a verified vendor or an unreviewed account.
- `docker system df` reports real disk usage; `docker image prune` clears dangling layers.
- Prefer `-slim` as a default base variant; Alpine is smaller but swaps glibc for musl and omits `bash`, `apt`, and often `curl`.
