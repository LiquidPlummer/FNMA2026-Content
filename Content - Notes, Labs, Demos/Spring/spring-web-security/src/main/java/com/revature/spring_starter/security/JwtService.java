package com.revature.spring_starter.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Signs and verifies JWTs. That is the whole job - it knows nothing about
 * cookies, HTTP, or Spring Security.
 *
 * A JWT is three base64 segments joined by dots: header.payload.signature. The
 * payload is NOT encrypted, it is only encoded - anyone holding the token can
 * read the claims. What the signature buys you is tamper-evidence: change one
 * character of the payload and the signature no longer matches, so verify()
 * rejects it. Never put a secret in a claim.
 */
public class JwtService {

    /** Claim key for the user's role. "sub" already holds the username. */
    public static final String ROLE_CLAIM = "role";

    private final SecretKey key;
    private final long ttlSeconds;

    public JwtService(String base64Secret, long ttlSeconds) {
        // Keys.hmacShaKeyFor throws WeakKeyException at startup if the decoded
        // secret is under 32 bytes, which is a much better failure than a quietly
        // weak signature. The key length also selects the algorithm later:
        // 32 bytes -> HS256, 48 -> HS384, 64 -> HS512.
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * Mint a token for a user. The username lands in the standard "sub" claim,
     * the role in a custom "role" claim.
     */
    public String issue(String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim(ROLE_CLAIM, role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key)
                .compact();
    }

    /**
     * Returns the claims if the token is genuine and unexpired, otherwise empty.
     *
     * JJWT signals every failure by throwing. We catch here and hand back an
     * empty Optional instead, so that a bad token is an ordinary value the
     * filter can branch on rather than an exception that unwinds the request.
     * That is what lets the filter stay silent and never reject anything.
     */
    public Optional<Claims> verify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)   // checks signature AND expiration
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            // Expired, wrong signature, malformed, null/blank - all the same to us.
            return Optional.empty();
        }
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }
}
