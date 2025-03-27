package com.example.automationdemo.automationdemo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.automationdemo.automationdemo.model.Book;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests using Testcontainers with a real PostgreSQL database. Unlike the repository
 * tests with H2, these tests run against the same PostgreSQL version we'll use in production,
 * ensuring full compatibility.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookRepositoryContainerIT {

  /**
   * Define PostgreSQL container. This container will be started before tests and stopped after
   * tests.
   */
  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:13.3")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  /**
   * Configure Spring datasource properties to point to the Testcontainers PostgreSQL instance. This
   * is a static method that will be called once during test context initialisation.
   */
  @DynamicPropertySource
  static void registerPgProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private BookRepository bookRepository;

  @Test
  void testFullTextSearch_WithPostgresSpecificFeatures() {
    // Arrange - Create test books
    Book book1 = createTestBook("The Hobbit", "J.R.R. Tolkien", "Fantasy");
    book1.setDescription(
        "A hobbit goes on an adventure with dwarves to reclaim their treasure from a dragon.");
    bookRepository.save(book1);

    Book book2 = createTestBook("The Fellowship of the Ring", "J.R.R. Tolkien", "Fantasy");
    book2.setDescription(
        "A hobbit must destroy a powerful ring to save Middle-earth from the Dark Lord Sauron.");
    bookRepository.save(book2);

    Book book3 = createTestBook("Dune", "Frank Herbert", "Science Fiction");
    book3.setDescription(
        "A desert planet is the only source of a valuable spice, and political factions fight for control.");
    bookRepository.save(book3);

    // Execute the repository method that uses PostgreSQL-specific features
    List<Book> results = bookRepository.findByFilters(null, "Tolkien", null, null, null);

    // Assert
    assertEquals(2, results.size());
    assertTrue(results.stream().allMatch(book -> book.getAuthor().contains("Tolkien")));
  }

  /**
   * Test PostgreSQL's case-insensitive search capabilities. This is important to test with
   * PostgreSQL specifically, as different databases handle case sensitivity differently.
   */
  @Test
  void testCaseInsensitiveSearch_WithPostgres() {
    // Arrange
    Book book = createTestBook("UPPER CASE TITLE", "UPPER CASE AUTHOR", "UPPER CASE GENRE");
    book.setPublisher("UPPER CASE PUBLISHER");
    bookRepository.save(book);

    // Act - Search with lowercase
    List<Book> titleResults = bookRepository.findByTitleContainingIgnoreCase("upper case");
    List<Book> authorResults = bookRepository.findByAuthorContainingIgnoreCase("upper case");
    List<Book> genreResults = bookRepository.findByGenreContainingIgnoreCase("upper case");
    List<Book> publisherResults = bookRepository.findByPublisherContainingIgnoreCase("upper case");

    // Assert
    assertEquals(1, titleResults.size());
    assertEquals(1, authorResults.size());
    assertEquals(1, genreResults.size());
    assertEquals(1, publisherResults.size());
  }

  /**
   * Test handling of special characters and non-English text. This is important to test with real
   * PostgreSQL, as database collations and character handling can vary.
   */
  @Test
  void testSpecialCharactersAndInternationalText() {
    // Arrange - Create books with special characters and non-English text
    Book book1 = createTestBook("Café au lait", "Author with é and ñ", "Genre");
    bookRepository.save(book1);

    Book book2 = createTestBook("Book with emojis 😊", "Normal Author", "Genre");
    bookRepository.save(book2);

    Book book3 = createTestBook("Кириллица", "Cyrillic Author", "Genre");
    bookRepository.save(book3);

    // Act
    List<Book> result1 = bookRepository.findByTitleContainingIgnoreCase("café");
    List<Book> result2 = bookRepository.findByTitleContainingIgnoreCase("emoji");
    List<Book> result3 = bookRepository.findByTitleContainingIgnoreCase("кирил");

    // Assert
    assertEquals(1, result1.size());
    assertEquals(1, result2.size());
    assertEquals(1, result3.size());
  }

  /** Helper method to create a test book */
  private Book createTestBook(String title, String author, String genre) {
    Book book = new Book(title, author, genre);
    book.setPublicationDate(LocalDate.of(2020, 1, 1));
    book.setDescription("Test description");
    return book;
  }
}
