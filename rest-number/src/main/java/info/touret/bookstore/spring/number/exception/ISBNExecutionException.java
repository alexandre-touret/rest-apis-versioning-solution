package info.touret.bookstore.spring.number.exception;

public class ISBNExecutionException extends RuntimeException{
    public ISBNExecutionException() {
    }

    public ISBNExecutionException(String message) {
        super(message);
    }

    public ISBNExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ISBNExecutionException(Throwable cause) {
        super(cause);
    }

    public ISBNExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
