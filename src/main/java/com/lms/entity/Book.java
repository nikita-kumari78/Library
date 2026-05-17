package com.lms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Book entity — @Table with @Index for fast lookups on isbn and title.
 * Without these indexes, MySQL does a full table scan on every search.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "books",
    indexes = {
        @Index(name = "idx_book_isbn",  columnList = "isbn",  unique = true),
        @Index(name = "idx_book_title", columnList = "title"),
        @Index(name = "idx_book_category", columnList = "category")
    }
)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    // Indexed — most searches happen by ISBN
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(length = 50)
    private String category;

    @Column(length = 20)
    private String status; // AVAILABLE, BORROWED, OVERDUE

    private Integer totalCopies;

    private Integer availableCopies;
}
