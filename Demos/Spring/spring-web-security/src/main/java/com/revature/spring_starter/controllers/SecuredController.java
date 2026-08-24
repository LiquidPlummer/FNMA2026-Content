package com.revature.spring_starter.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Test endpoints. Neither one contains a single line of security code - no token
 * parsing, no role checks, no "if (user == null)". By the time a method here
 * runs, the request has already cleared the filter chain, so reaching the body
 * at all IS the authorization result.
 *
 * That is the payoff of splitting authentication from authorization: the rules
 * live in one place (SecurityConfig) instead of being re-litigated in every
 * handler.
 */
@RestController
public class SecuredController {

    /**
     * GET /api/me - any logged-in user, USER or ADMIN.
     *
     * Spring injects the Authentication our filter built. Note what is and is not
     * here: the principal and the authorities survived, but the raw JWT and its
     * other claims (iat, exp) did not. toAuthentication() dropped them.
     */
    @GetMapping("/api/me")
    public Map<String, Object> me(Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)   // "ROLE_USER" - prefix included
                .toList();

        return Map.of(
                "username", authentication.getName(),
                "authorities", authorities);
    }

    /**
     * GET /api/admin/whoami - ADMIN only, enforced by the /api/admin/** rule.
     *
     * This is the endpoint that proves the whole design. Log in as a USER and
     * call both routes with the SAME cookie: /api/me returns 200, this one
     * returns 403. Identical credentials, different answer, because
     * authentication and authorization are separate questions.
     *
     * @AuthenticationPrincipal pulls out just the principal. Here that is the
     * username String, since that is what we passed to .authenticated().
     */
    @GetMapping("/api/admin/whoami")
    public Map<String, Object> adminOnly(@AuthenticationPrincipal String username) {
        return Map.of(
                "username", username,
                "message", "You reached an ADMIN-only endpoint.");
    }
}
