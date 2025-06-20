package com.example.LMS.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Entity
@Table(name = "books") 
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(nullable = false, unique = true) 
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false) 
    private String author;

    @Column(nullable = false) 
    private boolean available = true; 

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BorrowingRecord> borrowings; 

    /**
     
     *
     * @param isbn 
     * @param title 
     * @param author 
     */
    public Book(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.available = true; 
    }
}
