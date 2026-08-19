package com.revature.web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;


@RestController
@RequestMapping("/example")
public class ExampleController {

    @GetMapping("/request-and-response-objects")
    public void requestAndResponseObjects(HttpServletRequest req, HttpServletResponse resp) {

    }

    @GetMapping("/request-headers")
    public void headers(
            @RequestHeader("Content-Length") Integer contentLength,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId
    ) {
        //content length is a required header, we will get an exception if the request doesn't have it
        //But the other, X-Trace-Id, is not required. If missing you won't get that info, but won't get
        //an exception either
    }

    @GetMapping("/response-headers")
    public ResponseEntity<String> responseHeaders(HttpServletResponse resp) {
        resp.setHeader("key", "value");//we can use the servlet response object to set headers

        return ResponseEntity.ok()
                .header("key", "value")
                .header("key2", "value2")
                .body("This is how we can use ResponseEntity to set headers, as well as status code and body");

    }


    public void requestCookies(@CookieValue("username") String username) {//getting cookies is easy, just like headers
        System.out.println(username);
    }

    public ResponseEntity<String> responseCookies(HttpServletResponse resp) {
        //Build a cookie, then set it in the "Set-Cookie" response header
        ResponseCookie cookie = ResponseCookie.from("username", "kplummer")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Strict")
                .build();

        ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString());


        //These both do the same thing, cookies are just response headers with the key "Set-Cookie"
//        resp.addCookie(cookie);
        resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
//        resp.addHeader("Set-Cookie", cookie);
    }

    //path params

    //query params

    //request body & response body


}
