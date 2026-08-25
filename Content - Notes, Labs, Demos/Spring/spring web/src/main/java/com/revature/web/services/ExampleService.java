package com.revature.web.services;

import com.revature.web.models.ExampleModel;
import com.revature.web.repositories.ExampleRepository;
import org.springframework.stereotype.Service;

@Service
public class ExampleService {
    private ExampleRepository repository;

    public ExampleService(ExampleRepository repository) {
        System.out.println("Example Service Constructor.");
        this.repository = repository;
    }

    public ExampleModel saveOrUpdate(ExampleModel model) {
        return this.repository.save(model);
    }
}
