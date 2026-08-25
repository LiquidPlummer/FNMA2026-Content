package com.revature.spring_starter;

import com.revature.spring_starter.controllers.ExampleController;
import com.revature.spring_starter.models.ExampleModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = {
        "com.revature.spring_starter.controllers",
        "com.revature.spring_starter.services",
        "com.revature.spring_starter.repositories"
})
public class SpringStarterApplication {

	public static void main(String[] args) {
		ApplicationContext ac = SpringApplication.run(SpringStarterApplication.class, args);

        ExampleController controller = (ExampleController)ac.getBean("controller-name");
        ExampleModel model = new ExampleModel("username", "password", "first", "last");
        ExampleModel persistedModel = controller.persist(model);
        System.out.println(persistedModel);

	}

}
