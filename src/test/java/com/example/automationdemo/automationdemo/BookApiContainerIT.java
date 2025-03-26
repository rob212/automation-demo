package com.example.automationdemo.automationdemo;

import com.example.automationdemo.automationdemo.dto.BookDTO;
import com.example.automationdemo.automationdemo.model.Book;
import com.example.automationdemo.automationdemo.repository.BookRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testcontainers.shaded.org.hamcrest.Matchers.containsString;

/**
 * Integration tests for the Book API.
 * These tests use the entire Spring API application stack with a real database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class BookApiContainerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:${postgresql.version}")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() {
        // Clean up the database after each test
        bookRepository.deleteAll();
    }

    @Test
    void testCreateAndRetrieveBook() throws Exception {
        // Create a book DTO
        BookDTO bookDTO = new BookDTO();
        bookDTO.setTitle("Integration Test Book");
        bookDTO.setAuthor("Integration Test Author");
        bookDTO.setGenre("Test Genre");
        bookDTO.setIsbn("9876543210");
        bookDTO.setPublicationDate(LocalDate.of(2020, 1, 1));
        bookDTO.setDescription("Integration test description");

        // Create the book via API
        MvcResult createResult = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Book"))
                .andReturn();

        // Extract the created book's ID
        BookDTO createdBook = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                BookDTO.class);
        Long bookId = createdBook.getId();

        // Retrieve the book by ID
        mockMvc.perform(get("/api/books/" + bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId))
                .andExpect(jsonPath("$.title").value("Integration Test Book"))
                .andExpect(jsonPath("$.author").value("Integration Test Author"));

        // Verify the book exists in the database
        assertTrue(bookRepository.findById(bookId).isPresent());
    }

    @Test
    void testUpdateBook() throws Exception {
        // Create a book directly in the repository
        Book book = new Book("Original Title", "Original Author", "Original Genre");
        book.setIsbn("5555555555");
        book = bookRepository.save(book);
        Long bookId = book.getId();

        // Create an updated book DTO
        BookDTO updateDTO = new BookDTO();
        updateDTO.setId(bookId);
        updateDTO.setTitle("Updated Title");
        updateDTO.setAuthor("Updated Author");
        updateDTO.setGenre("Updated Genre");
        updateDTO.setIsbn("5555555555"); // Same ISBN

        // Update the book via API
        mockMvc.perform(put("/api/books/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId))
                .andExpect(jsonPath("$.title").value("Updated Title"));

        // Verify the book was updated in the database
        Book updatedBook = bookRepository.findById(bookId).orElseThrow();
        assertEquals("Updated Title", updatedBook.getTitle());
        assertEquals("Updated Author", updatedBook.getAuthor());
    }

    @Test
    void testDeleteBook() throws Exception {
        // Create a book directly in the repository
        Book book = new Book("Delete Test", "Delete Author", "Delete Genre");
        book = bookRepository.save(book);
        Long bookId = book.getId();

        // Delete the book via API
        mockMvc.perform(delete("/api/books/" + bookId))
                .andExpect(status().isNoContent());

        // Verify the book no longer exists in the database
        assertFalse(bookRepository.findById(bookId).isPresent());
    }

    @Test
    void testSearchBooks() throws Exception {
        // Create multiple books with different attributes
        Book book1 = new Book("Fantasy Book", "Fantasy Author", "Fantasy");
        book1.setPublisher("Fantasy Publisher");
        bookRepository.save(book1);

        Book book2 = new Book("Science Fiction", "Sci-Fi Author", "Sci-Fi");
        book2.setPublisher("Sci-Fi Publisher");
        bookRepository.save(book2);

        Book book3 = new Book("Another Fantasy", "Fantasy Author", "Fantasy");
        book3.setPublisher("Another Publisher");
        bookRepository.save(book3);

        // Search by genre
        MvcResult genreResult = mockMvc.perform(get("/api/books/search")
                        .param("genre", "Fantasy"))
                .andExpect(status().isOk())
                .andReturn();

        List<BookDTO> genreResults = objectMapper.readValue(
                genreResult.getResponse().getContentAsString(),
                new TypeReference<List<BookDTO>>() {});

        assertEquals(2, genreResults.size());
        assertTrue(genreResults.stream().allMatch(b -> b.getGenre().contains("Fantasy")));

        // Search by author
        MvcResult authorResult = mockMvc.perform(get("/api/books/search")
                        .param("author", "Fantasy Author"))
                .andExpect(status().isOk())
                .andReturn();

        List<BookDTO> authorResults = objectMapper.readValue(
                authorResult.getResponse().getContentAsString(),
                new TypeReference<List<BookDTO>>() {});

        assertEquals(2, authorResults.size());
        assertTrue(authorResults.stream().allMatch(b -> b.getAuthor().equals("Fantasy Author")));

        // Search with multiple criteria
        MvcResult multiResult = mockMvc.perform(get("/api/books/search")
                        .param("author", "Fantasy Author")
                        .param("publisher", "Another"))
                .andExpect(status().isOk())
                .andReturn();

        List<BookDTO> multiResults = objectMapper.readValue(
                multiResult.getResponse().getContentAsString(),
                new TypeReference<List<BookDTO>>() {});

        assertEquals(1, multiResults.size());
        assertEquals("Another Fantasy", multiResults.get(0).getTitle());
    }

    @Test
    void testCreateBookWithDuplicateIsbn() throws Exception {
        // Create a book with a specific ISBN
        Book book = new Book("Original Book", "Original Author", "Original Genre");
        book.setIsbn("1111111111");
        bookRepository.save(book);

        // Try to create another book with the same ISBN
        BookDTO duplicateBookDTO = new BookDTO();
        duplicateBookDTO.setTitle("Duplicate Book");
        duplicateBookDTO.setAuthor("Duplicate Author");
        duplicateBookDTO.setGenre("Duplicate Genre");
        duplicateBookDTO.setIsbn("1111111111"); // Same ISBN

        // Expect a bad request response
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateBookDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }
}
