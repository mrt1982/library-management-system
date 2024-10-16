package com.identitye2e.library.infrastructure.persistance.cache;

import com.identitye2e.library.infrastructure.cache.BookCache;
import com.identitye2e.library.infrastructure.cache.SimpleBookCache;
import com.identitye2e.library.book.model.Book;
import com.identitye2e.library.book.service.exceptions.InsufficientBookCopiesException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

class SimpleBookCacheTest {
    private BookCache<String, Book> testObj;

    @BeforeEach
    void setup() {
        testObj = new SimpleBookCache<>(10);
    }

    @Test
    void get_keyExists_returnValue() {
        //Given
        createBookAndUpdateCache("123", "title", "author", 2024, 10, 10);
        //When
        Optional<Book> actualValueCache = testObj.get("123");
        //Then
        assertThat(actualValueCache.isPresent(), is(equalTo(true)));
    }

    @Test
    void get_keyDoesNotExists_returnEmptyValue() {
        //Given & When
        Optional<Book> actualValueCache = testObj.get("123");
        //Then
        assertThat(actualValueCache.isEmpty(), is(equalTo(true)));
    }

    @Test
    void get_keyIsEvictedBeingEldestEntryAndExceedsCacheSize_returnEmptyValue() {
        //Given
        String eldestEntryIsbn = "123";
        createBookAndUpdateCache(eldestEntryIsbn, "title1", "author", 2024, 10, 10);
        createBookAndUpdateCache("124", "title2", "author", 2024, 10, 10);
        createBookAndUpdateCache("125", "title3", "author", 2024, 10, 10);
        createBookAndUpdateCache("126", "title4", "author", 2024, 10, 10);
        createBookAndUpdateCache("127", "title5", "author", 2024, 10, 10);
        createBookAndUpdateCache("128", "title6", "author", 2024, 10, 10);
        createBookAndUpdateCache("129", "title7", "author", 2024, 10, 10);
        createBookAndUpdateCache("1210", "title8", "author", 2024, 10, 10);
        createBookAndUpdateCache("1211", "titl9", "author", 2024, 10, 10);
        createBookAndUpdateCache("1212", "title10", "author", 2024, 10, 10);
        //When
        createBookAndUpdateCache("1213", "title11", "author", 2024, 10, 10);
        Optional<Book> eldestValue = testObj.get(eldestEntryIsbn);
        //Then
        assertThat(eldestValue.isEmpty(), is(equalTo(true)));
    }

    @Test
    void put_keyDoesNotExist_cacheIsUpdated(){
        //Given & When
        Book book = new Book("123", "title1", "author", 2024, 10, 10);
        testObj.put("123", book);
        //Then
        Optional<Book> value = testObj.get("123");
        assertThat(value.isPresent(), is(equalTo(true)));
        assertThat(value.get(), is(equalTo(book)));
    }

    @Test
    void put_keyExistsUpdateValue_cacheIsUpdated() throws InsufficientBookCopiesException {
        //Given
        Book originalBook = new Book("123", "title1", "author", 2024, 10, 10);
        testObj.put("123", originalBook);
        Book updatedBook = originalBook.borrowBook();
        //When
        testObj.put("123", updatedBook);
        //Then
        Optional<Book> valueOpt = testObj.get("123");
        assertThat(valueOpt.isPresent(), is(equalTo(true)));
        Book actualValue = valueOpt.get();
        assertThat(originalBook.getAvailableCopies(), is(equalTo(10)));
        assertThat(actualValue.getAvailableCopies(), is(equalTo(9)));
        assertThat(actualValue, is(equalTo(updatedBook)));
    }

    @Test
    void update_keyDoesNotExist_cacheIsUpdated(){
        //Given & When
        Book book = new Book("123", "title1", "author", 2024, 10, 10);
        testObj.update("123", book);
        Optional<Book> value = testObj.get("123");
        //Then
        assertThat(value.isPresent(), is(equalTo(true)));
        assertThat(value.get(), is(equalTo(book)));
    }

    @Test
    void update_keyExistsUpdateValues_cacheUpdated() throws InsufficientBookCopiesException {
        //Given
        Book originalBook = new Book("123", "title1", "author", 2024, 10, 10);
        testObj.put("123", originalBook);
        Book updatedBook = originalBook.borrowBook();
        //When
        testObj.update("123", updatedBook);
        Optional<Book> valueOpt = testObj.get("123");
        //Then
        assertThat(valueOpt.isPresent(), is(equalTo(true)));
        Book actualValue = valueOpt.get();
        assertThat(originalBook.getAvailableCopies(), is(equalTo(10)));
        assertThat(actualValue.getAvailableCopies(), is(equalTo(9)));
        assertThat(actualValue, is(equalTo(updatedBook)));
    }

    @Test
    void invalidate_keyDoesNotExist_cacheDoesNotHaveTheKey(){
        //Given & When
        testObj.invalidate("123");
        //Then
        Optional<Book> value = testObj.get("123");
        assertThat(value.isEmpty(), is(equalTo(true)));
    }

    @Test
    void invalidate_keyExists_keyIsRemovedFromTheCache() {
        //Given
        Book book = new Book("123", "title1", "author", 2024, 10, 10);
        testObj.put("123", book);
        //When
        testObj.invalidate("123");
        //Then
        Optional<Book> valueOpt = testObj.get("123");
        assertThat(valueOpt.isEmpty(), is(equalTo(true)));
    }

    private void createBookAndUpdateCache(String isbn, String title, String author, int publicationYear, int totalCopies, int availableCopies) {
        Book book = new Book(isbn, title, author, publicationYear, totalCopies, availableCopies);
        testObj.put(isbn, book);
    }
}