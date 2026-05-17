package com.lms.repository;

import com.lms.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * BookRepository
 *
 * Spring Data JPA automatically generates SQL with LIMIT/OFFSET
 * when you pass a Pageable — no manual query writing needed.
 *
 * INDEX BENEFIT: findByIsbn uses idx_book_isbn  → O(log n) lookup
 *                findByCategory uses idx_book_category → fast filter
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Uses idx_book_isbn — direct B-Tree lookup, no full scan
    Optional<Book> findByIsbn(String isbn);

    // Uses idx_book_category + pagination = fast + efficient
    Page<Book> findByCategory(String category, Pageable pageable);

    // Uses idx_book_title for LIKE search (prefix match only — "Clean%" is fast)
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Combined filter + pagination — both columns indexed
    @Query("SELECT b FROM Book b WHERE " +
           "(:category IS NULL OR b.category = :category) AND " +
           "(:status   IS NULL OR b.status   = :status)")
    Page<Book> findByFilters(
        @Param("category") String category,
        @Param("status")   String status,
        Pageable pageable
    );
}
