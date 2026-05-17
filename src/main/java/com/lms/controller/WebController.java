package com.lms.controller;

import com.lms.entity.Book;
import com.lms.entity.BorrowRecord.BorrowStatus;
import com.lms.entity.User;
import com.lms.repository.BookRepository;
import com.lms.repository.BorrowRecordRepository;
import com.lms.repository.UserRepository;
import com.lms.service.BookService;
import com.lms.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * WebController — serves Thymeleaf HTML pages.
 * Separate from REST API controllers (@RestController).
 */
@Controller
@RequiredArgsConstructor
public class WebController {

    private final BookRepository bookRepo;
    private final BorrowRecordRepository borrowRepo;
    private final UserRepository userRepo;
    private final BookService bookService;
    private final BorrowService borrowService;

    // ── Root redirect ──────────────────────────────────────────
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {
        if (error != null)
            model.addAttribute("error", "Invalid email or password!");
        if (logout != null)
            model.addAttribute("success", "Logged out successfully.");
        return "login";

    }

    // ── Dashboard ──────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalBooks", bookRepo.count());
        model.addAttribute("totalMembers", userRepo.count());
        model.addAttribute("totalBorrowed", borrowRepo.findByStatus(BorrowStatus.BORROWED).size());
        model.addAttribute("totalOverdue", borrowRepo.findByStatus(BorrowStatus.OVERDUE).size());
        model.addAttribute("recentBooks", bookRepo.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"))).getContent());
        model.addAttribute("currentDate",
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        return "dashboard";
    }

    // ── Books ──────────────────────────────────────────────────
    @GetMapping("/books")
    public String books(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String category,
            Model model) {
        var pageable = PageRequest.of(page, size, Sort.by("title"));
        var bookPage = (category != null && !category.isBlank())
                ? bookRepo.findByCategory(category, pageable)
                : bookRepo.findAll(pageable);
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("category", category);
        return "books";
    }

    @GetMapping("/books/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "book-form";
    }

    @PostMapping("/books/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveBook(@ModelAttribute Book book, RedirectAttributes ra) {
        book.setStatus("AVAILABLE");
        book.setAvailableCopies(book.getTotalCopies());
        bookRepo.save(book);
        ra.addFlashAttribute("success", "Book added successfully!");
        return "redirect:/books";
    }

    @GetMapping("/books/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editBookForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found")));
        return "book-form";
    }

    @PostMapping("/books/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateBook(@PathVariable Long id, @ModelAttribute Book updated,
            RedirectAttributes ra) {
        bookRepo.findById(id).ifPresent(book -> {
            book.setTitle(updated.getTitle());
            book.setAuthor(updated.getAuthor());
            book.setCategory(updated.getCategory());
            book.setTotalCopies(updated.getTotalCopies());
            bookRepo.save(book);
        });
        ra.addFlashAttribute("success", "Book updated successfully!");
        return "redirect:/books";
    }

    @PostMapping("/books/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteBook(@PathVariable Long id, RedirectAttributes ra) {
        bookService.deleteBook(id);
        ra.addFlashAttribute("success", "Book deleted.");
        return "redirect:/books";
    }

    // ── Members ────────────────────────────────────────────────
    @GetMapping("/members")
    @PreAuthorize("hasRole('ADMIN')")
    public String members(Model model) {
        model.addAttribute("members", userRepo.findAll());
        return "members";
    }

    // ── Borrowing ──────────────────────────────────────────────
    @GetMapping("/borrow")
    public String borrowRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model) {
        model.addAttribute("records", borrowService.getAllRecords(page, size));
        model.addAttribute("overdueCount", borrowRepo.findByStatus(BorrowStatus.OVERDUE).size());
        return "borrow";
    }

    @GetMapping("/borrow/issue")
    @PreAuthorize("hasRole('ADMIN')")
    public String issueBookForm(Model model) {
        model.addAttribute("members",
                userRepo.findAll().stream()
                        .filter(u -> u.getRole() == User.Role.ROLE_STUDENT)
                        .toList());
        model.addAttribute("availableBooks",
                bookRepo.findAll().stream()
                        .filter(b -> "AVAILABLE".equals(b.getStatus()))
                        .toList());
        return "issue-book";
    }

    @PostMapping("/borrow/issue")
    @PreAuthorize("hasRole('ADMIN')")
    public String issueBook(@RequestParam Long userId, @RequestParam Long bookId,
            RedirectAttributes ra) {
        try {
            borrowService.issueBook(userId, bookId);
            ra.addFlashAttribute("success", "Book issued successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/borrow";
    }

    @PostMapping("/borrow/return/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String returnBook(@PathVariable Long id, RedirectAttributes ra) {
        try {
            borrowService.returnBook(id);
            ra.addFlashAttribute("success", "Book returned successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/borrow";
    }
}
