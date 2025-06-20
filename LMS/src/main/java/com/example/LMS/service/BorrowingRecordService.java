package com.example.LMS.service;

import com.example.LMS.DTO.BorrowBookRequestDTO;
import com.example.LMS.DTO.BorrowingRecordDTO;
import java.util.List;

public interface BorrowingRecordService {

    BorrowingRecordDTO borrowBook(BorrowBookRequestDTO borrowRequestDTO);

    BorrowingRecordDTO returnBook(Long borrowingId);

    List<BorrowingRecordDTO> getAllBorrowingRecords();

    List<BorrowingRecordDTO> getCurrentlyBorrowedBooks();

    List<BorrowingRecordDTO> getBorrowingHistoryForStudent(Long studentId);
}