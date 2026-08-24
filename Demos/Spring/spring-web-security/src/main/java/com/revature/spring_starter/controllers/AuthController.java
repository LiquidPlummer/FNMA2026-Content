package com.revature.spring_starter.controllers;

import com.revature.spring_starter.dtos.LoginRequest;
import com.revature.spring_starter.dtos.RegisterRequest;
import com.revature.spring_starter.dtos.UserResponse;
import com.revature.spring_starter.models.User;
import com.revature.spring_starter.security.JwtCookieAuthenticationFilter;
import com.revature.spring_starter.security.JwtService;
import com.revature.spring_starter.services.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Optional;

/**
 * The two endpoints that hand out identity. Both are permitAll in SecurityConfig,
 * because requiring a token to reach the token dispenser is a locked-room problem.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final boolean cookieSecure;

    public AuthController(AuthService authService,
                          JwtService jwtService,
                          @Value("${app.jwt.cookie-secure:true}") boolean cookieSecure) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.cookieSecure = cookieSecure;
    }

    /** POST /api/auth/register - create an account. Does NOT log you in. */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        Optional<User> created = authService.register(request);

        // Empty means the username was taken. 409 Conflict.
        return created
                .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    /**
     * POST /api/auth/login - check credentials and, if good, set the JWT cookie.
     *
     * The response body is just a courtesy. The part that matters is the
     * Set-Cookie header, which the browser stores and then replays automatically
     * on every subsequent request to this origin. The client never has to touch
     * the token, and because the cookie is HttpOnly, its JavaScript cannot.
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        Optional<User> user = authService.authenticate(request.username(), request.password());

        if (user.isEmpty()) {
            // 401, and deliberately no hint about which half was wrong.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User authenticated = user.get();
        String token = jwtService.issue(
                authenticated.getUsername(),
                authenticated.getRole().name());   // bare "USER"/"ADMIN", no ROLE_ prefix

        ResponseCookie cookie = ResponseCookie
                .from(JwtCookieAuthenticationFilter.COOKIE_NAME, token)

                // JavaScript cannot read this cookie. That is the main reason to
                // use a cookie instead of stashing the token in localStorage:
                // an XSS bug can read localStorage, it cannot read HttpOnly.
                .httpOnly(true)

                // HTTPS only. Driven by a property because a Secure cookie is
                // silently dropped over plain http://localhost.
                .secure(cookieSecure)

                // Lax: the browser withholds this cookie on cross-SITE requests,
                // which is most of what CSRF needs. A front end on
                // localhost:5173 talking to localhost:8080 is a different ORIGIN
                // but the same SITE, so the cookie still flows in dev.
                .sameSite("Lax")

                // Send it on every path, not just /api/auth.
                .path("/")

                // Expire the cookie alongside the token inside it, so the browser
                // stops sending one it already knows is stale.
                .maxAge(Duration.ofSeconds(jwtService.getTtlSeconds()))

                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(UserResponse.from(authenticated));
    }
}
