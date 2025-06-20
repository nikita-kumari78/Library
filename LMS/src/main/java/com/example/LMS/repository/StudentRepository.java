package com.example.LMS.repository;

import com.example.LMS.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByContact(String contact);

    List<Student> findByNameContainingIgnoreCase(String name);

    boolean existsByContact(String contact);
}