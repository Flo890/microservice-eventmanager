package eventmanager.common.model.eventreturnmetadata;

import org.bson.Document;

import javax.print.Doc;
import java.util.Map;

/**
 * Created by flobe on 10/01/2017.
 * Object that holds data describing a event execution.
 * Wether it succeeded  or failed, how long it took, some resource statistics in executionMetaData
 */
public class EventReturnMetadata {

    // -------- common fields for all types ----------

    private String eventId;

    private EventReturnState eventReturnState;

    private Long startTime;

    private Long endTime;

    private EventExecutionMetadata eventExecutionMetadata;

    // --------- specific fields for failed ---------

    private Exception exception;

    // --------- specific fields for success ------

    // --------- specific fields for terminated --------

    private String reason;

    // -------- fields end --------

    public EventReturnMetadata() {
    }

    private EventReturnMetadata(EventReturnState eventReturnState, String eventId, Long startTime, Long endTime, EventExecutionMetadata eventExecutionMetadata){
        this.eventReturnState = eventReturnState;
        this.eventId = eventId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventExecutionMetadata = eventExecutionMetadata;
    }

    public static EventReturnMetadata createFailed(String eventId, Long startTime, Long endTime, EventExecutionMetadata eventExecutionMetadata, Exception exception) {
        EventReturnMetadata eventReturnMetadata = new EventReturnMetadata(EventReturnState.failed_withexception,eventId,startTime,endTime,eventExecutionMetadata);
        eventReturnMetadata.setException(exception);
        return eventReturnMetadata;
    }

    public static EventReturnMetadata createSuccess(String eventId, Long startTime, Long endTime, EventExecutionMetadata eventExecutionMetadata) {
        EventReturnMetadata eventReturnMetadata = new EventReturnMetadata(EventReturnState.completed_successful,eventId,startTime,endTime,eventExecutionMetadata);
        return eventReturnMetadata;
    }

    public static EventReturnMetadata createTerminated(String eventId, Long startTime, Long endTime, EventExecutionMetadata eventExecutionMetadata, String reason) {
        EventReturnMetadata eventReturnMetadata = new EventReturnMetadata(EventReturnState.terminated,eventId,startTime,endTime,eventExecutionMetadata);
        eventReturnMetadata.setReason(reason);
        return eventReturnMetadata;
    }

    public String getEventId() {
        return eventId;
    }

    public EventReturnState getEventReturnState() {
        return eventReturnState;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public EventExecutionMetadata getEventExecutionMetadata() {
        return eventExecutionMetadata;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setEventReturnState(EventReturnState eventReturnState) {
        this.eventReturnState = eventReturnState;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public void setEventExecutionMetadata(EventExecutionMetadata eventExecutionMetadata) {
        this.eventExecutionMetadata = eventExecutionMetadata;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


}
