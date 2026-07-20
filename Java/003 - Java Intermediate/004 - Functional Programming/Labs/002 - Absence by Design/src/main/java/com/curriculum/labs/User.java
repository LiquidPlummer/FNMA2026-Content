package com.curriculum.labs;

/**
 * A directory user. The manager reference is nullable — the top of the
 * chain reports to nobody — and that nullability drives the whole lab.
 */
public class User {
    private final String name;
    private final String email;
    private final User manager;          // null at the top of the chain

    public User(String name, String email, User manager) {
        this.name = name;
        this.email = email;
        this.manager = manager;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }

    public User getManager() {           // Part 4 upgrades this
        return manager;
    }

    @Override
    public String toString() {
        return name + " <" + email + ">";
    }
}
