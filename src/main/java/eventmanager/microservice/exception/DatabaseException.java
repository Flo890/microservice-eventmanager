package eventmanager.microservice.exception;

/**
 * Created by Flo on 26/07/2016.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(String message) {
        super(message);
    }
}
