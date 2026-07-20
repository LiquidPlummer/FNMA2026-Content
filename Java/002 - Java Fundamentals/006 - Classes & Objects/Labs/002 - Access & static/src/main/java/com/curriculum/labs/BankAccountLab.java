package com.curriculum.labs;

/**
 * Driver for the Bank Account Lab, continuing from "First Class." Follow
 * the README — each part points at one of the numbered markers below.
 */
public class BankAccountLab {

    public static void main(String[] args) {
        System.out.println("=== Bank Account Lab ===");

        BankAccount checking = new BankAccount("Ada", 100.0);
        BankAccount savings = new BankAccount("Ada", 5000.0);

        // [MARKER 6] Part 1: these lines touch fields directly — fix them once MARKER 1 is private.
        checking.balance += 50.0;
        System.out.println(checking.owner + " checking: $" + checking.balance);
        System.out.println(savings.owner + " savings: $" + savings.balance);

        try {
            BankAccount bad = new BankAccount("Ada", -1000.0);
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }

        BankAccount fresh = new BankAccount("Grace");
        System.out.println(fresh.owner + " opened with $" + fresh.balance);   // [MARKER 6] this one too

        System.out.println(checking.getOwner() + " checking: $" + checking.getBalance());
        System.out.println(savings.getOwner() + " savings: $" + savings.getBalance());

        checking.setOwner("Ada Lovelace");
        System.out.println("Renamed to: " + checking.getOwner());

        checking.deposit(50.0);
        checking.withdraw(30.0);
        System.out.println(checking.getOwner() + " checking: $" + checking.getBalance());

        try {
            checking.withdraw(1_000_000.0);
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }

        System.out.println(checking);

        // [MARKER 7] Part 1: this manual correction won't even compile once fields are private — delete it.
        checking.balance = checking.balance - 10000.0;
        System.out.println(checking);

        // [MARKER 8] Part 2: print the account counter and each account's generated id.

        // [MARKER 9] Part 4: isValidId(...) and auditLabel() demonstration.

        System.out.println("Done.");
    }
}
