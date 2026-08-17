package com.example.javaConfig;

import com.example.componentScan.MyService;

public class MyBean {
    private MyService myService;

    public MyBean(MyService myService) {
        this.myService = myService;
    }

    public void test() {
        this.myService.test();
    }
}
