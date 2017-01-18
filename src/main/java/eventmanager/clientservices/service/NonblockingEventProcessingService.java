package eventmanager.clientservices.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eventmanager.clientservices.configuration.EventReceiverConfiguration;
import eventmanager.clientservices.configuration.EventSubscription;
import eventmanager.clientservices.service.clientservercommunication.EventmanagerCommunicationService;
import eventmanager.clientservices.service.clientservercommunication.EventmanagerCommunicationServiceHttp;
import eventmanager.common.model.Event;
import eventmanager.common.model.EventFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by flobe on 10/01/2017.
 * this service should be launched by your microservice when it is ready to do work.
 * it receives events from the eventmanager which are determined for this microservice.
 * This service is non-blocking, so it will return immediate and continue runing in
 * a separate thread.
 */
public class NonblockingEventProcessingService<EventtypesClass extends Enum<EventtypesClass>> extends Thread {

    private static final String THREAD_NAME = "eventProcessingServiceThread";
    private static final String THREADGROUP_NAME = "eventmanagerThreadgroup";

    private static final Logger LOGGER = LogManager.getLogger(NonblockingEventProcessingService.class);
    private final String logPrefix = "["+THREAD_NAME+"]";

    private final EventmanagerCommunicationService eventmanagerCommunicationService;
    private final String microserviceName;
    private final EventReceiverConfiguration eventReceiverConfiguration;
    private final AbstractEventProcessorService eventProcessorService;
    private List<EventReceivingThread> eventReceivingThreads;

    /**
     * extended constructor for test uses only
     * @param microserviceName
     * @param eventReceiverConfiguration
     * @param eventProcessorService
     * @param eventmanagerCommunicationService
     */
    public NonblockingEventProcessingService(String microserviceName, EventReceiverConfiguration eventReceiverConfiguration, AbstractEventProcessorService eventProcessorService, EventmanagerCommunicationService eventmanagerCommunicationService) {
        super(new ThreadGroup(THREADGROUP_NAME), THREAD_NAME);

        this.eventmanagerCommunicationService = eventmanagerCommunicationService;
        this.microserviceName = microserviceName;
        this.eventReceiverConfiguration = eventReceiverConfiguration;
        this.eventProcessorService = eventProcessorService;

        this.eventProcessorService.setEventmanagerCommunicationService(eventmanagerCommunicationService);
        this.eventReceivingThreads = new ArrayList<>();
    }

    /**
     * creates a new instance of this service for a microservice
     * @param microserviceName of the embedding microservice, for whom events should be received
     * @param eventReceiverConfiguration specifies which events the microservice will subscribe to,
     *                                   and other stuff in this regard
     * @param eventProcessorService the microservice function that will be called for processing a event
     * @param eventtypesClass you have to provide a enum specifing all occuring event types
     * @param eventmanagerServerHost url of the eventmanager microservice; if you run it locally
     *                               and didn't change the port it is http://localhost:9120
     */
    public NonblockingEventProcessingService(String microserviceName, EventReceiverConfiguration eventReceiverConfiguration, AbstractEventProcessorService eventProcessorService, Class eventtypesClass, String eventmanagerServerHost){
        this(
                microserviceName,
                eventReceiverConfiguration,
                eventProcessorService,
                new EventmanagerCommunicationServiceHttp(
                        eventmanagerServerHost,
                        eventtypesClass,
                        new EventFactory(),
                        new ObjectMapper())
        );
    }

    @Override
    public void run() {

        // ------------ SUBSCRIBE TO EVENTS -------------
        for(EventSubscription<EventtypesClass> eventSubscription : (List<EventSubscription<EventtypesClass>>) eventReceiverConfiguration.getEventsToSubscribe()){
            eventmanagerCommunicationService.subscribeServiceToEvent(
                    microserviceName,
                    eventSubscription.getEventtype()
            );
        }

        // --------- START RECEIVING THREADS -----------
        // for each subscribed event
        for(EventSubscription<EventtypesClass> eventSubscription : (List<EventSubscription<EventtypesClass>>) eventReceiverConfiguration.getEventsToSubscribe()){
            EventReceivingThread eventReceivingThread = new EventReceivingThread(
                    getThreadGroup(),
                    eventSubscription,
                    eventProcessorService,
                    eventmanagerCommunicationService,
                    microserviceName
            );
            eventReceivingThreads.add(eventReceivingThread);
            eventReceivingThread.start();
        }

    }

    public void publishEvent(Event event){
        eventmanagerCommunicationService.publishEvent(event);
        LOGGER.debug(logPrefix+" published new event of type "+event.toString());
    }
}
