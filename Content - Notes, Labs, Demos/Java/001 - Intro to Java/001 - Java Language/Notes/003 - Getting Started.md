# Getting Started

With the theory in place, it's time to set up a working Java environment: install a JDK, configure the environment variables tooling expects, verify everything works, and get an IDE ready. Along the way we'll cover two pieces of foundational mechanics — packages/the classpath and the `main` method — that every Java program depends on from day one.

---

## Installing the JDK

We install a **JDK** (not a standalone JRE — see the platform notes). Two decisions to make first:

**Which version?** Use an **LTS (Long-Term Support)** release unless told otherwise — Java 21 is the current safe default, with Java 17 still common in enterprises. Check which version the project or team has standardized on before installing.

**Which distribution?** All mainstream distributions are builds of OpenJDK and behave identically for our purposes. **Eclipse Temurin** (from the Adoptium project) is a good free default; **Amazon Corretto**, **Microsoft Build of OpenJDK**, and **Oracle JDK** are equally fine. Again, match the team standard if one exists.

Installation options, from most to least common:

- **Installer download.** Grab the installer for your OS from the vendor's site (e.g., [adoptium.net](https://adoptium.net) for Temurin) and run it. On Windows, check the installer options to **set JAVA_HOME** and **add to PATH** — Temurin's installer offers both, and selecting them saves the manual setup below.
- **Package manager.** `winget install EclipseAdoptium.Temurin.21.JDK` on Windows, `brew install temurin@21` on macOS, or the distro package on Linux.
- **Through the IDE.** IntelliJ IDEA can download and manage JDKs itself (File → Project Structure → SDKs → Add SDK → Download JDK). Convenient, but the JDK it installs isn't automatically on the command-line PATH, so we still want a system install for terminal work.

---

## JAVA_HOME and Environment Variables

Two environment variables matter, and they do different jobs:

**`PATH`** is what lets us type `java` or `javac` in a terminal and have the OS find the executable. The JDK's `bin` directory must be on the PATH.

**`JAVA_HOME`** points at the JDK's *root* installation directory (the folder containing `bin`, not `bin` itself). The `java` command doesn't need it — but the tooling ecosystem does. Build tools (Maven, Gradle), application servers, and many scripts locate the JDK via `JAVA_HOME`. Setting it correctly now prevents a classic category of "build works in the IDE but fails in the terminal" confusion later.

On **Windows**, set both via *System Properties → Environment Variables*, or in PowerShell (run as administrator, then open a new terminal):

```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-21.0.5-hotspot", "Machine")
```

Then ensure `%JAVA_HOME%\bin` appears in the PATH variable.

On **macOS/Linux**, add to the shell profile (`~/.zshrc` or `~/.bashrc`):

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

The one gotcha worth flagging: **multiple JDKs accumulate on real machines**, and PATH and JAVA_HOME can silently point at different versions. When a build behaves strangely, checking that both agree is the first diagnostic step.

---

## Verifying the Install

Open a **new** terminal (environment variable changes don't apply to terminals already open) and check both tools:

```bash
java -version
```
```text
openjdk version "21.0.5" 2024-10-15 LTS
OpenJDK Runtime Environment Temurin-21.0.5+11 (build 21.0.5+11-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.5+11 (build 21.0.5+11-LTS, mixed mode)
```

```bash
javac -version
```
```text
javac 21.0.5
```

Both should print, and both should report the **same version** — `java` working while `javac` fails means a JRE-only install or a PATH problem, and mismatched versions mean PATH is picking up two different installations. To confirm `JAVA_HOME`:

```powershell
echo $env:JAVA_HOME     # PowerShell
```
```bash
echo $JAVA_HOME         # macOS/Linux
```

---

## The `main` Method and Program Entry Point

Every Java *application* starts in exactly one place: a method with this precise signature, inside some class:

```java
public class App {
    public static void main(String[] args) {
        System.out.println("Hello, Java!");
    }
}
```

When we run `java App`, the JVM loads the `App` class, finds its `main` method, and calls it. When `main` returns, the program ends. Each part of the signature is required, and each is there for a reason:

- **`public`** — the JVM calls the method from outside the class, so it must be visible.
- **`static`** — the JVM calls it on the class itself, before any objects exist; no instance is created for us.
- **`void`** — it returns nothing. (To signal failure to the OS, use `System.exit(1)`.)
- **`String[] args`** — the command-line arguments. `java App alpha beta` puts `"alpha"` in `args[0]` and `"beta"` in `args[1]`.

Get any of these wrong — say, forget `static` — and the code *compiles* fine but fails at launch with `Error: Main method not found`. A program can contain many classes with `main` methods (handy for quick experiments); the one we name on the command line is the one that runs.

The full compile-and-run cycle, by hand:

```bash
javac App.java     # compile: produces App.class
java App           # run: note — class name, no .class extension
java App.java      # shortcut (Java 11+): compile and run a single file in one step
```

One file-level rule to know: a `public` class must live in a file named after it — `public class App` must be in `App.java`. The compiler enforces this.

---

## Packages and the Classpath

Real programs aren't single classes in a single folder, which brings in two pieces of machinery: packages (how classes are named and organized) and the classpath (how the JVM finds them).

### Packages

A **package** is a namespace for classes, declared at the top of the file, with the convention of a reverse-domain prefix to guarantee global uniqueness:

```java
package com.example.billing;

public class Invoice {
    // ...
}
```

This class's real, full name is now `com.example.billing.Invoice` — the **fully qualified name**. Packages prevent name collisions (our `Invoice` and a library's `Invoice` can coexist) and group related classes together. To use a class from another package, we **import** it by its fully qualified name:

```java
package com.example.app;

import com.example.billing.Invoice;   // now plain 'Invoice' refers to it
import java.util.List;                // standard library classes need imports too

public class Main {
    public static void main(String[] args) {
        Invoice inv = new Invoice();
        // ...
    }
}
```

(Only `java.lang` — `String`, `System`, `Math` — is imported automatically.)

The critical mechanical rule: **the directory structure must mirror the package name.** `com.example.billing.Invoice` must live at `com/example/billing/Invoice.java` under the source root. Get this wrong and the compiler refuses with errors about the declared package not matching. IDEs maintain this correspondence automatically, which is one of the main reasons we use them.

### The Classpath

The **classpath** is the list of locations — directories and JAR files — where the JVM and compiler search for classes. When our code references `com.example.billing.Invoice`, the JVM walks the classpath entries looking for `com/example/billing/Invoice.class`.

By default the classpath is just the current directory. We extend it with `-cp` (or `--class-path`):

```bash
# compiled classes are under ./out, and we use a library JAR in ./lib
java -cp "out;lib/gson-2.11.0.jar" com.example.app.Main      # Windows: ; separator
java -cp "out:lib/gson-2.11.0.jar" com.example.app.Main      # macOS/Linux: :
```

Note that we run the class by its **fully qualified name** from the classpath root — `com.example.app.Main`, not a file path.

The classpath is behind the most famous error messages in Java. The distinction is worth memorizing:

- **`error: cannot find symbol`** (at compile time) — the compiler can't find a class: missing import or missing classpath entry.
- **`ClassNotFoundException` / `NoClassDefFoundError`** (at run time) — the code compiled, but the JVM can't find a class now: the runtime classpath is missing something the compile-time classpath had.

In day-to-day work, build tools like Maven and Gradle assemble the classpath for us from declared dependencies, and we'll rarely type `-cp` by hand. But every "class not found" investigation comes down to one question — *what was on the classpath?* — so the concept stays relevant long after the flag stops being typed manually.

---

## Setting Up an IDE

Java is far more pleasant with a real IDE: error checking as we type, autocomplete driven by the type system, automated refactoring, an integrated debugger, and automatic management of the package/directory rules above. Two good options:

### IntelliJ IDEA

The de facto standard for professional Java development. The free **Community Edition** covers everything this course needs (the paid Ultimate Edition adds framework and web-stack tooling we won't miss yet).

1. Download from [jetbrains.com/idea](https://www.jetbrains.com/idea/download/) — select Community Edition — and install.
2. *New Project* → choose **Java**, and point the **JDK** dropdown at our installed JDK (or use *Download JDK* from that menu).
3. Create a class in the `src` folder, type `main` and hit Tab (a live template expands the whole method), and run with the green ▶ icon in the gutter.

### VS Code with the Java Extension Pack

A lighter-weight alternative, sensible if VS Code is already the daily editor.

1. Install [VS Code](https://code.visualstudio.com/), then from the Extensions view install **"Extension Pack for Java"** (published by Microsoft). It bundles language support, debugger, test runner, and project manager.
2. The extension auto-detects the JDK via `JAVA_HOME` (another payoff for setting it). It can be pointed elsewhere via the `java.jdt.ls.java.home` setting if needed.
3. Open a folder, create a `.java` file, and use the *Run | Debug* links that appear above `main`.

Either tool works for this curriculum. If undecided, choose **IntelliJ IDEA Community** — it's the most common in enterprise Java teams, and the muscle memory transfers directly to the workplace. Whichever we pick, it's worth running one program from the plain terminal first (`javac` then `java`, as above) so the IDE never feels like magic: it's running the same two commands on our behalf.

---

## Key Takeaways

- Install an LTS JDK (Java 21 by default) from any mainstream distribution — Eclipse Temurin is a solid free choice; match the team's standard when one exists.
- `PATH` makes `java`/`javac` work in the terminal; `JAVA_HOME` points build tools at the JDK root. Set both, and keep them pointing at the same install.
- Verify with `java -version` and `javac -version` — both must work and match.
- Execution starts at `public static void main(String[] args)` in whatever class we tell the JVM to run.
- Packages namespace classes (directory structure must mirror the package name), and the classpath is where the JVM searches for them — virtually every "class not found" error is a classpath question.
- Use IntelliJ IDEA Community or VS Code with the Java Extension Pack; both automate the mechanics, but run the compiler by hand once so we know what they're doing.
