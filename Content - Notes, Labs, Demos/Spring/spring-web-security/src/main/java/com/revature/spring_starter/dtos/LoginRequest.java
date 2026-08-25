package com.revature.spring_starter.dtos;

/** Body of POST /api/auth/login. */
public record LoginRequest(String username, String password) {
}
