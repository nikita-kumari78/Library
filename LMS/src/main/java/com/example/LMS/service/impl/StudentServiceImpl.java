package com.example.LMS.service.impl;

import com.example.LMS.DTO.StudentDTO;
import com.example.LMS.model.Student;
import com.example.LMS.Exception.DuplicateEntryException;
import com.example.LMS.Exception.ResourceNotFoundException;
import com.example.LMS.Exception.StudentHasActiveBorrowingsException;
import com.example.LMS.repository.BorrowingRecordRepository;
import com.example.LMS.repository.StudentRepository;
import com.example.LMS.service.StudentService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository,
            BorrowingRecordRepository borrowingRecordRepository,
            ModelMapper modelMapper) {
        this.studentRepository = studentRepository;
        this.borrowingRecordRepository = borrowingRecordRepository;
        this.modelMapper = modelMapper;
    }

    private StudentDTO convertToDTO(Student student) {
        return modelMapper.map(student, StudentDTO.class);
    }

    private Student convertToEntity(StudentDTO studentDTO) {
        if (studentDTO.getId() != null) {
            Student existingStudent = studentRepository.findById(studentDTO.getId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Student not found with ID: " + studentDTO.getId()));
            modelMapper.map(studentDTO, existingStudent);
            return existingStudent;
        } else {
            return modelMapper.map(studentDTO, Student.class);
        }
    }

    @Override
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        return convertToDTO(student);
    }

    @Override
    @Transactional
    public StudentDTO addStudent(StudentDTO studentDTO) {
        if (studentRepository.existsByContact(studentDTO.getContact())) {
            throw new DuplicateEntryException("Student with contact '" + studentDTO.getContact() + "' already exists.");
        }
        Student student = modelMapper.map(studentDTO, Student.class);
        Student savedStudent = studentRepository.save(student);
        return convertToDTO(savedStudent);
    }

    @Override
    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        if (!existingStudent.getContact().equals(studentDTO.getContact())
                && studentRepository.existsByContact(studentDTO.getContact())) {
            throw new DuplicateEntryException("Student with contact '" + studentDTO.getContact() + "' already exists.");
        }

        modelMapper.map(studentDTO, existingStudent);

        Student updatedStudent = studentRepository.save(existingStudent);
        return convertToDTO(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        if (!borrowingRecordRepository.findByStudentAndReturnDateIsNull(student).isEmpty()) {
            throw new StudentHasActiveBorrowingsException(
                    "Cannot delete student with ID " + id + " as they have active borrowings.");
        }

        studentRepository.delete(student);
    }

    @Override
    public List<StudentDTO> searchStudents(String name) {
        return studentRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
