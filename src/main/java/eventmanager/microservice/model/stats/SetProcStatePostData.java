package eventmanager.microservice.model.stats;

import java.util.List;

/**
 * Created by flobe on 01/04/2017.
 */
public class SetProcStatePostData {

    private String newProcessingState;

    private List<String> eventIds;

    public String getNewProcessingState() {
        return newProcessingState;
    }

    public void setNewProcessingState(String newProcessingState) {
        this.newProcessingState = newProcessingState;
    }

    public List<String> getEventIds() {
        return eventIds;
    }

    public void setEventIds(List<String> eventIds) {
        this.eventIds = eventIds;
    }
}
