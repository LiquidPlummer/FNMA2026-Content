package com.curriculum.labs;

/**
 * The target for Act 3. Provided WITHOUT annotations — you'll add @Audited
 * to the methods the walkthrough calls for, once the annotation exists.
 *
 * All the business logic is fake; what matters is the method shapes:
 * public, no-arg, some worth auditing and some not.
 */
public class PayrollService {

    private int runsCompleted = 0;

    public int runPayroll() {
        runsCompleted++;
        return 42;                       // "42 employees paid"
    }

    public String previewPayroll() {
        return "42 employees, $187,300.00 total (preview only)";
    }

    public String exportTaxReport() {
        return "tax-report-2026-Q3.csv";
    }

    private String closeQuarter() {
        return "quarter closed after " + runsCompleted + " payroll run(s)";
    }
}
