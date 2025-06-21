package com.example.LMS.repository;

import com.example.LMS.model.BorrowingRecord;
import com.example.LMS.model.Book;
import com.example.LMS.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {

    List<BorrowingRecord> findByStudent(Student student);

    Optional<BorrowingRecord> findByBookAndReturnDateIsNull(Book book);

    List<BorrowingRecord> findByReturnDateIsNull();

    List<BorrowingRecord> findByBook(Book book);

    Optional<BorrowingRecord> findByBookAndStudentAndReturnDateIsNull(Book book, Student student);

    List<BorrowingRecord> findByStudentAndReturnDateIsNull(Student student);
}