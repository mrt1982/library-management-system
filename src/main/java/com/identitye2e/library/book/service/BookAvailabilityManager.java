package com.identitye2e.library.book.service;

import com.identitye2e.library.book.model.Book;
import com.identitye2e.library.book.service.exceptions.BookNotFoundException;
import com.identitye2e.library.book.service.exceptions.InsufficientBookCopiesException;
import com.identitye2e.library.book.service.exceptions.ReturnExceededException;

public interface BookAvailabilityManager {
    Book checkAndBorrowBook(String isbn) throws BookNotFoundException, InsufficientBookCopiesException;

    Book checkAndReturnBook(String isbn) throws BookNotFoundException, ReturnExceededException;
}
