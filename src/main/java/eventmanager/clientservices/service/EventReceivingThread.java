package eventmanager.clientservices.service;

import eventmanager.clientservices.configuration.EventSubscription;
import eventmanager.clientservices.configuration.EventSubscriptionBatch;
import eventmanager.clientservices.service.clientservercommunication.EventmanagerCommunicationService;
import eventmanager.clientservices.service.eventprocessing.EventProcessingCallable;
import eventmanager.common.model.Event;
import eventmanager.common.model.EventProperty;
import eventmanager.common.model.eventreturnmetadata.*;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by flobe on 12/01/2017.
 * For each event a service subscribes to, one instance of this is created (each as separate Thread)
 * So different events / event subscription are processed in parallel and with its
 * individual properties
 */
public class EventReceivingThread extends Thread {

    /**
     * the eventtype and its required properties which should be processed by this thread
     */
    private final EventSubscription eventSubscription;

    private final AbstractEventProcessorService eventProcessorService;

    private final EventmanagerCommunicationService eventmanagerCommunicationService;

    private final String microserviceName;

    private final Logger LOGGER = LogManager.getLogger(EventReceivingThread.class);
    private final String logPrefix;

    public EventReceivingThread(ThreadGroup threadGroup, EventSubscription eventSubscription, AbstractEventProcessorService eventProcessorService, EventmanagerCommunicationService eventmanagerCommunicationService, String microserviceName) {
        super(threadGroup, "eventReceivingThread_"+eventSubscription.getEventtype().name());
        this.eventSubscription = eventSubscription;
        this.eventProcessorService = eventProcessorService;
        this.eventmanagerCommunicationService = eventmanagerCommunicationService;
        this.microserviceName = microserviceName;

        logPrefix = "["+getName()+"] ";
    }

    @Override
    public void run() {
        super.run();

        List<Event> fetchedEvents = new ArrayList<>();
        while(!interrupted()) {

            try {

                // ----------- LISTEN / LOOK FOR EVENTS TO PROCESS -----------

                if (fetchedEvents.isEmpty()) {
                    try {
                        switch (eventSubscription.getEventFetchType()) {
                            case singly:
                                Event aEvent = eventmanagerCommunicationService.fetchAndBlockEvent(
                                        microserviceName,
                                        eventSubscription.getEventtype()
                                );
                                if (aEvent != null) {
                                    fetchedEvents.add(aEvent);
                                }
                                break;

                            case batch:
                                EventSubscriptionBatch eventSubscriptionBatch = (EventSubscriptionBatch) eventSubscription;
                                fetchedEvents.addAll(eventmanagerCommunicationService.fetchAndBlockBatchOfEvents(
                                        microserviceName,
                                        eventSubscriptionBatch.getEventtype(),
                                        eventSubscriptionBatch.getMinBatchSize(),
                                        DateUtils.addMinutes(new Date(), eventSubscriptionBatch.getFlushIfOlderThanMinutes())
                                ));
                                break;
                        }
                    } catch (Exception e) {
                        LOGGER.fatal(logPrefix + "exception fetch work loop: ", e);
                    }
                }

                if (fetchedEvents.isEmpty()) {
                    LOGGER.debug(logPrefix + "no events found to process");
                    try {
                        sleep(eventSubscription.getLookForWorkIntervall());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(logPrefix + "sleep in fetch event loop failed", e);
                    }

                } else {

                    Event fetchedEvent = fetchedEvents.get(0);
                    fetchedEvents.remove(fetchedEvent);
                    // ------- PROCESS A RECEIVED EVENT ---------
                    EventProcessingCallable eventProcessingCallable = new EventProcessingCallable(
                            fetchedEvent,
                            eventProcessorService
                    );

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Future<EventExecutionMetadata> future = executor.submit(eventProcessingCallable);

                    EventReturnMetadata eventReturnMetadata = null;
                    Long startTime = new Date().getTime();
                    try {
                        LOGGER.debug(logPrefix + "Starting processing event " + fetchedEvent.getId());
                        EventExecutionMetadata eventExecutionMetadata = future.get((Integer) fetchedEvent.getMetaFields().get(EventProperty.timeout), TimeUnit.SECONDS);
                        Long endTime = new Date().getTime();
                        LOGGER.debug(logPrefix + "Finished processing event " + fetchedEvent.getId());
                        eventReturnMetadata = EventReturnMetadata.createSuccess(fetchedEvent.getId(), startTime, endTime, eventExecutionMetadata);

                    } catch (TimeoutException te) {
                        future.cancel(true);
                        LOGGER.warn(logPrefix + "Processing event " + fetchedEvent.getId() + " was terminated due to timeout exceedance: " + fetchedEvent.getMetaFields().get(EventProperty.timeout));
                        eventReturnMetadata = EventReturnMetadata.createTerminated(
                                fetchedEvent.getId(),
                                startTime,
                                new Date().getTime(),
                                null, //TODO find a way to get the execution metadata in case of failure
                                "exceeded timeout: " + fetchedEvent.getMetaFields().get(EventProperty.timeout)
                        );

                    } catch (InterruptedException | ExecutionException e) {
                        LOGGER.warn(logPrefix + "Processing event " + fetchedEvent.getId() + " failed due to exception: "+e.getMessage(), e);
                        eventReturnMetadata = EventReturnMetadata.createFailed(
                                fetchedEvent.getId(),
                                startTime,
                                new Date().getTime(),
                                null, //TODO find a way to get the execution metadata in case of failure
                                e
                        );
                    }

                    // send the result of this event processing to the server
                    eventmanagerCommunicationService.persistEventReturnMetadata(eventReturnMetadata);

                    executor.shutdownNow();
                }
            } catch(Exception e){
                LOGGER.fatal("fetch work loop failed unexpectedly",e);
                try {
                    sleep(5000L);
                } catch (InterruptedException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
        LOGGER.fatal("fetch work loop of thread "+getName()+" stopped.");
    }
}
