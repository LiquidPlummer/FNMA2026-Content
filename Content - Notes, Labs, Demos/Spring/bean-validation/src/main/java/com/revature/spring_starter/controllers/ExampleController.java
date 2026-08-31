package com.revature.spring_starter.controllers;

import com.revature.spring_starter.models.ExampleModel;
import com.revature.spring_starter.services.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.ValidationException;


@RestController("controller-name")
public class ExampleController {
    private ExampleService service;

    @Autowired
    public ExampleController(ExampleService service) {
        System.out.println("Example Controller Constructor.");
        this.service = service;
    }

    public ExampleModel persist(ExampleModel model) {
        throw new ValidationException("test");

        return this.service.saveOrUpdate(model);
    }


}
