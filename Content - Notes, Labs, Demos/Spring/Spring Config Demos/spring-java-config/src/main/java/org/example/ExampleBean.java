package org.example;

import org.example.components.ComponentBean;

public class ExampleBean {
    DependentBean dependentBean;
    ComponentBean componentBean;

    public ExampleBean(DependentBean dependentBean, ComponentBean componentBean) {
        System.out.println("example bean constructor...");
        this.componentBean = componentBean;
        this.dependentBean = dependentBean;
    }

    public void test() {
        this.dependentBean.test();
        this.componentBean.test();
    }
}
