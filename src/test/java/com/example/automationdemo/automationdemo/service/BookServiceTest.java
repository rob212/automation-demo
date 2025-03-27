package com.example.automationdemo.automationdemo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.automationdemo.automationdemo.dto.BookDTO;
import com.example.automationdemo.automationdemo.exception.BookNotFoundException;
import com.example.automationdemo.automationdemo.model.Book;
import com.example.automationdemo.automationdemo.repository.BookRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

  @Mock private BookRepository bookRepository;

  @InjectMocks private BookService bookService;

  private Book testBook;
  private BookDTO testBookDTO;

  @BeforeEach
  void setUp() {
    // Create test book
    testBook = new Book("Test Title", "Test Author", "Fiction");
    testBook.setId(1L);
    testBook.setPublisher("Test Publisher");
    testBook.setIsbn("1234567890");
    testBook.setPublicationDate(LocalDate.of(2020, 1, 1));
    testBook.setDescription("Test Description");

    // Create test BookDTO
    testBookDTO = new BookDTO(testBook);
  }

  @Test
  void getAllBooks_ShouldReturnAllBooks() {
    // Arrange
    when(bookRepository.findAll()).thenReturn(Collections.singletonList(testBook));

    // Act
    List<BookDTO> result = bookService.getAllBooks();

    // Assert
    assertEquals(1, result.size());
    assertEquals(testBook.getId(), result.get(0).getId());
    assertEquals(testBook.getTitle(), result.get(0).getTitle());
    verify(bookRepository, times(1)).findAll();
  }

  @Test
  void getBookById_WithValidId_ShouldReturnBook() {
    // Arrange
    when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

    // Act
    BookDTO result = bookService.getBookById(1L);

    // Assert
    assertNotNull(result);
    assertEquals(testBook.getId(), result.getId());
    assertEquals(testBook.getTitle(), result.getTitle());
    verify(bookRepository, times(1)).findById(1L);
  }

  @Test
  void getBookById_WithInvalidId_ShouldThrowException() {
    // Arrange
    when(bookRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(BookNotFoundException.class, () -> bookService.getBookById(99L));
    verify(bookRepository, times(1)).findById(99L);
  }

  @Test
  void getBookByIsbn_WithValidIsbn_ShouldReturnBook() {
    // Arrange
    when(bookRepository.findByIsbn("1234567890")).thenReturn(Optional.of(testBook));

    // Act
    BookDTO result = bookService.getBookByIsbn("1234567890");

    // Assert
    assertNotNull(result);
    assertEquals(testBook.getIsbn(), result.getIsbn());
    verify(bookRepository, times(1)).findByIsbn("1234567890");
  }

  @Test
  void getBookByIsbn_WithInvalidIsbn_ShouldThrowException() {
    // Arrange
    when(bookRepository.findByIsbn("invalid")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(BookNotFoundException.class, () -> bookService.getBookByIsbn("invalid"));
    verify(bookRepository, times(1)).findByIsbn("invalid");
  }

  @Test
  void createBook_WithValidData_ShouldReturnCreatedBook() {
    // Arrange
    when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
    when(bookRepository.save(any(Book.class))).thenReturn(testBook);

    // Act
    BookDTO result = bookService.createBook(testBookDTO);

    // Assert
    assertNotNull(result);
    assertEquals(testBook.getTitle(), result.getTitle());
    verify(bookRepository, times(1)).findByIsbn(anyString());
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  void createBook_WithDuplicateIsbn_ShouldThrowException() {
    // Arrange
    when(bookRepository.findByIsbn("1234567890")).thenReturn(Optional.of(testBook));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> bookService.createBook(testBookDTO));
    verify(bookRepository, times(1)).findByIsbn("1234567890");
    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  void updateBook_WithValidData_ShouldReturnUpdatedBook() {
    // Arrange
    when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
    when(bookRepository.save(any(Book.class))).thenReturn(testBook);

    // Update DTO
    testBookDTO.setTitle("Updated Title");

    // Act
    BookDTO result = bookService.updateBook(1L, testBookDTO);

    // Assert
    assertNotNull(result);
    assertEquals("Updated Title", result.getTitle());
    verify(bookRepository, times(1)).findById(1L);
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  void updateBook_WithInvalidId_ShouldThrowException() {
    // Arrange
    when(bookRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(BookNotFoundException.class, () -> bookService.updateBook(99L, testBookDTO));
    verify(bookRepository, times(1)).findById(99L);
    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  void updateBook_WithDuplicateIsbn_ShouldThrowException() {
    // Arrange
    Book existingBook = new Book("Existing Title", "Existing Author", "Fiction");
    existingBook.setId(2L);
    existingBook.setIsbn("9876543210");

    // Our test book has id 1 and isbn 1234567890
    when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

    // Another book with different id has the isbn we want to change to
    when(bookRepository.findByIsbn("9876543210")).thenReturn(Optional.of(existingBook));

    // Update DTO with conflicting ISBN
    testBookDTO.setIsbn("9876543210");

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> bookService.updateBook(1L, testBookDTO));
    verify(bookRepository, times(1)).findById(1L);
    verify(bookRepository, times(1)).findByIsbn("9876543210");
    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  void deleteBook_WithValidId_ShouldDeleteBook() {
    // Arrange
    when(bookRepository.existsById(1L)).thenReturn(true);
    doNothing().when(bookRepository).deleteById(1L);

    // Act
    bookService.deleteBook(1L);

    // Assert
    verify(bookRepository, times(1)).existsById(1L);
    verify(bookRepository, times(1)).deleteById(1L);
  }

  @Test
  void deleteBook_WithInvalidId_ShouldThrowException() {
    // Arrange
    when(bookRepository.existsById(99L)).thenReturn(false);

    // Act & Assert
    assertThrows(BookNotFoundException.class, () -> bookService.deleteBook(99L));
    verify(bookRepository, times(1)).existsById(99L);
    verify(bookRepository, never()).deleteById(anyLong());
  }

  @Test
  void searchBooks_ShouldReturnFilteredBooks() {
    // Arrange
    when(bookRepository.findByFilters("Test", "Author", "Fiction", "Publisher", "1234"))
        .thenReturn(Arrays.asList(testBook));

    // Act
    List<BookDTO> result =
        bookService.searchBooks("Test", "Author", "Fiction", "Publisher", "1234");

    // Assert
    assertEquals(1, result.size());
    assertEquals(testBook.getId(), result.get(0).getId());
    verify(bookRepository, times(1))
        .findByFilters("Test", "Author", "Fiction", "Publisher", "1234");
  }
}
