package org.example;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:beans.xml");

//        ExampleBean bean = ctx.getBean(ExampleBean.class);
//        bean.test();


        ExampleBean steve = (ExampleBean)ctx.getBean("Steve");
        steve.test();
    }
}