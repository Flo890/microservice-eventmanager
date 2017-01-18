package eventmanager.clientservices.service.clientservercommunication;

import eventmanager.common.model.Event;
import eventmanager.common.model.eventreturnmetadata.EventReturnMetadata;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by flobe on 10/01/2017.
 */
public interface EventmanagerCommunicationService<T extends Enum<T>> {

    Event fetchAndBlockEvent(String serviceIdentifier, T eventIdentifier);

    void persistEventReturnMetadata(EventReturnMetadata eventReturnMetadata);

    void subscribeServiceToEvent(String serviceIdentifier, T eventIdentifier);

    void publishEvent(Event event);

    List<Event> fetchAndBlockBatchOfEvents(String serviceIdentifier, Enum eventIdentifier, Integer minBatchSize, Date flushIfOlderThan);

    void setEventsOutdated(Map<String,Object> keyFields);
}
