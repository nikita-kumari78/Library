package com.lms.config;

import com.lms.entity.Book;
import com.lms.entity.User;
import com.lms.repository.BookRepository;
import com.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataSeeder — runs on startup and inserts sample data if DB is empty.
 * Useful for demos and interviews — no manual SQL needed.
 *
 * Creates:
 *   - 1 Admin user  (admin@library.com / admin123)
 *   - 4 Students    (student@college.edu / student123)
 *   - 8 Sample books
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final BookRepository bookRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepo.count() == 0) seedUsers();
        if (bookRepo.count() == 0) seedBooks();
    }

    private void seedUsers() {
        // Admin
        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@library.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(User.Role.ROLE_ADMIN);
        admin.setDepartment("Library");
        userRepo.save(admin);

        // Students
        String[][] students = {
            {"Rahul Sharma",  "rahul@college.edu",  "CS 2021",  "STU-001"},
            {"Priya Patel",   "priya@college.edu",  "IT 2022",  "STU-002"},
            {"Amit Kumar",    "amit@college.edu",   "CS 2021",  "STU-003"},
            {"Sneha Gupta",   "sneha@college.edu",  "ECE 2023", "STU-004"},
        };
        for (String[] s : students) {
            User u = new User();
            u.setName(s[0]); u.setEmail(s[1]);
            u.setPassword(passwordEncoder.encode("student123"));
            u.setRole(User.Role.ROLE_STUDENT);
            u.setDepartment(s[2]); u.setStudentId(s[3]);
            userRepo.save(u);
        }
        log.info("Seeded 5 users (1 admin + 4 students)");
    }

    private void seedBooks() {
        String[][] books = {
            {"Clean Code",                "Robert C. Martin",   "978-0132350884", "Computer Science", "AVAILABLE", "3"},
            {"Introduction to Algorithms","Cormen, Leiserson",  "978-0262033848", "Computer Science", "AVAILABLE", "2"},
            {"DBMS by Korth",             "Silberschatz, Korth","978-0073523323", "Computer Science", "BORROWED",  "2"},
            {"Operating Systems",         "Galvin, Gagne",      "978-1118063330", "Computer Science", "OVERDUE",   "2"},
            {"Computer Networks",         "Forouzan",           "978-0073376226", "Computer Science", "AVAILABLE", "3"},
            {"Spring in Action",          "Craig Walls",        "978-1617294945", "Computer Science", "AVAILABLE", "2"},
            {"Java: Complete Reference",  "Herbert Schildt",    "978-1260440249", "Computer Science", "BORROWED",  "2"},
            {"Design Patterns",           "Gang of Four",       "978-0201633610", "Computer Science", "AVAILABLE", "1"},
        };
        for (String[] b : books) {
            Book book = new Book();
            book.setTitle(b[0]); book.setAuthor(b[1]); book.setIsbn(b[2]);
            book.setCategory(b[3]); book.setStatus(b[4]);
            book.setTotalCopies(Integer.parseInt(b[5]));
            book.setAvailableCopies(b[4].equals("AVAILABLE") ? Integer.parseInt(b[5]) : Integer.parseInt(b[5]) - 1);
            bookRepo.save(book);
        }
        log.info("Seeded 8 books");
    }
}
