package com.lms.service;

import com.lms.dto.BookDTO;
import com.lms.dto.BookPageResponse;
import com.lms.entity.Book;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * BookService — THREE scalability features combined:
 *
 * 1. PAGINATION  → PageRequest.of(page, size, sort)
 * 2. INDEXING    → handled at entity/DB level, transparent here
 * 3. CACHING     → @Cacheable, @CachePut, @CacheEvict
 *
 * How caching works here:
 *   - findById("books", key=#id)   → 1st call: DB hit, stored in cache
 *                                    2nd+ call: returned from cache, DB NOT called
 *   - updateBook(@CachePut)        → updates cache with new value after DB update
 *   - deleteBook(@CacheEvict)      → removes stale entry from cache
 *   - getAllBooks() is NOT cached  → paginated list changes too often; caching it
 *                                    would serve stale page data. Cache single records instead.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    // ─────────────────────────────────────────────
    // PAGINATION — Get books with page + sort
    // ─────────────────────────────────────────────

    /**
     * Returns one page of books.
     *
     * @param page     0-based page number (page=0 → first page)
     * @param size     number of books per page (default 10)
     * @param sortBy   column to sort by (e.g. "title", "author")
     * @param category optional filter — uses idx_book_category index
     *
     * SQL generated: SELECT * FROM books
     *                WHERE category = ?        ← index scan
     *                ORDER BY title ASC
     *                LIMIT 10 OFFSET 0;        ← pagination
     */
    public BookPageResponse getAllBooks(int page, int size, String sortBy, String category) {
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.ASC, sortBy)
        );

        Page<Book> bookPage = (category != null && !category.isBlank())
            ? bookRepository.findByCategory(category, pageable)
            : bookRepository.findAll(pageable);

        log.info("Fetched page {} of {} — {} books total",
            page, bookPage.getTotalPages(), bookPage.getTotalElements());

        return BookPageResponse.builder()
            .books(bookPage.getContent())
            .currentPage(bookPage.getNumber())
            .totalPages(bookPage.getTotalPages())
            .totalElements(bookPage.getTotalElements())
            .isLastPage(bookPage.isLast())
            .build();
    }

    // ─────────────────────────────────────────────
    // CACHING — Single book lookup
    // ─────────────────────────────────────────────

    /**
     * @Cacheable("books") means:
     *   - key = "books::5" (cache name + id)
     *   - First call   → hits DB, stores result in cache
     *   - Second+ call → returns from cache, DB is NOT called
     *
     * You'll see "Fetching book from DB" in logs only on first call.
     */
    @Cacheable(value = "books", key = "#id")
    public Book findById(Long id) {
        log.info("Fetching book from DB — id: {}", id);  // Only logged on cache MISS
        return bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
    }

    /**
     * @Cacheable on ISBN lookup — isbn is indexed, cache adds another layer.
     * Cache key = the isbn string itself.
     */
    @Cacheable(value = "books", key = "#isbn")
    public Book findByIsbn(String isbn) {
        log.info("Fetching book from DB — isbn: {}", isbn);
        return bookRepository.findByIsbn(isbn)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + isbn));
    }

    // ─────────────────────────────────────────────
    // CREATE — no cache involvement on insert
    // ─────────────────────────────────────────────

    public Book createBook(BookDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setCategory(dto.getCategory());
        book.setStatus("AVAILABLE");
        book.setTotalCopies(dto.getTotalCopies());
        book.setAvailableCopies(dto.getTotalCopies());
        return bookRepository.save(book);
    }

    // ─────────────────────────────────────────────
    // UPDATE — @CachePut refreshes cache
    // ─────────────────────────────────────────────

    /**
     * @CachePut always calls the DB AND updates the cache.
     * Unlike @Cacheable, it never skips the method.
     * Use this on updates so cache stays in sync with DB.
     */
    @CachePut(value = "books", key = "#id")
    public Book updateBook(Long id, BookDTO dto) {
        Book book = findById(id);         // may come from cache
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setCategory(dto.getCategory());
        book.setTotalCopies(dto.getTotalCopies());
        log.info("Updated book {} — cache refreshed", id);
        return bookRepository.save(book);
    }

    // ─────────────────────────────────────────────
    // DELETE — @CacheEvict removes from cache
    // ─────────────────────────────────────────────

    /**
     * @CacheEvict removes the entry so next findById goes to DB.
     * Without this, deleted books would still be served from cache!
     */
    @CacheEvict(value = "books", key = "#id")
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
        log.info("Deleted book {} — evicted from cache", id);
    }

    /**
     * Nuclear option — clear ALL cached books.
     * Use when bulk import or admin reset happens.
     */
    @CacheEvict(value = "books", allEntries = true)
    public void clearAllBookCache() {
        log.info("All book cache entries cleared");
    }
}
