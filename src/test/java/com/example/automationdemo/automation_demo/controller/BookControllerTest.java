package com.example.automationdemo.automation_demo.controller;

import com.example.automationdemo.automation_demo.dto.BookDTO;
import com.example.automationdemo.automation_demo.exception.BookNotFoundException;
import com.example.automationdemo.automation_demo.exception.GlobalExceptionHandler;
import com.example.automationdemo.automation_demo.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests using standalone MockMvc setup with constructor injection.
 * These tests focus on validating the REST API contract, HTTP status codes,
 * and proper request/response handling without involving the database.
 */
public class BookControllerTest {

    private MockMvc mockMvc;
    private BookService bookService;
    private ObjectMapper objectMapper;
    private BookDTO testBookDTO;

    @BeforeEach
    void setUp() {
        // Create the mocks
        this.bookService = Mockito.mock(BookService.class);

        // Create an instance of the controller with the mocked service
        BookController bookController = new BookController(bookService);

        // Set up MockMvc with the controller and exception handler
        this.mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Set up ObjectMapper for JSON conversion
        this.objectMapper = new ObjectMapper();
        // Register the JavaTimeModule to handle LocalDate serialization
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        // Create test book DTO
        testBookDTO = new BookDTO();
        testBookDTO.setId(1L);
        testBookDTO.setTitle("Test Title");
        testBookDTO.setAuthor("Test Author");
        testBookDTO.setGenre("Fiction");
        testBookDTO.setIsbn("1234567890");
        testBookDTO.setPublisher("Test Publisher");
        testBookDTO.setPublicationDate(LocalDate.of(2020, 1, 1));
        testBookDTO.setDescription("Test Description");
    }

    @Test
    void getAllBooks_ShouldReturnListOfBooks() throws Exception {
        // Arrange
        List<BookDTO> books = Collections.singletonList(testBookDTO);
        when(bookService.getAllBooks()).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Title")))
                .andExpect(jsonPath("$[0].author", is("Test Author")));

        verify(bookService, times(1)).getAllBooks();
    }

    @Test
    void getBookById_WithValidId_ShouldReturnBook() throws Exception {
        // Arrange
        when(bookService.getBookById(1L)).thenReturn(testBookDTO);

        // Act & Assert
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Title")))
                .andExpect(jsonPath("$.author", is("Test Author")));

        verify(bookService, times(1)).getBookById(1L);
    }

    @Test
    void getBookById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(bookService.getBookById(99L)).thenThrow(new BookNotFoundException("Book not found with ID: 99"));

        // Act & Assert
        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Book not found")));

        verify(bookService, times(1)).getBookById(99L);
    }

    @Test
    void getBookByIsbn_WithValidIsbn_ShouldReturnBook() throws Exception {
        // Arrange
        when(bookService.getBookByIsbn("1234567890")).thenReturn(testBookDTO);

        // Act & Assert
        mockMvc.perform(get("/api/books/isbn/1234567890"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Title")))
                .andExpect(jsonPath("$.author", is("Test Author")));

        verify(bookService, times(1)).getBookByIsbn("1234567890");
    }

    @Test
    void getBookByIsbn_WithInvalidIsbn_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(bookService.getBookByIsbn("invalid")).thenThrow(new BookNotFoundException("Book not found with ISBN: invalid"));

        // Act & Assert
        mockMvc.perform(get("/api/books/isbn/invalid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Book not found")));

        verify(bookService, times(1)).getBookByIsbn("invalid");
    }

    @Test
    void createBook_WithValidData_ShouldReturnCreatedBook() throws Exception {
        // Arrange
        when(bookService.createBook(any(BookDTO.class))).thenReturn(testBookDTO);

        // Act & Assert
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Title")))
                .andExpect(jsonPath("$.author", is("Test Author")));

        verify(bookService, times(1)).createBook(any(BookDTO.class));
    }

    @Test
    void createBook_WithDuplicateIsbn_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(bookService.createBook(any(BookDTO.class)))
                .thenThrow(new IllegalArgumentException("Book with ISBN 1234567890 already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("already exists")));

        verify(bookService, times(1)).createBook(any(BookDTO.class));
    }

    @Test
    void updateBook_WithValidData_ShouldReturnUpdatedBook() throws Exception {
        // Arrange
        BookDTO updatedBookDTO = new BookDTO();
        updatedBookDTO.setId(1L);
        updatedBookDTO.setTitle("Updated Title");
        updatedBookDTO.setAuthor("Test Author");
        updatedBookDTO.setGenre("Fiction");

        when(bookService.updateBook(eq(1L), any(BookDTO.class))).thenReturn(updatedBookDTO);

        // Act & Assert
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Updated Title")));

        verify(bookService, times(1)).updateBook(eq(1L), any(BookDTO.class));
    }

    @Test
    void updateBook_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(bookService.updateBook(eq(99L), any(BookDTO.class)))
                .thenThrow(new BookNotFoundException("Book not found with ID: 99"));

        // Act & Assert
        mockMvc.perform(put("/api/books/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Book not found")));

        verify(bookService, times(1)).updateBook(eq(99L), any(BookDTO.class));
    }

    @Test
    void deleteBook_WithValidId_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(bookService).deleteBook(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBook(1L);
    }

    @Test
    void deleteBook_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new BookNotFoundException("Book not found with ID: 99"))
                .when(bookService).deleteBook(99L);

        // Act & Assert
        mockMvc.perform(delete("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Book not found")));

        verify(bookService, times(1)).deleteBook(99L);
    }

    @Test
    void searchBooks_WithFilters_ShouldReturnFilteredBooks() throws Exception {
        // Arrange
        List<BookDTO> filteredBooks = Collections.singletonList(testBookDTO);
        when(bookService.searchBooks("Test", "Author", "Fiction", "Publisher", "1234"))
                .thenReturn(filteredBooks);

        // Act & Assert
        mockMvc.perform(get("/api/books/search")
                        .param("title", "Test")
                        .param("author", "Author")
                        .param("genre", "Fiction")
                        .param("publisher", "Publisher")
                        .param("isbn", "1234"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Title")));

        verify(bookService, times(1))
                .searchBooks("Test", "Author", "Fiction", "Publisher", "1234");
    }

    @Test
    void searchBooks_WithNoFilters_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(bookService.searchBooks(null, null, null, null, null))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/books/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(bookService, times(1))
                .searchBooks(null, null, null, null, null);
    }
}