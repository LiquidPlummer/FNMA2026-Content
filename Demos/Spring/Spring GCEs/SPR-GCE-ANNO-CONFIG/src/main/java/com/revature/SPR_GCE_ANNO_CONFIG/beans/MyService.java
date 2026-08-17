package com.revature.SPR_GCE_ANNO_CONFIG.beans;

import org.springframework.stereotype.Service;

@Service
public class MyService {
    private MyRepository myRepository;

    public MyService(MyRepository myRepository) {
        System.out.println("MyService Constructor!");
        this.myRepository = myRepository;
    }
}
