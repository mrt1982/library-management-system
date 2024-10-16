package com.identitye2e.library.book.service;

import com.identitye2e.library.infrastructure.cache.BookCache;
import com.identitye2e.library.book.model.Book;
import com.identitye2e.library.book.repository.BookRepository;
import com.identitye2e.library.book.service.exceptions.InsufficientBookCopiesException;
import com.identitye2e.library.book.service.exceptions.BookNotFoundException;
import com.identitye2e.library.book.service.exceptions.ReturnExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@RequiredArgsConstructor
public class BookAvailabilityManagerImpl implements BookAvailabilityManager{
    private final BookRepository bookRepository;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final BookCache<String, Book> bookCacheByIsbn;
    @Override
    public Book checkAndBorrowBook(String isbn) throws BookNotFoundException, InsufficientBookCopiesException {
        readWriteLock.writeLock().lock();
        try {
            Book book = getBook(isbn);
            Book updatedBook = book.borrowBook();
            bookRepository.update(updatedBook);
            bookCacheByIsbn.update(isbn, updatedBook);
            log.info("Successfully borrowed a book={}", updatedBook);
            return updatedBook;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public Book checkAndReturnBook(String isbn) throws BookNotFoundException, ReturnExceededException {
        readWriteLock.writeLock().lock();
        try {
            Book book = getBook(isbn);
            Book updatedBook = book.returnBook();
            bookRepository.update(updatedBook);
            bookCacheByIsbn.update(isbn, updatedBook);
            log.info("Successfully return a book={}", updatedBook);
            return updatedBook;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private Book getBook(String isbn) throws BookNotFoundException {
        Optional<Book> bookOpt = bookRepository.getByIsbn(isbn);
        if(bookOpt.isEmpty()){
            throw new BookNotFoundException("Book with ISBN %s not found.".formatted(isbn));
        }
        return bookOpt.get();
    }
}
