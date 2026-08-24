package com.revature.spring_starter.repositories;

import com.revature.spring_starter.models.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Integer> {
}
