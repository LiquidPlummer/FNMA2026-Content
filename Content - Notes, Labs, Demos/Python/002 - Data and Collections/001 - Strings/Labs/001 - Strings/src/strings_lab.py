# Strings Lab
#
# RAW_ENTRIES below is a helpdesk export pasted in from another system. It's
# messy: stray whitespace, random capitalization, a comment line, a blank
# line, inconsistent separators, and urgent tags typed however people felt
# like typing them.
#
# The numbered stubs clean one piece of an entry at a time. The driver block
# at the bottom calls each stub and prints the results, ending with the full
# aligned report.
#
# Work through the numbers in order: implement the stub, run the file, and
# compare that section's output with the expected output in the README:
#
#     python src/strings_lab.py

RAW_ENTRIES = [
    "# helpdesk export 2026-03-14",
    "  tkt-2026-0042 | ada LOVELACE | Billing: card declined twice  ",
    "TKT-2026-0107|grace hopper|login:   password reset loop [URGENT]",
    "",
    "  TKT-2025-0993 | linus   torvalds | BUG: panic on boot   ",
    "TKT_2026_0150 |  Margaret HAMILTON| outage: api down [urgent] ",
]

URGENT_TAG = "[urgent]"


# 1. Skipping lines
def is_ticket_line(raw):
    """Return True if raw holds a ticket, or False if it's blank or a comment.

    Surrounding whitespace doesn't count, so a line of only spaces is blank.
    A comment is a line that starts with "#" once that whitespace is removed.
    """
    pass  # TODO: implement


# 2. Splitting a line
def split_entry(raw):
    """Return a ticket line's code, name, and summary, each with whitespace stripped.

    Fields are separated by "|". split_entry(" a | b |c ") returns ("a", "b", "c").
    """
    pass  # TODO: implement


# 3. Slicing the code
def clean_code(code):
    """Return code in uppercase, with any "_" separators replaced by "-".

    clean_code("tkt_2026_0150") returns "TKT-2026-0150".
    """
    pass  # TODO: implement


def code_year(code):
    """Return the four-digit year from a clean code like "TKT-2026-0150"."""
    pass  # TODO: implement with a slice


def code_number(code):
    """Return the last four characters of a clean code, counting from the end."""
    pass  # TODO: implement with a slice that uses a negative index


# 4. Fixing capitals
def capitalize(word):
    """Return word with its first letter uppercase and the rest lowercase.

    capitalize("lOVELACE") returns "Lovelace". Build it from slices; don't use
    the str.capitalize() or str.title() methods.
    """
    pass  # TODO: implement


def format_name(name):
    """Return "LAST, First" from a "first last" name in any capitalization.

    The two words may be separated by more than one space.
    format_name("ada   LOVELACE") returns "LOVELACE, Ada".
    """
    pass  # TODO: implement, using capitalize()


# 5. Urgent tags
def is_urgent(summary):
    """Return True if summary contains URGENT_TAG, in any capitalization."""
    pass  # TODO: implement


def remove_urgent_tag(summary):
    """Return summary with its urgent tag removed and whitespace stripped.

    When the tag is present, it's always the last thing in the summary.
    Summaries without the tag come back unchanged apart from stripping.
    """
    pass  # TODO: implement with a slice that uses a negative index


# 6. Category and detail
def split_summary(summary):
    """Return (CATEGORY, detail) from a summary like "category: detail".

    The category comes back uppercase, and both parts are stripped.
    split_summary("login:  reset loop") returns ("LOGIN", "reset loop").
    """
    pass  # TODO: implement


# 7. Aligned rows
def render_row(flag, ticket, year, name, category, detail):
    """Return one report row as a single string, using f-string format specs.

    Left to right: flag, then one space; ticket left-aligned in 7 characters;
    year right-aligned in 4 characters, then two spaces; name left-aligned in
    20 characters; category centered in 10 characters; detail as it is.
    """
    pass  # TODO: implement


# 8. Report header
def report_header(title, subtitle):
    """Return a two-line header built from one triple-quoted f-string.

    Line 1: title with one space on each side, centered in 64 characters
    and padded with "=" instead of spaces.
    Line 2: subtitle, right-aligned in 64 characters.
    """
    pass  # TODO: implement


# 9. Building the report
def build_report_concat(rows):
    """Return rows as one string with a newline between rows, using += in a loop.

    There's no newline before the first row or after the last one.
    """
    pass  # TODO: implement


def build_report_join(rows):
    """Return the same string as build_report_concat, built with str.join()."""
    pass  # TODO: implement


# 10. The full report
def build_rows(raw_entries):
    """Return a list of rendered report rows: a column header row, then one row per ticket.

    The header row is render_row(" ", "TICKET", "YEAR", "NAME", "CATEGORY", "DETAIL").
    Skip blank and comment lines. Clean each ticket with the functions above,
    show its ticket as "#" plus its number, and flag it "!" if it was urgent
    (or " " if not).
    """
    pass  # TODO: implement


if __name__ == "__main__":
    print("== 1. Skipping lines ==")
    print(is_ticket_line("  TKT-2026-0042 | ada LOVELACE | billing: declined"))
    print(is_ticket_line("    "))
    print(is_ticket_line("  # exported 2026-03-14"))

    print("== 2. Splitting a line ==")
    print(split_entry("  tkt-2026-0042 | ada LOVELACE | Billing: card declined twice  "))

    print("== 3. Slicing the code ==")
    code = clean_code("tkt_2026_0150")
    print(code)
    print(code_year("TKT-2026-0150"))
    print(code_number("TKT-2026-0150"))

    print("== 4. Fixing capitals ==")
    print(capitalize("lOVELACE"))
    print(format_name("ada   LOVELACE"))

    print("== 5. Urgent tags ==")
    print(is_urgent("outage: api down [URGENT]"))
    print(is_urgent("billing: card declined"))
    # The bars show exactly where the returned string starts and ends.
    print(f"|{remove_urgent_tag('outage: api down [urgent]')}|")

    print("== 6. Category and detail ==")
    print(split_summary("login:   password reset loop"))

    print("== 7. Aligned rows ==")
    print(render_row(" ", "TICKET", "YEAR", "NAME", "CATEGORY", "DETAIL"))
    print(render_row("!", "#0107", "2026", "HOPPER, Grace", "LOGIN", "password reset loop"))

    print("== 8. Report header ==")
    print(report_header("Helpdesk Tickets", "exported 2026-03-14"))

    print("== 9. Building the report ==")
    sample_rows = ["first row", "second row", "third row"]
    by_concat = build_report_concat(sample_rows)
    by_join = build_report_join(sample_rows)
    print(by_concat)
    print("same result:", by_concat == by_join)
    # Explain: build_report_concat needs extra logic to avoid a stray newline.
    # Why doesn't build_report_join?
    #
    # Explain: why is join the better choice when there are thousands of rows?

    print("== 10. The full report ==")
    rows = build_rows(RAW_ENTRIES)
    if rows is None:
        print("(build_rows is not implemented yet)")
    else:
        print(report_header("Helpdesk Tickets", "exported 2026-03-14"))
        print(build_report_join(rows))
