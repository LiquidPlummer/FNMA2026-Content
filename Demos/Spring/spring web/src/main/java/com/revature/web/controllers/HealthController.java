package com.revature.web.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/ping")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String hello() {
        return "pong!";
    }
}
