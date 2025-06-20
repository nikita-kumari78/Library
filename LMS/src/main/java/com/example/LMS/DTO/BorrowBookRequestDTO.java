package com.example.LMS.DTO; // Corrected package to 'dto'

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data 
@NoArgsConstructor
@AllArgsConstructor 
@Builder 
public class BorrowBookRequestDTO {

    @NotNull(message = "Book ID cannot be null")
    private Long bookId;

    @NotNull(message = "Student ID cannot be null")
    private Long studentId;
}
