package com.example.LMS.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BookHasActiveBorrowingsException extends RuntimeException {

    public BookHasActiveBorrowingsException(String message) {
        super(message);
    }
}
