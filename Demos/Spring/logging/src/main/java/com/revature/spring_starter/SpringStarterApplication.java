package com.revature.spring_starter;

import com.revature.spring_starter.components.MessagePrinter;
import com.revature.spring_starter.controllers.MyController;
import com.revature.spring_starter.services.MyService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

//TODO:
//Revisit Inversion of Control as a concept
//Component scanning - stereotypes, annotations
//springbootapplication settings & implicit annotations
//autowiring - constructor, setter, why?, and the big no-no
//bean scopes - SINGLETON is EAGER by default - "earliest available opportunity"
//             PROTOTYPE is LAZY - created at the last possible moment - "ad hoc"
//             Prototype beans cannot be EAGER because we cannot know how many we need, 0-many
//The basics of the bean lifecycle
//hooking into the bean lifecycle with interfaces & annotations
//actuator and devtools


@SpringBootApplication(scanBasePackages = {
        "com.revature.spring_starter.controllers",
        "com.revature.spring_starter.repositories",
        "com.revature.spring_starter.services",
        "com.revature.spring_starter.components"})
public class SpringStarterApplication {

	public static void main(String[] args) {

        MyService service1;  //did we get classloaded here? No!

		ApplicationContext applicationContext = SpringApplication.run(SpringStarterApplication.class, args);


        MyController controller = applicationContext.getBean("myController", MyController.class);
        controller.bark();


        MessagePrinter printer = applicationContext.getBean(MessagePrinter.class);
        printer.print();

	}



}

