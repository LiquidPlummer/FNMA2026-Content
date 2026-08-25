package com.revature.demos.javalin.controllers;

import io.javalin.http.Context;

public class HealthController {
    public static void ping(Context ctx) {
        ctx.status(200);
        ctx.result("Pong!");
    }
}
