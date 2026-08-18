package com.revature.web;

import com.revature.web.controllers.ExampleController;
import com.revature.web.models.ExampleModel;
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
