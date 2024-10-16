package com.identitye2e.library.rest.v1;

import com.identitye2e.library.book.model.Book;
import com.identitye2e.library.book.service.BookService;
import com.identitye2e.library.book.service.exceptions.BookAlreadyExistsException;
import com.identitye2e.library.book.service.exceptions.BookNotFoundException;
import com.identitye2e.library.book.service.exceptions.InsufficientBookCopiesException;
import com.identitye2e.library.book.service.exceptions.ReturnExceededException;
import com.identitye2e.library.rest.v1.request.BookRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryResourceTest {
    @Mock
    private BookService bookServiceMock;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private LibraryResource testObj;

    @BeforeEach
    void setup() {
        testObj = new LibraryResource(bookServiceMock, validator);
    }

    @Test
    void createBook_validBookRequest_returnCreated201() throws BookAlreadyExistsException {
        //Given
        BookRequest bookRequest = BookRequest.builder()
                .isbn("123")
                .title("title")
                .author("auth")
                .publicationYear(2024)
                .availableCopies(10)
                .build();
        Book expectedBook = new Book("123", "title", "auth", 2024, 10, 10);
        when(bookServiceMock.addBook("123", "title", "auth", 2024, 10)).thenReturn(expectedBook);
        //When
        Response response = testObj.createBook(bookRequest);
        //Then
        assertThat(response.getStatus(), is(equalTo(201)));
    }

    @ParameterizedTest
    @MethodSource("invalidFieldsForBookRequests")
    void roundUpByDuration_InvalidFieldsForARoundUpRequest_ThrowConstraintViolationException(BookRequest bookRequest) {
        assertThrows(ConstraintViolationException.class, () -> testObj.createBook(bookRequest));
    }

    @Test
    void removeBook_bookExist_returnNoContent204() throws BookNotFoundException {
        //Given & When
        Response response = testObj.removeBook("isbn");
        //Then
        assertThat(response.getStatus(), is(equalTo(204)));
    }

    @Test
    void removeBook_bookDoesNotExist_throwBookNotFoundException() throws BookNotFoundException {
        //Given
        doThrow(BookNotFoundException.class).when(bookServiceMock).deleteBook("isbn");
        // When & Then
        assertThrows(BookNotFoundException.class, () -> testObj.removeBook("isbn"));
    }

    @Test
    void findBookByIsbn_bookDoesExist_return200() {
        //Given
        Book book = new Book("123", "title", "auth", 2024, 10, 10);
        when(bookServiceMock.findBookByISBN("123")).thenReturn(Optional.of(book));
        // When
        Response response = testObj.findBookByIsbn("123");
        //Then
        assertThat(response.getStatus(), is(equalTo(200)));
    }

    @Test
    void findBookByIsbn_bookDoesNotExist_return404() {
        //Given & When
        Response response = testObj.findBookByIsbn("123");
        //Then
        assertThat(response.getStatus(), is(equalTo(404)));
    }

    @Test
    void findBooksByAuthor_booksForAuthorExist_return200() {
        //Given
        Book book = new Book("123", "title", "auth", 2024, 10, 10);
        List<Book> books = List.of(book);
        when(bookServiceMock.findBooksByAuthor("auth")).thenReturn(books);
        // When
        Response response = testObj.findBooksByAuthor("auth");
        //Then
        assertThat(response.getStatus(), is(equalTo(200)));
    }

    @Test
    void findBooksByAuthor_booksForAuthorDoesNotExist_return200() {
        //Given & When
        Response response = testObj.findBooksByAuthor("123");
        //Then
        assertThat(response.getStatus(), is(equalTo(200)));
    }

    @Test
    void borrowBook_bookExist_return200() throws InsufficientBookCopiesException, BookNotFoundException {
        //Given
        Book book = new Book("123", "title", "auth", 2024, 10, 10);
        when(bookServiceMock.borrowBook("isbn")).thenReturn(book);
        //When
        Response response = testObj.borrowBook("isbn");
        //Then
        assertThat(response.getStatus(), is(equalTo(200)));
    }

    @Test
    void borrowBook_bookDoesNotExist_throwBookNotFoundException() throws InsufficientBookCopiesException, BookNotFoundException {
        //Given
        doThrow(BookNotFoundException.class).when(bookServiceMock).borrowBook("isbn");
        //When & Then
        assertThrows(BookNotFoundException.class, () -> testObj.borrowBook("isbn"));
    }

    @Test
    void borrowBook_bookHasNoMoreCopiesAvailable_throwInsufficientBookCopiesException() throws InsufficientBookCopiesException, BookNotFoundException {
        //Given
        doThrow(InsufficientBookCopiesException.class).when(bookServiceMock).borrowBook("isbn");
        //When & Then
        assertThrows(InsufficientBookCopiesException.class, () -> testObj.borrowBook("isbn"));
    }

    @Test
    void returnBook_bookExist_return200() throws BookNotFoundException, ReturnExceededException {
        //Given
        Book book = new Book("123", "title", "auth", 2024, 10, 10);
        when(bookServiceMock.returnBook("isbn")).thenReturn(book);
        //When
        Response response = testObj.returnBook("isbn");
        //Then
        assertThat(response.getStatus(), is(equalTo(200)));
    }

    @Test
    void returnBook_bookDoesNotExist_throwBookNotFoundException() throws BookNotFoundException, ReturnExceededException {
        //Given
        doThrow(BookNotFoundException.class).when(bookServiceMock).returnBook("isbn");
        //When & Then
        assertThrows(BookNotFoundException.class, () -> testObj.returnBook("isbn"));
    }

    @Test
    void returnBook_breachedTotalCopies_throwReturnExceededException() throws BookNotFoundException, ReturnExceededException {
        //Given
        doThrow(ReturnExceededException.class).when(bookServiceMock).returnBook("isbn");
        //When & Then
        assertThrows(ReturnExceededException.class, () -> testObj.returnBook("isbn"));
    }

    private static Stream<Arguments> invalidFieldsForBookRequests() {
        return Stream.of(
                Arguments.of(BookRequest.builder().build()),
                Arguments.of(BookRequest.builder()
                        .title("title")
                        .author("auth")
                        .publicationYear(2024)
                        .availableCopies(10)
                        .build()),
                Arguments.of(BookRequest.builder()
                        .isbn("isbn")
                        .author("auth")
                        .publicationYear(2024)
                        .availableCopies(10)
                        .build()),
                Arguments.of(BookRequest.builder()
                        .isbn("isbn")
                        .title("title")
                        .publicationYear(2024)
                        .availableCopies(10)
                        .build()),
                Arguments.of(BookRequest.builder()
                        .isbn("isbn")
                        .author("author")
                        .title("title")
                        .availableCopies(10)
                        .build()),
                Arguments.of(BookRequest.builder()
                        .isbn("isbn")
                        .author("author")
                        .title("title")
                        .publicationYear(2024)
                        .build()));
    }
}