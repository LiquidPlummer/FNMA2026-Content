package org.example;

public class SingletonBean {
    private ExampleBean exampleBean;

    public SingletonBean() {
        System.out.println("Singleton constructor...");
    }

    public ExampleBean getExampleBean() {
        return exampleBean;
    }

    public void setExampleBean(ExampleBean exampleBean) {
        this.exampleBean = exampleBean;
    }
}
