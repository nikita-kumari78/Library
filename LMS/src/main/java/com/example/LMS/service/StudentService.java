package com.example.LMS.service;

import com.example.LMS.DTO.StudentDTO;
import java.util.List;

public interface StudentService {

    List<StudentDTO> getAllStudents();

    StudentDTO getStudentById(Long id);

    StudentDTO addStudent(StudentDTO studentDTO);

    StudentDTO updateStudent(Long id, StudentDTO studentDTO);

    void deleteStudent(Long id);

    List<StudentDTO> searchStudents(String name);
}