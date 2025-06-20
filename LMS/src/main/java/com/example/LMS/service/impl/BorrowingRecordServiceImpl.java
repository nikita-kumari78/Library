package com.example.LMS.service.impl;

import com.example.LMS.DTO.BorrowBookRequestDTO;
import com.example.LMS.DTO.BorrowingRecordDTO;
import com.example.LMS.model.Book;
import com.example.LMS.model.BorrowingRecord;
import com.example.LMS.model.Student;
import com.example.LMS.Exception.BookAlreadyBorrowedException;
import com.example.LMS.Exception.BookNotAvailableException;
import com.example.LMS.Exception.ResourceNotFoundException;
import com.example.LMS.repository.BookRepository;
import com.example.LMS.repository.BorrowingRecordRepository;
import com.example.LMS.repository.StudentRepository;
import com.example.LMS.service.BorrowingRecordService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class BorrowingRecordServiceImpl implements BorrowingRecordService {

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BookRepository bookRepository;
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public BorrowingRecordServiceImpl(BorrowingRecordRepository borrowingRecordRepository,
                                      BookRepository bookRepository,
                                      StudentRepository studentRepository,
                                      ModelMapper modelMapper) {
        this.borrowingRecordRepository = borrowingRecordRepository;
        this.bookRepository = bookRepository;
        this.studentRepository = studentRepository;
        this.modelMapper = modelMapper;
    }

    private BorrowingRecordDTO convertToDTO(BorrowingRecord record) {
        BorrowingRecordDTO dto = modelMapper.map(record, BorrowingRecordDTO.class);
        dto.setBookTitle(record.getBook().getTitle());
        dto.setStudentName(record.getStudent().getName());
        return dto;
    }

    private BorrowingRecord convertToEntity(BorrowBookRequestDTO dto) {
        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + dto.getBookId()));
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + dto.getStudentId()));

        BorrowingRecord record = new BorrowingRecord();
        record.setBook(book);
        record.setStudent(student);
        record.setBorrowDate(LocalDate.now());
        record.setReturnDate(null);

        return record;
    }

    @Override
    @Transactional
    public BorrowingRecordDTO borrowBook(BorrowBookRequestDTO borrowRequestDTO) {
        Book book = bookRepository.findById(borrowRequestDTO.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + borrowRequestDTO.getBookId()));

        Student student = studentRepository.findById(borrowRequestDTO.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + borrowRequestDTO.getStudentId()));

        if (!book.isAvailable()) {
            throw new BookNotAvailableException("Book '" + book.getTitle() + "' (ID: " + book.getId() + ") is not available for borrowing.");
        }

        if (borrowingRecordRepository.findByBookAndReturnDateIsNull(book).isPresent()) {
            throw new BookAlreadyBorrowedException("Book '" + book.getTitle() + "' (ID: " + book.getId() + ") is already borrowed.");
        }

        BorrowingRecord newBorrowing = new BorrowingRecord(book, student, LocalDate.now());
        BorrowingRecord savedBorrowing = borrowingRecordRepository.save(newBorrowing);

        book.setAvailable(false);
        bookRepository.save(book);

        return convertToDTO(savedBorrowing);
    }

    @Override
    @Transactional
    public BorrowingRecordDTO returnBook(Long borrowingId) {
        BorrowingRecord borrowingRecord = borrowingRecordRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing record not found with ID: " + borrowingId));

        if (borrowingRecord.getReturnDate() != null) {
            throw new BookNotAvailableException("Book associated with borrowing record ID " + borrowingId + " has already been returned.");
        }

        Book book = borrowingRecord.getBook();
        if (book.isAvailable()) {
            throw new BookNotAvailableException("Book '" + book.getTitle() + "' (ID: " + book.getId() + ") is unexpectedly available. It might have been returned already.");
        }

        borrowingRecord.setReturnDate(LocalDate.now());
        BorrowingRecord updatedBorrowing = borrowingRecordRepository.save(borrowingRecord);

        book.setAvailable(true);
        bookRepository.save(book);

        return convertToDTO(updatedBorrowing);
    }

    @Override
    public List<BorrowingRecordDTO> getAllBorrowingRecords() {
        return borrowingRecordRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowingRecordDTO> getCurrentlyBorrowedBooks() {
        return borrowingRecordRepository.findByReturnDateIsNull().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowingRecordDTO> getBorrowingHistoryForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        return borrowingRecordRepository.findByStudent(student).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
