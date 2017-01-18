package eventmanager.common.model;

/**
 * Created by flobe on 11/12/2016.
 */
public class EventServiceResponse {

    private boolean success;

    private EventUngeneric event;

    private String message;

    public EventServiceResponse() {
    }

    public EventServiceResponse(boolean success, EventUngeneric event, String message) {
        this.success = success;
        this.event = event;
        this.message = message;
    }

    public EventServiceResponse(boolean success, EventUngeneric event) {
        this.success = success;
        this.event = event;
        this.message = null;
    }

    public EventServiceResponse(boolean success, String message) {
        this.success = success;
        this.event = null;
        this.message = message;
    }

    public EventServiceResponse(boolean success) {
        this.success = success;
        this.event = null;
        this.message = null;
    }

    public boolean isSuccess() {
        return success;
    }

    public EventUngeneric getEvent() {
        return event;
    }

    public String getMessage() {
        return message!=null ? message :  "";
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setEvent(EventUngeneric event) {
        this.event = event;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
