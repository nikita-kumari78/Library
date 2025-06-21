package com.example.LMS.service.impl;

import com.example.LMS.DTO.BookDTO;
import com.example.LMS.model.Book;
import com.example.LMS.repository.BookRepository;
import com.example.LMS.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    private BookDTO mapToDTO(Book book) {
        return BookDTO.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .available(book.isAvailable())
                .build();
    }

    private Book mapToEntity(BookDTO dto) {
        return new Book(dto.getIsbn(), dto.getTitle(), dto.getAuthor());
    }

    @Override
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookDTO getBookById(Long id) {
        return bookRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
    }

    @Override
    public void addBook(BookDTO bookDTO) {
        if (bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            throw new RuntimeException("Book with this ISBN already exists.");
        }
        Book book = mapToEntity(bookDTO);
        bookRepository.save(book);
    }

    @Override
    public void updateBook(Long id, BookDTO bookDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
        book.setIsbn(bookDTO.getIsbn());
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setAvailable(bookDTO.isAvailable());
        bookRepository.save(book);
    }

    @Override
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete: Book not found.");
        }
        bookRepository.deleteById(id);
    }

    @Override
    public List<BookDTO> searchBooks(String query) {
        return bookRepository.findByTitleContainingIgnoreCase(query)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookDTO> getAllAvailableBooks() {
        return bookRepository.findByAvailableTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}
