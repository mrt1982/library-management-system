package com.identitye2e.library.rest.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.identitye2e.library.AbstractMediumTest;
import com.identitye2e.library.rest.v1.request.BookRequest;
import com.identitye2e.library.rest.v1.response.BookResponse;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LibraryResourceMediumTest extends AbstractMediumTest {
    @Test
    void createBook_validBookRequest_return201Response() throws JsonProcessingException {
        //Given & When
        BookRequest bookRequest = BookRequest.builder()
                .isbn("123")
                .title("title")
                .author("auth")
                .publicationYear(2024)
                .availableCopies(10)
                .build();
        Response response = given()
                .log().all()
                .body(om.writeValueAsString(bookRequest))
                .contentType(JSON)
                .expect()
                .statusCode(201)
                .when()
                .post("/v1/books").andReturn();
        //Then
        JSONObject jsonObject = new JSONObject(response.getBody().asString());
        BookResponse actualBookResponse = om.readValue(jsonObject.toString(), BookResponse.class);
        assertThat(actualBookResponse, is(notNullValue()));
        assertThat(actualBookResponse.getIsbn(), is(equalTo("123")));
        assertThat(actualBookResponse.getTitle(), is(equalTo("title")));
        assertThat(actualBookResponse.getAuthor(), is(equalTo("auth")));
        assertThat(actualBookResponse.getPublicationYear(), is(equalTo(2024)));
        assertThat(actualBookResponse.getAvailableCopies(), is(equalTo(10)));
    }

    @Test
    void createBook_duplicateBookRequest_return412Response() throws JsonProcessingException {
        //Given
        BookRequest bookRequest = BookRequest.builder()
                .isbn("123")
                .title("title")
                .author("auth")
                .publicationYear(2024)
                .availableCopies(10)
                .build();
        given()
                .log().all()
                .body(om.writeValueAsString(bookRequest))
                .contentType(JSON)
                .expect()
                .statusCode(201)
                .when()
                .post("/v1/books").andReturn();
        //When & Then
        given()
                .log().all()
                .body(om.writeValueAsString(bookRequest))
                .contentType(JSON)
                .when()
                .post("/v1/books")
                .then().log().all()
                .assertThat().statusCode(412).contentType(JSON)
                .assertThat().body(notNullValue())
                .assertThat().body("errors[0].code", equalTo("book.duplicate.exists"));
    }

    @Test
    void removeBook_bookExists_return204Response() throws JsonProcessingException {
        //Given
        createBook("123", "title","auth", 2024, 10);
        //When & Then
        given()
                .log().all()
                .contentType(JSON)
                .pathParam("isbn", "123")
                .expect()
                .statusCode(204)
                .when()
                .delete("/v1/books/{isbn}").andReturn();
    }

    @Test
    void removeBook_bookDoesNotExists_return404Response() {
        //Given & When & Then
        given()
                .log().all()
                .contentType(JSON)
                .pathParam("isbn", "123")
                .expect()
                .statusCode(404)
                .when()
                .delete("/v1/books/{isbn}").andReturn();
    }

    @Test
    void findBookByIsbn_bookExists_return200Response() throws JsonProcessingException {
        //Given
        createBook("123", "title","auth", 2024, 10);
        //When
        Response response = given()
                .log().all()
                .contentType(JSON)
                .pathParam("isbn", "123")
                .expect()
                .statusCode(200)
                .when()
                .get("/v1/books/{isbn}").andReturn();
        //Then
        JSONObject jsonObject = new JSONObject(response.getBody().asString());
        BookResponse actualBookResponse = om.readValue(jsonObject.toString(), BookResponse.class);
        assertThat(actualBookResponse, is(notNullValue()));
        assertThat(actualBookResponse.getIsbn(), is(equalTo("123")));
        assertThat(actualBookResponse.getTitle(), is(equalTo("title")));
        assertThat(actualBookResponse.getAuthor(), is(equalTo("auth")));
        assertThat(actualBookResponse.getPublicationYear(), is(equalTo(2024)));
        assertThat(actualBookResponse.getAvailableCopies(), is(equalTo(10)));
    }

    @Test
    void findBookByIsbn_bookDoesNotExists_return404Response() {
        //Given & When & Then
        given()
                .log().all()
                .contentType(JSON)
                .pathParam("isbn", "123")
                .expect()
                .statusCode(404)
                .when()
                .get("/v1/books/{isbn}");
    }

    @Test
    void findBooksByAuthor_booksByAuthor_return200Response() throws JsonProcessingException {
        //Given
        createBook("123", "title","auth", 2024, 10);
        createBook("124", "title2","auth", 2024, 10);
        createBook("125", "title3","auth", 2024, 10);
        //When
        Response response = given()
                .log().all()
                .contentType(JSON)
                .queryParam("author", "auth")
                .expect()
                .statusCode(200)
                .when()
                .get("/v1/books").andReturn();
        //Then
        JSONArray jsonObjects = new JSONArray(response.getBody().asString());
        List actualBooksByAuthorResponse = om.readValue(jsonObjects.toString(), List.class);
        assertThat(actualBooksByAuthorResponse, is(notNullValue()));
        assertThat(actualBooksByAuthorResponse.isEmpty(), is(equalTo(false)));
        assertThat(actualBooksByAuthorResponse.size(), is(equalTo(3)));
    }

    @Test
    void findBooksByAuthor_booksByAuthorDoesNotExist_return200EmptyListResponse() throws JsonProcessingException {
        //Given & When
        Response response = given()
                .log().all()
                .contentType(JSON)
                .queryParam("author", "auth")
                .expect()
                .statusCode(200)
                .when()
                .get("/v1/books").andReturn();
        //Then
        JSONArray jsonObjects = new JSONArray(response.getBody().asString());
        List actualBooksByAuthorResponse = om.readValue(jsonObjects.toString(), List.class);
        assertThat(actualBooksByAuthorResponse.isEmpty(), is(equalTo(true)));
    }

    @Test
    void borrowBook_availableCopiesUpdated_return200Response() throws JsonProcessingException {
        //Given
        createBook("123", "title","auth", 2024, 10);
        //When
        Response response = given()
                .log().all()
                .contentType(JSON)
                .pathParam("isbn", "123")
                .expect()
                .statusCode(200)
                .when()
                .put("/v1/books/{isbn}/borrow").andReturn();
        //Then
        JSONObject jsonObject = new JSONObject(response.getBody().asString());
        BookResponse actualBookResponse = om.readValue(jsonObject.toString(), BookResponse.class);
        assertThat(actualBookResponse, is(notNullValue()));
        assertThat(actualBookResponse.getIsbn(), is(equalTo("123")));
        assertThat(actualBookResponse.getAvailableCopies(), is(equalTo(9)));
    }

    @Test
    void borrowBook_noMoreCopiesAvailable_return412Response() throws JsonProcessingException {
        //Given
        createBook("123", "title","auth", 2024, 1);
        given()
                .log().all()
                .contentType(JSON)
                .pathParam("isbn", "123")
                .expect()
                .statusCode(200)
                .when()
                .put("/v1/books/{isbn}/borrow");
        //When & Then
        given()
                .log().all()
                .contentType(JSON)
                .pathParam("isbn", "123")
                .expect()
                .when()
                .put("/v1/books/{isbn}/borrow")
                .then().log().all()
                .assertThat().statusCode(412).contentType(JSON)
                .assertThat().body(notNullValue())
                .assertThat().body("errors[0].code", equalTo("insufficient.book.copies"));
    }

    @Test
    void borrowBook_bookDoesNotExist_return404Response() {
        //Given & When & Then
        given()
                .log().all()
                .contentType(JSON)
                .pathParam("isbn", "123")
                .expect()
                .when()
                .put("/v1/books/{isbn}/borrow")
                .then().log().all()
                .assertThat().statusCode(404).contentType(JSON);
    }

    @Test
    void returnBook_availableCopiesUpdated_return200Response() throws JsonProcessingException {
        //Given
        createBook("123", "title","auth", 2024, 10);
        given()
                .log().all()
                .contentType(JSON)
                .pathParam("isbn", "123")
                .expect()
                .statusCode(200)
                .when()
                .put("/v1/books/{isbn}/borrow");
        given()
                .log().all()
                .contentType(JSON)
                .pathParam("isbn", "123")
                .expect()
                .statusCode(200)
                .when()
                .put("/v1/books/{isbn}/borrow");
        //When
        Response response = given()
                .log().all()
                .contentType(JSON)
                .pathParam("isbn", "123")
                .expect()
                .statusCode(200)
                .when()
                .put("/v1/books/{isbn}/return").andReturn();
        //Then
        JSONObject jsonObject = new JSONObject(response.getBody().asString());
        BookResponse actualBookResponse = om.readValue(jsonObject.toString(), BookResponse.class);
        assertThat(actualBookResponse, is(notNullValue()));
        assertThat(actualBookResponse.getIsbn(), is(equalTo("123")));
        assertThat(actualBookResponse.getAvailableCopies(), is(equalTo(9)));
    }

    @Test
    void returnBook_breachedTotalCopies_return412Response() throws JsonProcessingException {
        //Given & When & Then
        createBook("123", "title","auth", 2024, 10);
        given()
                .log().all()
                .contentType(JSON)
                .pathParam("isbn", "123")
                .expect()
                .when()
                .put("/v1/books/{isbn}/return")
                .then().log().all()
                .assertThat().statusCode(412).contentType(JSON)
                .assertThat().body(notNullValue())
                .assertThat().body("errors[0].code", equalTo("return.book.exceeded"));
    }

    @Test
    void returnBook_bookDoesNotExist_return404Response() {
        //Given & When & Then
        given()
                .log().all()
                .contentType(JSON)
                .pathParam("isbn", "123")
                .expect()
                .when()
                .put("/v1/books/{isbn}/return")
                .then().log().all()
                .assertThat().statusCode(404).contentType(JSON);
    }

    private void createBook(String isbn, String title, String author, Integer publicationYear, Integer availableCopies) throws JsonProcessingException {
        BookRequest bookRequest = BookRequest.builder()
                .isbn(isbn)
                .title(title)
                .author(author)
                .publicationYear(publicationYear)
                .availableCopies(availableCopies)
                .build();
        given()
                .log().all()
                .body(om.writeValueAsString(bookRequest))
                .contentType(JSON)
                .expect()
                .statusCode(201)
                .when()
                .post("/v1/books").andReturn();
    }
}
