package com.revature.spring_starter.repositories;

import com.revature.spring_starter.models.Book;

import java.util.List;

public interface BookRepositoryCustom {
    List<Book> search(String title, String lastName, Integer minYear);
}
