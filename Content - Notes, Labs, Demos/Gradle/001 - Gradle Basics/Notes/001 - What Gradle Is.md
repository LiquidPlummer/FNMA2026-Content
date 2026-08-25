# What Gradle Is

**Gradle** is a build tool: it compiles source, runs tests, packages artifacts (JARs, distributions), and — the part that dominates daily life — **resolves dependencies**, downloading the libraries a project declares and wiring them onto the right classpaths. One command (`gradle build`) runs the whole chain, the same way locally and in CI. If the JDK is the compiler and runtime, Gradle is the thing that orchestrates them plus everything around them.

---

## Gradle and Maven, in One Minute

Both tools do the same fundamental job, share the same dependency ecosystem (Gradle consumes Maven-format repositories, including Maven Central and internal mirrors of it), and follow the same source layout conventions (`src/main/java`, `src/test/java`). The visible difference is the build definition:

- **Maven** — declarative XML (`pom.xml`): fixed lifecycle, configuration by convention, verbose but rigidly predictable.
- **Gradle** — a build *script* (`build.gradle`, written in a **Groovy** or **Kotlin** DSL): the same conventions by default, but the build file is code — terser to read, and extensible without plugins when something custom is needed.

For orientation, that's enough: coming from a Maven shop, everything conceptual transfers — coordinates (`group:artifact:version`), scopes, repositories — only the syntax and some naming change. We won't go deeper into the DSL theory than the next lessons strictly need.

---

## One Housekeeping Note: Local Gradle, Not the Wrapper

Throughout this lesson we run the **locally installed** `gradle` command — check it now:

```console
$ gradle --version

Gradle 8.14
Launcher JVM:  21.0.4 (Eclipse Adoptium)
```

*Confirming the local installation — any Gradle 8.x with a JDK-based launcher JVM is what we expect here.*

Real projects usually commit and use the **Gradle wrapper** (`./gradlew`) instead — we'll see it get generated in the next lesson and note why it exists — but for this training environment, plain `gradle` keeps the moving parts down. Where the two differ matters exactly once (version pinning), and we'll flag it there.
