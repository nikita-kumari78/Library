package com.example.LMS.DTO; // Updated package to align with your provided entity

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;


@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class BorrowingRecordDTO {

    private Long id; 


    private Long bookId;
    private Long studentId;

    
    private String bookTitle; 
    private String studentName; 

    private LocalDate borrowDate;
    private LocalDate returnDate;
}



