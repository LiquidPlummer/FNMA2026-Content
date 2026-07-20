# Running the Basics

Four commands cover the daily Gradle workflow. All of them run from the project **root** (Gradle locates the subprojects itself — recall the `app/` layout), and all print `BUILD SUCCESSFUL` or a task-by-task account of what failed.

---

## gradle build — The Full Check

```console
$ gradle build

> Task :app:compileJava
> Task :app:test
> Task :app:jar
BUILD SUCCESSFUL in 6s
```

*`build` runs the whole chain: compile production code, compile and run tests, package the JAR.*

`build` is the "is everything OK?" command — the same thing CI runs. Two behaviors to know. **Failing tests fail the build** — that's a feature; the test report with details lands at `app/build/reports/tests/test/index.html`. **Gradle is incremental** — an immediate second `build` finishes in under a second with tasks marked `UP-TO-DATE`, because nothing changed; only edited files trigger real work. (Outputs land in `app/build/`; `gradle clean` deletes that directory for the rare case a truly fresh build is wanted.)

## gradle test — Just the Tests

```console
$ gradle test
```

*Compiles what's needed and runs the test suite — the tight loop while developing.*

Faster than `build` when tests are the question being asked. Useful variations: `gradle test --tests InvoiceServiceTest` filters to one class (patterns like `--tests '*Repository*'` work too), and `gradle test -i` shows individual test output when diagnosing a failure.

## gradle run — Launch the Application

```console
$ gradle run

> Task :app:run
Hello World!
```

*Runs the class configured as `mainClass` — courtesy of the `application` plugin.*

This task exists *only* because `application` is in the plugins block (the recurring theme). Program arguments pass through `--args`: `gradle run --args="--verbose input.csv"`.

## gradle dependencies — Inspect the Resolved Graph

```console
$ gradle app:dependencies --configuration runtimeClasspath

runtimeClasspath - Runtime classpath of source set 'main'.
\--- com.google.guava:guava:33.0.0-jre
     +--- com.google.guava:failureaccess:1.0.2
     \--- org.checkerframework:checker-qual:3.42.0
```

*The resolved dependency tree: every declared library plus everything each one dragged in (transitive dependencies), with final chosen versions.*

The diagnostic command of the four. Each declared dependency expands into its **transitive** dependencies, and the tree shows what actually landed on the classpath — including Gradle's conflict resolution (a version marked `1.2 -> 1.4` means something requested 1.2 but 1.4 won). This answers the recurring questions: *why is this jar on my classpath?* (find it in the tree, walk up), *which version of X am I really getting?*, and *what did adding this library actually cost?* Note the `app:` prefix — this one is per-subproject — and the `--configuration` flag to pick a classpath (`runtimeClasspath`, `testRuntimeClasspath`); bare `gradle app:dependencies` prints all of them, which is a lot.

---

## The Working Set, Summarized

| Question | Command |
|---|---|
| Is everything OK? (what CI runs) | `gradle build` |
| Do the tests pass? | `gradle test` |
| Run the program | `gradle run` |
| What's actually on the classpath? | `gradle app:dependencies` |
| Is my repo config really being used? | `gradle build --refresh-dependencies --info` (lesson 5) |
| What do these deprecations mean? | `gradle build --warning-mode all` (lesson 6) |

*The whole course on one card — the four dailies plus the two diagnostics from earlier lessons.*

That's basic working fluency: initialize a project, read and edit its build file, point it at the internal repository, verify the configuration honestly, recognize the environment traps, and drive the everyday tasks. Everything beyond — multi-project builds, custom tasks, publishing, the wrapper in team workflows — builds on exactly these foundations, and the team wiki picks up where this leaves off.
