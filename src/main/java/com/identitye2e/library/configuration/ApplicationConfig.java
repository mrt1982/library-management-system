package com.identitye2e.library.configuration;

import com.identitye2e.library.book.model.Book;
import com.identitye2e.library.book.repository.BookRepository;
import com.identitye2e.library.book.service.BookAvailabilityManager;
import com.identitye2e.library.book.service.BookAvailabilityManagerImpl;
import com.identitye2e.library.book.service.BookService;
import com.identitye2e.library.book.service.BookServiceServiceImpl;
import com.identitye2e.library.infrastructure.cache.BookCache;
import com.identitye2e.library.infrastructure.cache.SimpleBookCache;
import com.identitye2e.library.infrastructure.persistance.BookRepositoryImpl;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ApplicationConfig {
    @Bean
    Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Bean
    public BookRepository bookRepository(BookCache<String, Book> bookCacheByIsbn, BookCache<String, List<Book>> booksCacheByAuthor){
        return new BookRepositoryImpl(bookCacheByIsbn, booksCacheByAuthor);
    }

    @Bean
    public BookCache<String, Book> bookCacheByIsbn(){
        return new SimpleBookCache<>(16);
    }

    @Bean
    public BookCache<String, List<Book>> booksCacheByAuthor(){
        return new SimpleBookCache<>(16);
    }

    @Bean
    public BookAvailabilityManager bookAvailabilityManager(BookRepository bookRepository, BookCache<String, Book> bookCacheByIsbn){
        return new BookAvailabilityManagerImpl(bookRepository, bookCacheByIsbn);
    }

    @Bean
    public BookService bookService(BookRepository bookRepository, BookAvailabilityManager bookAvailabilityManager) {
        return new BookServiceServiceImpl(bookRepository, bookAvailabilityManager);
    }
}
