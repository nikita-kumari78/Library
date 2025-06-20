package com.example.LMS.service;

import com.example.LMS.DTO.BookDTO;
import java.util.List;

public interface BookService {

    List<BookDTO> getAllBooks();

    BookDTO getBookById(Long id);

    BookDTO addBook(BookDTO bookDTO);

    BookDTO updateBook(Long id, BookDTO bookDTO);

    void deleteBook(Long id);

    List<BookDTO> searchBooks(String query);

    List<BookDTO> getAllAvailableBooks();
}