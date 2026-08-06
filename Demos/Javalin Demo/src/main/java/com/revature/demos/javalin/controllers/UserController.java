package com.revature.demos.javalin.controllers;

import com.revature.demos.javalin.models.User;
import com.revature.demos.javalin.services.MockUserService;
import io.javalin.http.Context;


public class UserController {
    private MockUserService userService;

    public UserController(MockUserService userService){
        this.userService = userService;
    }
    //What do we need for users? CRUD! Create, Read one, read with filtering, update, delete
    //We don't really think of it as CRUD here in the presentation layer
    //We think of these as the HTTP operations, we translate the requested operation into a behavior
    //So what are the requested operations we want to implement and support?
    //GET - read one
    //GET - read many (we will have to handle params)
    //POST - create
    //PUT - update
    //DELETE - delete

    public void postNewUser(Context ctx) {

    }

    public void getUserByUsername(Context ctx) {
        //get the username from the request - get from: path params, query params, headers
        String username = ctx.pathParam("username");
        //fetch the user from the service layer
        User user = userService.findUserByUsername(username);
        //put the user in the response body to be dispatched for us by javalin
        ctx.json(user);
        ctx.status(200);
        //method ends, we don't need to return anything. Javalin will dispatch the response regardless
        //This is sort of like an "Out Parameter" in that the changes we make to the incoming Context
        //object are the output of this function, not any returned value.
    }

    public void getUsersWithFilters(Context ctx) {
        throw new RuntimeException("This method is not implemented yet.");
    }

    public void putUser(Context ctx) {
        throw new RuntimeException("This method is not implemented yet.");
    }

    public void deleteUser(Context ctx) {
        throw new RuntimeException("This method is not implemented yet.");
    }




}
