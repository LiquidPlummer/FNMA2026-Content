package com.curriculum.labs;

/**
 * The specimen for Acts 1 and 2. Provided — don't edit it; the point of the
 * lab is that our reflective code works on this class WITHOUT special access.
 *
 * Note what's here on purpose: private fields, a private helper method,
 * a static field, and an interface — the walkthrough interrogates all of it.
 */
public class BankAccount implements Comparable<BankAccount> {

    private static int accountsOpened = 0;

    private final String owner;
    private double balance;
    private boolean frozen;

    public BankAccount(String owner, double openingBalance) {
        this.owner = owner;
        this.balance = openingBalance;
        accountsOpened++;
    }

    public void deposit(double amount) {
        requirePositive(amount);
        balance += amount;
    }

    public void withdraw(double amount) {
        requirePositive(amount);
        if (amount > balance) {
            throw new IllegalArgumentException("insufficient funds");
        }
        balance -= amount;
    }

    public double getBalance() {
        return balance;
    }

    public String getOwner() {
        return owner;
    }

    private void requirePositive(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    @Override
    public int compareTo(BankAccount other) {
        return Double.compare(this.balance, other.balance);
    }

    @Override
    public String toString() {
        return "BankAccount[owner=" + owner + ", balance=" + balance + "]";
    }
}
