package com.identitye2e.library.book.service;

import com.identitye2e.library.book.model.Book;
import com.identitye2e.library.book.repository.BookRepository;
import com.identitye2e.library.book.service.exceptions.BookAlreadyExistsException;
import com.identitye2e.library.book.service.exceptions.BookNotFoundException;
import com.identitye2e.library.book.service.exceptions.InsufficientBookCopiesException;
import com.identitye2e.library.book.service.exceptions.ReturnExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class BookServiceServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookAvailabilityManager bookAvailabilityManager;

    @Override
    public Book addBook(String isbn, String title, String author, Integer publicationYear, Integer availableCopies) throws BookAlreadyExistsException {
        Book book = new Book(isbn, title, author, publicationYear, availableCopies, availableCopies);
        bookRepository.createBook(book);
        log.info("Successfully created a book={}", book);
        return book;
    }

    @Override
    public void deleteBook(String isbn) throws BookNotFoundException {
        bookRepository.removeBook(isbn);
        log.info("Successfully deleted book with isbn={}", isbn);
    }

    @Override
    public Optional<Book> findBookByISBN(String isbn) {
        return bookRepository.getByIsbn(isbn);
    }

    @Override
    public List<Book> findBooksByAuthor(String auth) {
        return bookRepository.getByAuthor(auth);
    }

    @Override
    public Book borrowBook(String isbn) throws BookNotFoundException, InsufficientBookCopiesException {
        return bookAvailabilityManager.checkAndBorrowBook(isbn);
    }

    @Override
    public Book returnBook(String isbn) throws BookNotFoundException, ReturnExceededException {
        return bookAvailabilityManager.checkAndReturnBook(isbn);
    }
}
