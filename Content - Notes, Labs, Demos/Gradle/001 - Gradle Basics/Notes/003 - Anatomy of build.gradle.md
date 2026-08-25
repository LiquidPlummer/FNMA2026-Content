# Anatomy of build.gradle

The generated `app/build.gradle`, trimmed to what matters, is three blocks — and three blocks is genuinely most of what a working Java build needs:

```groovy
plugins {
    id 'java'                    // compile/test/jar tasks, source sets, dependency scopes
    id 'application'             // adds the 'run' task + mainClass config
}

repositories {
    mavenCentral()               // where to LOOK for dependencies (we replace this next lesson)
}

dependencies {
    implementation 'com.google.guava:guava:33.0.0-jre'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

application {
    mainClass = 'org.example.App'
}
```

*The whole build definition: what capabilities the project has, where libraries come from, and which libraries it uses.*

---

## plugins {} — What the Build Can Do

A bare Gradle project can do almost nothing; **plugins** install capabilities. **`java`** brings the entire Java toolchain: `compileJava`, `test`, `jar` tasks, the `src/main`/`src/test` source-set conventions, and — important below — the dependency scopes like `implementation` and `testImplementation`. **`application`** layers on "this is a runnable program": the `gradle run` task and the `application { mainClass = ... }` config.

The dependency worth internalizing: **the scopes and conventions come *from* the plugins.** A build file missing the `java` plugin has no `testImplementation` — Gradle will fail with "method not found"-style errors that look bizarre until this connection is made (we'll meet that exact failure in the Gotchas lesson).

## repositories {} — Where Dependencies Come From

Declares the servers Gradle searches when resolving dependencies, in order. `mavenCentral()` is the public default that `init` generates; in our environment it gets replaced with the internal repository — that swap is the whole next lesson.

## dependencies {} — What the Project Uses

Each line is a coordinate — `group:artifact:version`, same scheme as Maven — assigned to a **scope** that controls which classpaths it lands on:

- **`implementation`** — production code needs it, at compile time and runtime. The everyday scope.
- **`testImplementation`** — tests need it; production code never sees it. JUnit belongs here, which keeps test libraries out of the shipped application.
- (Later, as needed: `runtimeOnly` — needed at runtime but not compilation, the classic JDBC-driver scope; `compileOnly` — the reverse.)

*Rule of thumb: everything is `implementation` unless it's test-only — then `testImplementation`.*

---

## Just Enough Groovy

The file looks like a config format; it's actually Groovy code, and two facts about it prevent 90% of the confusion. We deliberately stop at these two.

**1. Every `name { ... }` block is a method call taking a closure.** `plugins { ... }`, `dependencies { ... }` — each is a method handed a block of code to run, and *inside* the block, lines like `implementation 'com.google.guava:guava:33.0.0-jre'` are also method calls, with Groovy allowing the parentheses to be dropped: it means `implementation('com.google...')`. That's why the "keys" in these blocks aren't arbitrary — they're method names that must exist (provided by plugins — see above).

**2. `=` is assignment; no `=` is a method call.** `mainClass = 'org.example.App'` *sets a property*. `id 'java'` *calls a method* with one argument. Reading any build.gradle line means asking which of the two it is:

```groovy
version = '1.0.0'                          // assignment: setting the project's version property
implementation 'org.slf4j:slf4j-api:2.0.13'  // method call: implementation(...)
```

*The two shapes of every line in the file — property assignment vs. parenthesis-free method call.*

That's the full Groovy budget for this training. It's enough to read the file, edit it confidently, and understand the error messages — deeper DSL mechanics (delegates, custom tasks) can wait until a build actually demands them.
