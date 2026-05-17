# 📚 Library Management System

A full-stack web application built with **Spring Boot**, **Spring Security**, **JWT Authentication**, **Thymeleaf**, and **MySQL**.

---

## 🚀 Features

- 🔐 **JWT Authentication** — Secure login with HMAC-SHA256 signed tokens
- 👥 **Role-Based Access Control** — Admin and Student roles
- 📚 **Book Management** — Add, Edit, Delete books (Admin only)
- 🔄 **Borrowing System** — Issue and return books with due date tracking
- ⚠️ **Overdue Tracking** — Automatic overdue detection
- 📄 **Pagination** — Efficient data loading with Spring Data JPA
- ⚡ **Caching** — Caffeine in-memory cache with `@Cacheable`
- 🗂️ **Indexing** — MySQL B-Tree indexes on isbn, title, category
- 🎨 **Thymeleaf UI** — Server-side rendered responsive dashboard

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.2, Java 17 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Database | MySQL 8 + Spring Data JPA + Hibernate |
| Caching | Spring Cache + Caffeine |
| Frontend | Thymeleaf + Custom CSS |
| Build Tool | Maven |

---

## 📁 Project Structure

```
src/main/java/com/lms/
├── config/
│   ├── CacheConfig.java        # Caffeine cache setup
│   ├── SecurityConfig.java     # Spring Security + JWT config
│   └── DataSeeder.java         # Sample data on startup
├── controller/
│   ├── WebController.java      # Thymeleaf pages
│   ├── BookController.java     # REST API for books
│   ├── AuthController.java     # Login/Register API
│   └── BorrowController.java   # Borrow/Return API
├── entity/
│   ├── Book.java               # @Entity with @Index
│   ├── User.java               # Admin + Student roles
│   └── BorrowRecord.java       # Borrow history
├── repository/
│   ├── BookRepository.java     # Pagination queries
│   ├── UserRepository.java
│   └── BorrowRecordRepository.java
├── security/
│   ├── JwtUtil.java            # Token generate + validate
│   └── JwtAuthFilter.java      # Filter on every request
└── service/
    ├── BookService.java        # @Cacheable, @CachePut, @CacheEvict
    ├── BorrowService.java      # Issue + Return logic
    └── CustomUserDetailsService.java
```

---

## ⚙️ Setup & Run

### Prerequisites
- Java 17
- MySQL 8
- Maven

### 1. Create Database
```sql
CREATE DATABASE lms_db;
```

### 2. Configure application.properties
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/lms_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Run
```bash
mvn spring-boot:run
```

### 4. Open Browser
```
http://localhost:8080
```

---

## 👤 Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@library.com | admin123 |
| Student | rahul@college.edu | student123 |

---

## 🔑 REST API Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/login` | Get JWT token | Public |
| GET | `/api/books?page=0&size=10` | List books (paginated) | Authenticated |
| POST | `/api/books` | Add new book | Admin |
| PUT | `/api/books/{id}` | Update book | Admin |
| DELETE | `/api/books/{id}` | Delete book | Admin |
| POST | `/api/borrow?userId=1&bookId=1` | Issue book | Admin |
| PUT | `/api/borrow/{id}/return` | Return book | Admin |

---

## 📊 Scalability Features

### Pagination
```java
PageRequest.of(page, size, Sort.by("title"))
// SQL: SELECT * FROM books ORDER BY title LIMIT 10 OFFSET 0
```

### Indexing
```java
@Table(indexes = {
    @Index(columnList = "isbn"),    // O(log n) lookup
    @Index(columnList = "title"),
    @Index(columnList = "category")
})
```

### Caching
```java
@Cacheable("books")        // Read from cache
@CachePut("books")         // Update cache after write
@CacheEvict("books")       // Remove from cache on delete
```

---

## 🔐 Security Flow

```
POST /api/auth/login
        ↓
Spring Security validates credentials
        ↓
JwtUtil.generateToken() — HMAC-SHA256
        ↓
Client stores token
        ↓
Authorization: Bearer <token> on every request
        ↓
JwtAuthFilter validates → SecurityContext set
        ↓
@PreAuthorize("hasRole('ADMIN')") checks role
```

---

## 📸 Screenshots

> Login Page → Dashboard → Books (with pagination) → Borrowing Records

---

*Built with ❤️ using Spring Boot*
