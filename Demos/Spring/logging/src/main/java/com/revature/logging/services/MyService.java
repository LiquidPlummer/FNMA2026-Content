package com.revature.logging.services;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MyService {
    {
        System.out.println("MyService static initializer...");
    }


    public MyService() {
        System.out.println("This is the MyService contructor!");
    }

    public void bark() {
        System.out.println("This is the service method!");
    }
}
