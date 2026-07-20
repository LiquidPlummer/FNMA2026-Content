package com.curriculum.labs;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * An inclusive range of dates — used by exercise 2, which asks you to write
 * its full test class against the behavior spec in the README. As far as we
 * know, this one is correct. Your tests get to confirm that.
 */
public class DateRange {

    private final LocalDate start;
    private final LocalDate end;

    /**
     * @throws IllegalArgumentException if either date is null, or end is before start
     */
    public DateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end are both required");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end must not be before start");
        }
        this.start = start;
        this.end = end;
    }

    /** True if the date falls inside the range — both endpoints count as inside. */
    public boolean contains(LocalDate date) {
        return date != null && !date.isBefore(start) && !date.isAfter(end);
    }

    /** Number of days in the range, endpoints inclusive: a one-day range has length 1. */
    public long lengthInDays() {
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    /** True if the two ranges share at least one day. */
    public boolean overlaps(DateRange other) {
        return !start.isAfter(other.end) && !other.start.isAfter(end);
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }
}
