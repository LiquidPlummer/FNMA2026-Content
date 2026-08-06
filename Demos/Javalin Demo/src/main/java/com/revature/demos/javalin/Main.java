package com.revature.demos.javalin;

import com.revature.demos.javalin.controllers.UserController;
import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        //inversion - inverse, reverse, opposite... we change in some way to be doing the opposite, or the compliment,
        // or the flip side of the coin
        //flow control - what steps happen when (execution order) loops, branches, and jumps
        //IoC - Inversion of Control - What might this mean?
        /*
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
        //we're still in main()
        Javalin app = Javalin.create(
                config -> {config.routes.get("/", UserController::ping);}//GET HTTP METHOD
        ).start(7000);//ports we can pick from go from basically 1000 - 65000



    }
}
