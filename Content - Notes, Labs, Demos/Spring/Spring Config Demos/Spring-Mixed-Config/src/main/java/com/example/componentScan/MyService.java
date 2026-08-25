package com.example.componentScan;

import com.example.xmlConfig.MyClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("myService")//This is the stereotype annotation for component scanning
public class MyService {
    private MyClass myClass;
    private MyComponent myComponent;

    @Autowired
    public MyService(MyClass myClass, MyComponent myComponent) {
        this.myClass = myClass;
        this.myComponent = myComponent;
    }

    public void test() {
        this.myClass.test();
        this.myComponent.test();
    }
}
