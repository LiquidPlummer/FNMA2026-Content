package com.revature.spring_starter.services;

import com.revature.spring_starter.repositories.MyRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
