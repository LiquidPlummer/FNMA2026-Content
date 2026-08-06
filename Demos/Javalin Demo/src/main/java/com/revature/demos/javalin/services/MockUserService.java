package com.revature.demos.javalin.services;

import com.revature.demos.javalin.models.User;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

public class MockUserService {

    Map<String, User> userMap = new HashMap<>();



    public User findUserByUsername(String username) {
        return userMap.get(username);
    }

    public void createNewUser(User user) {
        userMap.put(user.getUsername(), user);
    }

    public void getUserWithFilters() {

    }

    public void putUser() {

    }

    public void deleteUser() {

    }
}
