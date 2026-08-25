package org.example;

public class DependentBean {
    public DependentBean() {
        System.out.println("dependency constructor...");
    }

    public void test() {
        System.out.println("dependent bean test");
    }
}
