package com.revature.controllers;

import com.revature.models.User;
import com.revature.utils.FakeDb;
import io.javalin.http.Context;

public class UserController {
    private FakeDb db;

    public UserController(FakeDb db) {
        this.db = db;
    }

    public void postNewUser(Context ctx) {
        db.saveOrUpdate(ctx.bodyAsClass(User.class));
        ctx.status(201);
    }

    public void getUserByUsername(Context ctx) {
        ctx.json(db.getByUsername(ctx.pathParam("username")));
        ctx.status(200);
    }
}
