package com.example.LMS.service.impl;
import com.example.LMS.DTO.BookDTO;
import com.example.LMS.model.Book;
import com.example.LMS.Exception.BookHasActiveBorrowingsException;
import com.example.LMS.Exception.DuplicateEntryException;
import com.example.LMS.Exception.ResourceNotFoundException;
import com.example.LMS.repository.BookRepository;
import com.example.LMS.repository.BorrowingRecordRepository;
import com.example.LMS.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository,
            BorrowingRecordRepository borrowingRecordRepository,
            ModelMapper modelMapper) {
        this.bookRepository = bookRepository;
        this.borrowingRecordRepository = borrowingRecordRepository;
        this.modelMapper = modelMapper;
    }

    private BookDTO convertToDTO(Book book) {
        return modelMapper.map(book, BookDTO.class);
    }

    private Book convertToEntity(BookDTO bookDTO) {
        if (bookDTO.getId() != null) {
            Book existingBook = bookRepository.findById(bookDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookDTO.getId()));
            modelMapper.map(bookDTO, existingBook);
            return existingBook;
        } else {
            return modelMapper.map(bookDTO, Book.class);
        }
    }

    @Override
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));
        return convertToDTO(book);
    }

    @Override
    @Transactional
    public BookDTO addBook(BookDTO bookDTO) {
        if (bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            throw new DuplicateEntryException("Book with ISBN '" + bookDTO.getIsbn() + "' already exists.");
        }
        Book book = modelMapper.map(bookDTO, Book.class);
        book.setAvailable(true);
        Book savedBook = bookRepository.save(book);
        return convertToDTO(savedBook);
    }

    @Override
    @Transactional
    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));

        if (!existingBook.getIsbn().equals(bookDTO.getIsbn()) && bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            throw new DuplicateEntryException("Book with ISBN '" + bookDTO.getIsbn() + "' already exists.");
        }

        modelMapper.map(bookDTO, existingBook);

        Book updatedBook = bookRepository.save(existingBook);
        return convertToDTO(updatedBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));

        if (borrowingRecordRepository.findByBookAndReturnDateIsNull(book).isPresent()) {
            throw new BookHasActiveBorrowingsException(
                    "Cannot delete book with ID " + id + " as it is currently borrowed.");
        }

        bookRepository.delete(book);
    }

    @Override
    public List<BookDTO> searchBooks(String query) {
        List<Book> booksByTitle = bookRepository.findByTitleContainingIgnoreCase(query);
        List<Book> booksByAuthor = bookRepository.findByAuthorContainingIgnoreCase(query);

        return java.util.stream.Stream.concat(booksByTitle.stream(), booksByAuthor.stream())
                .distinct()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookDTO> getAllAvailableBooks() {
        return bookRepository.findByAvailableTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
