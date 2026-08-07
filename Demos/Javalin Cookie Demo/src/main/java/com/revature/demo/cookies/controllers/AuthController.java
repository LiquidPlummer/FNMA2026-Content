package com.revature.demo.cookies.controllers;

import com.revature.demo.cookies.dtos.AuthDto;
import com.revature.demo.cookies.dtos.RegistrationDto;
import com.revature.demo.cookies.services.PersistenceService;
import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

public class AuthController {
    private PersistenceService fakeDb;
    private final SecretKey secretKey;

    public AuthController(PersistenceService fakeDb) {
        this.fakeDb = fakeDb;
        this.secretKey = Jwts.SIG.HS256.key().build();//generates a random key, use this once to get a key if you want and store it somewhere
//        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode("1234567890"));
    }

    public void register(Context ctx) {
        RegistrationDto newRegistration = ctx.bodyAsClass(RegistrationDto.class);
        RegistrationDto inUse = this.fakeDb.findByUsername(newRegistration.getUsername());
        if(inUse != null) {
            System.out.println("Username taken, unable to register new user");
            throw new RuntimeException("Username already in use");
        }

        this.fakeDb.saveOrUpdate(newRegistration);
        System.out.println("New user registered: \n" + newRegistration);

    }

    public void login(Context ctx) {
        AuthDto auth = ctx.bodyAsClass(AuthDto.class);

        RegistrationDto user = this.fakeDb.findByUsername(auth.getUsername());

        if(user.getPassword().equals(auth.getPassword())) {
            String jws = Jwts.builder()
                    .subject(user.getUsername())//"sub":"kplummer"
                    .claim("role", user.getRole())//"role":"admin"
                    .issuedAt(Date.from(Instant.now()))//"isa":Whatever that datetime is now()
                    .expiration(Date.from(Instant.now().plusSeconds(3600)))//"exp":Whatever time is 10 mins from now()
                    .signWith(secretKey)
                    .compact();

            Cookie authToken = new Cookie("Authorization", jws, "/", 3600, true, true, "localhost");
            ctx.cookie(authToken);
            System.out.println("Login success, cookie attached.");
        }


    }


    public void secureEndpoint(Context ctx) {
        String jws = ctx.cookie("Authorization");
        System.out.println(jws);

        Jws<Claims> parsedToken = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jws);//Validation happens now, this will throw exception if the token is tampered
        Claims claims = parsedToken.getPayload();



    }

    public void cookieDemo(Context ctx) {
        String cookie = ctx.cookie("key");//get cookie from request by its key name
        System.out.println(ctx.cookie("secure"));
        System.out.println(ctx.cookie("http"));
        System.out.println(ctx.cookie("both"));
//        ctx.cookie("key", "username=kplummer,admin=true");

        String encoded = Base64.getUrlEncoder().encodeToString("\"Hello, World;\"".getBytes(StandardCharsets.UTF_8));
        System.out.println("Encoded cookie value: " + encoded);
        Cookie cookieObj = new Cookie("cookieObj", encoded, "/", 3600, true, true, "localhost");
        ctx.cookie(cookieObj);
    }


    //Ignore this, kyle is just messing around looking for good ways to present how the keys work
    public static void main(String[] args) {
        //SecretKey secretKey = Jwts.SIG.HS256.key().build();//generates a random key, use this once to get a key if you want and store it somewhere

        SecretKey key = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode("abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        String encodedKey = String.valueOf(key.getEncoded());

        System.out.println(encodedKey);
    }
}
