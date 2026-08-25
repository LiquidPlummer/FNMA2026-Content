package com.revature.logging.components;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

//Stereotypes
@Component
@Scope("prototype")
public class MyComponent implements InitializingBean, DisposableBean {
    {        System.out.println("Component static initializer...");    }

    public MyComponent() {
        System.out.println("Component constructed!");
    }

    @PostConstruct
    public void init(){
        //This is the more modern way to do custom bean init
        System.out.println("This happens just AFTER the constructor.");
    }

    @PreDestroy
    public void cleanup(){
        //This is the more modern way to do custom bean de-initialization
        System.out.println("This is the cleanup, happens just BEFORE destruction.");
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        System.out.println("afterPropertiesSet() - The old way of hooking into initialization");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("destroy() - the old way of hooking into cleanup");


    }



}
