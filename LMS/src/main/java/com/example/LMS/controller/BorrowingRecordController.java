package com.example.LMS.controller;
import com.example.LMS.DTO.BorrowBookRequestDTO;
import com.example.LMS.DTO.BorrowingRecordDTO;
import com.example.LMS.DTO.BookDTO;
import com.example.LMS.DTO.StudentDTO;
import com.example.LMS.service.BookService;
import com.example.LMS.service.BorrowingRecordService;
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
@RequestMapping("/borrowings")
public class BorrowingRecordController {

    private final BorrowingRecordService borrowingRecordService;
    private final BookService bookService;
    private final StudentService studentService;

    @Autowired
    public BorrowingRecordController(BorrowingRecordService borrowingRecordService,
            BookService bookService,
            StudentService studentService) {
        this.borrowingRecordService = borrowingRecordService;
        this.bookService = bookService;
        this.studentService = studentService;
    }

    @GetMapping({ "", "/list" })
    public String listAllBorrowingRecords(Model model) {
        List<BorrowingRecordDTO> records = borrowingRecordService.getAllBorrowingRecords();
        model.addAttribute("records", records);
        return "borrowings/list";
    }

    @GetMapping("/active")
    public String listActiveBorrowings(Model model) {
        List<BorrowingRecordDTO> activeRecords = borrowingRecordService.getCurrentlyBorrowedBooks();
        model.addAttribute("records", activeRecords);
        model.addAttribute("title", "Currently Borrowed Books");
        return "borrowings/list";
    }

    @GetMapping("/borrow/new")
    public String showBorrowBookForm(Model model) {
        model.addAttribute("borrowRequest", new BorrowBookRequestDTO());
        model.addAttribute("books", bookService.getAllAvailableBooks());
        model.addAttribute("students", studentService.getAllStudents());
        return "borrowings/borrow-form";
    }

    @PostMapping("/borrow")
    public String borrowBook(@Valid @ModelAttribute("borrowRequest") BorrowBookRequestDTO borrowRequestDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("books", bookService.getAllAvailableBooks());
            model.addAttribute("students", studentService.getAllStudents());
            return "borrowings/borrow-form";
        }
        try {
            borrowingRecordService.borrowBook(borrowRequestDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Book borrowed successfully!");
            return "redirect:/borrowings/active";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("borrowRequest", borrowRequestDTO);
            return "redirect:/borrowings/borrow/new";
        }
    }

    @GetMapping("/return/{id}")
    public String returnBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            borrowingRecordService.returnBook(id);
            redirectAttributes.addFlashAttribute("successMessage", "Book returned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/borrowings/active";
    }

    @GetMapping("/student/{studentId}")
    public String getStudentBorrowingHistory(@PathVariable Long studentId, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            StudentDTO student = studentService.getStudentById(studentId);
            List<BorrowingRecordDTO> history = borrowingRecordService.getBorrowingHistoryForStudent(studentId);
            model.addAttribute("records", history);
            model.addAttribute("title", "Borrowing History for " + student.getName());
            return "borrowings/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/students";
        }
    }
}