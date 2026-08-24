package com.revature.spring_starter;

import com.revature.spring_starter.controllers.ExampleController;
import com.revature.spring_starter.models.Author;
import com.revature.spring_starter.models.Book;
import com.revature.spring_starter.models.ExampleModel;
import com.revature.spring_starter.repositories.AuthorRepository;
import com.revature.spring_starter.repositories.BookRepository;
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

//        ExampleController controller = (ExampleController)ac.getBean("controller-name");
//        ExampleModel model = new ExampleModel("username", "password", "first", "last");
//        ExampleModel persistedModel = controller.persist(model);
//        System.out.println(persistedModel);

//
//        BookRepository bookRepo = ac.getBean(BookRepository.class);
//        AuthorRepository authorRepo = ac.getBean(AuthorRepository.class);
//
//        bookRepo.save(new Book("1-sdfsdfsdf", "testsave", "sci fi", new Author("test")));
//        bookRepo.save(new Book("1-222222222", "testnullsave", "sci fi", null));//This didn't work one time, not sure why...
//        bookRepo.saveAndFlush(new Book("1-3333333", "testnullsaveflush", "sci fi", null));

//        Author author = new Author("Cormac McCarthy");
//        authorRepo.save(author);
//        bookRepo.save(new Book("1-298347283756", "The Road", "sci fi", author));

	}

}
