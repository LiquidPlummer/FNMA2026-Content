package com.curriculum.labs;

import java.util.HashMap;
import java.util.Map;

/**
 * The lookup service. Its finder currently returns null for a miss —
 * the honest-signature refactor in Part 2 is the point of the lab.
 */
public class UserDirectory {
    private final Map<String, User> byName = new HashMap<>();

    public UserDirectory() {
        User ada = new User("ada", "ada@example.com", null);
        User barbara = new User("barbara", "barbara@example.com", ada);
        User edsger = new User("edsger", "edsger@example.com", barbara);
        User grace = new User("grace", "grace@example.com", ada);
        byName.put("ada", ada);
        byName.put("barbara", barbara);
        byName.put("edsger", edsger);
        byName.put("grace", grace);
    }

    public User findByName(String name) {    // Part 2 changes this signature
        return byName.get(name);
    }
}
