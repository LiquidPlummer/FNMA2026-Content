package com.revature.spring_starter.controllers;

import com.revature.spring_starter.models.Book;
import com.revature.spring_starter.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
public class BookController {
    private BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/{title}")
    public Book getBookByIsbn(@PathVariable String title) {
        return this.bookService.getBookByTitle(title);
    }

    @PostMapping
    public Book createBook(@RequestBody Book book) {
        return this.bookService.saveBook(book);
    }
}
