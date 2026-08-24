package com.revature.spring_starter.dtos;

import com.revature.spring_starter.models.Role;
import com.revature.spring_starter.models.User;

/**
 * What we hand back to the client. Exists so the password hash never leaves the
 * server - returning the User entity straight from a controller would serialize
 * it right into the response body.
 */
public record UserResponse(Integer id, String username, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
