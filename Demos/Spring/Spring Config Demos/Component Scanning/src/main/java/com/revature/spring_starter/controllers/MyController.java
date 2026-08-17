package com.revature.spring_starter.controllers;

import com.revature.spring_starter.components.MyComponent;
import com.revature.spring_starter.services.MyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Controller
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)// same as @Scope("singleton")
public class MyController {
    {
        System.out.println("Controller static initializer...");
    }


    //@Autowired//Field autowiring - THE BIG NO NO, basically never choose this one
    private MyService service;
    private MyComponent component;

    /// Why might we pick constructor vs setter autowiring?
    /// Not about DRY - Don't repeat yourself

    @Autowired//constructor autowiring - for dependencies that are required
    public MyController(MyService service) {
        System.out.println("Controller constructor");
        this.service = service;
    }

    //@Autowired
    public void setService(MyService service) {
        this.service = service;
    }

    @Autowired//setter autowiring - for other dependencies, ones that are optional
    public void setComponent(MyComponent component) {
        this.component = component;
    }

    public void bark(){
        this.service.bark();
    }

}
