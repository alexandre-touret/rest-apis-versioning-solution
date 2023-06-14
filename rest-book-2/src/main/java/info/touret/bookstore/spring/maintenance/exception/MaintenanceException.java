package info.touret.bookstore.spring.maintenance.exception;

/**
 * Used for indicating the app is under maintenance
 */
//@ResponseStatus(value = HttpStatus.I_AM_A_TEAPOT)
public class MaintenanceException extends RuntimeException {
    public MaintenanceException() {
    }

    public MaintenanceException(String message) {
        super(message);
    }

    public MaintenanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MaintenanceException(Throwable cause) {
        super(cause);
    }

    public MaintenanceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
