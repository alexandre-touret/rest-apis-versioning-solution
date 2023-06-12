package info.touret.bookstore.spring.number;

import info.touret.bookstore.spring.number.exception.ISBNExecutionException;
import info.touret.bookstore.spring.number.generated.dto.APIErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.TimeoutException;

/**
 * Handles all the exceptions thrown by the application
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Indicates that the application is on maintenance
     */
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    @ExceptionHandler({ TimeoutException.class, ISBNExecutionException.class})
    public APIErrorDto timeout() {
        APIErrorDto apiErrorDto = new APIErrorDto();
        apiErrorDto.setCode(HttpStatus.GATEWAY_TIMEOUT.value());
        apiErrorDto.setReason("Timeout");
        return apiErrorDto;
    }

    /**
     * Any other exception
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({RuntimeException.class, Exception.class})
    public APIErrorDto anyException() {
        APIErrorDto apiErrorDto = new APIErrorDto();
        apiErrorDto.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        apiErrorDto.setReason("An unexpected server error occured");
        return apiErrorDto;
    }
}
