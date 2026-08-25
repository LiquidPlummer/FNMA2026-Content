package com.revature.spring_starter.models;

import jakarta.persistence.*;

@Entity
public class Book {
    @Id
    private String isbn;


    private String title;
    private String genre;

    @ManyToOne(
            fetch = FetchType.EAGER, //Don't fetch the author right away, only ad hoc
            cascade = CascadeType.PERSIST)//Do eagerly save the referenced object first
    @JoinColumn(name = "author_id")
    private Author author;

    public Book(String isbn, String title, String genre, Author author) {
        this.isbn = isbn;
        this.title = title;
        this.genre = genre;
        this.author = author;
    }

    public Book() {
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}
