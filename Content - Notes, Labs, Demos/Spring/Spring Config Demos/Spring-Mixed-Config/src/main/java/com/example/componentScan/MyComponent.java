package com.example.componentScan;

import org.springframework.stereotype.Component;

@Component("myComponent")//This is the stereotype annotation for component scanning
public class MyComponent {
    public void test() {
        System.out.println("This is myComponent");
    }
}
