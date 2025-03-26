package com.example.automationdemo.automationdemo.repository;

import com.example.automationdemo.automationdemo.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Find book by ISBN
    Optional<Book> findByIsbn(String isbn);

    // Find books by title containing the given string (case-insensitive)
    List<Book> findByTitleContainingIgnoreCase(String title);

    // Find books by author containing the given string (case-insensitive)
    List<Book> findByAuthorContainingIgnoreCase(String author);

    // Find books by genre containing the given string (case-insensitive)
    List<Book> findByGenreContainingIgnoreCase(String genre);

    // Find books by publisher containing the given string (case-insensitive)
    List<Book> findByPublisherContainingIgnoreCase(String publisher);

    // Custom query to search by multiple fields
    @Query("SELECT b FROM Book b WHERE " +
            "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
            "(:genre IS NULL OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))) AND " +
            "(:publisher IS NULL OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :publisher, '%'))) AND " +
            "(:isbn IS NULL OR b.isbn = :isbn)")
    List<Book> findByFilters(
            @Param("title") String title,
            @Param("author") String author,
            @Param("genre") String genre,
            @Param("publisher") String publisher,
            @Param("isbn") String isbn);
}
