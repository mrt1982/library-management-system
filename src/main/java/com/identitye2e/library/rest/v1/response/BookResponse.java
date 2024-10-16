package com.identitye2e.library.rest.v1.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.identitye2e.library.book.model.Book;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookResponse {
    private String isbn;
    private String title;
    private String author;
    private Integer publicationYear;
    private Integer availableCopies;

    public static BookResponse from(Book book) {
        return BookResponse.builder()
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publicationYear(book.getPublicationYear())
                .availableCopies(book.getAvailableCopies())
                .build();
    }
}
