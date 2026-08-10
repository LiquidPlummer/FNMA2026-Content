package com.revature;

import com.revature.controllers.UserController;
import com.revature.utils.FakeDb;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;

public class Main {
    public static void main(String[] args) {
        Javalin api = Javalin.create(Main::configureServer).start(7000);
    }

    private static void configureServer(JavalinConfig config) {
        UserController userController = new UserController(new FakeDb());

        config.routes.post("/users/register", userController::postNewUser);
        config.routes.get("/users/{username}", userController::getUserByUsername);
    }
}
