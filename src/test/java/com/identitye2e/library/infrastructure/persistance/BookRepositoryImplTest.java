package com.identitye2e.library.infrastructure.persistance;

import com.identitye2e.library.book.model.Book;
import com.identitye2e.library.book.repository.BookRepository;
import com.identitye2e.library.book.service.exceptions.BookAlreadyExistsException;
import com.identitye2e.library.book.service.exceptions.BookNotFoundException;
import com.identitye2e.library.book.service.exceptions.InsufficientBookCopiesException;
import com.identitye2e.library.infrastructure.cache.BookCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookRepositoryImplTest {
    @Mock
    private BookCache<String, Book> bookCacheByIsbnMock;
    @Mock
    private BookCache<String, List<Book>> booksCacheByAuthorMock;
    private BookRepository testObj;

    @BeforeEach
    void setup() {
        testObj = new BookRepositoryImpl(bookCacheByIsbnMock, booksCacheByAuthorMock);
    }

    @Test
    void createBook_validBook_success() throws BookAlreadyExistsException {
        //Given
        Book book = new Book("123", "title", "auth", 2024, 10, 10);
        //When & Then
        testObj.createBook(book);
    }

    @Test
    void createBook_duplicateBook_throwBookAlreadyExistsException() throws BookAlreadyExistsException {
        //Given
        Book book = new Book("123", "title", "auth", 2024, 10, 10);
        testObj.createBook(book);
        //When
        var duplicateException = assertThrows(BookAlreadyExistsException.class,
                () -> testObj.createBook(book));
        //Then
        assertThat(duplicateException.getMessage(), is(equalTo("Book with ISBN 123 already exists.")));
    }

    @Test
    void removeBook_bookExists_success() throws BookAlreadyExistsException, BookNotFoundException {
        //Given
        Book book = new Book("123", "title", "auth", 2024, 10, 10);
        testObj.createBook(book);
        //When & Then
        testObj.removeBook("123");
        verify(bookCacheByIsbnMock, times(1)).invalidate("123");
    }

    @Test
    void removeBook_bookDoesNotExist_throwBookNotFoundException() {
        //Given & When
        var removeBookNotFoundException = assertThrows(BookNotFoundException.class, () -> testObj.removeBook("123"));
        //Then
        assertThat(removeBookNotFoundException.getMessage(), is(equalTo("Cannot remove the Book with ISBN 123 does not exist.")));
        verify(bookCacheByIsbnMock, never()).invalidate(anyString());
    }

    @Test
    void getByIsbn_bookExistButNotInCache_returnBookFromStore() throws BookAlreadyExistsException {
        //Given
        Book book = new Book("123", "title", "auth", 2024, 10, 10);
        testObj.createBook(book);
        //When
        Optional<Book> bookOptional = testObj.getByIsbn("123");
        //Then
        InOrder inOrder = inOrder(bookCacheByIsbnMock);
        assertThat(bookOptional.isPresent(), is(equalTo(true)));
        assertThat(bookOptional.get(), is(equalTo(book)));
        inOrder.verify(bookCacheByIsbnMock, times(1)).get("123");
        inOrder.verify(bookCacheByIsbnMock, times(1)).put("123", bookOptional.get());
    }

    @Test
    void getByIsbn_bookExistInCache_returnBookFromCache() throws BookAlreadyExistsException {
        //Given
        Book book = new Book("123", "title", "auth", 2024, 10, 10);
        testObj.createBook(book);
        when(bookCacheByIsbnMock.get("123")).thenReturn(Optional.of(book));
        //When
        Optional<Book> bookOptional = testObj.getByIsbn("123");
        //Then
        InOrder inOrder = inOrder(bookCacheByIsbnMock);
        assertThat(bookOptional.isPresent(), is(equalTo(true)));
        assertThat(bookOptional.get(), is(equalTo(book)));
        inOrder.verify(bookCacheByIsbnMock, times(1)).get("123");
        inOrder.verify(bookCacheByIsbnMock, never()).put(anyString(), any(Book.class));
    }

    @Test
    void getByIsbn_bookDoesNotExistInCacheAndStore_returnEmptyBook() {
        //Given & When
        Optional<Book> bookOptional = testObj.getByIsbn("123");
        //Then
        InOrder inOrder = inOrder(bookCacheByIsbnMock);
        assertThat(bookOptional.isEmpty(), is(equalTo(true)));
        inOrder.verify(bookCacheByIsbnMock, times(1)).get("123");
        inOrder.verify(bookCacheByIsbnMock, never()).put(anyString(), any(Book.class));
    }

    @Test
    void getByAuthor_booksExistButNotInCache_returnListOfBooksFromStore() throws BookAlreadyExistsException {
        //Given
        Book authBook1 = new Book("123", "title", "auth", 2024, 10, 10);
        Book authBook2 = new Book("124", "title3", "auth", 2024, 10, 10);
        testObj.createBook(authBook1);
        testObj.createBook(authBook2);
        when(booksCacheByAuthorMock.get("auth")).thenReturn(Optional.empty());
        //When
        List<Book> booksByAuthor = testObj.getByAuthor("auth");
        //Then
        InOrder inOrder = inOrder(booksCacheByAuthorMock);
        assertThat(booksByAuthor.isEmpty(), is(equalTo(false)));
        assertThat(booksByAuthor.size(), is(equalTo(2)));
        inOrder.verify(booksCacheByAuthorMock, times(1)).get("auth");
        inOrder.verify(booksCacheByAuthorMock, times(1)).put("auth", booksByAuthor);
    }

    @Test
    void getByAuthor_booksExistInCache_returnListOfBooksFromCache() {
        //Given
        Book authBook1 = new Book("123", "title", "auth2", 2024, 10, 10);
        Book authBook2 = new Book("124", "title3", "auth2", 2024, 10, 10);
        List<Book> books = List.of(authBook1, authBook2);
        when(booksCacheByAuthorMock.get("auth")).thenReturn(Optional.of(books));
        //When
        List<Book> booksByAuthor = testObj.getByAuthor("auth");
        //Then
        InOrder inOrder = inOrder(booksCacheByAuthorMock);
        assertThat(booksByAuthor.isEmpty(), is(equalTo(false)));
        assertThat(booksByAuthor.size(), is(equalTo(2)));
        inOrder.verify(booksCacheByAuthorMock, times(1)).get("auth");
        inOrder.verify(booksCacheByAuthorMock, never()).put(anyString(), any(List.class));

    }

    @Test
    void getByAuthor_booksDoesNotExistByAuthorInCacheAndStore_returnEmptyListOfBooks() throws BookAlreadyExistsException {
        //Given
        Book authBook1 = new Book("123", "title", "auth2", 2024, 10, 10);
        Book authBook2 = new Book("124", "title3", "auth2", 2024, 10, 10);
        testObj.createBook(authBook1);
        testObj.createBook(authBook2);
        when(booksCacheByAuthorMock.get("auth")).thenReturn(Optional.empty());
        //Then
        List<Book> booksByAuthor = testObj.getByAuthor("auth");
        //When
        InOrder inOrder = inOrder(booksCacheByAuthorMock);
        assertThat(booksByAuthor.isEmpty(), is(equalTo(true)));
        inOrder.verify(booksCacheByAuthorMock, times(1)).get("auth");
        inOrder.verify(booksCacheByAuthorMock, never()).put(anyString(), any(List.class));
    }

    @Test
    void update_bookAlreadyExistUpdateBorrowAmount_bookUpdated() throws BookAlreadyExistsException, InsufficientBookCopiesException {
        //Given
        Book book = new Book("123", "title", "auth", 2024, 10, 10);
        testObj.createBook(book);
        Book updateBook = book.borrowBook();
        //When
        Book actualBookUpdate = testObj.update(updateBook);
        //Then
        assertThat(actualBookUpdate.getAvailableCopies(), is(equalTo(9)));
        assertThat(actualBookUpdate, is(equalTo(updateBook)));
    }

    @Test
    void update_bookDoesNotExistWhilstUpdating_returnBook() throws InsufficientBookCopiesException {
        //Given
        Book book = new Book("123", "title", "auth", 2024, 10, 10);
        Book updateBook = book.borrowBook();
        //When
        Book actualBookUpdate =  testObj.update(updateBook);
        //Then
        assertThat(actualBookUpdate.getAvailableCopies(), is(equalTo(9)));
        assertThat(actualBookUpdate, is(equalTo(updateBook)));
    }
}