package com.example.xmlConfig;

import com.example.componentScan.MyComponent;
import org.springframework.beans.factory.annotation.Autowired;

//This classes bean definition can be found in Beans.xml
public class MyClass {
    private MyComponent myComponent;

    @Autowired
    public MyClass(MyComponent myComponent) {
        this.myComponent = myComponent;
    }

    public void test() {
        System.out.println("This is myClass");
        this.myComponent.test();
    }
}
