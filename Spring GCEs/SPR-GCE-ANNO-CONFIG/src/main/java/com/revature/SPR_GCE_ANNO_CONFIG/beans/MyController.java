package com.revature.SPR_GCE_ANNO_CONFIG.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class MyController {
    private MyService myService;

    @Autowired
    public MyController(MyService myService) {
        this.myService = myService;
        System.out.println("MyController constructor!");
    }

}
