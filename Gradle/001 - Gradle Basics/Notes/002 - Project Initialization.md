# Project Initialization

**`gradle init`** scaffolds a new project interactively: it asks a short series of questions and generates a working, runnable build. Run it in an empty directory:

```console
$ mkdir order-tool && cd order-tool
$ gradle init

Select type of build to generate:
  1: Application          ← a runnable program (our choice)
  2: Library
  3: Gradle plugin
  4: Basic (no source)

Select implementation language: Java
Enter target Java version: 21
Project name: order-tool

Select application structure:
  1: Single application project     ← our choice
  2: Application and library project

Select build script DSL:
  1: Kotlin
  2: Groovy                         ← our choice, matching this training

Select test framework: JUnit Jupiter
```

*The `init` dialogue: application type, Java, single project, Groovy DSL, JUnit Jupiter — the answers we use throughout.*

Two of those answers deserve a word. **DSL choice**: Kotlin is the modern default and Groovy the long-standing incumbent — they configure the same Gradle, and we pick **Groovy** because it's what this team's existing builds use. **Test framework**: **JUnit Jupiter** (JUnit 5) is the current standard; the generated project arrives with a passing sample test.

---

## What Init Generates

```
order-tool/
├── settings.gradle          ← project name + which subprojects exist
├── gradlew, gradlew.bat     ← the wrapper scripts (see below)
├── gradle/                  ← wrapper jar + version pin, version catalog
└── app/                     ← the application subproject
    ├── build.gradle         ← THE build file — note where it lives!
    └── src/
        ├── main/java/       ← production code
        └── test/java/       ← tests
```

*The generated layout: a root that defines the project set, and an `app/` subproject holding the build file and all source.*

**`settings.gradle`** names the build and includes the `app` subproject — that's its whole job at this scale. The Maven-familiar source layout (`src/main/java`, `src/test/java`) works identically here.

**The gotcha worth circling: `build.gradle` is in `app/`, not the root.** The `Application` init type generates a *multi-project* shape with one subproject, so the root has no build file at all. The practical consequences: dependency and plugin edits go in `app/build.gradle`, and anyone who reflexively creates or edits a root-level `build.gradle` ends up with two build files and confusing behavior. Commands still run from the **root** directory — Gradle finds the subprojects itself.

---

## About Those `gradlew` Files

Init also drops in the **Gradle wrapper**: `gradlew` (Unix), `gradlew.bat` (Windows), and a small jar-plus-properties under `gradle/wrapper/`. Running `./gradlew build` instead of `gradle build` makes the build use the **exact Gradle version pinned** in `gradle-wrapper.properties` — downloading it on first use if needed. That's the point of it: everyone (and CI) builds with the same Gradle version, and collaborators need no Gradle installed at all — just a JDK.

We won't use the wrapper in this training (local `gradle` throughout, as noted), but leave the files in place: they're small, they belong in version control, and any project shared beyond this room should be built through them.

---

## Proving the Scaffold Works

```console
$ gradle build

BUILD SUCCESSFUL in 4s

$ gradle run

> Task :app:run
Hello World!
```

*The generated project compiles, tests, and runs out of the box — a known-good baseline before we touch anything.*

With a working skeleton in hand, the next step is understanding the file we'll actually be editing: `app/build.gradle`, block by block.
