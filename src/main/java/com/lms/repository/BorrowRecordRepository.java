package com.lms.repository;

import com.lms.entity.BorrowRecord;
import com.lms.entity.BorrowRecord.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    // Uses idx_borrow_user index
    Page<BorrowRecord> findByUserId(Long userId, Pageable pageable);
    // Uses idx_borrow_status index
    List<BorrowRecord> findByStatus(BorrowStatus status);
    // Uses idx_borrow_book index
    List<BorrowRecord> findByBookIdAndStatus(Long bookId, BorrowStatus status);
}
