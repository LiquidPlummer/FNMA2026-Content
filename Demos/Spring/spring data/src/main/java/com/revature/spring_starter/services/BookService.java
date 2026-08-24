package com.revature.spring_starter.services;

import com.revature.spring_starter.models.Book;
import com.revature.spring_starter.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
public class BookService {
    private BookRepository bookRepo;

    @Autowired
    public BookService(BookRepository bookRepo) {
        this.bookRepo = bookRepo;
    }


    public Book saveBook(Book book) {
        return this.bookRepo.save(book);
    }

    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.REPEATABLE_READ
    )
    public Book getBookByTitle(String title) {
        Book book = this.bookRepo.getBookByTitle(title);
        int id = book.getAuthor().getAuthorId();
        return book;
    }
}
