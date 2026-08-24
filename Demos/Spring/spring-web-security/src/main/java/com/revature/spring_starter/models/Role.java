package com.revature.spring_starter.models;

/**
 * A user has exactly one of these.
 *
 * Note there is no ROLE_ prefix here. Spring Security adds that prefix itself at
 * the point where authorities are built, so the name stored in the database and
 * written into the JWT stays clean. See JwtCookieAuthenticationFilter.
 */
public enum Role {
    USER,
    ADMIN
}
