package com.revature.demo.cookies;

import com.revature.demo.cookies.controllers.AuthController;
import com.revature.demo.cookies.services.PersistenceService;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class Main {
    public static void main(String[] args) {
        Javalin api = Javalin.create(Main::configureApi).start(7000);//<- listening
    }

    private static void configureApi(JavalinConfig config) {

        AuthController authController = new AuthController(new PersistenceService());

        //need a: URI, HTTP METHOD, Handler - the behavior to be invoked when this request is received
        config.routes.get("/ping", ctx -> {
            System.out.println("Pong!");
            ctx.status(HttpStatus.OK);
            ctx.result("Pong!");
        });

        config.routes.post("/register", authController::register);
        config.routes.post("/login", authController::login);
        config.routes.get("/cookieDemo", authController::cookieDemo);
    }



}
