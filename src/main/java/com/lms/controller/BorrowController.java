package com.lms.controller;

import com.lms.entity.BorrowRecord;
import com.lms.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    /** POST /api/borrow?userId=1&bookId=5 */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BorrowRecord> issueBook(
        @RequestParam Long userId,
        @RequestParam Long bookId
    ) {
        return ResponseEntity.ok(borrowService.issueBook(userId, bookId));
    }

    /** PUT /api/borrow/3/return */
    @PutMapping("/{recordId}/return")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BorrowRecord> returnBook(@PathVariable Long recordId) {
        return ResponseEntity.ok(borrowService.returnBook(recordId));
    }

    /** GET /api/borrow?page=0&size=10 */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<BorrowRecord>> getAllRecords(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(borrowService.getAllRecords(page, size));
    }

    /** GET /api/borrow/overdue */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BorrowRecord>> getOverdue() {
        return ResponseEntity.ok(borrowService.getOverdueRecords());
    }

    /** GET /api/borrow/student/1?page=0&size=5 */
    @GetMapping("/student/{userId}")
    public ResponseEntity<Page<BorrowRecord>> getStudentHistory(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "5")  int size
    ) {
        return ResponseEntity.ok(borrowService.getStudentHistory(userId, page, size));
    }
}
