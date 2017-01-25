package eventmanager.clientservices.service.clientservercommunication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eventmanager.common.model.*;
import eventmanager.common.model.eventreturnmetadata.EventReturnMetadata;
import microservicecommons.interservicecommunication.MicroserviceQueryCommand;
import microservicecommons.interservicecommunication.exception.MicroserviceCommunicationException;
import microservicecommons.interservicecommunication.model.RetryOptions;
import microservicecommons.interservicecommunication.model.SyncServiceResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.SocketAddressResolver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by flobe on 10/01/2017.
 */
public class EventmanagerCommunicationServiceHttp<T extends Enum<T>> implements EventmanagerCommunicationService<T> {

    private static final Logger LOGGER = LogManager.getLogger(EventmanagerCommunicationServiceHttp.class);

    private final String EVENTMANAGER_SERVER_HOST;

    private final Class<T> eventtypesClass;

    private final EventFactory<T> eventFactory;

    private final ObjectMapper objectMapper;

    public EventmanagerCommunicationServiceHttp(String EVENTMANAGER_SERVER_HOST, Class<T> eventtypeClass, EventFactory<T> eventFactory, ObjectMapper objectMapper) {
        this.EVENTMANAGER_SERVER_HOST = EVENTMANAGER_SERVER_HOST;
        this.eventtypesClass = eventtypeClass;
        this.eventFactory = eventFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public Event fetchAndBlockEvent(String serviceIdentifier, Enum eventIdentifier) {
        URL queryUrl = null;
        try {
            queryUrl = new URL(
                    EVENTMANAGER_SERVER_HOST+
                            "/fetch-and-block-event" +
                            "?serviceIdentifier="+serviceIdentifier+
                            (eventIdentifier!=null ? "&eventIdentifier="+eventIdentifier.name() : "")
            );
        } catch (MalformedURLException e) {
            LOGGER.error("could not build url from string",e);
        }

        MicroserviceQueryCommand<EventServiceResponse> microserviceQueryCommand = new MicroserviceQueryCommand<>(
                "fetchAndBlockEventCommand",
                queryUrl,
                EventServiceResponse.class,
                false,
                new RetryOptions(2, RetryOptions.RetryFunctionType.EXPONENTIAL,(Integer count) -> {LOGGER.warn("EventStoreService might not be available yet. fetchAndBlock requires retry #"+count+"..."); return null;}),
                20000
        );
        EventServiceResponse eventServiceResponse = microserviceQueryCommand.execute();

        if(eventServiceResponse==null || !eventServiceResponse.isSuccess()){
            throw new MicroserviceCommunicationException("could not fetch and block event "+(eventIdentifier!=null ? "&eventIdentifier="+eventIdentifier.name() : "")+": "+eventServiceResponse.getMessage());
        } else {
            if(eventServiceResponse.getEvent()==null){
                return null;
            }
            return eventFactory.fromEventUngeneric(eventServiceResponse.getEvent(),eventtypesClass);
        }
    }

    @Override
    public void persistEventReturnMetadata(EventReturnMetadata eventReturnMetadata) {
        URL queryUrl = null;
        try {
            queryUrl = new URL(
                    EVENTMANAGER_SERVER_HOST+
                            "/unblock-event"
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/json");

        String postData = null;
        try {
            postData = objectMapper.writeValueAsString(eventReturnMetadata);
        } catch (JsonProcessingException e) {
            LOGGER.error("could not write eventReturnMetadata to json",e);
        }

        MicroserviceQueryCommand<SyncServiceResponse> microserviceQueryCommand = new MicroserviceQueryCommand<>(
                "unblockEventCommand",
                queryUrl,
                "POST",
                headers,
                SyncServiceResponse.class,
                false,
                postData
        );

        SyncServiceResponse syncServiceResponse = microserviceQueryCommand.execute();

        if(!syncServiceResponse.isSuccess()){
            throw new MicroserviceCommunicationException("could not unblock event "+eventReturnMetadata.getEventId()+": "+syncServiceResponse.getMessage());
        }
    }

    @Override
    public void subscribeServiceToEvent(String serviceIdentifier, T eventIdentifier) {

        URL queryUrl = null;
        try {
            queryUrl = new URL(
                    EVENTMANAGER_SERVER_HOST+
                            "/subscribe-to-event" +
                            "?serviceIdentifier="+serviceIdentifier+
                            "&eventIdentifier="+eventIdentifier.name()
            );
        } catch (MalformedURLException e) {
            LOGGER.error("could not build url from string",e);
        }

        MicroserviceQueryCommand<SyncServiceResponse> microserviceQueryCommand = new MicroserviceQueryCommand<>(
                "subscribeToEventCommand",
                queryUrl,
                SyncServiceResponse.class,
                false,
                new RetryOptions(10, RetryOptions.RetryFunctionType.EXPONENTIAL,(Integer count) -> {LOGGER.warn("Eventmanager app might not be available yet. subscribeToEvent requires retry #"+count+"..."); return null;}),
                Integer.MAX_VALUE
        );
        SyncServiceResponse syncServiceResponse = microserviceQueryCommand.execute();

        if(!syncServiceResponse.isSuccess()){
            throw new MicroserviceCommunicationException("could not subscribe to event "+eventIdentifier.name()+": "+syncServiceResponse.getMessage());
        }
    }

    @Override
    public void publishEvent(Event event) {

        String eventFieldsString = null;
        if(event.getFields()!=null) {
            try {
                eventFieldsString = objectMapper.writeValueAsString(event.getFields());
            } catch (JsonProcessingException e) {
                throw new MicroserviceCommunicationException("could not publish event " + event.getEventtype().name() + ": parsing eventfields failed", e);
            }
        }
        String eventMetafieldsString = null;
        if(event.getMetaFields()!=null) {
            try {
                eventMetafieldsString = objectMapper.writeValueAsString(event.getMetaFields());
            } catch (JsonProcessingException e) {
                throw new MicroserviceCommunicationException("could not publish event " + event.getEventtype().name() + ": parsing metafields failed", e);
            }
        }

        URL queryUrl = null;
        try {
            queryUrl = new URL(
                    EVENTMANAGER_SERVER_HOST+
                            "/publish-event" +
                            "?eventIdentifier="+event.getEventtype().name()+
                            (eventFieldsString==null ? "" : "&eventFields="+eventFieldsString)+
                            (eventMetafieldsString==null ? "" : "&metaFields="+eventMetafieldsString)
            );
        } catch (MalformedURLException e) {
            LOGGER.error("could not build url from string",e);
        }

        MicroserviceQueryCommand<SyncServiceResponse> microserviceQueryCommand = new MicroserviceQueryCommand<>(
                "publishEventCommand",
                queryUrl,
                SyncServiceResponse.class,
                false,
                new RetryOptions(10, RetryOptions.RetryFunctionType.EXPONENTIAL,(Integer count) -> {LOGGER.warn("Eventmanager app might not be available yet. subscribeToEvent requires retry #"+count+"..."); return null;}),
                Integer.MAX_VALUE
        );
        SyncServiceResponse syncServiceResponse = microserviceQueryCommand.execute();

        if(!syncServiceResponse.isSuccess()){
            throw new MicroserviceCommunicationException("could not publish event "+event.getEventtype().name()+": "+syncServiceResponse.getMessage());
        }
    }

    @Override
    public List<Event> fetchAndBlockBatchOfEvents(String serviceIdentifier, Enum eventIdentifier, Integer minBatchSize, Date flushIfOlderThan) {
        URL queryUrl = null;
        try {
            queryUrl = new URL(
                    EVENTMANAGER_SERVER_HOST+
                            "/fetch-and-block-events-batch" +
                            "?serviceIdentifier="+serviceIdentifier+
                            (eventIdentifier!=null ? "&eventIdentifier="+eventIdentifier.name() : "") +
                            "&minBatchSize="+minBatchSize+
                            "&flushIfOlderThan="+flushIfOlderThan.getTime()
            );
        } catch (MalformedURLException e) {
            LOGGER.error("could not build url from string",e);
        }

        MicroserviceQueryCommand<MultiEventServiceResponse> microserviceQueryCommand = new MicroserviceQueryCommand<>(
                "fetchAndBlockEventsBatchCommand",
                queryUrl,
                MultiEventServiceResponse.class,
                false,
                new RetryOptions(2, RetryOptions.RetryFunctionType.EXPONENTIAL,(Integer count) -> {LOGGER.warn("EventStoreService might not be available yet. fetchAndBlockBatch requires retry #"+count+"..."); return null;}),
                20000
        );
        MultiEventServiceResponse eventServiceResponse = microserviceQueryCommand.execute();

        if(eventServiceResponse==null || !eventServiceResponse.isSuccess()){
            throw new MicroserviceCommunicationException("could not fetch and block events batch "+(eventIdentifier!=null ? "&eventIdentifier="+eventIdentifier.name() : "")+": "+eventServiceResponse.getMessage());
        } else {
            List<Event> events = new ArrayList<>();
            for(EventUngeneric eventUngeneric : eventServiceResponse.getEvents()) {
                events.add(eventFactory.fromEventUngeneric(eventUngeneric, eventtypesClass));
            }
            return events;
        }
    }

    @Override
    public void setEventsOutdated(Map<String, Object> keyFields) {
        URL queryUrl = null;
        try {
            queryUrl = new URL(
                    EVENTMANAGER_SERVER_HOST+
                            "/set-events-outdated" +
                            "?keyFields="+objectMapper.writeValueAsString(keyFields)
            );
        } catch (MalformedURLException e) {
            LOGGER.error("could not build url from string",e);
        } catch (JsonProcessingException je){
            LOGGER.error("could not write keyFields map to json",je);
        }

        MicroserviceQueryCommand<SyncServiceResponse> microserviceQueryCommand = new MicroserviceQueryCommand<>(
                "setEventsOutdatedCommand",
                queryUrl,
                SyncServiceResponse.class,
                false,
                new RetryOptions(2, RetryOptions.RetryFunctionType.EXPONENTIAL,(Integer count) -> {LOGGER.warn("EventStoreService might not be available yet. setEventsOutdated requires retry #"+count+"..."); return null;}),
                Integer.MAX_VALUE
        );
        SyncServiceResponse syncServiceResponse = microserviceQueryCommand.execute();

        if(syncServiceResponse==null || !syncServiceResponse.isSuccess()){
            throw new MicroserviceCommunicationException("could not set events outdated "+syncServiceResponse.getMessage());
        }
    }

}
