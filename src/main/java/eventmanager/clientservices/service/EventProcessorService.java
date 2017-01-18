package eventmanager.clientservices.service;

import eventmanager.common.model.Event;
import eventmanager.common.model.eventreturnmetadata.EventExecutionMetadata;
import eventmanager.common.model.eventreturnmetadata.EventReturnMetadata;

/**
 * Created by flobe on 10/01/2017.
 */
public interface EventProcessorService {

    /**
     * will be called by NonblockingEventProcessingService to process a received event
     * @param event
     * @return
     */
    EventExecutionMetadata processEvent(Event event) throws Exception;

    /**
     * call this to publish a event which follows this events execution
     * should be called as soon as possible, because in case the rest of the current execution fails,
     * event publishing would fail as well
     * @param eventToPublish
     */
    void publishEvent(Event eventToPublish);

}
