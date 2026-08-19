package com.revature.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = {
        "com.revature.web.controllers",
        "com.revature.web.services",
        "com.revature.web.repositories"
})
public class SpringStarterApplication {

	public static void main(String[] args) {
		ApplicationContext ac = SpringApplication.run(SpringStarterApplication.class, args);


	}

}
