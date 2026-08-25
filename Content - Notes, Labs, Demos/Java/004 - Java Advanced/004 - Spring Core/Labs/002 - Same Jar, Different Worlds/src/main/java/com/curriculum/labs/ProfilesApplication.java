package com.curriculum.labs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProfilesApplication implements CommandLineRunner {

    private final GreetingService service;

    public ProfilesApplication(GreetingService service) {
        this.service = service;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProfilesApplication.class, args);
    }

    @Override
    public void run(String... args) {
        service.greet("Ada");
    }
}
