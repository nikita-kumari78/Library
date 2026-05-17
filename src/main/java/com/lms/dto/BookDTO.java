package com.lms.dto;

import lombok.Data;

/**
 * BookDTO — used for CREATE and UPDATE requests.
 * Never expose the entity directly in API — use DTOs.
 */
@Data
public class BookDTO {
    private String title;
    private String author;
    private String isbn;
    private String category;
    private Integer totalCopies;
}
