package com.revature.web.controllers;

import com.revature.web.models.ExampleModel;
import com.revature.web.services.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController("controller-name")
@RequestMapping("/example")
public class ExampleController {
    private ExampleService service;

    @Autowired
    public ExampleController(ExampleService service) {
        System.out.println("Example Controller Constructor.");
        this.service = service;
    }

    @GetMapping("/ping")
    @ResponseBody
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public String hello() {
        return "pong!";
    }


    public ExampleModel persist(ExampleModel model) {
        return this.service.saveOrUpdate(model);
    }


}
