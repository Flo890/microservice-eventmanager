package eventmanager.microservice.service;

import eventmanager.clientservices.configuration.EventSubscription;
import eventmanager.common.model.Event;
import eventmanager.common.model.EventProperty;
import eventmanager.common.model.EventUngeneric;
import eventmanager.common.model.eventreturnmetadata.EventReturnMetadata;
import org.bson.Document;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by flobe on 31/07/2016.
 */
public interface DatabaseService {

    void addSubscription(String serviceIdentifier, String eventIdentifier);

    /**
     *
     * @param eventIdentifier
     * @param eventFields fields describing the event (e.g. what happend, affected entities, ...)
     * @param metaFields fields describing extra properties for the event, e.g. priority
     * @return amount of published events
     * @throws SQLException
     */
    Integer dispatchIncomingEvent(String eventIdentifier, Map<String,Object> eventFields, Map<EventProperty,Object> metaFields);

    /**
     *
     * @param dispatchedEventId
     * @return true if it could be blocked, false if not (e.g. an other thread took the event)
     */
    EventUngeneric fetchAndBlockEventForProcessing(String serviceIdentifier, String eventIdentifier);

    void unblockEvent(String eventId, EventReturnMetadata eventReturnMetadata);

    List<EventUngeneric> fetchBatchOfEvents(String serviceIdentifier, String eventIdentifier, Integer minBatchSize, Date flushIfOlderThan);

    Integer setEventsOutdatedByEventFields(Map<String,Object> fields);

    Integer countEventsOfTypesReceivedSince(List<String> types, Long since);
}
