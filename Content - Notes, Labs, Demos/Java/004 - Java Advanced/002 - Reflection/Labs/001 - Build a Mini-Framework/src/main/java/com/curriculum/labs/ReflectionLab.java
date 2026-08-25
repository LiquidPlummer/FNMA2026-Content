package com.curriculum.labs;

/**
 * Reflection Lab — interrogate a class, serialize any object, then build a
 * working annotation framework by hand. Three acts.
 *
 * Follow the README's guided walkthrough. Each act is a static method below,
 * selected by a command-line argument:
 *
 *     mvn -q compile exec:java -Dexec.args="1"     (runs act1)
 *
 * The numbered [MARKER] comments show where each act's code goes.
 */
public class ReflectionLab {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: mvn -q compile exec:java -Dexec.args=\"<act>\"  where <act> is 1-3");
            return;
        }
        switch (args[0]) {
            case "1" -> act1();
            case "2" -> act2();
            case "3" -> act3();
            default -> System.out.println("Unknown act: " + args[0]);
        }
    }

    // ------------------------------------------------------------------
    // Act 1 — interrogating a class
    // ------------------------------------------------------------------
    static void act1() throws Exception {
        System.out.println("=== Act 1: the class report ===");

        // [MARKER 1a] Three roads to the Class object go here.

        // [MARKER 1b] The classReport(...) call goes here once the method exists.
    }

    // [MARKER 1c] The classReport(Class<?> c) method goes here.

    // ------------------------------------------------------------------
    // Act 2 — the universal serializer
    // ------------------------------------------------------------------
    static void act2() throws Exception {
        System.out.println("=== Act 2: toMap ===");

        // [MARKER 2b] Serialize a BankAccount (and anything else) here.
    }

    // [MARKER 2a] The toMap(Object obj) method goes here.

    // ------------------------------------------------------------------
    // Act 3 — the annotation framework
    // ------------------------------------------------------------------
    static void act3() throws Exception {
        System.out.println("=== Act 3: the @Audited runner ===");

        // [MARKER 3b] Run the auditor against a PayrollService here.
    }

    // [MARKER 3a] The runAudited(Object target) method goes here.
    //             (The @Audited annotation itself gets its own file: Audited.java)
}
