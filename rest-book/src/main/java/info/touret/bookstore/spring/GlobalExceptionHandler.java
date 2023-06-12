package info.touret.bookstore.spring;

import info.touret.bookstore.spring.book.exception.ApiCallTimeoutException;
import info.touret.bookstore.spring.book.generated.dto.APIErrorDto;
import info.touret.bookstore.spring.maintenance.exception.MaintenanceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles all the exceptions thrown by the application
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Indicates that the application is on maintenance
     */
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    @ExceptionHandler(MaintenanceException.class)
    public APIErrorDto maintenance() {
        APIErrorDto apiErrorDto = new APIErrorDto();
        apiErrorDto.setCode(HttpStatus.I_AM_A_TEAPOT.value());
        apiErrorDto.setReason("Service currently in maintenance");
        return apiErrorDto;
    }

    /**
     * Indicates there is a timeout
     */
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    @ExceptionHandler({ApiCallTimeoutException.class})
    public APIErrorDto timeoutException() {
        APIErrorDto apiErrorDto = new APIErrorDto();
        apiErrorDto.setCode(HttpStatus.REQUEST_TIMEOUT.value());
        apiErrorDto.setReason("A timeout occured");
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
