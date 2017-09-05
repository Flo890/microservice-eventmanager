package eventmanager.clientservices.exception;

/**
 * thrown if the event cannot be processed, because of an unexpected and maybe temporal problem.
 * The reason of this failure should be a temporal system state problem, or maybe an unknown bug
 */
public class UnexpectedEventProeccsingException extends EventNotProcessableException {
    public UnexpectedEventProeccsingException(String message) {
        super(message);
    }

    public UnexpectedEventProeccsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
