package com.example.automationdemo.automationdemo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "title", "author", "genre", "isbn"})
@EqualsAndHashCode(of = "id")
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String author;

  @Column(nullable = false)
  private String genre;

  @Column(name = "publisher")
  private String publisher;

  @Column(unique = true)
  private String isbn;

  @Column(name = "publication_date")
  private LocalDate publicationDate;

  @Column(columnDefinition = "TEXT")
  private String description;

  // Constructor with required fields
  public Book(String title, String author, String genre) {
    this.title = title;
    this.author = author;
    this.genre = genre;
  }
}
