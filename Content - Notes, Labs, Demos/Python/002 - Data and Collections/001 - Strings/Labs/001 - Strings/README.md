# Strings

We'll turn a messy helpdesk export into a clean, aligned report, one small string function at a time. Along the way we'll cover `strip`, `split`, `replace`, `startswith`, `lower` and `upper`, `in` for substring tests, slicing with negative indexes, f-string format specs for width and alignment, triple-quoted strings, and building a large string with `+=` in a loop before switching to `str.join()`. The exercises finish with extending the normalizer to handle entries it wasn't written for.

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
   python src/strings_lab.py
   ```

The starter runs as-is. Every stub's body is just `pass`, so most lines print `None` for now.

### Project Layout

```text
001 - Strings/
├── README.md
└── src/
    └── strings_lab.py    # raw entries on top, numbered stubs, driver block at the bottom
```

### How the File Is Organized

- **`RAW_ENTRIES`** at the top is the export we're cleaning. Every kind of mess in it is deliberate.
- **The stubs** are numbered 1 through 10. Each docstring is the function's contract.
- **The driver** at the bottom sits under `if __name__ == "__main__":` and has a matching numbered header for each stub. Sections 1–9 test one stub at a time on a small sample; section 10 runs the whole export.

For each number, we'll implement the stub, run the file, and compare that section with the expected output below.

---

## Guided Walkthrough

### 1. Skipping Lines

The export contains a comment line and a blank line that aren't tickets. We'll strip the whitespace first, then test what's left:

```python
def is_ticket_line(raw):
    """Return True if raw holds a ticket, or False if it's blank or a comment.

    Surrounding whitespace doesn't count, so a line of only spaces is blank.
    A comment is a line that starts with "#" once that whitespace is removed.
    """
    line = raw.strip()
    if not line:
        return False
    return not line.startswith("#")
```

*`strip()` returns a new string without leading or trailing whitespace. An empty string is falsy, so `if not line:` catches blank lines.*

Expected output:

```text
== 1. Skipping lines ==
True
False
False
```

The third sample is `"  # exported 2026-03-14"`. Calling `startswith("#")` on the raw string would return `False`, because it starts with spaces — which is why we strip first.

### 2. Splitting a Line

Each ticket has three fields separated by `|`. We'll split on that character and strip each piece:

```python
def split_entry(raw):
    """Return a ticket line's code, name, and summary, each with whitespace stripped.

    Fields are separated by "|". split_entry(" a | b |c ") returns ("a", "b", "c").
    """
    code, name, summary = raw.split("|")
    return code.strip(), name.strip(), summary.strip()
```

*`split("|")` returns a list of three strings, which we unpack into three names. Stripping each field handles the inconsistent spacing around the separators.*

Expected output:

```text
== 2. Splitting a line ==
('tkt-2026-0042', 'ada LOVELACE', 'Billing: card declined twice')
```

The split doesn't care whether there are spaces around `|` or not — `"TKT-2026-0107|grace hopper|..."` splits just as cleanly.

### 3. Slicing the Code

Codes arrive in mixed case, and one uses underscores. We'll normalize them first:

```python
def clean_code(code):
    """Return code in uppercase, with any "_" separators replaced by "-".

    clean_code("tkt_2026_0150") returns "TKT-2026-0150".
    """
    return code.upper().replace("_", "-")
```

*Each method returns a new string, so the calls chain: `upper()` produces an uppercase copy, and `replace()` is called on that copy.*

A clean code has a fixed layout — `TKT-2026-0150` — so slices can pull out its parts. The year always sits at positions 4 through 7:

```python
def code_year(code):
    """Return the four-digit year from a clean code like "TKT-2026-0150"."""
    return code[4:8]
```

*The stop index is excluded, so `[4:8]` returns the characters at indexes 4, 5, 6, and 7.*

For the ticket number, we'll count from the end instead:

```python
def code_number(code):
    """Return the last four characters of a clean code, counting from the end."""
    return code[-4:]
```

*`-4` is the fourth character from the end, and omitting the stop means "to the end of the string."*

Expected output:

```text
== 3. Slicing the code ==
TKT-2026-0150
2026
0150
```

The negative index makes `code_number` independent of how long the prefix is. `"REQ-2026-0150"[-4:]` and `"INCIDENT-2026-0150"[-4:]` both return `"0150"`.

### 4. Fixing Capitals

We'll capitalize a word by slicing it into its first character and everything else:

```python
def capitalize(word):
    """Return word with its first letter uppercase and the rest lowercase.

    capitalize("lOVELACE") returns "Lovelace". Build it from slices; don't use
    the str.capitalize() or str.title() methods.
    """
    return word[0].upper() + word[1:].lower()
```

*`word[0]` is a one-character string — Python has no `char` type — so it has an `upper()` method like any other string.*

Then we'll use it to format a whole name:

```python
def format_name(name):
    """Return "LAST, First" from a "first last" name in any capitalization.

    The two words may be separated by more than one space.
    format_name("ada   LOVELACE") returns "LOVELACE, Ada".
    """
    first, last = name.split()
    return f"{last.upper()}, {capitalize(first)}"
```

*With no argument, `split()` splits on any run of whitespace and ignores the extra spaces.*

Expected output:

```text
== 4. Fixing capitals ==
Lovelace
LOVELACE, Ada
```

It's worth trying `name.split(" ")` once instead. On `"ada   LOVELACE"`, it returns `['ada', '', '', 'LOVELACE']` — an empty string for every extra space — and the unpacking into two names fails.

### 5. Urgent Tags

Urgent tickets end with `[urgent]`, typed in whatever case the submitter chose. Lowercasing the summary before testing handles every variation:

```python
def is_urgent(summary):
    """Return True if summary contains URGENT_TAG, in any capitalization."""
    return URGENT_TAG in summary.lower()
```

*`in` tests whether one string appears anywhere inside another. `URGENT_TAG` is already lowercase, so comparing against a lowercased summary ignores case.*

To remove the tag, we can't use `replace("[urgent]", "")` — it would miss `[URGENT]`. Since the tag is always last, we'll slice it off by length instead:

```python
def remove_urgent_tag(summary):
    """Return summary with its urgent tag removed and whitespace stripped.

    When the tag is present, it's always the last thing in the summary.
    Summaries without the tag come back unchanged apart from stripping.
    """
    if is_urgent(summary):
        summary = summary[:-len(URGENT_TAG)]
    return summary.strip()
```

*`[:-8]` means "everything except the last eight characters," and `len(URGENT_TAG)` is 8. The slice doesn't care what case those characters are in.*

Expected output:

```text
== 5. Urgent tags ==
True
False
|outage: api down|
```

The bars in the last line show that the space before the tag was stripped too.

### 6. Category and Detail

Summaries have a category and a detail separated by a colon:

```python
def split_summary(summary):
    """Return (CATEGORY, detail) from a summary like "category: detail".

    The category comes back uppercase, and both parts are stripped.
    split_summary("login:  reset loop") returns ("LOGIN", "reset loop").
    """
    category, detail = summary.split(":")
    return category.strip().upper(), detail.strip()
```

*The same split-unpack-strip pattern as section 2, with the category also uppercased.*

Expected output:

```text
== 6. Category and detail ==
('LOGIN', 'password reset loop')
```

### 7. Aligned Rows

Now we'll lay out a row. Each field gets a **format spec** after a colon: `<` left-aligns, `>` right-aligns, `^` centers, and the number is the width to pad to:

```python
def render_row(flag, ticket, year, name, category, detail):
    """Return one report row as a single string, using f-string format specs.

    Left to right: flag, then one space; ticket left-aligned in 7 characters;
    year right-aligned in 4 characters, then two spaces; name left-aligned in
    20 characters; category centered in 10 characters; detail as it is.
    """
    return f"{flag} {ticket:<7}{year:>4}  {name:<20}{category:^10}{detail}"
```

*Everything between the braces is either a padded field or literal text. The spaces written outside the braces are part of the output.*

Expected output:

```text
== 7. Aligned rows ==
  TICKET YEAR  NAME                 CATEGORY DETAIL
! #0107  2026  HOPPER, Grace         LOGIN   password reset loop
```

Both rows were padded to the same widths, so their columns line up even though the values have different lengths. `CATEGORY` fills 8 of its 10 characters, leaving one space on each side; `LOGIN` fills 5, leaving 2 on the left and 3 on the right.

### 8. Report Header

A **triple-quoted string** can span lines, and adding an `f` prefix makes it an f-string too. We'll build the two-line header as one literal:

```python
def report_header(title, subtitle):
    """Return a two-line header built from one triple-quoted f-string.

    Line 1: title with one space on each side, centered in 64 characters
    and padded with "=" instead of spaces.
    Line 2: subtitle, right-aligned in 64 characters.
    """
    return f"""{' ' + title + ' ':=^64}
{subtitle:>64}"""
```

*A character placed before the alignment symbol becomes the fill: `=^64` centers in 64 characters using `=` instead of spaces. The second line starts at the left margin, because indentation inside a triple-quoted string would become part of the string.*

Expected output:

```text
== 8. Report header ==
======================= Helpdesk Tickets =======================
                                             exported 2026-03-14
```

Inside the braces, `' ' + title + ' '` is an ordinary expression, and the format spec applies to its result. It uses single quotes so they can't be confused with the double quotes that delimit the f-string.

### 9. Building the Report

We'll join rows into one string twice, to compare approaches. First, with `+=` in a loop:

```python
def build_report_concat(rows):
    """Return rows as one string with a newline between rows, using += in a loop.

    There's no newline before the first row or after the last one.
    """
    report = ""
    for row in rows:
        if report:
            report += "\n"
        report += row
    return report
```

*A newline goes *between* rows, so every row except the first needs one added first. An empty `report` is falsy, which tells us we're on the first row.*

Now the same result with `join`:

```python
def build_report_join(rows):
    """Return the same string as build_report_concat, built with str.join()."""
    return "\n".join(rows)
```

*`join` is called on the separator and places it between each pair of items — never before the first or after the last.*

Expected output:

```text
== 9. Building the report ==
first row
second row
third row
same result: True
```

Before moving on, we'll answer both **Explain:** prompts in the driver.

> **Contrast — Java:** a Java `String` is immutable too, and the fix for concatenating in a loop there is `StringBuilder`. Python has no builder class. Collecting pieces and calling `"\n".join(...)` once does that job.

### 10. The Full Report

Finally, we'll connect every function in one loop. `build_rows` starts with the column header row, then appends one rendered row per ticket:

```python
def build_rows(raw_entries):
    """Return a list of rendered report rows: a column header row, then one row per ticket.

    The header row is render_row(" ", "TICKET", "YEAR", "NAME", "CATEGORY", "DETAIL").
    Skip blank and comment lines. Clean each ticket with the functions above,
    show its ticket as "#" plus its number, and flag it "!" if it was urgent
    (or " " if not).
    """
    rows = [render_row(" ", "TICKET", "YEAR", "NAME", "CATEGORY", "DETAIL")]
    for raw in raw_entries:
        if not is_ticket_line(raw):
            continue
        code, name, summary = split_entry(raw)
        code = clean_code(code)
        flag = "!" if is_urgent(summary) else " "
        category, detail = split_summary(remove_urgent_tag(summary))
        rows.append(render_row(flag, "#" + code_number(code), code_year(code),
                               format_name(name), category, detail))
    return rows
```

*`is_urgent` runs on the original summary, before `remove_urgent_tag` takes the tag away. The `render_row` call continues onto a second line, which Python allows inside parentheses.*

Expected output:

```text
== 10. The full report ==
======================= Helpdesk Tickets =======================
                                             exported 2026-03-14
  TICKET YEAR  NAME                 CATEGORY DETAIL
  #0042  2026  LOVELACE, Ada        BILLING  card declined twice
! #0107  2026  HOPPER, Grace         LOGIN   password reset loop
  #0993  2025  TORVALDS, Linus        BUG    panic on boot
! #0150  2026  HAMILTON, Margaret    OUTAGE  api down
```

It's worth comparing this against `RAW_ENTRIES` line by line. Every stray space, capital letter, underscore, and tag has been normalized, and the comment and blank lines are gone.

### Checking the Whole File

With all ten sections done, running `python src/strings_lab.py` prints:

```text
== 1. Skipping lines ==
True
False
False
== 2. Splitting a line ==
('tkt-2026-0042', 'ada LOVELACE', 'Billing: card declined twice')
== 3. Slicing the code ==
TKT-2026-0150
2026
0150
== 4. Fixing capitals ==
Lovelace
LOVELACE, Ada
== 5. Urgent tags ==
True
False
|outage: api down|
== 6. Category and detail ==
('LOGIN', 'password reset loop')
== 7. Aligned rows ==
  TICKET YEAR  NAME                 CATEGORY DETAIL
! #0107  2026  HOPPER, Grace         LOGIN   password reset loop
== 8. Report header ==
======================= Helpdesk Tickets =======================
                                             exported 2026-03-14
== 9. Building the report ==
first row
second row
third row
same result: True
== 10. The full report ==
======================= Helpdesk Tickets =======================
                                             exported 2026-03-14
  TICKET YEAR  NAME                 CATEGORY DETAIL
  #0042  2026  LOVELACE, Ada        BILLING  card declined twice
! #0107  2026  HOPPER, Grace         LOGIN   password reset loop
  #0993  2025  TORVALDS, Linus        BUG    panic on boot
! #0150  2026  HAMILTON, Margaret    OUTAGE  api down
```

---

## Exercises

Three more entries have arrived, and each breaks the normalizer in a different way. Add all three to the end of `RAW_ENTRIES`:

```python
    "TKT - 2026 - 0233 | alan TURING | access: vpn token expired",
    "TKT-2026-0240 | Liskov, BARBARA | login: locked out again [Urgent]",
    "TKT-2026-0251 | katherine johnson | printer on floor 3 is jammed",
```

Run the file and watch it fail. Then change the existing functions — without special-casing these particular tickets — so the full report handles every entry, old and new:

1. **Spaces in the code.** Some codes have spaces around their separators. A clean code must still come out as `TKT-2026-0233`, so its year and number slice correctly.
2. **Names already in "Last, First" order.** A name containing a comma is already last-name-first, though its capitalization still needs fixing. Names without a comma must keep working as before.
3. **Summaries with no category.** A summary without a colon has no category. Its category should be `GENERAL`, and the entire summary becomes the detail.

When all three work, section 10 should end with these rows after the original four:

```text
  #0233  2026  TURING, Alan          ACCESS  vpn token expired
! #0240  2026  LISKOV, Barbara       LOGIN   locked out again
  #0251  2026  JOHNSON, Katherine   GENERAL  printer on floor 3 is jammed
```

Finally, confirm that sections 1 through 9 still print exactly what they did before your changes.
