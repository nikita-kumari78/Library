package com.lms.dto;

import com.lms.entity.Book;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * BookPageResponse — wraps paginated result.
 *
 * Returned by GET /api/books?page=0&size=10
 *
 * JSON response:
 * {
 *   "books": [ {...}, {...} ],
 *   "currentPage": 0,
 *   "totalPages": 15,
 *   "totalElements": 142,
 *   "isLastPage": false
 * }
 *
 * Frontend uses totalPages to render pagination buttons.
 * isLastPage tells client when to disable "Next" button.
 */
@Data
@Builder
public class BookPageResponse {
    private List<Book> books;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean isLastPage;
}
