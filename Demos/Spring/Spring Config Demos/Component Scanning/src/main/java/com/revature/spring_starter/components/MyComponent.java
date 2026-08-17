package com.revature.spring_starter.components;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

//Stereotypes
@Component
@Scope("prototype")
public class MyComponent {
    {
        System.out.println("Component static initializer...");
    }

    public MyComponent() {
        System.out.println("Component constructed!");
    }

    public void bark() {
        System.out.println("This is the component!");
    }
}
