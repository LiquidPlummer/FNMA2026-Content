# Building & Tagging Images

We built an image at the end of the last lesson to prove the Dockerfile worked. Now we look at the build itself: what the command's arguments mean, how to read the output, how tags identify what we produced, how to publish it, and how to stop shipping a build toolchain we don't need.

---

## The Build Command

```bash
docker build -t my-app:1.0 .
```

*Builds an image and tags it `my-app:1.0`, using the current directory as the build context.*

Three parts, and the last one is the one people misread.

**`docker build`** tells the daemon to execute a Dockerfile.

**`-t my-app:1.0`** tags the result. Without it, the image is built but left untagged, showing up in `docker images` as `<none>` and referable only by ID. Almost always worth providing.

**`.`** is the **build context** — not "the Dockerfile," and not "build here." It's the directory packaged up and sent to the daemon, and it defines what `COPY` can reach. This is why `COPY ../shared/config.json` fails: `../shared` is outside the context and simply wasn't sent.

To use a Dockerfile with a different name or location, point at it explicitly while keeping the context separate:

```bash
docker build -f docker/Dockerfile.prod -t my-app:1.0 .
```

*`-f` selects the Dockerfile; the trailing `.` is still the context. The two are independent, which is what allows several Dockerfiles to build from the same project root.*

---

## Reading the Build Output

The build reports each instruction as it goes, and the useful signal is which steps ran versus which were reused.

```
 => [1/6] FROM docker.io/library/node:22-slim              0.0s
 => CACHED [2/6] WORKDIR /app                              0.0s
 => CACHED [3/6] COPY package*.json ./                     0.0s
 => CACHED [4/6] RUN npm ci --omit=dev                     0.0s
 => [5/6] COPY . .                                         0.3s
 => [6/6] ...                                              0.1s
 => exporting to image                                     0.4s
```

*`CACHED` means the layer was reused unchanged. Steps 5 and 6 ran because a source file changed, which invalidated `COPY . .` and everything after it.*

Two patterns are worth recognizing immediately:

**Everything after a certain step stops being cached.** That's the cache invalidation rule from the last lesson. Find the first non-cached step and ask whether its inputs really changed — if `RUN npm ci` is rebuilding on every build, the instruction ordering is wrong.

**A step fails.** The output names the instruction that failed and shows the command's error. The most common causes are a `COPY` for a path excluded by `.dockerignore` or outside the context, and a `RUN` whose command failed inside the container.

For a failing `RUN`, the fastest debugging move is to start a container from the base image and try the command by hand:

```bash
docker run -it --rm node:22-slim sh
```

*Drops into a shell in the same environment the build uses, where the failing command can be run interactively and iterated on.*

### When the Cache Is the Problem

Occasionally we want a rebuild that ignores the cache — a base image updated under a tag we're pinned to, or a `RUN` that fetched something that has since changed.

```bash
docker build --no-cache -t my-app:1.0 .
```

*Rebuilds every layer from scratch. Slow, and appropriate when something cached is stale.*

```bash
docker build --pull -t my-app:1.0 .
```

*Re-pulls the base image before building, picking up a newer image under the same tag.*

---

## Tags

A **tag** is a human-readable pointer to an image. The image itself is identified by a content hash; tags are labels we attach so we don't have to type hashes.

Tags are cheap and mutable. One image can carry several, and a tag can be moved to a different image at any time — which is exactly why `latest` is unreliable, as covered in lesson 003.

```bash
docker tag my-app:1.0 my-app:latest
```

*Adds a second tag to the same image. This creates no new image and consumes no additional space — both names now point at the same layers.*

```bash
docker build -t my-app:1.4.2 -t my-app:1.4 -t my-app:latest .
```

*Applies several tags in one build, which is the usual release pattern: an exact version, a moving minor version, and `latest`.*

Useful conventions, worth adopting before it matters:

- **Version tags** (`1.4.2`) for releases, ideally following whatever versioning the project already uses.
- **Never rely on `latest`** in a deployment. Deploy an exact tag, so what's running is identifiable.
- **Commit-based tags** (`my-app:a3f9c21`) tie an image to the exact source that produced it, which is invaluable when tracking down what shipped.
- **Environment tags** (`my-app:staging`) as *additional* pointers to a version tag, never as the only identity.

---

## Publishing to Docker Hub

An image is only local until pushed. Publishing takes three steps.

**1. Log in.**

```bash
docker login
```

*Prompts for Docker Hub credentials and stores a token for subsequent pushes. An access token from Docker Hub account settings is preferable to a password.*

**2. Name the image for the target repository.**

This is where the naming rules from lesson 003 become mandatory rather than academic. A push target must include the namespace — a Docker Hub username or organization. `my-app:1.0` has no namespace, so Docker would interpret it as an official image, which we cannot publish to.

```bash
docker tag my-app:1.0 myusername/my-app:1.0
```

*Adds a correctly namespaced tag pointing at the same image. Building with the full name from the start avoids this step.*

**3. Push.**

```bash
docker push myusername/my-app:1.0
```

*Uploads the layers. Layers already present in the registry are skipped, so pushing a second version of the same application is usually much faster than the first.*

Note that pushing publishes every layer, including anything unintentionally copied in. This is the moment a leaked credential stops being a local mistake — worth a moment's thought before the first push of any image built from a project directory.

Other registries work identically; the hostname in the image name selects the destination:

```bash
docker tag my-app:1.0 ghcr.io/my-org/my-app:1.0
docker push ghcr.io/my-org/my-app:1.0
```

*Pushes to GitHub Container Registry instead of Docker Hub, after a `docker login ghcr.io`.*

---

## Multi-Stage Builds

Here's a problem the Node example quietly has, and that compiled languages have severely.

Building an application requires a toolchain — compilers, build tools, dev dependencies, test frameworks. *Running* it usually requires none of that. A single-stage Dockerfile ships all of it anyway, because everything the build installed is in a layer.

For a Java service the numbers are stark: a full JDK plus Maven and a downloaded dependency cache is well over 500 MB, and running the result needs a JRE and one jar file.

A **multi-stage build** solves this by using several `FROM` instructions in one Dockerfile. Each `FROM` starts a new stage with a clean filesystem, and a later stage can copy specific files out of an earlier one. Only the last stage becomes the final image; everything else is discarded.

```dockerfile
# ---- Stage 1: build ----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

# ---- Stage 2: runtime ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /build/target/app.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

*The first stage has Maven and a full JDK and produces a jar. The second starts fresh from a JRE-only image and copies in just the jar. Maven, the JDK, and the dependency cache never reach the final image.*

The pieces that make it work:

- **`AS build`** names a stage so later stages can refer to it. Any name works.
- **`COPY --from=build`** copies from that stage's filesystem rather than the build context.
- **The final `FROM`** determines the image we ship. Earlier stages are build scaffolding.

Note that the dependency-before-source ordering from lesson 006 still applies *within* the build stage — `pom.xml` and `mvn dependency:go-offline` come before `COPY src`, so dependency resolution stays cached across source changes.

The same shape works for any compiled or bundled application:

```dockerfile
FROM node:22 AS build
WORKDIR /build
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /build/dist /usr/share/nginx/html
```

*A front-end build: Node compiles the bundle, then the final image is nginx serving the static output. Node itself is not in the shipped image at all.*

Beyond size, this narrows what's in the image at all — fewer packages means fewer things to keep patched, and a compiler that isn't present can't be used by anything that gets in.

---

## Key Takeaways

- The trailing `.` in `docker build` is the build context, not the Dockerfile — it defines what `COPY` can reach.
- `-f` selects a differently named or located Dockerfile, independently of the context.
- `CACHED` in the output means a layer was reused; the first uncached step is where a rebuild's cost begins.
- `--no-cache` forces a full rebuild; `--pull` refreshes the base image under an unchanged tag.
- Tags are cheap, mutable pointers — one image can carry several, and `docker tag` creates no new image.
- Deploy exact version tags, never `latest`; commit-based tags tie an image to its source.
- Pushing requires a namespaced name (`username/image:tag`), a `docker login`, and then `docker push`.
- Pushing publishes every layer — including anything accidentally copied in.
- Multi-stage builds use several `FROM` instructions and `COPY --from=<stage>` to keep build tooling out of the final image.
- Only the last stage ships; earlier stages are discarded entirely.
