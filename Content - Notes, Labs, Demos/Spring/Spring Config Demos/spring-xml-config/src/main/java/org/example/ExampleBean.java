package org.example;

public class ExampleBean {

    String name;

    public ExampleBean() {
        System.out.println("constructor... ");
    }

    public void test() {
        System.out.println("My name is: " + name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
