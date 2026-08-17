package org.example.components;

import org.springframework.stereotype.Component;

@Component
public class ComponentBean {
    public ComponentBean() {
        System.out.println("component bean constructor...");
    }

    public void test() {
        System.out.println("component bean test");
    }
}
