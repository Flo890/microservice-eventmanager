package eventmanager.clientservices.service.eventprocessing;

import eventmanager.clientservices.service.AbstractEventProcessorService;
import eventmanager.common.model.Event;
import eventmanager.common.model.eventreturnmetadata.EventExecutionMetadata;
import eventmanager.common.model.eventreturnmetadata.EventReturnMetadata;

import java.util.concurrent.Callable;

/**
 * Created by flobe on 10/01/2017.
 * for each incoming event, an instance of this will be created
 * executing each event in a separate thread allows stuff like timeouts
 *
 */
public class EventProcessingCallable implements Callable<EventExecutionMetadata> {

    private final Event event;

    private final AbstractEventProcessorService eventProcessorService;

    /**
     *
     * @param event to be processed
     * @param eventProcessorService class of the microservice that implements the processor service
     */
    public EventProcessingCallable(Event event, AbstractEventProcessorService eventProcessorService) {
        if(event == null){
            throw new IllegalArgumentException("event must not be null");
        }
        if(eventProcessorService == null){
            throw new IllegalArgumentException("eventProcessingService must not be null");
        }
       this.event = event;
       this.eventProcessorService = eventProcessorService;
    }

    @Override
    public EventExecutionMetadata call() throws Exception {
        //do work
        return eventProcessorService.processEvent(event);
    }


}
