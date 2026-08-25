package com.revature.utils;

import com.revature.models.User;

import java.util.HashMap;
import java.util.Map;

public class FakeDb {
    private final Map<String, User> userTable;

    public FakeDb() {
        this.userTable = new HashMap<>();
    }

    public void saveOrUpdate(User user) {
        if(user == null) {
            return;
        }
        userTable.put(user.getUsername(), user);
    }

    public User getByUsername(String username) {
        return userTable.get(username);
    }
}
