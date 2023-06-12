package info.touret.bookstore.spring.book.controller;

import info.touret.bookstore.spring.book.generated.controller.BooksApi;
import info.touret.bookstore.spring.book.generated.dto.BookDto;
import info.touret.bookstore.spring.book.mapper.BookMapper;
import info.touret.bookstore.spring.book.service.BookService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Book REST API.
 * the API time to respond is monitor using <code>@Timed</code> annotation
 *
 * @see Timed
 */
@Observed(name = "book",
        contextualName = "book")
@RestController
public class BookController implements BooksApi {
    private final BookMapper bookMapper;
    private final BookService bookService;

    public BookController(BookService bookService, BookMapper bookMapper) {
        this.bookService = bookService;
        this.bookMapper = bookMapper;
    }

    @Override
    public ResponseEntity<BookDto> getRandomBook() {

        return ResponseEntity.ok(bookMapper.toBookDto(bookService.findRandomBook()));
    }

    @Override
    public ResponseEntity<List<BookDto>> getAllBooks() {
        return ResponseEntity.ok(bookMapper.toBookDtos(bookService.findAllBooks()));
    }

    /**
     * Returns the number of books
     *
     * @return A Map which will be automatically transformed into a JSON Object
     */
    @Override
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("books.count", bookService.count()));
    }

    @Override
    public ResponseEntity<BookDto> getBook(Long id) {
        return ResponseEntity.of(bookService.findBookById(id).map(bookMapper::toBookDto));
    }

    @Override
    public ResponseEntity<URI> createBook(BookDto bookDto) {
        final var registerBook = bookService.registerBook(bookMapper.toBook(bookDto));
        final var uri = URI.create(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUriString() + "/" + registerBook.getId());
        return ResponseEntity.created(uri).build();
    }

    @Override
    public ResponseEntity<BookDto> updateBook(BookDto bookDto) {
        return ResponseEntity.ok(bookMapper.toBookDto(bookService.updateBook(bookMapper.toBook(bookDto))));
    }

    @Override
    public ResponseEntity<Void> deleteBook(Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
