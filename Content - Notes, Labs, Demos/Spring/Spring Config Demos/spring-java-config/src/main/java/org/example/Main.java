package org.example;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(ConfigurationClass.class);

        ExampleBean bean = ctx.getBean(ExampleBean.class);
        bean.test();

        ExampleBean2 ex2 = ctx.getBean(ExampleBean2.class);
        ex2.test();

        ExampleBean bean2 = ctx.getBean("Example", ExampleBean.class);
        if(bean == bean2) {
            System.out.println("Singleton beans are the same reference!");
        }
    }




}
