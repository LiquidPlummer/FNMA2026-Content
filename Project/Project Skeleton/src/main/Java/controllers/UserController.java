package controllers;

import services.UserService;

public class UserController {
    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
}


/*
 * Controllers are all about handling incoming reuqests and preparing outgoing responses to those requests.
 * The same methods that are in the DAO must be represented here, because it is up here in the PL that these "workflows" begin.
 * CREATE 
 * READ ONE (by username?)
 * READ MANY
 * UPDATE
 * DELETE
 */