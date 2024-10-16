package com.identitye2e.library.book.repository;

import com.identitye2e.library.book.model.Book;
import com.identitye2e.library.book.service.exceptions.BookAlreadyExistsException;
import com.identitye2e.library.book.service.exceptions.BookNotFoundException;

import java.util.List;
import java.util.Optional;

public interface BookRepository {
    void createBook(Book book) throws BookAlreadyExistsException;

    void removeBook(String isbn) throws BookNotFoundException;

    Optional<Book> getByIsbn(String isbn);

    List<Book> getByAuthor(String auth);

    Book update(Book updatedBook);
}
