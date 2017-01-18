package eventmanager.common.model;

import java.util.List;

/**
 * Created by flobe on 11/12/2016.
 */
public class MultiEventServiceResponse {

    private boolean success;

    private List<EventUngeneric> events;

    private String message;

    public MultiEventServiceResponse() {
    }

    public MultiEventServiceResponse(boolean success, List<EventUngeneric> events, String message) {
        this.success = success;
        this.events = events;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<EventUngeneric> getEvents() {
        return events;
    }

    public String getMessage() {
        return message!=null ? message : "";
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setEvents(List<EventUngeneric> events) {
        this.events = events;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
