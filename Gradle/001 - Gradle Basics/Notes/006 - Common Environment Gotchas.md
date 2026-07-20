# Common Environment Gotchas

Three failure modes account for most of the setup tickets from our own cohort environments. Each produces an error message pointing somewhere *other* than the actual cause — which is exactly why they're worth cataloguing in advance.

---

## JAVA_HOME Points at a JRE

**Symptom:** builds fail with variations of *"no compiler found"*, *"tools.jar not found"* (older messages), or compilation tasks erroring while `java -version` works fine.

**Cause:** `JAVA_HOME` aimed at a **JRE** (runtime only — can run Java, can't compile it) instead of a **JDK**. Gradle launches on the JVM `JAVA_HOME` names and, by default, compiles with it too; a JRE gets it as far as starting up and no further. The diagnosis is one command:

```console
$ gradle --version

Launcher JVM:  21.0.4 (Eclipse Adoptium)     ← check this line
$ "$JAVA_HOME/bin/javac" -version            # the real test: does javac exist there?
javac 21.0.4
```

*If `javac` isn't present under `JAVA_HOME`, it's a JRE — repoint the variable at the JDK install and reopen the terminal.*

On our Windows images the usual culprit is `JAVA_HOME` set to a bundled JRE path (or a stale path after a JDK upgrade) — fix it in Environment Variables, then confirm with the two commands above in a *fresh* terminal (existing shells keep the old value).

---

## Wrong Plugin — "testImplementation Not Found"

**Symptom:** a build file edit later, the build fails with something like:

```
Could not find method testImplementation() for arguments
[org.junit.jupiter:junit-jupiter:5.10.2] on object of type DependencyHandler.
```

*Groovy-flavored misdirection: it reads like a syntax error, but it's a missing plugin.*

**Cause:** the anatomy lesson's key fact biting for real — **dependency scopes come from plugins**. `testImplementation` exists only once the `java` plugin (or one that implies it) is applied. The classic version from our environments: someone assembling a build file for publishing applies `maven-publish` *alone* — that plugin adds publishing tasks, not the Java scopes — and every dependency line then "doesn't exist." The fix is the plugins block, not the dependencies block:

```groovy
plugins {
    id 'java'              // ← this is what provides implementation/testImplementation
    id 'maven-publish'     // publishing on top, not instead
}
```

*Rule: when a scope or task "method" can't be found, audit `plugins { }` first — the vocabulary of a build file is installed by its plugins.*

---

## Deprecation Warnings and the Gradle 9 Horizon

**Symptom:** builds succeed but print:

```
Deprecated Gradle features were used in this build,
making it incompatible with Gradle 9.0.
```

**What it means:** something — usually a *plugin*, sometimes our own build script — calls APIs scheduled for removal in the next major version. The build is fine *today*; it's the future upgrade being mortgaged. Where to look:

```console
$ gradle build --warning-mode all
```

*`--warning-mode all` expands the summary into individual warnings, each naming what was used, what replaces it, and a docs link.*

Each warning states its source; warnings tracing to our own `build.gradle` we fix now (the message names the replacement), warnings from third-party plugins mean checking for a newer plugin version. Triage habit, not emergency: keep the count moving toward zero as part of normal maintenance, and never *start* a new project with warnings already printing. (The full HTML report at `build/reports/` — linked at the end of the console output — gathers everything in one place.)

---

The common thread: Gradle's errors are precise about *what* failed and frequently silent about *why* — the three mappings above (JRE → repoint JAVA_HOME, missing method → missing plugin, deprecation → `--warning-mode all`) turn each cryptic message back into a two-minute fix. With the environment squared away, the last lesson is the daily driver commands.
