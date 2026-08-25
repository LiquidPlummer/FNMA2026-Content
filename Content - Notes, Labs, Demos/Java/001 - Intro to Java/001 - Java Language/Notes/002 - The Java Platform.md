# The Java Platform

Java is more than a language — it's a platform. The source code we write is only the visible top layer; underneath sits a runtime architecture that explains how Java delivers its signature promise of portability, why we "compile" Java but don't get a native executable, and why three similar-sounding acronyms (JDK, JRE, JVM) keep showing up. Untangling those is the goal of these notes.

---

## The JVM, JRE, and JDK

The three acronyms nest inside each other like Russian dolls. From the inside out:

**JVM — Java Virtual Machine.** The program that actually *runs* Java applications. It doesn't understand Java source code at all; it executes **bytecode**, a compact intermediate instruction set (more on this below). The JVM is the layer that talks to the real operating system and hardware — which means there's a different JVM implementation for Windows, macOS, and Linux, each presenting the same environment to the code it runs. The JVM also houses the garbage collector and the runtime optimizer.

**JRE — Java Runtime Environment.** The JVM plus the standard class libraries — everything needed to *run* a Java application, but nothing for building one. Historically, end users installed a standalone JRE to run Java programs. Since Java 11, Oracle no longer ships the JRE as a separate download; the term survives, but in practice we just install the JDK.

**JDK — Java Development Kit.** Everything in the JRE plus the development tools: the compiler (`javac`), the application launcher (`java`), the packager (`jar`), documentation generator (`javadoc`), debugger, and more. As developers, the JDK is what we install.

```
┌────────────────────────── JDK ──────────────────────────┐
│  javac (compiler)   jar   javadoc   debugger  ...        │
│  ┌──────────────────── JRE ───────────────────────────┐  │
│  │  Standard class libraries (java.util, java.io, …)  │  │
│  │  ┌───────────────── JVM ────────────────────────┐  │  │
│  │  │  Bytecode execution · JIT compiler · GC      │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  └─────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

The one-line summary: **the JDK is for developing, the JRE is for running, and the JVM is the machine that does the running.**

A note on flavors: "the JDK" is a specification with multiple vendors. **OpenJDK** is the open-source reference implementation, and most distributions — Eclipse Temurin, Amazon Corretto, Oracle JDK, Microsoft Build of OpenJDK — are builds of it. They're functionally interchangeable for our purposes; organizations pick one based on support and licensing.

---

## The Execution Model: Compile → Bytecode → JVM

Most languages take one of two routes. Compiled languages like C translate source code directly into native machine code for one specific platform — fast, but the binary only runs where it was built. Interpreted languages like Python read and execute source code on the fly — portable, but slower. Java deliberately takes a middle path with two distinct steps:

**Step 1 — Compile.** The `javac` compiler translates our `.java` source files into `.class` files containing **bytecode**: instructions for the JVM rather than for any real CPU. Bytecode is the same on every operating system.

**Step 2 — Execute.** The `java` command starts a JVM, which loads the `.class` files and executes the bytecode. The JVM translates bytecode into actual machine instructions for whatever hardware it's running on.

```
Greeter.java  ──(javac)──►  Greeter.class  ──(java)──►  Program runs
 (source code)               (bytecode —                 (JVM translates
                              platform-neutral)           bytecode to native
                                                          instructions)
```

Concretely:

```bash
javac Greeter.java     # produces Greeter.class (bytecode)
java Greeter           # starts a JVM and runs it
```

The JVM doesn't merely interpret bytecode line by line. Modern JVMs use **JIT (just-in-time) compilation**: they begin by interpreting, watch which methods run hottest, and compile those to optimized native machine code *while the program runs* — using live profiling data to optimize. This is why long-running Java services achieve performance in the same league as natively compiled languages, and also why Java programs "warm up": peak speed arrives after the JIT has done its work.

---

## Write Once, Run Anywhere

The two-step model is what makes Java's original slogan work. Because bytecode targets the *virtual* machine rather than any physical one, a compiled `.class` (or `.jar`) file is platform-neutral. Compile on a Windows laptop, and the identical artifact runs on a Linux server or a Mac — no recompilation, no platform-specific builds:

```
                          ┌──► JVM for Windows ──► runs
Greeter.class  (one file) ┼──► JVM for Linux   ──► runs
                          └──► JVM for macOS   ──► runs
```

The trick is where the platform-specific work moved: instead of compiling the *application* per platform, the *JVM itself* is ported per platform. Each JVM presents the same bytecode contract upward and handles the OS differences downward. We write to the contract; the JVM vendors handle the rest.

This matters daily in enterprise work. The standard deliverable for a Java application is a **JAR file** (Java ARchive — essentially a zip of `.class` files plus metadata), and the same JAR moves unchanged from a developer's machine through CI, testing, and production, regardless of what operating systems those environments run. The only requirement on each machine is a JVM of a compatible version — bytecode compiled for Java 21 needs a Java 21 (or newer) JVM to run.

One honest caveat: "anywhere" assumes we stick to the platform's abstractions. Code that hardcodes Windows-style file paths or shells out to OS-specific commands breaks the promise — the platform gives us portability, but we can still opt out of it by accident.

---

## The Standard Library

The third pillar of the platform is the **standard library** (historically called the **Java Class Library**): thousands of ready-made classes that ship with every JDK. The "write once, run anywhere" promise would be hollow if file access or networking required platform-specific code — so the standard library wraps all of it in portable APIs.

The library is organized into **packages**, with names that double as a map of what's available:

| Package | What it provides |
|---|---|
| `java.lang` | Core types — `String`, `Math`, `System`, wrapper classes. Imported automatically. |
| `java.util` | Collections (`List`, `Map`, `Set`), plus utilities like `Scanner` and `Optional`. |
| `java.io` / `java.nio` | File access and input/output streams. |
| `java.net` / `java.net.http` | Networking, including a modern HTTP client. |
| `java.time` | Dates, times, durations, time zones. |
| `java.sql` | Database access via JDBC. |
| `java.util.concurrent` | Threading and concurrency utilities. |

A taste of what "batteries included" looks like in practice:

```java
import java.time.LocalDate;
import java.util.List;

public class Demo {
    public static void main(String[] args) {
        List<String> names = List.of("Alice", "Bob", "Carol");
        System.out.println(names.size());            // 3 — collections built in

        LocalDate due = LocalDate.now().plusDays(30); // date math built in
        System.out.println(due);
    }
}
```

Nothing here required installing anything beyond the JDK. The practical habit to build: **before writing a utility or reaching for a third-party dependency, check whether the standard library already does it.** It very often does, and the standard library is the one dependency that's guaranteed present, maintained, and portable everywhere our code runs. (The official documentation — the **Javadoc API reference** at [docs.oracle.com](https://docs.oracle.com/en/java/javase/21/docs/api/) — is the catalog, and it's worth bookmarking now.)

---

## Key Takeaways

- The JDK (development tools) contains the JRE (runtime libraries), which contains the JVM (the engine that executes bytecode). We install the JDK.
- Java uses a two-step execution model: `javac` compiles source to platform-neutral bytecode, and a platform-specific JVM executes it, with JIT compilation providing native-level speed for hot code.
- "Write once, run anywhere" works because the same bytecode runs on any JVM — the per-platform work lives in the JVM, not in our application.
- The standard library ships portable APIs for collections, I/O, networking, dates, and much more — check it before writing it yourself.
