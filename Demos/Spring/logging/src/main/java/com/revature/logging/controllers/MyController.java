package com.revature.logging.controllers;

import com.revature.logging.components.MyComponent;
import com.revature.logging.services.MyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RestController;

@RestController//implies @Controller & @ResponseBody, applied to all methods in the class
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)// same as @Scope("singleton")
public class MyController {
    {
        System.out.println("Controller static initializer...");
    }



    //@Autowired//Field autowiring - THE BIG NO NO, basically never choose this one
    private final MyService service;
    private MyComponent component;

    /// Why might we pick constructor vs setter autowiring?
    /// Not about DRY - Don't repeat yourself

    @Autowired//constructor autowiring - for dependencies that are required
    public MyController(MyService service) {
        System.out.println("Controller constructor");
        this.service = service;
    }

//    //@Autowired
//    public void setService(MyService service) {
//        this.service = service;
//    }

    @Autowired//setter autowiring - for other dependencies, ones that are optional
    public void setComponent(MyComponent component) {
        this.component = component;
    }

    public void bark(){
        this.service.bark();
    }

}
