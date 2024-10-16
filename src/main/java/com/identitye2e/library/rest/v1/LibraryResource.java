package com.identitye2e.library.rest.v1;

import com.identitye2e.library.book.model.Book;
import com.identitye2e.library.book.service.BookService;
import com.identitye2e.library.book.service.exceptions.BookAlreadyExistsException;
import com.identitye2e.library.book.service.exceptions.BookNotFoundException;
import com.identitye2e.library.book.service.exceptions.InsufficientBookCopiesException;
import com.identitye2e.library.book.service.exceptions.ReturnExceededException;
import com.identitye2e.library.rest.v1.request.BookRequest;
import com.identitye2e.library.rest.v1.response.BookResponse;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Path("/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class LibraryResource {
    private final BookService bookService;
    private final Validator validator;

    @Inject
    public LibraryResource(BookService bookService, Validator validator) {
        this.bookService = bookService;
        this.validator = validator;
    }

    @POST
    @Path("/books")
    public Response createBook(BookRequest bookRequest) throws BookAlreadyExistsException {
        validateRequest(bookRequest);
        Book book = bookService.addBook(bookRequest.getIsbn(), bookRequest.getTitle(), bookRequest.getAuthor(),
        bookRequest.getPublicationYear(), bookRequest.getAvailableCopies());
        BookResponse bookResponse = BookResponse.from(book);
        return Response.status(HttpStatus.CREATED.value())
                .entity(bookResponse).build();
    }

    @DELETE
    @Path("/books/{isbn}")
    public Response removeBook(@PathParam("isbn") String isbn) throws BookNotFoundException {
        bookService.deleteBook(isbn);
        return Response.status(HttpStatus.NO_CONTENT.value())
                .build();
    }

    @GET
    @Path("/books/{isbn}")
    public Response findBookByIsbn(@PathParam("isbn") String isbn){
        Optional<Book> book = bookService.findBookByISBN(isbn);
        if(book.isPresent()){
            BookResponse bookResponse = BookResponse.from(book.get());
            return Response.status(HttpStatus.OK.value())
                    .entity(bookResponse).build();
        }
        return Response.status(HttpStatus.NOT_FOUND.value()).build();
    }

    @GET
    @Path("/books")
    public Response findBooksByAuthor(@QueryParam("author") String author){
        List<Book> booksByAuthor = bookService.findBooksByAuthor(author);
        return Response.status(HttpStatus.OK.value())
                .entity(booksByAuthor)
                .build();
    }

    @PUT
    @Path("/books/{isbn}/borrow")
    public Response borrowBook(@PathParam("isbn") String isbn) throws InsufficientBookCopiesException, BookNotFoundException {
        Book book = bookService.borrowBook(isbn);
        BookResponse bookResponse = BookResponse.from(book);
        return Response.status(HttpStatus.OK.value())
                .entity(bookResponse).build();
    }

    @PUT
    @Path("/books/{isbn}/return")
    public Response returnBook(@PathParam("isbn") String isbn) throws BookNotFoundException, ReturnExceededException {
        Book book = bookService.returnBook(isbn);
        BookResponse bookResponse = BookResponse.from(book);
        return Response.status(HttpStatus.OK.value())
                .entity(bookResponse).build();
    }

    private void validateRequest(BookRequest roundUpRequest) {
        Set<ConstraintViolation<Object>> violations = validator.validate(roundUpRequest);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("Validation failed", violations);
        }
    }
}
