package com.revature.spring_starter.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Reads the JWT cookie, and if it checks out, tells Spring Security who the
 * caller is. That is all it does.
 *
 * THE ONE RULE: this filter never rejects a request. Missing cookie, expired
 * token, forged signature, outright garbage - every one of those ends the same
 * way, by calling chain.doFilter() and letting the request continue as an
 * unauthenticated one. Deciding whether "unauthenticated" is acceptable for a
 * given URL is somebody else's job (AuthorizationFilter, driven by the rules in
 * SecurityConfig).
 *
 * Two questions, two different components:
 *     Who is this?        -> this filter          (authentication)
 *     May they do this?   -> AuthorizationFilter  (authorization)
 *
 * Keeping them apart is what lets the exact same cookie earn a 200 on one route
 * and a 403 on another.
 *
 * Extends OncePerRequestFilter so it runs a single time per request even when a
 * forward or error dispatch re-enters the chain.
 */
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    public static final String COOKIE_NAME = "ACCESS_TOKEN";

    private final JwtService jwtService;

    public JwtCookieAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        // Has something already authenticated this request? If so, leave it be
        // rather than stomping on an identity somebody else established.
        //
        // This is null here only because we sit EARLY in the chain.
        // AnonymousAuthenticationFilter runs after us and fills an still-empty
        // context with an anonymous token, so by the time a controller runs this
        // is never null - it is either a real user or "anonymousUser".
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            readCookie(request)                      // Optional<String>  - the raw token
                    .flatMap(jwtService::verify)     // Optional<Claims>  - flatMap: verify already returns an Optional
                    .map(this::toAuthentication)     // Optional<Authentication> - map: returns a plain value
                    .ifPresent(auth -> {
                        // Build a fresh context rather than mutating whatever is
                        // already bound to this thread.
                        SecurityContext context = SecurityContextHolder.createEmptyContext();
                        context.setAuthentication(auth);
                        SecurityContextHolder.setContext(context);
                    });
            // Any step failing yields an empty Optional and the whole chain of
            // calls quietly does nothing. No null checks, no early returns.
        }

        // ALWAYS continue. Outside the if, so it runs on every path through.
        // No cleanup needed afterwards - SecurityContextHolderFilter clears the
        // thread-local when the chain unwinds. Do NOT add a finally-clearContext.
        chain.doFilter(request, response);
    }

    /** Pull our cookie's value out of the request, if it is there at all. */
    private Optional<String> readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        // getCookies() returns NULL when no cookies were sent, not an empty array.
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(StringUtils::hasText)
                .findFirst();
    }

    /** Turn verified claims into the object Spring Security understands. */
    private Authentication toAuthentication(Claims claims) {
        String username = claims.getSubject();
        String role = claims.get(JwtService.ROLE_CLAIM, String.class);

        // The ROLE_ prefix goes on HERE, exactly once, and nowhere else.
        // hasRole("ADMIN") is just sugar for hasAuthority("ROLE_ADMIN"), so the
        // token stores a bare "ADMIN" and we prefix it on the way in. Storing
        // "ROLE_ADMIN" in the token would produce ROLE_ROLE_ADMIN and match nothing.
        List<SimpleGrantedAuthority> authorities = (role == null)
                ? List.of()
                : List.of(new SimpleGrantedAuthority("ROLE_" + role));

        // Use the static .authenticated(...) factory. The two-arg constructor
        // new UsernamePasswordAuthenticationToken(principal, credentials) builds an
        // UNauthenticated token, which fails later in a very confusing way.
        return UsernamePasswordAuthenticationToken.authenticated(
                username,      // principal   - what @AuthenticationPrincipal injects
                null,          // credentials - the JWT already served as the credential
                authorities);  // authorities - what hasRole()/hasAnyRole() check against
    }
}
