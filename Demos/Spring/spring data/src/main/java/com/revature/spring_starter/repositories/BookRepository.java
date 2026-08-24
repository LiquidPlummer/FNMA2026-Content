package com.revature.spring_starter.repositories;

import com.revature.spring_starter.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, String> {

    /*
    save
    saveAll
    getById
    update
    updateAll
    delete
    deleteAll
    sort
    ...
     */

    //"named" or "derived" query - dynamically generated impl based on method signature
    Book getBookByTitle(String title);

    //JPQL or HQL - SQL like syntax to describe the query you want
    //HQL is Hibernate Query Language
    //JPQL is Java Persistence Query Language
    // Hibernate came first, and when JPA was standardized, a subset of HQL was created
    //All JPQL is valid HQL, but not the other way around. Superset/subset relationship
    @Query("SELECT b FROM Book b WHERE b.author.lastName = :name")
    List<Book> findByAuthorName(@Param("name") String name);

    //Native Query - Basically the same SQL we would write in JDBC, parameterization syntax is a little different
    //Add the "nativeQuery" attribute and set to true.
    @Query(value = "SELECT * FROM books WHERE title ILIKE %:fragment%", nativeQuery = true)
    List<Book> searchTitles(@Param("fragment") String fragment);


}
