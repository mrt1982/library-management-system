package com.identitye2e.library.infrastructure.persistance;

import com.identitye2e.library.infrastructure.cache.BookCache;
import com.identitye2e.library.book.model.Book;
import com.identitye2e.library.book.repository.BookRepository;
import com.identitye2e.library.book.service.exceptions.BookAlreadyExistsException;
import com.identitye2e.library.book.service.exceptions.BookNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepository {
    private final Map<String, Book> bookStore = new ConcurrentHashMap<>();
    private final BookCache<String, Book> bookCacheByIsbn;
    private final BookCache<String, List<Book>> booksCacheByAuthor;
    @Override
    public void createBook(Book book) throws BookAlreadyExistsException {
        if(bookStore.putIfAbsent(book.getIsbn(), book) != null){
            throw new BookAlreadyExistsException("Book with ISBN %s already exists.".formatted(book.getIsbn()));
        }
    }

    @Override
    public void removeBook(String isbn) throws BookNotFoundException {
        Book removedBook = bookStore.remove(isbn);
        if(removedBook == null){
            throw new BookNotFoundException("Cannot remove the Book with ISBN %s does not exist.".formatted(isbn));
        }else{
            bookCacheByIsbn.invalidate(isbn);
        }
    }

    @Override
    public Optional<Book> getByIsbn(String isbn) {
        return bookCacheByIsbn.get(isbn)
                .or(() -> {
                    Optional<Book> actualBook = Optional.ofNullable(bookStore.get(isbn));
                    actualBook.ifPresent(book -> bookCacheByIsbn.put(isbn, book));
                    return actualBook;
                });
    }

    @Override
    public List<Book>
    getByAuthor(String auth) {
        return booksCacheByAuthor.get(auth)
                .or(() -> {
                    List<Book> booksByAuthor = bookStore.values().stream()
                            .filter(book -> book.getAuthor().equals(auth))
                            .toList();
                    if (!booksByAuthor.isEmpty()) {
                        booksCacheByAuthor.put(auth, booksByAuthor);
                    }
                    return Optional.of(booksByAuthor);
                }).orElseGet(Collections::emptyList);
    }

    @Override
    public Book update(Book updatedBook) {
        return bookStore.compute(updatedBook.getIsbn(), (key, oldBook) -> updatedBook);
    }
}
