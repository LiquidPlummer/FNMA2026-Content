# Environment and Entry Point

We'll set up a Python project the way real ones start: create a **virtual environment** with `python -m venv`, activate it and confirm which interpreter is running, install the third-party `rich` package with **pip**, and record our dependencies in `requirements.txt`. Then we'll split our script into two modules to watch **import-time execution** happen, compare **`__name__`** in each file, and add the **`if __name__ == "__main__":`** guard.

---

## Prerequisites

| Software | Required Version |
|---|---|
| Python | 3.11 or newer (current stable release: 3.14.7) |
| pip | Bundled with Python |
| rich | Latest — 15.0.0 at time of writing (installed during the lab; requires Python ≥ 3.9) |

### Getting Started

1. Open a terminal in this lab's folder — the one containing this `README.md`. Every command in this lab runs from here.
2. Confirm Python is installed:

   ```text
   python --version
   ```

> **Command names:** on macOS and most Linux systems, use `python3` wherever this lab says `python` *until the virtual environment is activated*. Once it's active, plain `python` works on every platform. On Windows, `python` (or the `py` launcher) works throughout.

### Project Layout

```text
001 - Environment and Entry Point/
├── README.md
└── src/
    └── main.py
```

---

## Guided Walkthrough

### Part 1: Run the Starter

We'll start by running the one-line starter script:

```text
python src/main.py
```

Expected output:

```text
Hello from main.py
```

Now let's check *which* Python ran it. The `-c` flag runs a line of code directly, and `sys.prefix` holds the root folder of the interpreter in use:

```text
python -c "import sys; print(sys.prefix)"
```

This prints the folder of our system-wide Python installation — something like `C:\Program Files\Python314` on Windows or `/usr/local` on macOS/Linux. **Note this path down**; we'll compare against it shortly.

### Part 2: Create the Virtual Environment

We'll create an environment in a folder named `.venv`:

```text
python -m venv .venv
```

A new `.venv` folder appears in the lab directory. Let's look inside it:

- **`Scripts\`** (Windows) or **`bin/`** (macOS/Linux) holds the environment's own `python`, `pip`, and activation scripts.
- **`Lib\site-packages\`** (Windows) or **`lib/python3.X/site-packages/`** (macOS/Linux) is where packages will be installed. It's nearly empty right now.
- **`pyvenv.cfg`** is a small text file. Opening it, we'll see a `home` line pointing back at the base Python installation this environment was created from.

Creating the environment doesn't switch anything over. Our terminal is still using the system Python until we activate it.

### Part 3: Activate and Confirm

We'll activate the environment using the command for our shell:

```text
# macOS / Linux (bash, zsh)
source .venv/bin/activate

# Windows PowerShell
.venv\Scripts\Activate.ps1

# Windows Command Prompt
.venv\Scripts\activate.bat
```

*Activation puts the environment's `Scripts` or `bin` folder at the front of `PATH` for this terminal session.*

The prompt now starts with **`(.venv)`**.

> **PowerShell:** if activation fails with a message that running scripts is disabled, run `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` once, then activate again.

Now we'll repeat the interpreter check from Part 1:

```text
python -c "import sys; print(sys.prefix)"
```

This time the path ends in **`.venv`** — the lab folder's environment, not the system installation we noted earlier. Same command, different interpreter.

Let's see what's installed in the fresh environment:

```text
pip list
```

Expected output: a table containing only **`pip`** (on Python 3.11, `setuptools` appears too). None of the packages from our system Python are visible here.

### Part 4: Install and Use `rich`

We'll install `rich`, a library for colored terminal output:

```text
python -m pip install rich
```

*Running pip through `python -m` guarantees we're using the pip that belongs to the active environment.*

Now let's list the environment's packages again:

```text
pip list
```

Expected output (version numbers may be newer):

```text
Package        Version
-------------- -------
markdown-it-py 4.2.0
mdurl          0.1.2
pip            25.3
Pygments       2.21.0
rich           15.0.0
```

We asked for one package and got four. `markdown-it-py`, `mdurl`, and `Pygments` are packages `rich` depends on, and pip installed them automatically.

Let's use the library. Open `src/main.py` and make it look like this — the `import` goes at the top, and the `rich.print` line goes at the bottom:

```python
import rich

print("Hello from main.py")
rich.print("[bold green]Hello[/bold green] from [italic]rich[/italic]!")
```

*`import rich` binds the name `rich` to the installed package, and `rich.print` understands style tags written in square brackets.*

Run it:

```text
python src/main.py
```

Expected output:

```text
Hello from main.py
Hello from rich!
```

In the second line, **Hello** is bold green and **rich** is italic. (A few terminals don't render italics, but the color should appear.)

### Part 5: Prove the Isolation

We'll leave the environment and run the exact same file again:

```text
deactivate
python src/main.py
```

This time it fails with an error ending in **`No module named 'rich'`**. The `(.venv)` prefix is gone, the system Python is back in charge, and `rich` only exists inside the environment. (If it runs anyway, `rich` happens to be installed in the system Python as well.)

Let's re-activate using the same command from Part 3, then run again:

```text
python src/main.py
```

Both lines print again. The code didn't change — only which interpreter ran it.

### Part 6: Record Our Dependencies

We'll capture the environment's exact contents in a requirements file:

```text
pip freeze > requirements.txt
```

A new `requirements.txt` appears in the lab folder. Opening it, we'll see:

```text
markdown-it-py==4.2.0
mdurl==0.1.2
Pygments==2.21.0
rich==15.0.0
```

A few things to observe:

- **Every line is pinned** with `==` to the exact version installed.
- **Dependencies are included**, not just `rich`, so a rebuild reproduces the whole environment.
- **`pip` itself isn't listed** — `pip freeze` leaves out the packaging tools.

`requirements.txt` is the file a project commits to version control. The `.venv` folder never is: it's full of machine-specific paths and can be rebuilt from this file at any time.

### Part 7: Split into Two Modules

Now we'll move from the environment to the program itself. We'll create a second file, `src/greeter.py`, with this content:

```python
print("greeter.py is running")
print("  __name__ in greeter.py is:", __name__)

message = "Hello from the greeter module"
```

*A module that announces itself, reports its own `__name__`, and defines one name, `message`, for other modules to use.*

Next, we'll update `src/main.py` to import it. Replace the file's contents with:

```python
import rich
import greeter

print("Hello from main.py")
print("  __name__ in main.py is:", __name__)
rich.print(f"[bold green]{greeter.message}[/bold green]")
```

*`import greeter` binds the name `greeter` to the module, so `greeter.message` reaches the `message` name defined inside it.*

Run `main.py`:

```text
python src/main.py
```

Expected output:

```text
greeter.py is running
  __name__ in greeter.py is: greeter
Hello from main.py
  __name__ in main.py is: __main__
Hello from the greeter module
```

The last line appears in bold green. There's a lot to observe here:

- **`greeter.py`'s lines print first.** The `import greeter` line executed all of `greeter.py` before `main.py` moved on to its own `print` calls.
- **`__name__` differs between the files.** The file we ran is `"__main__"`; the imported file is `"greeter"`.
- **A `__pycache__` folder appeared inside `src/`.** It holds a `greeter.cpython-3XX.pyc` file — the compiled bytecode of the module we imported. There's no `.pyc` for `main.py`, because the script we run directly isn't cached.

Now let's run `greeter.py` directly instead:

```text
python src/greeter.py
```

Expected output:

```text
greeter.py is running
  __name__ in greeter.py is: __main__
```

The same file now reports `__main__`, because this time it's the entry point.

### Part 8: Add the Main Guard

`greeter.py`'s two `print` lines fire every time anything imports it. That's fine when we run it directly, but it's noise for every module that just wants `message`. We'll guard them. Rewrite `src/greeter.py` as:

```python
message = "Hello from the greeter module"

if __name__ == "__main__":
    print("greeter.py is running")
    print("  __name__ in greeter.py is:", __name__)
```

*The `print` lines are indented four spaces under the `if`, so they run only when `greeter.py` is the file being executed. `message` stays unindented, so it's defined either way.*

Let's run it both ways. First through `main.py`:

```text
python src/main.py
```

Expected output:

```text
Hello from main.py
  __name__ in main.py is: __main__
Hello from the greeter module
```

`greeter.py`'s lines are gone — but `main.py` still reads `greeter.message` successfully. Now directly:

```text
python src/greeter.py
```

Expected output:

```text
greeter.py is running
  __name__ in greeter.py is: __main__
```

Next we'll give `main.py` the same treatment, so it could safely be imported too. Update `src/main.py` to:

```python
import rich
import greeter

if __name__ == "__main__":
    print("Hello from main.py")
    print("  __name__ in main.py is:", __name__)
    rich.print(f"[bold green]{greeter.message}[/bold green]")
```

*The imports stay at the top; only the code that should run when `main.py` is launched moves under the guard.*

Running `python src/main.py` prints the same three lines as before. To see the guard do its job, we'll import `main.py` instead of running it. From the lab folder, with the environment still active:

```text
cd src
python
>>> import main
>>> exit()
cd ..
```

**`import main` prints nothing.** The import still ran `main.py` top to bottom — it loaded `rich` and `greeter` — but `__name__` was `"main"`, so the guarded block was skipped.

> **Contrast — Java:** `public static void main(String[] args)` is required; the launcher won't start a class without it. Python's guard is an ordinary `if` we opt into — `python src/greeter.py` ran perfectly well before we ever added one.

---

## Exercises

1. **Rebuild from `requirements.txt`.** Deactivate the environment and delete the `.venv` folder completely (`Remove-Item -Recurse -Force .venv` in PowerShell, `rm -rf .venv` on macOS/Linux). Then create a new environment, activate it, and install everything using only `requirements.txt`. Confirm that `pip list` shows the same packages as in Part 4, and that `python src/main.py` prints the same three lines as it did at the end of Part 8.

2. **Imported once, run once.** Add an unguarded line at the very top of `greeter.py` that prints `greeter loaded`. Then add a second `import greeter` line in `main.py`, directly below the first one. Before running anything, write down how many times you expect `greeter loaded` to appear when you run `main.py`. Run it and check your prediction.

3. **Above and below the guard.** Add a `print` call to `main.py` *above* its guard. Predict exactly what appears in each of these cases, then check both:
   - running `python src/main.py`
   - running `import main` from a fresh REPL started inside `src/`
