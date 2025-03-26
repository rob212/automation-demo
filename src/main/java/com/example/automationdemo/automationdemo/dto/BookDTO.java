package com.example.automationdemo.automationdemo.dto;

import com.example.automationdemo.automationdemo.model.Book;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BookDTO {

    private Long id;
    private String title;
    private String author;
    private String genre;
    private String publisher;
    private String isbn;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publicationDate;

    private String description;

    // Constructor from entity
    public BookDTO(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.genre = book.getGenre();
        this.publisher = book.getPublisher();
        this.isbn = book.getIsbn();
        this.publicationDate = book.getPublicationDate();
        this.description = book.getDescription();
    }

    // Convert DTO to entity
    public Book toEntity() {
        return Book.builder()
                .id(this.id)
                .title(this.title)
                .author(this.author)
                .genre(this.genre)
                .publisher(this.publisher)
                .isbn(this.isbn)
                .publicationDate(this.publicationDate)
                .description(this.description)
                .build();
    }
}
