package com.lms.controller;

import com.lms.dto.BookDTO;
import com.lms.dto.BookPageResponse;
import com.lms.entity.Book;
import com.lms.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * BookController — REST API for the LMS book catalog.
 *
 * PAGINATION endpoints accept:
 *   page=0       → which page (0-indexed)
 *   size=10      → how many per page
 *   sortBy=title → sort column
 *   category=CS  → optional filter
 *
 * Example call: GET /api/books?page=0&size=10&sortBy=title&category=CS
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // ─────────────────────────────────────────────
    // GET all books — paginated
    // ─────────────────────────────────────────────

    /**
     * GET /api/books?page=0&size=10&sortBy=title&category=CS
     *
     * Response includes:
     *   {
     *     "books": [...],
     *     "currentPage": 0,
     *     "totalPages": 14,
     *     "totalElements": 142,
     *     "isLastPage": false
     *   }
     */
    @GetMapping
    public ResponseEntity<BookPageResponse> getAllBooks(
        @RequestParam(defaultValue = "0")     int    page,
        @RequestParam(defaultValue = "10")    int    size,
        @RequestParam(defaultValue = "title") String sortBy,
        @RequestParam(required = false)       String category
    ) {
        return ResponseEntity.ok(
            bookService.getAllBooks(page, size, sortBy, category)
        );
    }

    // ─────────────────────────────────────────────
    // GET single book — cached
    // ─────────────────────────────────────────────

    /**
     * GET /api/books/5
     *
     * First request  → DB query (cache miss)
     * Second request → cache hit, DB not called
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    /**
     * GET /api/books/isbn/978-0132350884
     * Uses idx_book_isbn index → O(log n) lookup + cache
     */
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(bookService.findByIsbn(isbn));
    }

    // ─────────────────────────────────────────────
    // POST — create book (ADMIN only)
    // ─────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Book> createBook(@RequestBody BookDTO dto) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(bookService.createBook(dto));
    }

    // ─────────────────────────────────────────────
    // PUT — update book (ADMIN only) — @CachePut inside
    // ─────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Book> updateBook(
        @PathVariable Long id,
        @RequestBody BookDTO dto
    ) {
        return ResponseEntity.ok(bookService.updateBook(id, dto));
    }

    // ─────────────────────────────────────────────
    // DELETE — remove book (ADMIN only) — @CacheEvict inside
    // ─────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // Admin utility: clear all book cache
    @DeleteMapping("/cache/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> clearCache() {
        bookService.clearAllBookCache();
        return ResponseEntity.ok("Book cache cleared successfully");
    }
}
