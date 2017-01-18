package eventmanager.clientservices.service;

import eventmanager.clientservices.service.clientservercommunication.EventmanagerCommunicationService;
import eventmanager.common.model.Event;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Created by flobe on 10/01/2017.
 * has to be extended + implemented by the microservice
 * provides functions like publish events
 */
public abstract class AbstractEventProcessorService implements EventProcessorService{

    private static final Logger LOGGER = LogManager.getLogger(AbstractEventProcessorService.class);
    private final String logPrefix = "["+Thread.currentThread().getName()+"]";

    private EventmanagerCommunicationService eventmanagerCommunicationService;

    public void publishEvent(Event eventToPublish){
        eventmanagerCommunicationService.publishEvent(eventToPublish);
        LOGGER.debug(logPrefix+" published new event of type "+eventToPublish.toString());
    }

    public void setEventsOutdated(Map<String,Object> keyFields){
        eventmanagerCommunicationService.setEventsOutdated(keyFields);
    }

    public void setEventmanagerCommunicationService(EventmanagerCommunicationService eventmanagerCommunicationService) {
        this.eventmanagerCommunicationService = eventmanagerCommunicationService;
    }
}
