package com.identitye2e.library.book.service.exceptions;

public class InsufficientBookCopiesException extends Exception {
    public InsufficientBookCopiesException(String message) {
        super(message);
    }
}
