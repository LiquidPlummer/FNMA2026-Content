# The Standard Library Lab
#
# data/deployments.json is a week of deployments exported from a build
# system. The numbered stubs load it, summarize it, and write a report file
# into output/ — using nothing but the standard library.
#
# Each stub builds on the ones before it, so the driver runs one section at a
# time. After finishing a section, raise RUN_THROUGH at the bottom of this
# file to that section's number, run the file, and compare with the README.
# Run it from the lab folder (the one containing README.md):
#
#     python src/report.py

import json
from collections import Counter, defaultdict
from datetime import datetime
from pathlib import Path

LAB_DIR = Path(__file__).parent.parent
EXPORT_PATH = LAB_DIR / "data" / "deployments.json"
REPORT_PATH = LAB_DIR / "output" / "deploy_report.json"


# 1. Loading JSON
def load_export(path):
    """Return the Python object stored in the JSON file at path, using json.load()."""
    pass  # TODO: implement


# 2. Parsing timestamps
def parse_timestamp(text):
    """Return a datetime parsed from text in the export's format, like "2026-03-12 13:10"."""
    pass  # TODO: implement with datetime.strptime()


# 3. Formatting timestamps
def short_time(moment):
    """Return moment formatted like "Thu 12 Mar, 13:10"."""
    pass  # TODO: implement with strftime()


# 4. Counter
def status_counts(deployments):
    """Return a Counter of how many deployments ended with each status."""
    pass  # TODO: implement


# 5. most_common
def top_authors(deployments, n):
    """Return the n authors with the most deployments, as (author, count) pairs, most first."""
    pass  # TODO: implement


# 6. defaultdict
def seconds_by_service(deployments):
    """Return a defaultdict mapping each service to a list of its deployments' seconds, in order."""
    pass  # TODO: implement


# 7. From the docs
def median_seconds(grouped):
    """Return a dict mapping each service in grouped to the median of its seconds.

    The median is the middle value once the values are sorted — or, for an
    even number of values, the average of the two middle ones. Don't write
    that calculation yourself: the standard library already has a function
    for it. Finding it, and importing it, is part of the task.
    """
    pass  # TODO: implement


# 8. Busiest day
def busiest_day(deployments):
    """Return the full weekday name, like "Monday", on which the most deployments started."""
    pass  # TODO: implement


# 9. Rollbacks
def rollback_times(deployments):
    """Return a list of short_time() strings for every deployment that was rolled back, in order."""
    pass  # TODO: implement


# 10. Building the report
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
    pass  # TODO: implement


# 11. Writing the report
def write_report(path, report):
    """Write report to the file at path as JSON indented by 2 spaces, ending with a newline.

    Create the file's folder first if it doesn't exist. Use json.dumps() and
    the Path's write_text() method.
    """
    pass  # TODO: implement


if __name__ == "__main__":
    # Raise this after finishing each section. The driver runs every section
    # up to and including this number.
    RUN_THROUGH = 0

    if RUN_THROUGH == 0:
        print("Nothing to run yet: finish section 1, then set RUN_THROUGH = 1.")

    if RUN_THROUGH >= 1:
        print("== 1. Loading JSON ==")
        data = load_export(EXPORT_PATH)
        deployments = data["deployments"]
        print(type(data), list(data))
        print(type(deployments), len(deployments))
        first = deployments[0]
        second = deployments[1]
        print(type(first["seconds"]), first["seconds"])
        print(second["ticket"], first["rollback"])

    if RUN_THROUGH >= 2:
        print("== 2. Parsing timestamps ==")
        moment = parse_timestamp("2026-03-12 13:10")
        print(moment)
        print(type(moment))
        print(moment.year, moment.month, moment.day)

    if RUN_THROUGH >= 3:
        print("== 3. Formatting timestamps ==")
        print(short_time(moment))
        print(moment.isoformat())

    if RUN_THROUGH >= 4:
        print("== 4. Counter ==")
        counts = status_counts(deployments)
        print(counts)
        print(counts["success"], counts["skipped"])

    if RUN_THROUGH >= 5:
        print("== 5. most_common ==")
        print(top_authors(deployments, 2))

    if RUN_THROUGH >= 6:
        print("== 6. defaultdict ==")
        grouped = seconds_by_service(deployments)
        print(dict(grouped))

    if RUN_THROUGH >= 7:
        print("== 7. From the docs ==")
        print(median_seconds(grouped))

    if RUN_THROUGH >= 8:
        print("== 8. Busiest day ==")
        print(busiest_day(deployments))

    if RUN_THROUGH >= 9:
        print("== 9. Rollbacks ==")
        print(rollback_times(deployments))

    if RUN_THROUGH >= 10:
        print("== 10. Building the report ==")
        report = build_report(data)
        print(json.dumps(report, indent=2))

    if RUN_THROUGH >= 11:
        print("== 11. Writing the report ==")
        write_report(REPORT_PATH, report)
        print("written:", REPORT_PATH.exists())
        loaded = json.loads(REPORT_PATH.read_text(encoding="utf-8"))
        print("round trip equal:", loaded == report)
        print(report["top_authors"])
        print(loaded["top_authors"])
        # Explain: why isn't the report read back from the file equal to the
        # report that was written?
