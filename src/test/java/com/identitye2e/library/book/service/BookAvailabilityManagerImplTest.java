package com.identitye2e.library.book.service;

import com.identitye2e.library.book.model.Book;
import com.identitye2e.library.book.repository.BookRepository;
import com.identitye2e.library.book.service.exceptions.BookNotFoundException;
import com.identitye2e.library.book.service.exceptions.InsufficientBookCopiesException;
import com.identitye2e.library.book.service.exceptions.ReturnExceededException;
import com.identitye2e.library.infrastructure.cache.BookCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookAvailabilityManagerImplTest {
    @Mock
    private BookRepository bookRepositoryMock;
    @Mock
    private BookCache<String, Book> bookCacheByIsbnMock;
    private BookAvailabilityManager testObj;

    @BeforeEach
    void setup() {
        testObj = new BookAvailabilityManagerImpl(bookRepositoryMock, bookCacheByIsbnMock);
    }

    @Test
    void checkAndBorrowBook_availableCopiesUpdated_success() throws BookNotFoundException, InsufficientBookCopiesException {
        //Given
        Book book = new Book("123", "title", "author", 2024, 10, 10);
        when(bookRepositoryMock.getByIsbn("123")).thenReturn(Optional.of(book));
        //When
        testObj.checkAndBorrowBook("123");
        //Then
        ArgumentCaptor<Book> captor = forClass(Book.class);
        verify(bookRepositoryMock).update(captor.capture());
        Book updatedBook = captor.getValue();
        assertThat(9, is(equalTo(updatedBook.getAvailableCopies())));
        verify(bookCacheByIsbnMock, times(1)).update("123", updatedBook);
    }

    @Test
    void checkAndBorrowBook_bookByIsbnDoesNotExist_throwBookNotFoundException() {
        //Given
        when(bookRepositoryMock.getByIsbn("123")).thenReturn(Optional.empty());
        //When & Then
        assertThrows(BookNotFoundException.class, () -> testObj.checkAndBorrowBook("123"));
        verify(bookCacheByIsbnMock, times(0)).update(anyString(), any(Book.class));
    }

    @Test
    void checkAndBorrowBook_noMoreCopiesAvailable_throwInsufficientBookCopiesException() {
        //Given
        Book book = new Book("123", "title", "author", 2024, 0, 0);
        when(bookRepositoryMock.getByIsbn("123")).thenReturn(Optional.of(book));
        //When & Then
        assertThrows(InsufficientBookCopiesException.class, () -> testObj.checkAndBorrowBook("123"));
        verify(bookCacheByIsbnMock, times(0)).update(anyString(), any(Book.class));
    }

    @Test
    void checkAndReturnBook_availableCopiesUpdated_success() throws BookNotFoundException, ReturnExceededException {
        //Given
        Book book = new Book("123", "title", "author", 2024, 10, 8);
        when(bookRepositoryMock.getByIsbn("123")).thenReturn(Optional.of(book));
        //When
        testObj.checkAndReturnBook("123");
        //Then
        ArgumentCaptor<Book> captor = forClass(Book.class);
        verify(bookRepositoryMock).update(captor.capture());
        Book updatedBook = captor.getValue();
        assertThat(9, is(equalTo(updatedBook.getAvailableCopies())));
        verify(bookCacheByIsbnMock, times(1)).update("123", updatedBook);
    }

    @Test
    void checkAndReturnBook_bookByIsbnDoesNotExist_throwBookNotFoundException() {
        //Given
        when(bookRepositoryMock.getByIsbn("123")).thenReturn(Optional.empty());
        //When & Then
        assertThrows(BookNotFoundException.class, () -> testObj.checkAndReturnBook("123"));
        verify(bookCacheByIsbnMock, times(0)).update(anyString(), any(Book.class));
    }

    @Test
    void checkAndReturnBook_breachedTotalCopies_throwReturnExceededException() {
        //Given
        Book book = new Book("123", "title", "author", 2024, 10, 10);
        when(bookRepositoryMock.getByIsbn("123")).thenReturn(Optional.of(book));
        //When & Then
        assertThrows(ReturnExceededException.class, () -> testObj.checkAndReturnBook("123"));
        verify(bookCacheByIsbnMock, times(0)).update(anyString(), any(Book.class));
    }
}