package com.curriculum.labs;

import java.util.List;

/**
 * The data every exporter consumes: a title and rows of columns.
 * Provided so the lab can focus on the contracts, not the data plumbing.
 */
public class ReportData {
    private final String title;
    private final List<String[]> rows;

    public ReportData(String title, List<String[]> rows) {
        this.title = title;
        this.rows = rows;
    }

    public String getTitle() { return title; }
    public List<String[]> getRows() { return List.copyOf(rows); }

    public static ReportData sample() {
        return new ReportData("Q3 Unit Sales", List.of(
                new String[] {"widget", "120"},
                new String[] {"gadget", "75"},
                new String[] {"gizmo", "301"}
        ));
    }
}
