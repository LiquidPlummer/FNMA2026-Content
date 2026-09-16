# How Python Runs

Coming from Java and TypeScript, we carry a mental model where code passes through a build step before it runs: `javac` turns `.java` into `.class`, and `tsc` turns `.ts` into `.js`. Python is usually described as "interpreted," as if no such step exists. It does exist — it's folded into the act of running the program. Understanding *when* that compilation happens, and how little it checks, explains most of what feels loose about Python compared to the languages we already know.

---

## Source → Bytecode → Virtual Machine

When we run `python app.py`, the standard interpreter does three things in sequence:

1. **Parses** the source file and confirms it is syntactically valid Python.
2. **Compiles** it to **bytecode** — a compact, low-level instruction set for a virtual machine, conceptually the same as JVM bytecode.
3. **Executes** that bytecode in the **CPython virtual machine**, an interpreter loop that runs the instructions one at a time.

Structurally, that's Java's pipeline: source, bytecode, VM. The differences are in who triggers each step, when it happens, and what gets checked along the way.

We can look at the bytecode ourselves with the standard library's `dis` module:

```python
import dis

dis.dis("total = price * 2")
```

*Disassembles a one-line statement into the bytecode instructions CPython executes. The exact instruction names change between Python versions, which is a hint that bytecode is an internal detail, not a public format.*

---

## `__pycache__` and `.pyc` Files

Compilation leaves evidence on disk. When one file imports another, CPython writes the imported module's bytecode to a `__pycache__` folder so later runs can skip recompiling it:

```text
project/
├── app.py
├── helpers.py
└── __pycache__/
    └── helpers.cpython-312.pyc
```

*A project after running `app.py`, which imports `helpers.py`. The imported module's bytecode is cached in a `.pyc` file tagged with the interpreter version that produced it.*

A few details worth knowing:

- **The filename carries an interpreter tag** (`cpython-312` above) so different Python versions don't overwrite each other's caches. The tag on our machines matches whatever version is installed.
- **Recompilation is automatic.** CPython compares the cached file against the source and recompiles when the source has changed. We never manage these files by hand.
- **Only imported modules are cached.** The script we run directly is compiled in memory on every run and never gets a `.pyc`.
- **`__pycache__/` is disposable.** It's always safe to delete and never belongs in version control.

So Python *is* compiled. The compilation is just automatic, invisible, and happens on the way to running rather than as a separate build.

---

## "Interpreted vs Compiled" Is the Wrong Question

Java compiles to bytecode and runs it on a VM. Python compiles to bytecode and runs it on a VM. Calling one "compiled" and the other "interpreted" hides how similar the pipelines are. Two better questions separate them:

- **When does compilation happen?** Java: ahead of time, as a step we run explicitly. Python: at the moment we run the program or import a module.
- **What does compilation check?** Java: syntax, types, and that every referenced method and variable exists. Python: syntax, and very little else.

The second question is the one that shapes day-to-day work.

---

## What Python Checks — and What It Doesn't

Python's compile step rejects code that isn't valid syntax, and it does so for the *whole file* before a single line runs:

```python
print("Starting up")

if mode == "debug"          # missing colon
    print("debug details")
```

*A syntax error anywhere in the file stops everything. "Starting up" never prints — the file fails to compile, so nothing executes.*

A misspelled name, on the other hand, is perfectly valid syntax. It's just a name that happens not to exist, and Python won't discover that until the line actually executes:

```python
print("Starting up")
mode = "normal"

if mode == "debug":
    pritn("debug details")  # typo — but this line never runs
print("Done")
```

*This program runs cleanly and prints both "Starting up" and "Done". Change `mode` to `"debug"` and the same file fails at runtime with `NameError: name 'pritn' is not defined`.*

`javac` would have refused to compile that second file at all. Python happily runs it, and the typo waits on the unvisited branch until the day that branch executes.

---

## Compared to `javac` and `tsc`

**Java.** `javac` is a separate step that produces `.class` files we ship, and it acts as a gatekeeper. Type errors, missing methods, and undeclared variables all block the build. By the time Java code runs, an entire category of mistakes is already impossible.

**TypeScript.** `tsc` checks types and emits plain JavaScript. The types vanish, and the runtime never sees them. The checker is a separate tool that does its work before runtime begins.

**Python.** There is no equivalent step. Nothing sits between saving the file and running it, and the interpreter checks almost nothing before execution starts. Optional type-checking tools exist, but the interpreter neither requires nor consults them.

---

## What This Buys and What It Costs

| | Java / TypeScript | Python |
|---|---|---|
| Build step before running | Required | None |
| Feedback loop | Edit → compile → run | Edit → run |
| Typos and type errors caught | Before running | When the line executes |
| A bug on a rarely-run code path | Fails the build | Ships fine, fails in production |

The benefit is speed of iteration: no build wait, and a REPL where we can try code instantly. The cost is the missing safety net. A typo in an error handler or an end-of-quarter report can sit in production for months, untouched, until the one day that path runs. Python teams recover that safety deliberately — with tests, linters, and type checkers — because the language won't provide it for free.

---

## CPython, the Reference Implementation

"Python" is a language, and **CPython** — written in C — is its **reference implementation**. The installer from python.org is CPython, and it's what nearly everyone means when they say "Python."

Being the reference implementation means CPython *defines* how the language behaves in practice. New language features land in CPython first, and other implementations chase compatibility with it rather than the other way around. Those alternatives include **PyPy** (a faster implementation with a JIT compiler), **MicroPython** (for microcontrollers), and **GraalPy** (running on GraalVM).

This is a different arrangement from Java's. The JVM specification is a formal standard that multiple JVMs implement, and `.class` files are a stable, shippable format. CPython's bytecode is an implementation detail that changes between versions, which is why nobody ships `.pyc` files the way we ship `.jar` files — we ship source. The bytecode and `__pycache__` behavior described above are CPython specifics.

---

## Running Code: the REPL and `python file.py`

### The REPL

Running `python` with no arguments starts the **REPL** (read–eval–print loop), an interactive prompt that executes one statement at a time:

```text
$ python
>>> 2 ** 10
1024
>>> name = "Ada"
>>> "Hello, " + name
'Hello, Ada'
>>> exit()
```

*An interactive session. The `>>>` prompt accepts a statement, and the value of any expression is echoed back automatically.*

- A `...` prompt appears while we're partway through a multi-line block; an empty line finishes it.
- Exit with `exit()`, or Ctrl+D on macOS/Linux and Ctrl+Z then Enter on Windows.
- **The automatic echo only happens in the REPL.** A bare expression like `2 ** 10` on its own line in a file computes the value and discards it — files need `print()`.

Java has `jshell` and Node has its own REPL, so the idea isn't new. The difference is cultural: Python developers use the REPL constantly to check how something behaves before writing it into a file.

### Running a File

```text
$ python app.py
```

*Compiles `app.py` and executes it from the top of the file to the bottom.*

The command name varies by platform. On Windows it's usually `python` (or the `py` launcher). On macOS and many Linux distributions it's `python3`, and plain `python` may be missing or point somewhere unexpected. Running `python --version` (or `python3 --version`) confirms which interpreter we're getting.

---

## Key Takeaways

- Python compiles source to bytecode and runs it on a virtual machine — the same shape as Java, triggered automatically at run time.
- `__pycache__` holds cached bytecode for imported modules; it's automatic, version-tagged, and disposable.
- The compile step checks syntax for the whole file, but misspelled names and type errors surface only when the offending line executes.
- There is no `javac` or `tsc` equivalent: no build wait, and no compile-time safety net.
- CPython is the reference implementation that defines the language in practice; its bytecode is not a stable, shippable format.
- The REPL echoes expression values and is an everyday tool; `python file.py` runs a file top to bottom.
