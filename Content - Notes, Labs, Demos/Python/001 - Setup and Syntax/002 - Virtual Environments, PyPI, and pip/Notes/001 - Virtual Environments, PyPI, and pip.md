# Virtual Environments, PyPI, and pip

In Maven and npm, dependencies belong to the project. We declare them, the tooling fetches them, and one project's versions never interfere with another's. Python's default is the opposite: installed packages belong to the *interpreter*, and every project using that interpreter shares them. Isolation exists, but it's something we create on purpose — a virtual environment — before we install anything.

---

## The Problem: One Interpreter, One `site-packages`

Every Python installation has a **`site-packages`** directory where third-party libraries live. When we run `pip install`, the package lands in the `site-packages` of whichever interpreter that `pip` belongs to, and **only one version of a given package can be installed there at a time.**

Now picture two projects on the same machine, both run with the same interpreter:

- Project A was built against `requests` 2.28 and breaks on newer versions.
- Project B needs a feature from `requests` 2.32.

Installing 2.32 for project B *replaces* 2.28 in the shared `site-packages`, and project A breaks. There's no per-project scoping to prevent it.

Many modern operating systems now guard against this directly. On recent Debian, Ubuntu, and Homebrew-managed Pythons, running `pip install` against the system interpreter fails with an `externally-managed-environment` error — the OS is telling us to use a virtual environment instead.

---

## Contrast: How Maven and npm Avoid This

- **Maven** caches JARs in a shared `~/.m2` repository, but that cache happily holds many versions side by side. Each project's classpath is built from exactly the versions its `pom.xml` declares.
- **npm** installs into a `node_modules` folder inside each project directory. Two projects, two separate copies.

Both give us isolation by default without our asking. Python doesn't. A **virtual environment** is how we get the same guarantee — and because it's opt-in, forgetting to create or activate one is the most common way Python dependency problems start.

---

## Creating a Virtual Environment

```text
python -m venv .venv
```

*Creates a virtual environment in a folder named `.venv` inside the current directory.*

- **`-m venv`** tells Python to run the standard library's `venv` module as a program. Nothing needs to be installed first.
- **`.venv`** is the conventional folder name. Editors like VS Code detect it automatically.
- On some Linux distributions `venv` is packaged separately (for example, `python3-venv` on Debian and Ubuntu) and must be installed through the system package manager.

We create one environment per project, in the project's root folder.

---

## What Lives in the Environment Directory

A virtual environment isn't a full copy of Python. It's a lightweight directory with three important pieces:

| Piece | Windows | macOS / Linux |
|---|---|---|
| Interpreter, `pip`, and activation scripts | `.venv\Scripts\` | `.venv/bin/` |
| The environment's own, initially empty, `site-packages` | `.venv\Lib\site-packages\` | `.venv/lib/python3.X/site-packages/` |
| Config recording which base interpreter it came from | `.venv\pyvenv.cfg` | `.venv/pyvenv.cfg` |

The standard library still comes from the base installation; only third-party packages go into the environment. That's what makes environments cheap to create and throw away.

### Why It's Never Committed

The environment directory doesn't belong in version control:

- It contains **absolute paths** and **platform-specific executables** tied to our machine. Copied to a teammate's laptop or a Linux server, it simply won't work.
- It can grow large once real libraries are installed.
- It's **fully reproducible** from a requirements file, so there's nothing in it worth preserving.

We add `.venv/` to `.gitignore` and treat the environment as disposable — delete it and rebuild it whenever something seems off.

---

## Activation and Deactivation

**Activating** an environment makes its interpreter and `pip` the ones our terminal uses:

```text
# macOS / Linux (bash, zsh)
source .venv/bin/activate

# Windows PowerShell
.venv\Scripts\Activate.ps1

# Windows Command Prompt
.venv\Scripts\activate.bat
```

*Activation commands for each common shell. Once active, the prompt gains a `(.venv)` prefix.*

Activation isn't magic. It **prepends the environment's scripts folder to `PATH`**, so typing `python` or `pip` finds the environment's copies first, and it changes the prompt so we can see it's active. It affects only the current terminal session — a new terminal window starts without it.

To leave the environment:

```text
deactivate
```

*Restores the original `PATH` and prompt; available in every shell once an environment is active.*

> **PowerShell note:** if activation fails with a message about running scripts being disabled, the machine's execution policy is blocking it. Running `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` once allows local scripts like `Activate.ps1`.

### Confirming Which Interpreter Is Active

```text
python -c "import sys; print(sys.prefix)"
```

*Prints the root directory of the interpreter currently in use — a path ending in `.venv` when the environment is active, the base installation's path otherwise.*

`sys.prefix` points at the environment when one is active, while `sys.base_prefix` always points at the base installation. If the two differ, we're inside a virtual environment.

### Activation Is a Convenience

We can skip activation entirely by calling the environment's interpreter by path — `.venv/bin/python app.py` on macOS/Linux, `.venv\Scripts\python app.py` on Windows. That's exactly what activation arranges for us behind the scenes. It's also why activation matters so much: in Python the packages live in the environment, not beside the code, so the interpreter we launch decides which packages our program can see.

---

## Installing Packages with pip

**pip** is Python's package installer and ships with Python. Inside an active environment, it installs into that environment's `site-packages`.

| Command | What it does |
|---|---|
| `pip install requests` | Install the latest version, plus its dependencies |
| `pip install "requests==2.32.3"` | Install a specific version |
| `pip install --upgrade requests` | Upgrade to the latest version |
| `pip uninstall requests` | Remove the package (its dependencies stay behind) |
| `pip list` | Show every installed package and version, human-readable |
| `pip freeze` | Show installed packages in requirements-file format |
| `pip install -r requirements.txt` | Install everything listed in a requirements file |

A useful habit is running pip as **`python -m pip`** rather than bare `pip`. It guarantees the `pip` we run belongs to the `python` we expect, which removes a whole class of "I installed it but it can't be imported" confusion.

---

## `requirements.txt`

`pip freeze` writes out every installed package pinned to its exact version. Redirecting that output into a file gives us a **requirements file**:

```text
pip freeze > requirements.txt
```

*Captures the environment's current contents into `requirements.txt`.*

```text
certifi==2024.8.30
charset-normalizer==3.3.2
idna==3.10
requests==2.32.3
urllib3==2.2.3
```

*A frozen requirements file after installing only `requests`. The other four lines are packages `requests` depends on — `pip freeze` lists those too, all pinned.*

Anyone with the project can now rebuild an identical environment:

```text
python -m venv .venv
source .venv/bin/activate          # or the Windows equivalent
pip install -r requirements.txt
```

*Recreating the environment on a fresh clone, or after deleting a broken one.*

Unlike `npm install` reading `package.json`, nothing reads `requirements.txt` automatically. It's a plain list we explicitly hand to pip with `-r`.

---

## Pinned Versions vs Range Specifiers

A requirements line can pin an exact version or allow a range:

| Specifier | Meaning |
|---|---|
| `requests==2.32.3` | Exactly this version |
| `requests>=2.28` | This version or anything newer |
| `requests>=2.28,<3` | At least 2.28, but below 3.0 |
| `requests~=2.32.0` | "Compatible release": at least 2.32.0, below 2.33 |
| `requests` | Whatever is newest at install time |

**Pin exact versions for applications** we deploy, so every install is identical and an upstream release can't change behavior underneath us. **Ranges suit libraries** meant to be installed alongside other code, where overly strict pins cause conflicts. `pip freeze` always produces exact pins, which is why it's the usual way to snapshot a working application environment.

---

## PyPI

**PyPI**, the Python Package Index at pypi.org, is the public registry pip installs from by default — Python's equivalent of Maven Central or the npm registry.

Two practical notes:

- **The install name and the import name can differ.** `pip install beautifulsoup4` provides `import bs4`; `pip install pillow` provides `import PIL`. The package's PyPI page shows the import name.
- **Anyone can publish.** Typo-squatted packages with names one letter away from popular libraries do appear, so it's worth checking a package's PyPI page before installing something unfamiliar.

---

## `pyproject.toml`

Modern Python projects declare their metadata and dependencies in **`pyproject.toml`**, the closest equivalent to `package.json` or `pom.xml`:

```toml
[project]
name = "inventory-report"
version = "0.1.0"
dependencies = [
    "requests>=2.28",
    "rich",
]
```

*A minimal `pyproject.toml` declaring a project's name, version, and dependency ranges.*

pip and newer tools such as Poetry and uv all understand this file. `requirements.txt` remains common for simple applications and for pinning the exact contents of a deployed environment, so we'll see both in real projects.

---

## Key Takeaways

- Packages install into an interpreter's `site-packages`, which holds one version per package and is shared by everything using that interpreter.
- Maven and npm isolate dependencies per project by default; Python requires us to create a virtual environment deliberately.
- `python -m venv .venv` creates an environment; activation puts its `python` and `pip` first on `PATH`, and `deactivate` undoes it.
- `sys.prefix` confirms which interpreter is active.
- The environment folder is machine-specific and disposable — never committed, always rebuildable.
- `pip freeze > requirements.txt` snapshots exact versions; `pip install -r requirements.txt` rebuilds from them.
- Pin exact versions for applications; use ranges for libraries.
- PyPI is the default registry; `pyproject.toml` is where modern projects declare dependencies.
