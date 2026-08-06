package com.revature.demos.javalin;

import com.revature.demos.javalin.controllers.HealthController;
import com.revature.demos.javalin.controllers.UserController;
import com.revature.demos.javalin.services.MockUserService;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson3;

public class Main {
    public static void main(String[] args) {

        /*
        inversion - inverse, reverse, opposite... we change in some way to be doing the opposite, or the compliment,
         or the flip side of the coin
        flow control - what steps happen when (execution order) loops, branches, and jumps
        IoC - Inversion of Control - What might this mean?

        traditionally we might look at programming like this:
        We are the programmers and we use tools (libraries) as we need to accomplish our goals

        With IoC, we might look at it like:
        We are the library, providing implementations, so that some other system can accomplish our goals


        With Javalin we won't be starting our application and controlling what it does when, and when the app terminates.
        Javalin does that. Instead of the application starting, interacting with users, and terminating, this app is
        long-lived, it starts and keeps going in a "listening" phase (listening for requests)

        Listening looks like this:
        Check the network buffer for new traffic (network buffer is just some memory, does it have new "unconsumed" bytes?)
        When we find something in the network buffer, we take action. (Well, not us... Javalin takes action)

         */
        System.out.println("This output comes from main right before starting the server.");
        Javalin app = Javalin.create(Main::configJavalinServer).start(7000);
        System.out.println("This output comes after the server starts. After this output, there are no instructions left in the main method...");
        System.out.println("While the server is running we will see this output. We can add in other functionality to occur after the server starts.");
        System.out.println("We don't have to wait for the javalin loop to quit before these instructions are executed.");
        //Here we are implicitly waiting for Javalin to "join"
    }

    private static void configJavalinServer(JavalinConfig config) {
        //Set up some dependencies
        UserController userController = new UserController(new MockUserService());//dependency injection pattern

        //Tell Javalin to use the newer jackson tools:
        config.jsonMapper(new JavalinJackson3());

        //Register Health Controller endpoints
        config.routes.get("/health", HealthController::ping);//This one is static

        //Here's another example of the same endpoint, but this one written as a lambda:
        config.routes.get("/", (ctx)->{ctx.status(200); ctx.result("Pong!");});

        //This syntax with methods as parameters is part of Java's "Functional Programming". Some devs love this
        //style of ad-hoc syntax, though it's not really of the Java Paradigm. This is actually more popular in languages
        //like JS, or Kotlin, which is a Java superset with JS syntax. Javalin is technically a Kotlin library, hence
        //the frequent use of lambdas and method params.

        //Register User Controller endpoints - "Request Handlers"
        config.routes.get("/users/{username}", userController::getUserByUsername);//Notice this one is not static
        config.routes.get("/users", userController::getUsersWithFilters);
        config.routes.post("/users", userController::postNewUser);
        config.routes.put("/users", userController::putUser);
        config.routes.delete("/users",userController::deleteUser);

        //register exception handlers
        config.routes.exception(RuntimeException.class, (e, ctx) -> {
            System.out.println("An exception occurred in a controller: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500);
            ctx.result(e.getMessage());
        });
    }
}
