package com.curriculum.labs;

/**
 * BankAccount — carried forward from the "First Class" lab, now getting
 * properly encapsulated: private fields, a static id counter, constants,
 * and a static utility method.
 *
 * Follow the README's guided walkthrough. Each part points at one of the
 * numbered markers below.
 */
public class BankAccount {

    // [MARKER 1] Part 1: lock these down — change `public` to `private`.
    public String owner;
    public double balance;

    // [MARKER 2] Part 2: a static counter and a per-instance id go here.

    // [MARKER 3] Part 3: static final constants go here.

    public BankAccount(String owner, double openingBalance) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("owner is required");
        }
        if (openingBalance < 0) {
            throw new IllegalArgumentException("opening balance cannot be negative");
        }
        this.owner = owner;
        this.balance = openingBalance;
    }

    public BankAccount(String owner) {
        this(owner, 0.0);
    }

    public String getOwner() {
        return owner;
    }

    public double getBalance() {
        return balance;
    }

    public void setOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("owner is required");
        }
        this.owner = owner;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("deposit amount must be positive");
        }
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("withdrawal amount must be positive");
        }
        if (amount > balance) {
            throw new IllegalArgumentException("insufficient funds");
        }
        balance -= amount;
    }

    // [MARKER 4] Part 4: isValidId(String) — a static utility — goes here.

    // [MARKER 5] Part 4: auditLabel() — an instance method — goes here.

    @Override
    public String toString() {
        return owner + ": $" + balance;
    }
}
