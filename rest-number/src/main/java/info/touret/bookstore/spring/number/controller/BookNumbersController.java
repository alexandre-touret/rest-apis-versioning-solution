package info.touret.bookstore.spring.number.controller;

import info.touret.bookstore.spring.number.exception.ISBNExecutionException;
import info.touret.bookstore.spring.number.generated.controller.IsbnsApi;
import info.touret.bookstore.spring.number.generated.dto.BookNumbersDto;
import info.touret.bookstore.spring.number.service.BookNumbersService;
import io.micrometer.core.annotation.Timed;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Numbers API spring controller
 * the time to respond is monitored using <code>@Timed</code> annotation
 */
@Timed(value = "numberController")
@RestController
public class BookNumbersController implements IsbnsApi {

    private final BookNumbersService bookNumbersService;

    public BookNumbersController(BookNumbersService bookNumbersService) {
        this.bookNumbersService = bookNumbersService;
    }

    /**
     * Gets the ISBN numbers for a given book. If there is a timeout calling {@link #bookNumbersService}, the method {@link BookNumbersService#generateBookNumbersFallBack(TimeoutException)} is called
     * The timeout mechanism is specified in the <code>application.yml</code> . You can check the <code>book-numbers</code> timeout instance out.
     *
     * @return The ISBN numbers
     * @see BookNumbersService#generateBookNumbersFallBack(TimeoutException)
     * @see BookNumbersService#createBookNumbersAsync()
     */

    @Override
    @SuppressWarnings("java:S2142")
    public ResponseEntity<BookNumbersDto> generateBookNumbers() {
        try {
            return ResponseEntity.ok(bookNumbersService.createBookNumbersAsync().get());
        } catch (InterruptedException | ExecutionException e) {
            throw new ISBNExecutionException(e);
        }
    }


}
