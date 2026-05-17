package com.lms.repository;

import com.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Uses idx_user_email index — O(log n) lookup during login
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
