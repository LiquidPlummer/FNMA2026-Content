package com.revature.demos.javalin.services;

import com.revature.demos.javalin.models.User;
import io.javalin.http.Context;

public class MockUserService {
    public User findUserByUsername(String username) {
        return new User(username, "pass123", "Kyle", "Plummer");
    }

    public void getUserWithFilters(Context ctx) {

    }

    public void putUser(Context ctx) {

    }

    public void deleteUser(Context ctx) {

    }
}
