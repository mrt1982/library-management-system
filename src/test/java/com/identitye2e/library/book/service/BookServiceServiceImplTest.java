package com.identitye2e.library.book.service;


import com.identitye2e.library.book.model.Book;
import com.identitye2e.library.book.repository.BookRepository;
import com.identitye2e.library.book.service.exceptions.BookAlreadyExistsException;
import com.identitye2e.library.book.service.exceptions.BookNotFoundException;
import com.identitye2e.library.book.service.exceptions.InsufficientBookCopiesException;
import com.identitye2e.library.book.service.exceptions.ReturnExceededException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceServiceImplTest {
    @Mock
    private BookRepository bookRepositoryMock;

    @Mock
    private BookAvailabilityManager bookAvailabilityManagerMock;
    private BookService testObj;

    @BeforeEach
    void setup() {
        testObj = new BookServiceServiceImpl(bookRepositoryMock, bookAvailabilityManagerMock);
    }

    @Test
    void addBook_validBook_returnBook() throws BookAlreadyExistsException {
        //Given & When
        Book actualBook = testObj.addBook("123", "title", "auth", 2024, 10);
        //Then
        assertThat(actualBook, is(notNullValue()));
        assertThat(actualBook, allOf(
                hasProperty("isbn", Matchers.is(equalTo("123"))),
                hasProperty("title", Matchers.is(equalTo("title"))),
                hasProperty("author", Matchers.is(equalTo("auth"))),
                hasProperty("publicationYear", Matchers.is(equalTo(2024))),
                hasProperty("availableCopies", Matchers.is(equalTo(10)))));
        verify(bookRepositoryMock, times(1)).createBook(any(Book.class));
    }

    @Test
    void addBook_bookAlreadyExist_throwBookAlreadyExistsException() throws BookAlreadyExistsException {
        //Given
        doThrow(BookAlreadyExistsException.class).when(bookRepositoryMock).createBook(any(Book.class));
        //When & Then
        assertThrows(BookAlreadyExistsException.class,
                () -> testObj.addBook("123", "title", "auth", 2024, 10));
    }

    @Test
    void deleteBook_bookExists_removeBookSuccessfully() throws BookNotFoundException {
        testObj.deleteBook("123");

        verify(bookRepositoryMock, times(1)).removeBook("123");
    }

    @Test
    void deleteBook_bookDoesNotExists_throwBookNotFoundException() throws BookNotFoundException {
        //Given
        doThrow(BookNotFoundException.class).when(bookRepositoryMock).removeBook("123");
        //When & Then
        assertThrows(BookNotFoundException.class,
                () -> testObj.deleteBook("123"));
    }

    @Test
    void findBookByISBN_bookExistsForIsbn_returnBook() {
        //Given
        Book book = new Book("123", "title", "auth", 2024, 10, 10);
        when(bookRepositoryMock.getByIsbn("123")).thenReturn(Optional.of(book));
        //When
        Optional<Book> actualBookOpt = testObj.findBookByISBN("123");
        //Then
        assertThat(actualBookOpt.isPresent(), is(equalTo(true)));
        Book actualBook = actualBookOpt.get();
        assertThat(actualBook.getIsbn(), is(equalTo("123")));
        verify(bookRepositoryMock, times(1)).getByIsbn("123");
    }

    @Test
    void findBookByISBN_bookDoesNotExistsForIsbn_returnEmptyBook() {
        //Given
        when(bookRepositoryMock.getByIsbn("123")).thenReturn(Optional.empty());
        //When
        Optional<Book> actualBookOpt = testObj.findBookByISBN("123");
        //Then
        assertThat(actualBookOpt.isPresent(), is(equalTo(false)));
        verify(bookRepositoryMock, times(1)).getByIsbn("123");
    }

    @Test
    void findBooksByAuthor_booksExistsForAuthor_returnListOfBooks() {
        //Given
        Book authBook1 = new Book("123", "title", "auth", 2024, 10, 10);
        Book authBook2 = new Book("1234", "title2", "auth", 2024, 10, 10);
        List<Book> authBooks = List.of(authBook1, authBook2);
        when(bookRepositoryMock.getByAuthor("auth")).thenReturn(authBooks);
        //When
        List<Book> actualBooksByAuth = testObj.findBooksByAuthor("auth");
        //Then
        assertThat(actualBooksByAuth.isEmpty(), is(false));
        assertThat(actualBooksByAuth.size(), is(equalTo(2)));
        verify(bookRepositoryMock, times(1)).getByAuthor("auth");
    }

    @Test
    void findBooksByAuthor_noBooksExistForAuthor_returnEmptyListOfBooks() {
        //Given
        List<Book> authBooks = List.of();
        when(bookRepositoryMock.getByAuthor("auth")).thenReturn(authBooks);
        //When
        List<Book> actualBooksByAuth = testObj.findBooksByAuthor("auth");
        //Then
        assertThat(actualBooksByAuth.isEmpty(), is(true));
        assertThat(actualBooksByAuth.size(), is(equalTo(0)));
        verify(bookRepositoryMock, times(1)).getByAuthor("auth");
    }

    @Test
    void borrowBook_validNumberOfCopiesAvailable_success() throws BookNotFoundException, InsufficientBookCopiesException {
        //Given & When
        testObj.borrowBook("123");
        //Then
        verify(bookAvailabilityManagerMock, times(1)).checkAndBorrowBook("123");
    }

    @Test
    void borrowBook_bookIsNotAvailable_throwBookNotFoundException() throws BookNotFoundException, InsufficientBookCopiesException {
        //Given
        doThrow(BookNotFoundException.class).when(bookAvailabilityManagerMock).checkAndBorrowBook("123");
        // When & Then
        assertThrows(BookNotFoundException.class,
                () -> testObj.borrowBook("123"));
    }

    @Test
    void borrowBook_bookExceededNumberOfCopies_throwInsufficientBookCopiesException() throws BookNotFoundException, InsufficientBookCopiesException {
        //Given
        doThrow(InsufficientBookCopiesException.class).when(bookAvailabilityManagerMock).checkAndBorrowBook("123");
        // When & Then
        assertThrows(InsufficientBookCopiesException.class,
                () -> testObj.borrowBook("123"));
    }

    @Test
    void returnBook_returnBookIsWithinNumberOfCopies_success() throws BookNotFoundException, ReturnExceededException {
        //Given & When
        testObj.returnBook("123");
        //Then
        verify(bookAvailabilityManagerMock, times(1)).checkAndReturnBook("123");
    }

    @Test
    void returnBook_bookIsNotAvailable_throwBookNotFoundException() throws BookNotFoundException, ReturnExceededException {
        //Given
        doThrow(BookNotFoundException.class).when(bookAvailabilityManagerMock).checkAndReturnBook("123");
        // When & Then
        assertThrows(BookNotFoundException.class,
                () -> testObj.returnBook("123"));
    }

    @Test
    void returnBook_returnBookIsGreaterThanNumberOfCopies_throwReturnExceededException() throws BookNotFoundException, ReturnExceededException {
        //Given
        doThrow(ReturnExceededException.class).when(bookAvailabilityManagerMock).checkAndReturnBook("123");
        // When & Then
        assertThrows(ReturnExceededException.class,
                () -> testObj.returnBook("123"));
    }

}