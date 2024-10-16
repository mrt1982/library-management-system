package com.identitye2e.library.rest.v1.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {
    @NotNull(message = "Isbn is required")
    private String isbn;
    @NotNull(message = "Title is required") private String title;
    @NotNull(message = "Author is required") private String author;
    @NotNull(message = "Publication Year is required") private Integer publicationYear;
    @NotNull(message = "Available Copies is required") private Integer availableCopies;
}
