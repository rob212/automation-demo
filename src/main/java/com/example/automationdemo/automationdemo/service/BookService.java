package com.example.automationdemo.automationdemo.service;

import com.example.automationdemo.automationdemo.dto.BookDTO;
import com.example.automationdemo.automationdemo.exception.BookNotFoundException;
import com.example.automationdemo.automationdemo.model.Book;
import com.example.automationdemo.automationdemo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // Get all books
    @Transactional(readOnly = true)
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(BookDTO::new)
                .collect(Collectors.toList());
    }

    // Get book by ID
    @Transactional(readOnly = true)
    public BookDTO getBookById(Long id) {
        return bookRepository.findById(id)
                .map(BookDTO::new)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
    }

    // Get book by ISBN
    @Transactional(readOnly = true)
    public BookDTO getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .map(BookDTO::new)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ISBN: " + isbn));
    }

    // Create a new book
    @Transactional
    public BookDTO createBook(BookDTO bookDTO) {
        // Check if ISBN already exists
        if (bookDTO.getIsbn() != null && !bookDTO.getIsbn().isEmpty()) {
            Optional<Book> existingBook = bookRepository.findByIsbn(bookDTO.getIsbn());
            if (existingBook.isPresent()) {
                throw new IllegalArgumentException("Book with ISBN " + bookDTO.getIsbn() + " already exists");
            }
        }

        Book book = bookDTO.toEntity();
        book.setId(null); // Ensure we're creating a new book, not updating
        Book savedBook = bookRepository.save(book);
        return new BookDTO(savedBook);
    }

    // Update an existing book
    @Transactional
    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));

        // Check ISBN uniqueness if it's being changed
        if (bookDTO.getIsbn() != null && !bookDTO.getIsbn().equals(existingBook.getIsbn())) {
            bookRepository.findByIsbn(bookDTO.getIsbn()).ifPresent(book -> {
                if (!book.getId().equals(id)) {
                    throw new IllegalArgumentException("Book with ISBN " + bookDTO.getIsbn() + " already exists");
                }
            });
        }

        // Update fields
        existingBook.setTitle(bookDTO.getTitle());
        existingBook.setAuthor(bookDTO.getAuthor());
        existingBook.setGenre(bookDTO.getGenre());
        existingBook.setPublisher(bookDTO.getPublisher());
        existingBook.setIsbn(bookDTO.getIsbn());
        existingBook.setPublicationDate(bookDTO.getPublicationDate());
        existingBook.setDescription(bookDTO.getDescription());

        Book updatedBook = bookRepository.save(existingBook);
        return new BookDTO(updatedBook);
    }

    // Delete a book
    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book not found with ID: " + id);
        }
        bookRepository.deleteById(id);
    }

    // Search books by filters
    @Transactional(readOnly = true)
    public List<BookDTO> searchBooks(String title, String author, String genre, String publisher, String isbn) {
        return bookRepository.findByFilters(title, author, genre, publisher, isbn)
                .stream()
                .map(BookDTO::new)
                .collect(Collectors.toList());
    }
}
