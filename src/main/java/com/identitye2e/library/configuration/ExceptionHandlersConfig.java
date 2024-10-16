package com.identitye2e.library.configuration;

import com.identitye2e.library.book.service.exceptions.BookAlreadyExistsException;
import com.identitye2e.library.book.service.exceptions.BookNotFoundException;
import com.identitye2e.library.book.service.exceptions.InsufficientBookCopiesException;
import com.identitye2e.library.book.service.exceptions.ReturnExceededException;
import com.identitye2e.library.rest.v1.response.ErrorListResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Configuration
public class ExceptionHandlersConfig {
    public ExceptionHandlersConfig() {
    }

    @Provider
    @Component
    public static class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

        @Override
        public Response toResponse(ConstraintViolationException exception) {
            return Response.status(400)
                    .entity(generateErrorResponse(exception))
                    .build();
        }

        private ErrorListResponse generateErrorResponse(final ConstraintViolationException exception) {
            return new ErrorListResponse(exception.getConstraintViolations().stream()
                    .map(v -> new ErrorListResponse.ErrorResponse(getProperty(v), v.getMessage()))
                    .collect(Collectors.toList()));
        }

        private String getProperty(ConstraintViolation v) {
            PathImpl path = (PathImpl) v.getPropertyPath();
            return path.getLeafNode().toString();
        }
    }
    @Provider
    @Component
    public static class BookAlreadyExistsExceptionMapper implements ExceptionMapper<BookAlreadyExistsException> {
        @Override
        public Response toResponse(BookAlreadyExistsException exception) {
            log.error("Book already exists: {}", exception.getMessage());
            return Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity(new ErrorListResponse("book.duplicate.exists", exception.getMessage()))
                    .build();
        }
    }

    @Provider
    @Component
    public static class BookNotFoundExceptionMapper implements ExceptionMapper<BookNotFoundException> {
        @Override
        public Response toResponse(BookNotFoundException exception) {
            log.error("Book not found: {}", exception.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorListResponse("book.not.found", exception.getMessage()))
                    .build();
        }
    }

    @Provider
    @Component
    public static class InsufficientBookCopiesExceptionMapper implements ExceptionMapper<InsufficientBookCopiesException> {
        @Override
        public Response toResponse(InsufficientBookCopiesException exception) {
            log.error("Not enough copies: {}", exception.getMessage());
            return Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity(new ErrorListResponse("insufficient.book.copies", exception.getMessage()))
                    .build();
        }
    }

    @Provider
    @Component
    public static class ReturnExceededExceptionMapper implements ExceptionMapper<ReturnExceededException> {
        @Override
        public Response toResponse(ReturnExceededException exception) {
            log.error("Return book exceeded: {}", exception.getMessage());
            return Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity(new ErrorListResponse("return.book.exceeded", exception.getMessage()))
                    .build();
        }
    }
}
