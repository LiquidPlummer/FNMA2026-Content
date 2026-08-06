package com.revature.demos.javalin.controllers;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

public class UserController {
    public static void ping(Context ctx) {
        ctx.status(200);
        ctx.result("Pong!");
    }

}
