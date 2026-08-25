# Groovy for Gradle Build Scripts

A `build.gradle` file looks like configuration, but it isn't — it's a program. Every line is **Groovy** code that Gradle executes to configure the build. That's easy to ignore right up until we need to read a script we didn't write, add a repository with credentials, or debug why a block isn't doing what we expect. We don't need to become Groovy developers; we need just enough of the language to see what the script is actually *doing*. That's the goal here: decode the example script from the previous lesson construct by construct, then use that fluency to do something real — point the build at a private repository.

---

## Groovy in a Nutshell

**Groovy** is a dynamic language that runs on the JVM and is deliberately Java-friendly: most Java syntax is valid Groovy, and Groovy code compiles to bytecode that calls Java classes directly (which is why a build script can reference a Java class like `JavaLanguageVersion`). What makes it *look* so different from Java is what it lets us leave out:

- **Semicolons are optional.** A line break ends a statement.
- **Parentheses on method calls are optional** (when there's at least one argument).
- **Types are optional.** Variables can be declared with `def` instead of a type.
- **`return` is optional.** The last expression in a method or closure is its return value.

Gradle's **Groovy DSL** (domain-specific language) is just Groovy with those shortcuts used aggressively, plus a Gradle-provided object model underneath. Once we can mentally translate the shortcuts back into plain method calls, the mystery evaporates.

---

## The Big Idea: Everything Is a Method Call on an Object

When Gradle runs `build.gradle`, the script executes against a **`Project` object** — the in-memory representation of our project. Every top-level statement in the script is one of two things:

1. **A property assignment** on the project: `group = 'com.example'` literally sets the `group` property of the `Project` object.
2. **A method call** on the project: `dependencies { ... }` calls the project's `dependencies()` method.

So this script:

```groovy
group = 'com.example'

repositories {
    mavenCentral()
}
```

is, with the sugar removed, equivalent to:

```groovy
project.setGroup('com.example')

project.repositories({
    mavenCentral()
})
```

Nothing in a build script is special syntax. It's all properties and methods, which means the [Gradle API documentation](https://docs.gradle.org/current/dsl/) for `Project` is effectively the reference manual for what we're allowed to write at the top level.

---

## Strings: Single vs Double Quotes

Groovy has two kinds of string literals, and build scripts use both:

```groovy
def name = 'jackson-databind'           // single quotes: plain string, taken literally
def coord = "com.fasterxml.jackson.core:${name}:2.17.1"   // double quotes: a GString
println "Building $project.name version $version"
```

Single-quoted strings are plain `java.lang.String`. Double-quoted strings are **GStrings**, which support **interpolation**: `${expression}` (or just `$property` for simple cases) is evaluated and spliced in. The convention in build scripts is single quotes unless interpolation is needed — so when we see double quotes in a script, it's a hint that a variable is being substituted somewhere.

---

## Optional Parentheses

Groovy lets us drop the parentheses on a method call with arguments. These pairs are identical:

```groovy
implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.1'
implementation('com.fasterxml.jackson.core:jackson-databind:2.17.1')

id 'java'
id('java')

useJUnitPlatform()        // no arguments — parentheses required here
```

This is the trick behind the DSL's "configuration file" feel: `implementation 'something'` reads like a declaration, but it's a method named `implementation` being called with one string argument. Note the flip side: with *zero* arguments the parentheses are mandatory — `mavenCentral()` and `useJUnitPlatform()` keep theirs.

---

## Closures: The `{ ... }` Blocks

The construct that makes or breaks reading a Gradle script. A **closure** is an anonymous block of code, written in braces, that can be stored and executed later — Groovy's equivalent of a Java lambda:

```groovy
def shout = { String word -> println word.toUpperCase() }
shout('hello')      // HELLO
```

In a build script, every `name { ... }` block is **a method call whose argument is a closure**. Groovy lets a closure argument sit outside the parentheses (and then lets the empty parentheses disappear), so:

```groovy
test {
    useJUnitPlatform()
}
```

is really:

```groovy
test({ useJUnitPlatform() })
```

— a call to the `test` method, passing a closure for Gradle to execute later.

### Delegation: Why Code Inside a Block "Just Works"

Inside that closure, what object does `useJUnitPlatform()` belong to? Not the project — the `Test` task. Every closure has a **delegate**: an object that Groovy tries when a method or property isn't defined in the closure itself. Before Gradle executes a configuration closure, it sets the delegate to the object being configured:

- Inside `test { ... }`, the delegate is the `Test` task — so `useJUnitPlatform()` lands there.
- Inside `dependencies { ... }`, the delegate is a `DependencyHandler` — which is where the `implementation` method actually lives.
- Inside `repositories { ... }`, the delegate is a `RepositoryHandler` — home of `mavenCentral()`.

This is the entire mechanism of the DSL: **each named block hands us a different object, and everything written inside the braces is method calls and property assignments on that object.** Nesting just repeats the pattern — in `java { toolchain { ... } }`, the outer closure delegates to the Java configuration, and `toolchain { ... }` is a method on *that*, whose closure delegates to the toolchain object. To find out what's legal inside any block, we look up the delegate's type in the Gradle DSL docs.

---

## Maps and Named Arguments

One more literal we'll meet constantly. Groovy writes maps as `[key: value, ...]`, and a method call can take its arguments as key–value pairs:

```groovy
def coords = [group: 'com.fasterxml.jackson.core', name: 'jackson-databind']

// Two spellings of the same dependency:
implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.1'
implementation group: 'com.fasterxml.jackson.core', name: 'jackson-databind', version: '2.17.1'
```

The second form is a single method call receiving a map. Older scripts and some plugins use the map style heavily, so it's worth recognizing on sight. (Lists, for completeness, are `['a', 'b', 'c']`.)

---

## The Example Script, Decoded

With those five ideas — method calls on `Project`, strings, optional parentheses, closures with delegates, and map literals — the entire example script translates into plain statements:

```groovy
plugins {                        // method call: plugins(closure)
    id 'java'                    //   id('java') — apply the Java plugin
    id 'application'             //   id('application') — adds 'run' task + packaging
}

group = 'com.example'            // property assignment on the Project
version = '1.0.0-SNAPSHOT'       // property assignment on the Project

java {                           // configure the JavaPluginExtension (added by 'java' plugin)
    toolchain {                  //   nested call: configure the toolchain spec
        languageVersion = JavaLanguageVersion.of(21)
    }                            //   plain Java static method call — Groovy calls Java freely
}

repositories {                   // repositories(closure); delegate: RepositoryHandler
    mavenCentral()               //   zero-arg method — parentheses required
}

dependencies {                   // dependencies(closure); delegate: DependencyHandler
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.1'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}                                //   each line: a method call with one string argument

application {                    // configure the extension added by the 'application' plugin
    mainClass = 'com.example.App'    // property assignment on that extension
}

test {                           // configure the 'test' task (type: Test)
    useJUnitPlatform()           //   method on the Test task
}
```

Two details worth calling out:

- **`plugins { }` is the one block that really *is* special.** Gradle parses it before running the rest of the script, so only literal `id 'name'` (optionally `version 'x'`) lines are allowed inside — no variables, no logic. Everything else in the file is ordinary Groovy.
- **Order matters for the rest.** The script runs top to bottom like any program. The `java { }` and `application { }` blocks only work because the `plugins` block already applied the plugins that *add* those extensions — configuring an extension before its plugin is applied is an error.

---

## Pointing at a Custom Repository

Now the practical payoff. In a security-first environment, builds typically aren't allowed to pull dependencies from the public internet at all. Instead, the organization runs a **repository manager** — **JFrog Artifactory** and **Sonatype Nexus** are the common ones — that proxies Maven Central, scans and approves artifacts, and hosts internal libraries. Our build should resolve *everything* through it.

Since we can now read the DSL, the `repositories` block holds no surprises: `maven { ... }` is a method on the `RepositoryHandler` whose closure configures one repository.

```groovy
repositories {
    maven {
        name = 'corp-artifactory'                                  // label used in log/error messages
        url = 'https://artifactory.example.com/artifactory/libs-release'
        credentials {
            username = providers.gradleProperty('artifactoryUser').get()
            password = providers.gradleProperty('artifactoryToken').get()
        }
    }
}
```

In a locked-down build, this **replaces** `mavenCentral()` rather than sitting alongside it. The repository manager's "virtual" repository proxies Central for us, so a single internal URL serves both public and internal artifacts — and the build makes no direct external connections. If both are listed, Gradle searches repositories in declaration order and uses the first hit, which makes a stray `mavenCentral()` an easy way to silently bypass the approved path.

### Keeping Credentials Out of the Script

The `build.gradle` file is committed to version control, so credentials must never appear in it as literals. The standard approach is **`gradle.properties`** in the Gradle user home (`~/.gradle/gradle.properties` — per machine, never committed):

```properties
artifactoryUser=kplummer
artifactoryToken=AKCp8...
```

The `providers.gradleProperty(...)` calls in the snippet read those values at build time. On CI, the same properties are supplied as environment variables instead — Gradle automatically maps `ORG_GRADLE_PROJECT_artifactoryUser` to the property `artifactoryUser`, so the identical script works on a laptop and in the pipeline. Gradle also offers a shorthand that does all of this by convention:

```groovy
maven {
    name = 'corpArtifactory'
    url = 'https://artifactory.example.com/artifactory/libs-release'
    credentials(PasswordCredentials)     // looks up corpArtifactoryUsername / corpArtifactoryPassword
}
```

With `credentials(PasswordCredentials)`, Gradle derives the property names from the repository's `name` (`corpArtifactoryUsername`, `corpArtifactoryPassword`) and fails with a clear message if they're missing.

### Enforcing It Project-Wide

One repository block per `build.gradle` invites drift in a multi-module project — and a single module quietly adding `mavenCentral()` defeats the policy. Modern Gradle lets us centralize repository configuration in `settings.gradle` and *forbid* modules from declaring their own:

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        maven {
            name = 'corpArtifactory'
            url = 'https://artifactory.example.com/artifactory/libs-release'
            credentials(PasswordCredentials)
        }
    }
}
```

With `FAIL_ON_PROJECT_REPOS`, any `repositories { }` block in a module's `build.gradle` fails the build outright. In a security-first shop, this is the configuration to look for — and to reach for — first: one approved source of artifacts, declared once, impossible to bypass casually.
