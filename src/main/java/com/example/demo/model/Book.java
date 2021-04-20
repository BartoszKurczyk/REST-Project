package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private String id;
    private String title;
    private String author;
    private String publisher;
    private String genre;
    private int yop;

    public boolean equals(Book book)
    {
        return this.title.equals(book.getTitle()) && this.author.equals(book.getAuthor())
                && this.publisher.equals(book.getPublisher()) && this.genre.equals(book.getGenre()) && this.yop == book.getYop();
    }
}
