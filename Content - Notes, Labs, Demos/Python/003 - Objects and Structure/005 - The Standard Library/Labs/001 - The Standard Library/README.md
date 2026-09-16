# The Standard Library

We'll load a week of deployment records from a JSON file, summarize them, and write a report back out as JSON — without installing a single package. Along the way we'll cover `json.load` and `json.dumps` and how JSON maps onto Python types, `collections.Counter` and `defaultdict`, parsing and formatting timestamps with `datetime`, `pathlib.Path` for locating and writing files, and tracking down a standard library function in the official docs that this README deliberately doesn't name. The exercise finishes by splitting the finished script into a package.

---

## Prerequisites

| Software | Required Version |
|---|---|
| Python | 3.11 or newer (current stable release: 3.14.7) |

Everything this lab uses ships with Python, so there's nothing to install and a virtual environment is optional.

### Getting Started

1. Open a terminal in this lab's folder — the one containing this `README.md`.
2. Run the starter file (use `python3` on macOS/Linux if `python` isn't available):

   ```text
   python src/report.py
   ```

The starter runs as-is and prints a single line:

```text
Nothing to run yet: finish section 1, then set RUN_THROUGH = 1.
```

### Project Layout

```text
001 - The Standard Library/
├── README.md
├── data/
│   └── deployments.json    # the export we'll summarize
└── src/
    └── report.py           # numbered stubs on top, driver block at the bottom
```

*`output/` isn't shipped with the lab. Section 11 creates it and writes the report there.*

### The Data File

`data/deployments.json` holds a team name, an export timestamp, and a list of twelve deployments. Each deployment looks like this:

```json
{"service": "search", "author": "grace", "started": "2026-03-09 14:40", "seconds": 505, "status": "success", "ticket": null, "rollback": false}
```

*One deployment: which service was deployed, by whom, when it started, how long it took, how it ended, an optional ticket, and whether it was rolled back.*

### How the Driver Works

Unlike earlier labs, the stubs here build on each other: section 1 loads the data every later section uses, and section 10 calls nearly every function before it. So the driver runs only as far as we tell it to.

At the bottom of `src/report.py`, `RUN_THROUGH` starts at `0`. After finishing each section, we'll set it to that section's number and run the file. Every section up to and including that number runs.

---

## Guided Walkthrough

### 1. Loading JSON

`json.load` reads JSON from an open file and returns the equivalent Python object:

```python
def load_export(path):
    """Return the Python object stored in the JSON file at path, using json.load()."""
    with open(path, encoding="utf-8") as file:
        return json.load(file)
```

*`load` takes a file object; its sibling `loads` — "load string" — takes a string instead.*

With `RUN_THROUGH = 1`, the expected output is:

```text
== 1. Loading JSON ==
<class 'dict'> ['team', 'exported', 'deployments']
<class 'list'> 12
<class 'int'> 312
None False
```

There are no data classes or mapping annotations here: every JSON value became an ordinary built-in type. The top-level object became a `dict`, the array a `list`, the number an `int`, `null` became `None`, and `false` became `False`.

> **Contrast — Java:** parsing this file in Java means adding Jackson or Gson to the build, then usually writing classes for the data to bind to. Python's `json` module ships with the interpreter, and it hands back dicts and lists we can use immediately.

### 2. Parsing Timestamps

The `started` values are strings. `datetime.strptime` parses a string using a format made of `%` codes:

```python
def parse_timestamp(text):
    """Return a datetime parsed from text in the export's format, like "2026-03-12 13:10"."""
    return datetime.strptime(text, "%Y-%m-%d %H:%M")
```

*`%Y` is the four-digit year, `%m` the month, `%d` the day, `%H` the 24-hour hour, and `%M` the minute. Every other character in the format — the dashes, the space, the colon — must appear literally in the text.*

With `RUN_THROUGH = 2`:

```text
== 2. Parsing timestamps ==
2026-03-12 13:10:00
<class 'datetime.datetime'>
2026 3 12
```

The result is a real `datetime` object, with attributes like `year` and `month` rather than just characters. Lowercase `%m` is the month and uppercase `%M` is the minute; swapping them is the classic mistake, and it either fails to parse or parses the wrong date.

### 3. Formatting Timestamps

`strftime` goes the other way, turning a `datetime` into text:

```python
def short_time(moment):
    """Return moment formatted like "Thu 12 Mar, 13:10"."""
    return moment.strftime("%a %d %b, %H:%M")
```

*`%a` is the abbreviated weekday name and `%b` the abbreviated month name.*

With `RUN_THROUGH = 3`:

```text
== 3. Formatting timestamps ==
Thu 12 Mar, 13:10
2026-03-12T13:10:00
```

The second line comes from `isoformat()`, which produces the standard ISO 8601 form without needing any format codes. Weekday and month names follow the computer's language settings, so a non-English system may print different names.

### 4. `Counter`

A `Counter` counts everything in an iterable in a single call:

```python
def status_counts(deployments):
    """Return a Counter of how many deployments ended with each status."""
    return Counter(deployment["status"] for deployment in deployments)
```

*The generator expression produces each deployment's status, and `Counter` tallies them.*

With `RUN_THROUGH = 4`:

```text
== 4. Counter ==
Counter({'success': 9, 'failed': 2, 'cancelled': 1})
9 0
```

A `Counter` is a dict underneath, so `counts["success"]` works as expected. The difference shows on the last value: `"skipped"` never appears in the data, and a `Counter` returns `0` for it instead of raising `KeyError`.

### 5. `most_common`

`most_common(n)` returns the `n` highest counts as `(value, count)` tuples, highest first:

```python
def top_authors(deployments, n):
    """Return the n authors with the most deployments, as (author, count) pairs, most first."""
    return Counter(deployment["author"] for deployment in deployments).most_common(n)
```

*Counting authors, then asking for the top `n`.*

With `RUN_THROUGH = 5`:

```text
== 5. most_common ==
[('ada', 4), ('grace', 3)]
```

Grace and Margaret both made three deployments. When counts tie, `most_common` keeps them in the order they were first counted, and Grace appears earlier in the file.

### 6. `defaultdict`

We'll group each service's durations into lists. A `defaultdict(list)` creates an empty list the first time a missing key is used:

```python
def seconds_by_service(deployments):
    """Return a defaultdict mapping each service to a list of its deployments' seconds, in order."""
    grouped = defaultdict(list)
    for deployment in deployments:
        grouped[deployment["service"]].append(deployment["seconds"])
    return grouped
```

*`list` is passed without parentheses — the `defaultdict` calls it whenever it needs a new empty list. There's no need to check whether a service is already a key.*

With `RUN_THROUGH = 6`:

```text
== 6. defaultdict ==
{'billing': [312, 290, 301, 330, 305], 'search': [505, 498, 512, 640], 'auth': [188, 201, 176]}
```

The driver wraps the result in `dict()` only for printing; a `defaultdict` prints with its default factory shown first, which is noisier.

### 7. From the Docs

This section has no code to copy. `median_seconds` needs the **median** of each service's durations — the middle value once they're sorted, or the average of the two middle values when there's an even number of them. The standard library already has a function that calculates it, and part of this section is finding it.

A few ways to hunt:

- **Browse the library reference** at [docs.python.org/3/library](https://docs.python.org/3/library/). Modules are grouped by purpose, so start by skimming the group headings for the one where a function like this would live.
- **Read the module page** once a likely module turns up. Its table of contents lists every function with a one-line description.
- **Confirm in the REPL** before writing any code. Start `python`, import the module, and try the function on a short list, such as `[505, 498, 512, 640]`. `help()` on the function shows its documentation right in the terminal.

Once found, we'll import it at the top of `src/report.py` with the other imports, and use it inside a dict comprehension over `grouped.items()`, so each service maps to the median of its list.

With `RUN_THROUGH = 7`:

```text
== 7. From the docs ==
{'billing': 305, 'search': 508.5, 'auth': 188}
```

`search` has four durations, so its median — 508.5 — isn't one of the values at all. It's the average of 505 and 512. That's a good test that the function we found really computes a median.

### 8. Busiest Day

To find the busiest weekday, we'll combine the last few sections — parse each start time, format it as a weekday name, and count the names:

```python
def busiest_day(deployments):
    """Return the full weekday name, like "Monday", on which the most deployments started."""
    days = Counter(parse_timestamp(deployment["started"]).strftime("%A") for deployment in deployments)
    return days.most_common(1)[0][0]
```

*`%A` is the full weekday name. `most_common(1)` returns a one-item list like `[('Thursday', 4)]`, so `[0][0]` takes the name out of the first pair.*

With `RUN_THROUGH = 8`:

```text
== 8. Busiest day ==
Thursday
```

### 9. Rollbacks

A list comprehension with a filter collects the rolled-back deployments' start times:

```python
def rollback_times(deployments):
    """Return a list of short_time() strings for every deployment that was rolled back, in order."""
    return [short_time(parse_timestamp(deployment["started"])) for deployment in deployments if deployment["rollback"]]
```

*`deployment["rollback"]` is already a Python `bool`, loaded from JSON's `true` or `false`, so it works directly as the filter.*

With `RUN_THROUGH = 9`:

```text
== 9. Rollbacks ==
['Tue 10 Mar, 10:05', 'Thu 12 Mar, 13:10']
```

### 10. Building the Report

The report is an ordinary dict assembled from the functions above:

```python
def build_report(data):
    """Return the report as a dict, built from the loaded export.

    Keys, in this order:
      "team"           the export's team
      "exported"       the export's timestamp in ISO 8601 format
      "deployments"    how many deployments there were
      "by_status"      status_counts() of the deployments
      "median_seconds" median_seconds() of seconds_by_service()
      "busiest_day"    busiest_day() of the deployments
      "top_authors"    top_authors() of the deployments, top 2
      "rollbacks"      rollback_times() of the deployments
    """
    deployments = data["deployments"]
    return {
        "team": data["team"],
        "exported": parse_timestamp(data["exported"]).isoformat(),
        "deployments": len(deployments),
        "by_status": status_counts(deployments),
        "median_seconds": median_seconds(seconds_by_service(deployments)),
        "busiest_day": busiest_day(deployments),
        "top_authors": top_authors(deployments, 2),
        "rollbacks": rollback_times(deployments),
    }
```

*A dict literal spread over several lines, one key per line. The trailing comma after the last entry is legal and conventional.*

The driver prints the report with `json.dumps(report, indent=2)`, which converts it to a JSON string. With `RUN_THROUGH = 10`:

```text
== 10. Building the report ==
{
  "team": "platform",
  "exported": "2026-03-16T08:00:00",
  "deployments": 12,
  "by_status": {
    "success": 9,
    "failed": 2,
    "cancelled": 1
  },
  "median_seconds": {
    "billing": 305,
    "search": 508.5,
    "auth": 188
  },
  "busiest_day": "Thursday",
  "top_authors": [
    [
      "ada",
      4
    ],
    [
      "grace",
      3
    ]
  ],
  "rollbacks": [
    "Tue 10 Mar, 10:05",
    "Thu 12 Mar, 13:10"
  ]
}
```

A few conversions happened on the way to JSON:

- **The `Counter` became a JSON object**, because a `Counter` is a dict.
- **Each `(author, count)` tuple became a JSON array.** JSON has no tuple type.
- **`True`, `False`, and `None` would become `true`, `false`, and `null`**, though this report doesn't contain any.

It's worth deleting `.isoformat()` from the `"exported"` line once and running again. `json.dumps` fails with `TypeError: Object of type datetime is not JSON serializable` — JSON has no date type, so dates have to become strings first. Then we'll put `.isoformat()` back.

### 11. Writing the Report

`Path` objects can write a whole file in one call:

```python
def write_report(path, report):
    """Write report to the file at path as JSON indented by 2 spaces, ending with a newline.

    Create the file's folder first if it doesn't exist. Use json.dumps() and
    the Path's write_text() method.
    """
    path.parent.mkdir(exist_ok=True)
    path.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
```

*`path.parent` is the `output/` folder, and `exist_ok=True` means creating it is harmless when it already exists. `write_text` opens, writes, and closes the file for us.*

With `RUN_THROUGH = 11`:

```text
== 11. Writing the report ==
written: True
round trip equal: False
[('ada', 4), ('grace', 3)]
[['ada', 4], ['grace', 3]]
```

`output/deploy_report.json` now contains exactly the JSON printed in section 10, followed by a newline.

The driver then reads the file back with `json.loads` and compares it with the report we wrote — and they're not equal. The last two lines hold the evidence, and the **Explain:** prompt at the end of the driver asks us to account for it.

### Checking the Whole File

With `RUN_THROUGH = 11` and every section done, running `python src/report.py` prints:

```text
== 1. Loading JSON ==
<class 'dict'> ['team', 'exported', 'deployments']
<class 'list'> 12
<class 'int'> 312
None False
== 2. Parsing timestamps ==
2026-03-12 13:10:00
<class 'datetime.datetime'>
2026 3 12
== 3. Formatting timestamps ==
Thu 12 Mar, 13:10
2026-03-12T13:10:00
== 4. Counter ==
Counter({'success': 9, 'failed': 2, 'cancelled': 1})
9 0
== 5. most_common ==
[('ada', 4), ('grace', 3)]
== 6. defaultdict ==
{'billing': [312, 290, 301, 330, 305], 'search': [505, 498, 512, 640], 'auth': [188, 201, 176]}
== 7. From the docs ==
{'billing': 305, 'search': 508.5, 'auth': 188}
== 8. Busiest day ==
Thursday
== 9. Rollbacks ==
['Tue 10 Mar, 10:05', 'Thu 12 Mar, 13:10']
== 10. Building the report ==
{
  "team": "platform",
  "exported": "2026-03-16T08:00:00",
  "deployments": 12,
  "by_status": {
    "success": 9,
    "failed": 2,
    "cancelled": 1
  },
  "median_seconds": {
    "billing": 305,
    "search": 508.5,
    "auth": 188
  },
  "busiest_day": "Thursday",
  "top_authors": [
    [
      "ada",
      4
    ],
    [
      "grace",
      3
    ]
  ],
  "rollbacks": [
    "Tue 10 Mar, 10:05",
    "Thu 12 Mar, 13:10"
  ]
}
== 11. Writing the report ==
written: True
round trip equal: False
[('ada', 4), ('grace', 3)]
[['ada', 4], ['grace', 3]]
```

---

## Exercises

`src/report.py` does three different jobs in one file: loading and parsing the export, summarizing it, and running the whole thing as a script. Split it into a **package** so each job has its own module.

Create this structure inside `src/`:

```text
src/
└── deploy_report/
    ├── __init__.py
    ├── loading.py
    ├── summary.py
    └── main.py
```

*A package named `deploy_report`, holding four modules.*

The requirements:

1. **`loading.py`** holds the functions that read and interpret the raw export: `load_export`, `parse_timestamp`, and `short_time`.
2. **`summary.py`** holds the functions from sections 4 through 10. Whatever it needs from `loading.py`, it imports using an absolute import that starts with the package name.
3. **`__init__.py`** makes this import work from outside the package:

   ```python
   from deploy_report import load_export, build_report
   ```

4. **`main.py`** holds the path constants and the code that loads the export, builds the report, and writes it with `write_report` — which belongs in whichever module you think fits best. Its script code must be guarded, so that importing `deploy_report.main` writes nothing and prints nothing. The paths must still point at this lab's `data/` and `output/` folders, even though the file now lives one folder deeper.
5. **When run**, `main.py` prints exactly one line:

   ```text
   wrote deploy_report.json (12 deployments)
   ```

Run the package's entry module from inside `src/`, by module name rather than by file path:

```text
cd src
python -m deploy_report.main
```

When it works, `output/deploy_report.json` must be identical to the file `report.py` wrote in section 11. Once it is, `report.py` is no longer needed — delete it, and confirm that `python -m deploy_report.main` still produces the same file.
