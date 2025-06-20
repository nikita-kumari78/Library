package com.example.LMS.controller;

import com.example.LMS.DTO.StudentDTO;
import com.example.LMS.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping({ "", "/list" })
    public String listStudents(Model model) {
        List<StudentDTO> students = studentService.getAllStudents();
        model.addAttribute("students", students);
        return "students/list";
    }

    @GetMapping("/new")
    public String showAddStudentForm(Model model) {
        model.addAttribute("student", new StudentDTO());
        return "students/add-edit";
    }

    @PostMapping("/new")
    public String addStudent(@Valid @ModelAttribute("student") StudentDTO studentDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "students/add-edit";
        }
        try {
            studentService.addStudent(studentDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Student registered successfully!");
            return "redirect:/students";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("student", studentDTO);
            return "redirect:/students/new";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditStudentForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            StudentDTO studentDTO = studentService.getStudentById(id);
            model.addAttribute("student", studentDTO);
            return "students/add-edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/students";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateStudent(@PathVariable Long id,
            @Valid @ModelAttribute("student") StudentDTO studentDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "students/add-edit";
        }
        try {
            studentService.updateStudent(id, studentDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Student updated successfully!");
            return "redirect:/students";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("student", studentDTO);
            return "redirect:/students/edit/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/students";
    }

    @GetMapping("/search")
    public String searchStudents(@RequestParam(value = "name") String name, Model model) {
        List<StudentDTO> students = studentService.searchStudents(name);
        model.addAttribute("students", students);
        model.addAttribute("searchQuery", name);
        return "students/list";
    }
}