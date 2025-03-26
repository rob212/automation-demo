package com.example.automationdemo.automation_demo.repository;

import com.example.automationdemo.automation_demo.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests using H2 in-memory database.
 * The @DataJpaTest annotation provides autoconfiguration for JPA tests.
 * It uses an in-memory database by default and disables full autoconfiguration.
 */
@DataJpaTest
@ActiveProfiles("test")
public class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void findById_WithExistingId_ShouldReturnBook() {
        // Arrange
        Book book = createTestBook("Test Title", "Test Author", "Fiction");
        book = bookRepository.save(book);

        // Act
        Optional<Book> result = bookRepository.findById(book.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Title", result.get().getTitle());
    }

    @Test
    void findByIsbn_WithExistingIsbn_ShouldReturnBook() {
        // Arrange
        Book book = createTestBook("ISBN Test", "ISBN Author", "Fiction");
        book.setIsbn("9781234567897");
        bookRepository.save(book);

        // Act
        Optional<Book> result = bookRepository.findByIsbn("9781234567897");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("ISBN Test", result.get().getTitle());
    }

    @Test
    void findByTitleContainingIgnoreCase_WithExistingTitle_ShouldReturnBooks() {
        // Arrange
        bookRepository.save(createTestBook("Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "Fantasy"));
        bookRepository.save(createTestBook("Harry Potter and the Chamber of Secrets", "J.K. Rowling", "Fantasy"));
        bookRepository.save(createTestBook("Lord of the Rings", "J.R.R. Tolkien", "Fantasy"));

        // Act - Case insensitive search
        List<Book> result = bookRepository.findByTitleContainingIgnoreCase("harry");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(book -> book.getTitle().toLowerCase().contains("harry")));
    }

    @Test
    void findByAuthorContainingIgnoreCase_WithExistingAuthor_ShouldReturnBooks() {
        // Arrange
        bookRepository.save(createTestBook("Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "Fantasy"));
        bookRepository.save(createTestBook("Harry Potter and the Chamber of Secrets.", "J.K. Rowling", "Fantasy"));
        bookRepository.save(createTestBook("Lord of the Rings", "J.R.R. Tolkien", "Fantasy"));

        // Act - Case insensitive search
        List<Book> result = bookRepository.findByAuthorContainingIgnoreCase("rowling");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(book -> book.getAuthor().toLowerCase().contains("rowling")));
    }

    @Test
    void findByGenreContainingIgnoreCase_WithExistingGenre_ShouldReturnBooks() {
        // Arrange
        bookRepository.save(createTestBook("Harry Potter and the Prisoner of Azkaban", "J.K. Rowling", "Fantasy"));
        bookRepository.save(createTestBook("The Shining", "Stephen King", "Horror"));
        bookRepository.save(createTestBook("It", "Stephen King", "Horror"));

        // Act - Case insensitive search
        List<Book> result = bookRepository.findByGenreContainingIgnoreCase("horror");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(book -> book.getGenre().toLowerCase().contains("horror")));
    }

    @Test
    void findByPublisherContainingIgnoreCase_WithExistingPublisher_ShouldReturnBooks() {
        // Arrange
        Book book1 = createTestBook("Book 1", "Author 1", "Genre 1");
        book1.setPublisher("Penguin Random House");
        bookRepository.save(book1);

        Book book2 = createTestBook("Book 2", "Author 2", "Genre 2");
        book2.setPublisher("HarperCollins");
        bookRepository.save(book2);

        // Act - Case insensitive search
        List<Book> result = bookRepository.findByPublisherContainingIgnoreCase("penguin");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Penguin Random House", result.get(0).getPublisher());
    }

    @Test
    void findByFilters_WithMultipleFilters_ShouldReturnFilteredBooks() {
        // Arrange
        bookRepository.save(createTestBook("Fantasy Book", "Fantasy Author", "Fantasy"));

        Book scifiBook = createTestBook("Sci-Fi Book", "Sci-Fi Author", "Science Fiction");
        scifiBook.setPublisher("Ace Books");
        bookRepository.save(scifiBook);

        Book mysteryBook = createTestBook("Mystery Book", "Mystery Author", "Mystery");
        mysteryBook.setPublisher("Ace Books");
        bookRepository.save(mysteryBook);

        // Act - Test with publisher filter
        List<Book> result1 = bookRepository.findByFilters(null, null, null, "Ace", null);

        // Act - Test with multiple filters
        List<Book> result2 = bookRepository.findByFilters(null, null, "Science", "Ace", null);

        // Assert
        assertEquals(2, result1.size()); // Both Sci-Fi and Mystery books have Ace Books publisher
        assertEquals(1, result2.size()); // Only Sci-Fi book matches both filters
        assertEquals("Sci-Fi Book", result2.get(0).getTitle());
    }

    /**
     * Helper method to create a test book
     */
    private Book createTestBook(String title, String author, String genre) {
        Book book = new Book(title, author, genre);
        book.setPublicationDate(LocalDate.of(2020, 1, 1));
        book.setDescription("Test description");
        return book;
    }
}