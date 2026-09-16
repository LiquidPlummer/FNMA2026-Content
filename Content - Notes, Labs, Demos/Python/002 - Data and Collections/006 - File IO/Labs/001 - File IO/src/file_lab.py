# File I/O Lab
#
# data/timesheet.txt holds one week of hours. Its first line names the week,
# and after that each entry is "name, day, hours" — padded with extra spaces,
# with a blank line and a comment line mixed in.
#
# The numbered stubs read that file in several ways, turn its entries into
# per-person totals, write a summary file into output/, and append to a log.
# The driver block at the bottom calls each stub and prints the results.
#
# Work through the numbers in order: implement the stub, run the file, and
# compare that section's output with the expected output in the README.
# Run it from the lab folder (the one containing README.md):
#
#     python src/file_lab.py

from pathlib import Path

# Paths are built from this file's own location, so the lab works no matter
# which directory it's run from.
LAB_DIR = Path(__file__).parent.parent
DATA_DIR = LAB_DIR / "data"
OUTPUT_DIR = LAB_DIR / "output"

TIMESHEET_PATH = DATA_DIR / "timesheet.txt"
SUMMARY_PATH = OUTPUT_DIR / "hours_summary.txt"
LOG_PATH = OUTPUT_DIR / "run_log.txt"


# 2. Reading everything
def read_whole(path):
    """Return the entire contents of the text file at path as one string.

    Open it with a with block, in text mode, with an explicit UTF-8 encoding.
    """
    return ""  # TODO: implement


# 3. readlines()
def read_lines_list(path):
    """Return a list of every line in the file at path, using readlines()."""
    return []  # TODO: implement


# 4. Iterating the file
def is_entry(line):
    """Return True if line holds an entry, or False if it's blank or a comment.

    A comment starts with "#" once surrounding whitespace is removed.
    """
    return False  # TODO: implement


def count_entries(path):
    """Return how many entry lines the file at path holds.

    Loop over the file object itself, one line at a time.
    """
    return 0  # TODO: implement


# 5. Parsing a line
def parse_line(line):
    """Return (name, day, hours) from a line like "Ada Lovelace  , mon , 7.5".

    name and day are stripped strings; hours is a float.
    """
    return ("", "", 0.0)  # TODO: implement


# 6. Reading entries
def read_entries(path):
    """Return a list of (name, day, hours) tuples, one per entry line in the file.

    Skip blank and comment lines.
    """
    return []  # TODO: implement


# 7. The week label
def week_label(path):
    """Return the week label from the file's first line, using readline().

    The first line looks like "# week of 2026-03-09"; return "week of 2026-03-09".
    """
    return ""  # TODO: implement


# 8. Totals
def total_hours(entries):
    """Return a dict mapping each name in entries to that person's total hours."""
    return {}  # TODO: implement


# 9. Summary lines
def summary_lines(week, totals):
    """Return the summary file's lines as a list of strings with no newlines.

    First line: "Hours for <week>".
    Then one line per person, sorted by name: the name left-aligned in 18
    characters, then the total right-aligned in 6 characters with 2 decimals.
    Last line: "Total" formatted the same way, with the sum of every total.
    """
    return []  # TODO: implement


# 10. Writing the summary
def write_lines(path, lines):
    """Write lines to the file at path, one per line, replacing any existing file.

    Use writelines(). Each line in the file must end with a newline.
    """
    pass  # TODO: implement


# 11. Appending to a log
def log_run(path, message):
    """Add message as a new line at the end of the file at path, keeping what's already there."""
    pass  # TODO: implement


if __name__ == "__main__":
    print("== 1. Paths ==")
    # TODO: replace each None with an attribute or method of TIMESHEET_PATH:
    #   - its file name
    #   - its file name without the extension
    #   - its extension
    #   - the name of the folder it's in
    #   - whether it exists
    print("name:", None)
    print("stem:", None)
    print("suffix:", None)
    print("folder:", None)
    print("exists:", None)
    print("missing exists:", (DATA_DIR / "missing.txt").exists())

    print("== 2. Reading everything ==")
    text = read_whole(TIMESHEET_PATH)
    print("characters:", len(text))
    print("first line:", text.split("\n")[0])

    print("== 3. readlines() ==")
    lines = read_lines_list(TIMESHEET_PATH)
    print("lines:", len(lines))
    print(lines[:2])
    # Explain: what's the "\n" at the end of each item, and where did it come from?

    print("== 4. Iterating the file ==")
    print("entries:", count_entries(TIMESHEET_PATH))

    print("== 5. Parsing a line ==")
    print(parse_line("Grace Hopper    , wed , 7.75\n"))

    print("== 6. Reading entries ==")
    entries = read_entries(TIMESHEET_PATH)
    print("entries:", len(entries))
    print(entries[:2])

    print("== 7. The week label ==")
    print(week_label(TIMESHEET_PATH))

    print("== 8. Totals ==")
    totals = total_hours(entries)
    print(totals)

    print("== 9. Summary lines ==")
    summary = summary_lines(week_label(TIMESHEET_PATH), totals)
    for line in summary:
        print(line)

    print("== 10. Writing the summary ==")
    OUTPUT_DIR.mkdir(exist_ok=True)
    write_lines(SUMMARY_PATH, summary)
    print("written:", SUMMARY_PATH.exists())
    if SUMMARY_PATH.exists():
        print(read_whole(SUMMARY_PATH), end="")

    print("== 11. Appending to a log ==")
    with open(LOG_PATH, "w", encoding="utf-8") as log:
        log.write("run log\n")
    log_run(LOG_PATH, f"wrote {SUMMARY_PATH.name}")
    log_run(LOG_PATH, f"{len(entries)} entries summarized")
    print(read_whole(LOG_PATH), end="")
