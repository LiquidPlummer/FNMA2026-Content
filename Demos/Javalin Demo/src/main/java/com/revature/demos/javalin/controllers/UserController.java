package com.revature.demos.javalin.controllers;

import com.revature.demos.javalin.models.User;
import com.revature.demos.javalin.services.MockUserService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;


import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


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




    //This is a "Request Handler" - This gets registered and called when the request comes in, and this prepares the response.
    public void getUserByUsername(Context ctx) {// we will have a uri like this {pathParam} other tokens after the param
        //get the username from the request - get from: path params, query params, headers
        String username = ctx.pathParam("username");// If we send: GET-http://localhost:7000/users/kplummer - then the username is "kplummer"
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
        String username = ctx.queryParam("username");
        List<String> whatever = ctx.queryParams("orders");
        Map<String, List<String>> queryParams = ctx.queryParamMap();
        Set<String> queryKeys = queryParams.keySet();
        List<String> values = new LinkedList<>();
        for(String k : queryKeys) {
            values = queryParams.get(k);
            for(String v : values) {
                System.out.println(v);
            }
            //do whatever here - check against the catalog, etc...
        }

        ctx.json(values);
        //We could have a catalog of acceptable keys for filters to check against
        //we could then produce some kind of thing that would indicate which filters are used in this case
        //

        // 1 - username = 1
        // 2 - firstname = 2
        // 4 - lastname = 4
        //5 - 0000 0101

        //username + lastname = 5 = 0000 0101

        //Then we would pass this down with the filtering info to the service layer
        //the hard part here is implementing this in the JDBC because that's going to be just a ton
        //of switches, or ifs, or whatever and bunch of concatinating SQL together.

    }

    public void postNewUser(Context ctx) {
        System.out.println("Entered into POST new user endpoint.");
        User user = ctx.bodyAsClass(User.class);
        userService.createNewUser(user);
        ctx.status(HttpStatus.CREATED);//same as int 201
        ctx.json(user);
    }

    public void putUser(Context ctx) {
        throw new RuntimeException("This method is not implemented yet.");
    }

    public void deleteUser(Context ctx) {
        throw new RuntimeException("This method is not implemented yet.");
    }




}
