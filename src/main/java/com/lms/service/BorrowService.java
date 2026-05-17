package com.lms.service;

import com.lms.entity.Book;
import com.lms.entity.BorrowRecord;
import com.lms.entity.BorrowRecord.BorrowStatus;
import com.lms.entity.User;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.BookRepository;
import com.lms.repository.BorrowRecordRepository;
import com.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRecordRepository borrowRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;

    /**
     * Issue a book to a student.
     * After issuing, evict book from cache (availableCopies changed).
     */
    @Transactional
    @CacheEvict(value = "books", key = "#bookId")
    public BorrowRecord issueBook(Long userId, Long bookId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Book book = bookRepo.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No copies available for: " + book.getTitle());
        }

        // Decrease available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        if (book.getAvailableCopies() == 0) book.setStatus("BORROWED");
        bookRepo.save(book);

        // Create borrow record — due date 14 days from today
        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setIssueDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(14));
        record.setStatus(BorrowStatus.BORROWED);

        log.info("Book '{}' issued to '{}'", book.getTitle(), user.getName());
        return borrowRepo.save(record);
    }

    /**
     * Return a book — updates record and book availability.
     */
    @Transactional
    @CacheEvict(value = "books", key = "#result.book.id")
    public BorrowRecord returnBook(Long recordId) {
        BorrowRecord record = borrowRepo.findById(recordId)
            .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found: " + recordId));

        record.setReturnDate(LocalDate.now());
        record.setStatus(BorrowStatus.RETURNED);

        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        book.setStatus("AVAILABLE");
        bookRepo.save(book);

        log.info("Book '{}' returned", book.getTitle());
        return borrowRepo.save(record);
    }

    /** Get all borrow records — paginated */
    public Page<BorrowRecord> getAllRecords(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "issueDate"));
        return borrowRepo.findAll(pageable);
    }

    /** Get borrow history for one student — paginated */
    public Page<BorrowRecord> getStudentHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "issueDate"));
        return borrowRepo.findByUserId(userId, pageable);
    }

    /** Get all overdue records */
    public List<BorrowRecord> getOverdueRecords() {
        return borrowRepo.findByStatus(BorrowStatus.OVERDUE);
    }
}
