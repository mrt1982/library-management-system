package com.identitye2e.library.book.model;

import com.identitye2e.library.book.service.exceptions.InsufficientBookCopiesException;
import com.identitye2e.library.book.service.exceptions.ReturnExceededException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookTest {

    @Test
    void createBookObject_valid_success() {
        //Given & When
        Book actualBook = new Book("xsc", "xxx", "auth", 2017, 5, 5);
        //Then
        assertThat(actualBook, allOf(
                hasProperty("totalCopies", is(equalTo(5))),
                hasProperty("availableCopies", is(equalTo(5))),
                hasProperty("isbn", is(equalTo("xsc"))),
                hasProperty("title", is(equalTo("xxx"))),
                hasProperty("author", is(equalTo("auth"))),
                hasProperty("publicationYear", is(equalTo(2017)))));
    }
    @Test
    void createBookObject_invalidNegativeTotalCopies_throwIllegalArgumentException() {
        //Given & When & Then
        assertThrows(IllegalArgumentException.class,
                ()-> new Book("123", "xxx", "auth", 1982, -1, 1));
    }

    @Test
    void createBookObject_invalidNegativeAvailableCopies_throwIllegalArgumentException() {
        //Given & When & Then
        assertThrows(IllegalArgumentException.class,
                ()-> new Book("123", "xxx", "auth", 1982, 1, -1));
    }

    @Test
    void createBookObject_emptyIsbn_throwIllegalArgumentException() {
        //Given & When & Then
        assertThrows(IllegalArgumentException.class,
                ()-> new Book("", "xxx", "auth", 1982, 5, 5));
    }

    @Test
    void createBookObject_nullIsbn_throwIllegalArgumentException() {
        //Given & When & Then
        assertThrows(NullPointerException.class,
                ()-> new Book(null, "xxx", "auth", 1982, 5, 5));
    }

    @Test
    void createBookObject_emptyTitle_throwIllegalArgumentException() {
        //Given & When & Then
        assertThrows(IllegalArgumentException.class,
                ()-> new Book("xsc", "", "auth", 1982, 5, 5));
    }

    @Test
    void createBookObject_nullTitle_throwIllegalArgumentException() {
        //Given & When & Then
        assertThrows(NullPointerException.class,
                ()-> new Book("xsc", null, "auth", 1982, 5, 5));
    }

    @Test
    void createBookObject_emptyAuthor_throwIllegalArgumentException() {
        //Given & When & Then
        assertThrows(IllegalArgumentException.class,
                ()-> new Book("xsc", "xxx", "", 1982, 5, 5));
    }

    @Test
    void createBookObject_nullAuthor_throwIllegalArgumentException() {
        //Given & When & Then
        assertThrows(NullPointerException.class,
                ()-> new Book("xsc", "xxx", null, 1982, 5, 5));
    }

    @Test
    void borrowBook_whenSufficientCopiesAvailable_returnBookWithAvailableCopiesDecrease() throws InsufficientBookCopiesException {
        //Given
        Book currentBook = new Book("xsc", "xxx", "auth", 2017, 5, 5);
        //When
        Book updateBook = currentBook.borrowBook();
        //Then
        assertThat(currentBook, allOf(
                hasProperty("totalCopies", is(equalTo(5))),
                hasProperty("availableCopies", is(equalTo(5))),
                hasProperty("isbn", is(equalTo("xsc"))),
                hasProperty("title", is(equalTo("xxx"))),
                hasProperty("author", is(equalTo("auth"))),
                hasProperty("publicationYear", is(equalTo(2017)))));
        assertThat(updateBook, allOf(
                hasProperty("totalCopies", is(equalTo(5))),
                hasProperty("availableCopies", is(equalTo(4))),
                hasProperty("isbn", is(equalTo("xsc"))),
                hasProperty("title", is(equalTo("xxx"))),
                hasProperty("author", is(equalTo("auth"))),
                hasProperty("publicationYear", is(equalTo(2017)))));
    }

    @Test
    void borrowBook_breachAvailableCopies_throwInsufficientBookCopiesException() {
        //Given
        Book currentBook = new Book("xsc", "xxx", "auth", 2017, 10, 0);
        //When & Then
        Exception exception = assertThrows(InsufficientBookCopiesException.class, currentBook::borrowBook);
        assertThat(exception.getMessage(), is((equalTo("No copies of the book with ISBN xsc are available for borrowing."))));
    }

    @Test
    void returnBook_whenSufficientCopiesAvailable_returnBookWithAvailableCopiesIncrease() throws ReturnExceededException {
        //Given
        Book currentBook = new Book("xsc", "xxx", "auth", 2017, 5, 4);
        //When
        Book updateBook = currentBook.returnBook();
        //Then
        assertThat(currentBook, allOf(
                hasProperty("totalCopies", is(equalTo(5))),
                hasProperty("availableCopies", is(equalTo(4))),
                hasProperty("isbn", is(equalTo("xsc"))),
                hasProperty("title", is(equalTo("xxx"))),
                hasProperty("author", is(equalTo("auth"))),
                hasProperty("publicationYear", is(equalTo(2017)))));
        assertThat(updateBook, allOf(
                hasProperty("totalCopies", is(equalTo(5))),
                hasProperty("availableCopies", is(equalTo(5))),
                hasProperty("isbn", is(equalTo("xsc"))),
                hasProperty("title", is(equalTo("xxx"))),
                hasProperty("author", is(equalTo("auth"))),
                hasProperty("publicationYear", is(equalTo(2017)))));
    }

    @Test
    void returnBook_breachAvailableCopies_throwReturnExceededException() {
        //Given
        Book currentBook = new Book("xsc", "xxx", "auth", 2017, 10, 10);
        //When & Then
        Exception exception = assertThrows(ReturnExceededException.class, currentBook::returnBook);
        assertThat(exception.getMessage(), is((equalTo("Cannot return the book. All copies of the book with ISBN xsc are already in the library."))));
    }
}