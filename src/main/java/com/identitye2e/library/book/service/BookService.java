package com.identitye2e.library.book.service;

import com.identitye2e.library.book.model.Book;
import com.identitye2e.library.book.service.exceptions.BookAlreadyExistsException;
import com.identitye2e.library.book.service.exceptions.BookNotFoundException;
import com.identitye2e.library.book.service.exceptions.InsufficientBookCopiesException;
import com.identitye2e.library.book.service.exceptions.ReturnExceededException;

import java.util.List;
import java.util.Optional;

public interface BookService {
    Book addBook(String isbn, String title, String author, Integer publicationYear, Integer availableCopies) throws BookAlreadyExistsException;

    void deleteBook(String isbn) throws BookNotFoundException;

    Optional<Book> findBookByISBN(String isbn);

    List<Book> findBooksByAuthor(String auth);

    Book borrowBook(String isbn) throws BookNotFoundException, InsufficientBookCopiesException;

    Book returnBook(String isbn) throws BookNotFoundException, ReturnExceededException;
}
