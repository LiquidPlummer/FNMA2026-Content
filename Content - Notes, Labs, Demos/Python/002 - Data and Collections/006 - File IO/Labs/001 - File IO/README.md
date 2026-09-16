# File I/O

We'll read a week's timesheet from a text file, total everyone's hours, and write a summary file back out. Along the way we'll cover `with open(...)` for reading and writing, text mode with an explicit encoding, `read()`, `readlines()` contrasted with iterating a file object line by line, `readline()`, `writelines()` and `write()`, append mode, and `pathlib.Path` for building paths and checking whether files exist — reusing the string and comprehension work from earlier in the unit on the file's contents. The exercise finishes with a second timesheet in a different layout.

---

## Prerequisites

| Software | Required Version |
|---|---|
| Python | 3.11 or newer (current stable release: 3.14.7) |

This lab uses no third-party packages, so there's nothing to install and a virtual environment is optional.

### Getting Started

1. Open a terminal in this lab's folder — the one containing this `README.md`.
2. Run the starter file (use `python3` on macOS/Linux if `python` isn't available):

   ```text
   python src/file_lab.py
   ```

The starter runs as-is. Every stub returns an empty placeholder, so most lines print `None`, `0`, or an empty value for now. The first run also creates an `output/` folder next to `data/`.

### Project Layout

```text
001 - File IO/
├── README.md
├── data/
│   └── timesheet.txt    # the week of hours we'll read
└── src/
    └── file_lab.py      # paths and numbered stubs on top, driver block at the bottom
```

*`output/` isn't shipped with the lab. The program creates it and writes its files there.*

### The Data File

`data/timesheet.txt` looks like this:

```text
# week of 2026-03-09
Ada Lovelace    , mon , 7.5
Grace Hopper    , mon , 8
Ada Lovelace    , tue , 6

Linus Torvalds  , tue , 9.25
Grace Hopper    , wed , 7.75
# Linus was out Wednesday
Ada Lovelace    , wed , 8
Linus Torvalds  , thu , 4.5
Grace Hopper    , fri , 8
```

*The first line names the week. Every other non-blank, non-comment line is an entry: a name, a day, and hours, separated by commas and padded with spaces.*

### How the File Is Organized

- **The path constants** at the top of `src/file_lab.py` are built from `Path(__file__)`, the location of the script itself. A relative path like `Path("data/timesheet.txt")` would depend on which directory the terminal is in; these don't.
- **The stubs** are numbered 2 through 11. Section 1 lives entirely in the driver.
- **The driver** at the bottom has a matching numbered header for each section.

---

## Guided Walkthrough

### 1. Paths

In section 1 of the driver, we'll replace the five `None`s with parts of `TIMESHEET_PATH`:

```python
    print("name:", TIMESHEET_PATH.name)
    print("stem:", TIMESHEET_PATH.stem)
    print("suffix:", TIMESHEET_PATH.suffix)
    print("folder:", TIMESHEET_PATH.parent.name)
    print("exists:", TIMESHEET_PATH.exists())
```

*`name`, `stem`, and `suffix` are attributes holding pieces of the path. `parent` is another `Path`, so it has a `name` of its own. `exists()` is a method, because it has to check the disk.*

Expected output:

```text
== 1. Paths ==
name: timesheet.txt
stem: timesheet
suffix: .txt
folder: data
exists: True
missing exists: False
```

The last line was already written. `DATA_DIR / "missing.txt"` builds a perfectly valid `Path` — building a path never touches the disk, so there's no error until something tries to open it.

### 2. Reading Everything

We'll open the file in a `with` block and read it in one call:

```python
def read_whole(path):
    """Return the entire contents of the text file at path as one string.

    Open it with a with block, in text mode, with an explicit UTF-8 encoding.
    """
    with open(path, encoding="utf-8") as file:
        return file.read()
```

*With no mode argument, `open` reads in text mode. The `return` happens inside the `with` block, and the file is still closed on the way out.*

Expected output:

```text
== 2. Reading everything ==
characters: 266
first line: # week of 2026-03-09
```

`read()` returns every character in the file as a single string, newlines included. That's convenient for small files, but the whole file sits in memory at once.

### 3. `readlines()`

`readlines()` also reads the whole file, but splits it into a list of lines:

```python
def read_lines_list(path):
    """Return a list of every line in the file at path, using readlines()."""
    with open(path, encoding="utf-8") as file:
        return file.readlines()
```

*One list item per line in the file, blank lines and comments included.*

Expected output:

```text
== 3. readlines() ==
lines: 11
['# week of 2026-03-09\n', 'Ada Lovelace    , mon , 7.5\n']
```

Printing a list shows each string in quotes, which makes the `\n` at the end of every line visible. Before moving on, we'll answer the **Explain:** prompt about it.

### 4. Iterating the File

First, a helper that recognizes entry lines — the same strip-then-test pattern from the Strings lab:

```python
def is_entry(line):
    """Return True if line holds an entry, or False if it's blank or a comment.

    A comment starts with "#" once surrounding whitespace is removed.
    """
    stripped = line.strip()
    return stripped != "" and not stripped.startswith("#")
```

*`strip()` also removes the trailing `\n`, so a blank line becomes an empty string.*

Now we'll count entries by looping over the file object directly:

```python
def count_entries(path):
    """Return how many entry lines the file at path holds.

    Loop over the file object itself, one line at a time.
    """
    count = 0
    with open(path, encoding="utf-8") as file:
        for line in file:
            if is_entry(line):
                count += 1
    return count
```

*A file object is iterable: each pass of the `for` loop reads the next line, including its `\n`.*

Expected output:

```text
== 4. Iterating the file ==
entries: 8
```

Of the 11 lines, one is the week label, one is blank, and one is a comment, leaving 8 entries.

Iterating and `readlines()` both give us lines, but not in the same way. `readlines()` reads the entire file into a list before we see any of it. Iterating reads one line at a time as the loop asks for it, so a multi-gigabyte log uses no more memory than this 11-line file. For looping over a file's lines, iterating is the default choice.

### 5. Parsing a Line

Each entry splits on commas into three padded fields:

```python
def parse_line(line):
    """Return (name, day, hours) from a line like "Ada Lovelace  , mon , 7.5".

    name and day are stripped strings; hours is a float.
    """
    name, day, hours = line.split(",")
    return name.strip(), day.strip(), float(hours)
```

*`float()` ignores the surrounding whitespace and the trailing newline, so `hours` needs no stripping.*

Expected output:

```text
== 5. Parsing a line ==
('Grace Hopper', 'wed', 7.75)
```

### 6. Reading Entries

With `is_entry` and `parse_line` written, reading every entry is one comprehension over the file object:

```python
def read_entries(path):
    """Return a list of (name, day, hours) tuples, one per entry line in the file.

    Skip blank and comment lines.
    """
    with open(path, encoding="utf-8") as file:
        return [parse_line(line) for line in file if is_entry(line)]
```

*The comprehension iterates the file lazily like the loop in section 4, filters with `is_entry`, and parses each line that passes.*

Expected output:

```text
== 6. Reading entries ==
entries: 8
[('Ada Lovelace', 'mon', 7.5), ('Grace Hopper', 'mon', 8.0)]
```

The comprehension has to finish inside the `with` block. Once the block ends, the file is closed and can't be read.

### 7. The Week Label

We only need the first line, so we'll read just that one with `readline()`:

```python
def week_label(path):
    """Return the week label from the file's first line, using readline().

    The first line looks like "# week of 2026-03-09"; return "week of 2026-03-09".
    """
    with open(path, encoding="utf-8") as file:
        first = file.readline()
    return first[2:].strip()
```

*`readline()` returns the next single line, `\n` included. The slice drops the leading `"# "`, and `strip()` removes the newline.*

Expected output:

```text
== 7. The week label ==
week of 2026-03-09
```

### 8. Totals

We'll accumulate each person's hours in a dict:

```python
def total_hours(entries):
    """Return a dict mapping each name in entries to that person's total hours."""
    totals = {}
    for name, day, hours in entries:
        totals[name] = totals.get(name, 0) + hours
    return totals
```

*`get(name, 0)` supplies zero the first time a name appears. `day` is unpacked but not needed here.*

Expected output:

```text
== 8. Totals ==
{'Ada Lovelace': 21.5, 'Grace Hopper': 23.75, 'Linus Torvalds': 13.75}
```

### 9. Summary Lines

Next, we'll shape the totals into the lines of the summary file:

```python
def summary_lines(week, totals):
    """Return the summary file's lines as a list of strings with no newlines.

    First line: "Hours for <week>".
    Then one line per person, sorted by name: the name left-aligned in 18
    characters, then the total right-aligned in 6 characters with 2 decimals.
    Last line: "Total" formatted the same way, with the sum of every total.
    """
    lines = [f"Hours for {week}"]
    lines.extend(f"{name:<18}{hours:>6.2f}" for name, hours in sorted(totals.items()))
    lines.append(f"{'Total':<18}{sum(totals.values()):>6.2f}")
    return lines
```

*`sorted(totals.items())` sorts the `(name, hours)` pairs by name. `extend` accepts the generator expression directly, adding one formatted line per person.*

Expected output:

```text
== 9. Summary lines ==
Hours for week of 2026-03-09
Ada Lovelace       21.50
Grace Hopper       23.75
Linus Torvalds     13.75
Total              59.00
```

### 10. Writing the Summary

We'll open the summary file in write mode and write every line:

```python
def write_lines(path, lines):
    """Write lines to the file at path, one per line, replacing any existing file.

    Use writelines(). Each line in the file must end with a newline.
    """
    with open(path, "w", encoding="utf-8") as file:
        file.writelines(f"{line}\n" for line in lines)
```

*`"w"` creates the file, or empties it if it already exists. `writelines()` adds no newlines of its own, so the generator expression adds one to each line.*

The driver creates `output/` with `mkdir(exist_ok=True)` before calling `write_lines`, and reads the file back only if it exists.

Expected output:

```text
== 10. Writing the summary ==
written: True
Hours for week of 2026-03-09
Ada Lovelace       21.50
Grace Hopper       23.75
Linus Torvalds     13.75
Total              59.00
```

Opening `output/hours_summary.txt` in an editor, its exact contents are:

```text
Hours for week of 2026-03-09
Ada Lovelace       21.50
Grace Hopper       23.75
Linus Torvalds     13.75
Total              59.00
```

*Five lines, each ending in a newline.*

It's worth trying `file.writelines(lines)` once instead. Every line runs together into one long line in the file, because nothing added the newlines.

### 11. Appending to a Log

Append mode adds to the end of a file instead of replacing it:

```python
def log_run(path, message):
    """Add message as a new line at the end of the file at path, keeping what's already there."""
    with open(path, "a", encoding="utf-8") as log:
        log.write(message + "\n")
```

*`write()` writes exactly the string it's given, so the newline is added by hand.*

Expected output:

```text
== 11. Appending to a log ==
run log
wrote hours_summary.txt
8 entries summarized
```

The driver starts section 11 by opening the log in `"w"` mode and writing `run log`, which resets the file on every run. Then both `log_run` calls append after it.

To see the difference between the two modes, we'll comment out the driver's three-line `with open(LOG_PATH, "w", ...)` block and run the file three more times. `output/run_log.txt` grows by two lines on every run, because `"a"` never removes anything. Then we'll restore the block.

> **Contrast — Java:** `with` does the job of try-with-resources — the file is closed when the block ends, even if something inside it fails. What Python doesn't have is a checked `IOException`: if `timesheet.txt` were missing, `open` would raise `FileNotFoundError`, but nothing in the language forces us to plan for that. Checking with `exists()` first is on us.

### Checking the Whole File

With all eleven sections done, running `python src/file_lab.py` prints:

```text
== 1. Paths ==
name: timesheet.txt
stem: timesheet
suffix: .txt
folder: data
exists: True
missing exists: False
== 2. Reading everything ==
characters: 266
first line: # week of 2026-03-09
== 3. readlines() ==
lines: 11
['# week of 2026-03-09\n', 'Ada Lovelace    , mon , 7.5\n']
== 4. Iterating the file ==
entries: 8
== 5. Parsing a line ==
('Grace Hopper', 'wed', 7.75)
== 6. Reading entries ==
entries: 8
[('Ada Lovelace', 'mon', 7.5), ('Grace Hopper', 'mon', 8.0)]
== 7. The week label ==
week of 2026-03-09
== 8. Totals ==
{'Ada Lovelace': 21.5, 'Grace Hopper': 23.75, 'Linus Torvalds': 13.75}
== 9. Summary lines ==
Hours for week of 2026-03-09
Ada Lovelace       21.50
Grace Hopper       23.75
Linus Torvalds     13.75
Total              59.00
== 10. Writing the summary ==
written: True
Hours for week of 2026-03-09
Ada Lovelace       21.50
Grace Hopper       23.75
Linus Torvalds     13.75
Total              59.00
== 11. Appending to a log ==
run log
wrote hours_summary.txt
8 entries summarized
```

---

## Exercises

A second timesheet has arrived from a different system, in a different layout. Create a new file, `data/timesheet_week2.txt`, with exactly this content:

```text
WEEK 2026-03-16
day=mon; name=Grace Hopper; hours=8
name=Alan Turing; day=mon; hours=6.5

hours=7; day=tue; name=Grace Hopper
day=wed; name=Alan Turing; hours=8.25
day=thu; name=Ada Lovelace; hours=5
```

*The first line holds the week's date. Each entry is a set of `key=value` fields separated by semicolons — and the fields don't always come in the same order.*

Then extend `src/file_lab.py` so that running it also summarizes this second file. Add whatever new functions you need, plus an `== Exercise ==` section at the end of the driver. The requirements:

1. **Check before reading.** Use `Path` to check whether `data/timesheet_week2.txt` exists. If it doesn't, print `missing: timesheet_week2.txt` and skip the rest of the exercise. Temporarily renaming the file is a good way to test this.
2. **Parse the new layout.** Read the week label as `week of 2026-03-16`, skip blank lines, and turn each entry into a `(name, day, hours)` tuple. Don't depend on the order of the fields.
3. **Reuse what exists.** `total_hours`, `summary_lines`, and `write_lines` must work on the new entries without being changed.
4. **Write the result.** Write the summary to `output/hours_summary_2026-03-16.txt`, building the file name from the date in the week label rather than typing it out.
5. **Log it.** Append a line to `output/run_log.txt` naming the file that was written.

When it works, `output/hours_summary_2026-03-16.txt` contains exactly:

```text
Hours for week of 2026-03-16
Ada Lovelace        5.00
Alan Turing        14.75
Grace Hopper       15.00
Total              34.75
```

Finally, confirm that sections 1 through 11 still print exactly what they did before, and that `output/hours_summary.txt` is unchanged.
