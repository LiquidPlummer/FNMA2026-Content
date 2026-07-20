# Pointing at Our Internal Repository

In this environment, builds have **no access to public repositories** — `mavenCentral()` is unreachable by policy. All dependencies come through the organization's internal repository manager (an Artifactory/Nexus-style server), which proxies and vets the public artifacts and hosts internal ones. So the one mandatory edit to every generated build file: replace the `repositories` block.

```groovy
repositories {
    maven {
        name = 'internal'                                  // label used in logs and errors
        url = 'https://repo.example-corp.internal/maven'   // ← our actual URL from the team wiki
        // credentials, if the repo requires them, come from gradle.properties —
        // never hard-coded here (this file is committed)
    }
}
```

*The swap: `mavenCentral()` out, one `maven { ... }` entry pointing at the internal server in.*

Reading it with last lesson's Groovy eyes: `maven { ... }` is a method call configuring one repository; `name` and `url` are property assignments. Nothing else in the build changes — the internal server speaks the same Maven repository format, so the same coordinates in `dependencies { }` resolve identically; they're just served from inside the network.

Two practical notes, then we move on:

- **Don't leave `mavenCentral()` in "as a fallback."** Gradle searches repositories in declaration order and uses the first hit — a stray public entry either silently bypasses the vetted path or (here) hangs and fails when the network blocks it. One repository, the internal one.
- **Credentials never go in the build file.** If the repo requires them, they live in `~/.gradle/gradle.properties` (per machine, uncommitted) and are referenced from the build — the setup guide on the team wiki covers the exact property names.

That's deliberately the whole story for now — enough to make every build in this course resolve. The deeper machinery (credential providers, `settings.gradle`-level repository enforcement so no subproject can sneak a repo in, publishing *to* the repo) is documented on the team wiki and worth a read after the training. Next: proving the configuration actually works, and that it's actually being used.
