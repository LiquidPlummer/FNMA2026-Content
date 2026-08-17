package com.revature.SPR_GCE_ANNO_CONFIG.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyComponent {
    private MyService myService;

    @Autowired
    public MyComponent(MyService myService) {
        System.out.println("MyComponent Constructor!");
        this.myService = myService;
    }
}
