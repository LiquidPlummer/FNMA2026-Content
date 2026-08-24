package com.revature.spring_starter.dtos;

import com.revature.spring_starter.models.Role;

/**
 * Body of POST /api/auth/register.
 *
 * "role" is optional and defaults to USER. Letting the caller pick a role is a
 * terrible idea in a real system - it means anyone can mint themselves an admin
 * account - but it makes the ADMIN routes testable here without editing the
 * database by hand.
 */
public record RegisterRequest(String username, String password, Role role) {

    public Role roleOrDefault() {
        return role == null ? Role.USER : role;
    }
}
