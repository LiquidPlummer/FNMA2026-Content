package com.revature.spring_starter.repositories;

import com.revature.spring_starter.models.ExampleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExampleRepository extends JpaRepository<ExampleModel, Integer>{

}
