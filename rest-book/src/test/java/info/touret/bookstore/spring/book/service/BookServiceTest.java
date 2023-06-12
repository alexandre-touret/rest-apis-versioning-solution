package info.touret.bookstore.spring.book.service;

import info.touret.bookstore.spring.book.dto.IsbnNumbers;
import info.touret.bookstore.spring.book.entity.Book;
import info.touret.bookstore.spring.book.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BookServiceTest {

    @MockBean
    private BookRepository bookRepository;
    private BookService bookService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private CircuitBreakerFactory circuitBreakerFactory;


    private ResponseEntity<IsbnNumbers> isbnNumbersResponseEntity;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, restTemplate, "URL", circuitBreakerFactory);
    }

    @Test
    void should_find_a_random_book() {
        var longList = createBookList().stream().map(Book::getId).collect(Collectors.toList());
        when(bookRepository.findAllIds()).thenReturn(longList);

        Book book = new Book();
        book.setId(1L);
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book));
        assertNotNull(bookService.findRandomBook());
    }

    @Test
    void should_register_a_book() {
        /* book & Isbn creation */
        var book = new Book();
        book.setId(1L);
        book.setAuthor("author");
        book.setDescription("description");
        book.setPrice(BigDecimal.TEN);

        IsbnNumbers isbnNumbers = new IsbnNumbers();
        isbnNumbers.setIsbn10("0123456789");
        isbnNumbers.setIsbn13("0123456789012");
        /* Mock configuration */
        isbnNumbersResponseEntity = new ResponseEntity<>(isbnNumbers, HttpStatus.OK);
        when(restTemplate.getForEntity(eq("URL"), eq(IsbnNumbers.class))).thenReturn(isbnNumbersResponseEntity);
        final var circuitBreaker = mock(CircuitBreaker.class);
        when(circuitBreaker.run(any(), any())).thenReturn(book);
        when(circuitBreakerFactory.create(eq("slowNumbers"))).thenReturn(circuitBreaker);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        var registerBook = bookService.registerBook(book);
        assertNotNull(registerBook);
        assertEquals(book.getId(), registerBook.getId());
    }

    @Test
    void should_find_all_books() {
        List<Book> books = createBookList();
        when(bookRepository.findAll()).thenReturn(books);

        var allBooks = bookService.findAllBooks();
        assertNotNull(allBooks);
        assertEquals(2, allBooks.size());
        assertTrue(allBooks.containsAll(books));
    }

    private List<Book> createBookList() {
        var book = new Book();
        book.setId(1L);
        var book2 = new Book();
        book2.setId(2L);
        return Arrays.asList(book, book2);
    }

    @Test
    void should_get_count() {
        when(bookRepository.count()).thenReturn(2L);
        assertEquals(2L, bookService.count());
    }

    @Test
    void should_find_books_by_id() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        var bookById = bookService.findBookById(1L);
        assertEquals(1L, bookById.orElseThrow().getId());
    }

    @Test
    void should_update_book() {
        Book book = new Book();
        book.setId(1L);
        book.setAuthor("Harriet Beecher Stowe");
        when(bookRepository.save(book)).thenReturn(book);
        var updateBook = bookService.updateBook(book);
        assertNotNull(updateBook);
        assertEquals(book.getAuthor(), updateBook.getAuthor());
    }

    @Test
    void should_delete_book() {
        Book book = new Book();
        book.setId(1L);
        book.setAuthor("Harriet Beecher Stowe");

        doNothing().when(bookRepository).deleteById(1L);
        bookService.deleteBook(1L);
    }


}
