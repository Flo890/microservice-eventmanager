package eventmanager.clientservices.exception;

/**
 * Created by flobe on 01/04/2017.
 */
public class EventNotProcessableException extends RuntimeException {

    public EventNotProcessableException(String reason) {
        this.reason = reason;
    }

    private String reason;

    public String getReason() {
        return reason;
    }
}
