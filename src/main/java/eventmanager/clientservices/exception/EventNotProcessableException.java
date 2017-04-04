package eventmanager.clientservices.exception;

/**
 * Created by flobe on 01/04/2017.
 */
public class EventNotProcessableException extends RuntimeException {
    public EventNotProcessableException(String message) {
        super(message);
    }

    public EventNotProcessableException(String message, Throwable cause) {
        super(message, cause);
    }
}
