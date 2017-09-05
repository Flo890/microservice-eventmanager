package eventmanager.clientservices.exception;

/**
 * thrown if the system is not capable to process this event.
 * The reason is a durable problem, it is known that the given data cannot be processed.
 * e.g. required features not implemented in this microservice, a requested microservice answeared with 501 Not Implemented, ...
 *
 */
public class EventNotProcessableBecauseIncompatibleToSystemException extends EventNotProcessableException {
    public EventNotProcessableBecauseIncompatibleToSystemException(String message) {
        super(message);
    }

    public EventNotProcessableBecauseIncompatibleToSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
