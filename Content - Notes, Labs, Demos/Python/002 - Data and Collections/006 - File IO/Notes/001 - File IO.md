# File I/O

Reading and writing files in Python takes far less ceremony than in Java: one built-in function opens the file, a `with` block closes it, and a `for` loop reads it line by line. What Python doesn't do is make us think about failure — there are no checked exceptions — or pick a sensible text encoding everywhere by default. Both are left to us.

---

## `open()` and Modes

The built-in **`open()`** takes a path and a **mode** string, and returns a **file object**:

```python
file = open("servers.txt", "r", encoding="utf-8")
```

*Opening a file for reading as text, with an explicit encoding.*

The mode controls what we can do and what happens to an existing file:

| Mode | Purpose | If the file exists | If it doesn't |
|------|---------|--------------------|---------------|
| `"r"` | Read (the default) | Reads from the start | `FileNotFoundError` |
| `"w"` | Write | **Truncates it to empty** | Creates it |
| `"a"` | Append | Writes after the existing content | Creates it |
| `"x"` | Exclusive create | `FileExistsError` | Creates it |

`"w"` deserves respect: it empties the file the moment `open()` runs, before we've written anything.

### Text vs Binary

Adding `"b"` to a mode — `"rb"`, `"wb"` — opens the file in **binary mode**. The difference is what comes out:

- **Text mode** (the default) decodes the file's bytes into `str` using an encoding, and normalizes line endings so `\r\n` reads as `\n`. Writing `\n` produces the platform's line ending.
- **Binary mode** returns raw `bytes`, with no decoding and no newline translation. It's for images, archives, and anything that isn't text. It doesn't accept an `encoding` argument.

```python
with open("logo.png", "rb") as image:
    header = image.read(8)

print(type(header))     # <class 'bytes'>
```

*Reading the first eight bytes of an image file in binary mode.*

---

## `with` Is How We Open Files

A file object holds an operating-system resource, and it must be closed. Calling `close()` by hand is fragile — if anything fails between `open()` and `close()`, the close never runs, and buffered writes may never reach the disk:

```python
file = open("notes.txt", encoding="utf-8")
content = file.read()
file.close()
```

*Manual open and close. Correct only when nothing goes wrong in between.*

A **`with`** statement closes the file automatically when its block ends — whether the block finishes normally or an error escapes from it:

```python
with open("notes.txt", encoding="utf-8") as file:
    content = file.read()

print(file.closed)      # True — closed as soon as the block ended
print(len(content))     # the data we read is still available
```

*The file is bound to `file` for the block and closed on the way out.*

This is how Python code opens files, essentially always. For now we'll treat `with` as a usage pattern; the mechanism that makes it work — `__enter__` and `__exit__` — arrives in unit 003's Dunder Methods lesson.

**Contrast:** `with` replaces Java's try-with-resources, and it's shorter than either Java form — the `finally { reader.close(); }` style or `try (BufferedReader reader = ...)`.

---

## Reading

A file can be read all at once or a line at a time. Given a `servers.txt` containing three lines:

```text
web-01
web-02
db-01
```

*The sample file used in the examples below.*

```python
with open("servers.txt", encoding="utf-8") as file:
    text = file.read()          # 'web-01\nweb-02\ndb-01\n' — one string

with open("servers.txt", encoding="utf-8") as file:
    lines = file.readlines()    # ['web-01\n', 'web-02\n', 'db-01\n']

with open("servers.txt", encoding="utf-8") as file:
    first = file.readline()     # 'web-01\n' — one line per call
```

*`read()` returns the whole file as a string, `readlines()` returns a list of lines, and `readline()` returns the next single line.*

### Iterating the File Object

The most common approach is none of those: a file object is itself an iterable of lines, so we loop over it directly:

```python
with open("servers.txt", encoding="utf-8") as file:
    for line in file:
        name = line.rstrip("\n")
        print(f"checking {name}")
```

*Reading one line at a time, stripping each line's trailing newline.*

Iterating reads lazily, one line at a time, so it handles a multi-gigabyte log with the same memory as a three-line file. `read()` and `readlines()` load everything at once — fine for small files, a problem for large ones.

Two details catch people out:

- **Each line keeps its trailing `\n`.** Printing lines unmodified produces double spacing, and comparisons like `line == "web-01"` fail. Use `rstrip("\n")` or `strip()`.
- **A file object is a one-shot iterator.** A second `for` loop over the same open file finds nothing, because the first loop already consumed it.

---

## Writing and Appending

`write()` writes a string. It adds nothing — including newlines — so we supply them ourselves:

```python
servers = ["web-01", "web-02", "db-01"]

with open("inventory.txt", "w", encoding="utf-8") as file:
    file.write("Inventory\n")
    for server in servers:
        file.write(server + "\n")
```

*Creating (or overwriting) a file and writing a header followed by one server per line.*

**`writelines()`** writes every string from an iterable, and — despite its name — also adds no newlines:

```python
with open("inventory.txt", "w", encoding="utf-8") as file:
    file.writelines(f"{server}\n" for server in servers)
```

*Writing all the lines in one call, with newlines built into each string.*

`print()` can also write to a file through its `file` argument, and it does add a newline: `print(server, file=file)`.

**Append mode** adds to the end of an existing file instead of replacing it:

```python
with open("audit.log", "a", encoding="utf-8") as log:
    log.write("2026-03-14 09:30 deploy finished\n")
```

*Adding one entry to a log file without disturbing earlier entries.*

---

## Encodings

A text file is bytes on disk, and an **encoding** is the rule for turning those bytes into characters. When we don't pass `encoding=`, Python has historically used the operating system's locale setting — so the same script can read a file correctly on a Mac and produce garbled characters, or a `UnicodeDecodeError`, on a Windows machine.

The fix is to always name the encoding in text mode:

```python
with open("names.txt", encoding="utf-8") as file:
    names = [line.strip() for line in file]
```

*Reading with an explicit UTF-8 encoding, so the result is the same on every machine.*

UTF-8 is the right choice for almost everything we'll create. If a file came from another system and fails to decode, the error is telling us that file uses a different encoding, and we name that one instead.

---

## `pathlib.Path`

The **`pathlib`** module represents file-system paths as objects rather than strings. The `/` operator joins path segments using the correct separator for the operating system:

```python
from pathlib import Path

data_dir = Path("data")
input_path = data_dir / "servers.txt"

print(input_path)            # data/servers.txt (data\servers.txt on Windows)
print(input_path.name)       # servers.txt
print(input_path.stem)       # servers
print(input_path.suffix)     # .txt
print(input_path.parent)     # data
print(input_path.exists())   # True or False
print(input_path.is_file())  # True only if it exists and is a file
```

*Building a path from segments, then inspecting its parts and checking whether it exists.*

`open()` accepts a `Path` directly, and a `Path` also offers shortcuts for small whole-file reads and writes:

```python
output_dir = Path("reports")
output_dir.mkdir(parents=True, exist_ok=True)

report = output_dir / "summary.txt"
report.write_text("3 servers checked\n", encoding="utf-8")
print(report.read_text(encoding="utf-8"))
```

*Creating a directory if needed, then writing and reading a whole file through the `Path` object.*

### Relative Paths and the Working Directory

A relative path like `Path("data/servers.txt")` is resolved against the **current working directory** — wherever the terminal was when we ran `python` — not the folder the script lives in. Running the same script from a different directory breaks it. To locate a file relative to the script itself, we start from `__file__`:

```python
from pathlib import Path

here = Path(__file__).parent
input_path = here / "data" / "servers.txt"
```

*Building a path that works no matter which directory the script is launched from.*

### Checking Before Opening

Opening a file that isn't there raises an error:

```python
open("missing.txt", encoding="utf-8")
# FileNotFoundError: [Errno 2] No such file or directory: 'missing.txt'
```

*Reading a nonexistent file fails at the `open()` call.*

Until exceptions are covered in unit 003, the straightforward guard is a check first:

```python
if input_path.exists():
    with open(input_path, encoding="utf-8") as file:
        print(file.read())
else:
    print(f"No input found at {input_path}")
```

*Checking that a file exists before trying to read it.*

**Contrast:** Java's `IOException` is checked, so the compiler refuses to build code that opens a file without catching or declaring it. Python raises `FileNotFoundError` just as surely, but nothing forces us to plan for it — the failure is ours to anticipate.

---

## Key Takeaways

- `open(path, mode, encoding=...)` returns a file object. `"r"` reads, `"w"` truncates and writes, `"a"` appends, `"x"` creates only if absent.
- Text mode decodes to `str` and normalizes newlines; adding `"b"` gives raw `bytes`.
- Open files with `with`, which closes them even when something fails. Its mechanism comes in unit 003.
- `read()` returns the whole file, `readlines()` a list of lines, and iterating the file object reads lazily line by line. Lines keep their trailing `\n`.
- `write()` and `writelines()` add no newlines; `print(..., file=f)` does.
- Always pass `encoding="utf-8"` in text mode — the default depends on the machine.
- `pathlib.Path` builds paths with `/`, exposes `name`, `stem`, `suffix`, and `parent`, and checks `exists()`.
- Relative paths resolve against the working directory; use `Path(__file__).parent` to find files next to the script.
- There's no checked `IOException`: a missing file raises `FileNotFoundError`, and nothing forces us to handle it.
