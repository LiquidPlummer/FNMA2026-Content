package com.revature.spring_starter.repositories;

import com.revature.spring_starter.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Spring Data derives the query from the method name.
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
