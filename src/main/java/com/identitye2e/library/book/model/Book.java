package com.identitye2e.library.book.model;

import com.identitye2e.library.book.service.exceptions.InsufficientBookCopiesException;
import com.identitye2e.library.book.service.exceptions.ReturnExceededException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Book {
    @EqualsAndHashCode.Include
    private final String isbn;
    private final String title;
    private final String author;
    private final Integer publicationYear;
    private final Integer totalCopies;
    private final Integer availableCopies;

    public Book(String isbn, String title, String author, Integer publicationYear, Integer totalCopies, Integer availableCopies) {
        if (totalCopies < 0) {
            throw new IllegalArgumentException("Total copies cannot be negative");
        }
        if (availableCopies < 0) {
            throw new IllegalArgumentException("Available copies cannot be negative");
        }
        ObjectUtils.requireNonEmpty(isbn, "ISBN cannot be null or empty");
        ObjectUtils.requireNonEmpty(title, "Title cannot be null or empty");
        ObjectUtils.requireNonEmpty(author, "Author cannot be null or empty");

        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public Book borrowBook () throws InsufficientBookCopiesException {
        final int remainingAvailableCopies = availableCopies - 1;
        if (remainingAvailableCopies < 0) {
            throw new InsufficientBookCopiesException("No copies of the book with ISBN %s are available for borrowing.".formatted(isbn));
        }
        return new Book(isbn, title, author, publicationYear, totalCopies, remainingAvailableCopies);
    }

    public Book returnBook() throws ReturnExceededException {
        final int remainingAvailableCopies = availableCopies + 1;
        if (remainingAvailableCopies > totalCopies) {
            throw new ReturnExceededException("Cannot return the book. All copies of the book with ISBN %s are already in the library.".formatted(isbn));
        }
        return new Book(isbn, title, author, publicationYear, totalCopies, remainingAvailableCopies);
    }
}

