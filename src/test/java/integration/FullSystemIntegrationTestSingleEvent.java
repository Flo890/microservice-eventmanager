package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import eventmanager.clientservices.configuration.EventReceiverConfiguration;
import eventmanager.clientservices.configuration.EventSubscription;
import eventmanager.clientservices.configuration.EventSubscriptionBatch;
import eventmanager.clientservices.configuration.EventSubscriptionSingly;
import eventmanager.clientservices.service.AbstractEventProcessorService;
import eventmanager.clientservices.service.NonblockingEventProcessingService;
import eventmanager.clientservices.service.clientservercommunication.EventmanagerCommunicationService;
import eventmanager.clientservices.service.clientservercommunication.EventmanagerCommunicationServiceHttp;
import eventmanager.common.model.Event;
import eventmanager.common.model.eventreturnmetadata.EventExecutionMetadata;
import eventmanager.microservice.app.EventManagerApp;
import eventmanager.microservice.service.DatabaseServiceImpl;
import org.bson.Document;
import org.junit.BeforeClass;
import org.junit.Test;
import testutils.BikemapEventtype;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by flobe on 13/01/2017.
 */
public class FullSystemIntegrationTestSingleEvent {

    private static MongoDatabase mongoDatabase;

    private final String testServiceIdentifier = "integration-test-service";

    private static EventmanagerCommunicationService httpService;

    @BeforeClass
    public static void setupTest() throws Exception {
        //create mongo client
        //TODO read properties from app config yml, they have to be the same!
        MongoClient mongoClient = new MongoClient("localhost" , 27017);
        mongoDatabase = mongoClient.getDatabase("eventmanager");

        //start server
        EventManagerApp eventManagerApp = new EventManagerApp();
        eventManagerApp.main(new String[]{"server","app-configuration.yml"});
        Thread.sleep(10000);
        //TODO trigger healthcheck endpoint instead of fixed waiting time

        //create http service (to simplify creating the test conditions)
        httpService = new EventmanagerCommunicationServiceHttp<BikemapEventtype>(
                "http://localhost:9120",
                BikemapEventtype.class,
                new EventFactory<BikemapEventtype>(),
                new ObjectMapper());

    }

    @Test
    public void shouldRunClientservicesAsAMicroserviceWould() throws InterruptedException {

        MongoCollection<Document> subscrCollection = mongoDatabase.getCollection(DatabaseServiceImpl.SUBSCRIPTIONS_COLLECTION_NAME);
        MongoCollection<Document> eventCollection = mongoDatabase.getCollection(DatabaseServiceImpl.EVENTS_COLLECTION_NAME);

        //remove data
        eventCollection.deleteMany(new Document().append("serviceIdentifier",testServiceIdentifier));
        subscrCollection.deleteOne(new Document().append("serviceIdentifier",testServiceIdentifier));

        EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();
        List<EventSubscription<BikemapEventtype>> eventSubscriptions = new ArrayList<>();
        EventSubscription<BikemapEventtype> eventSubscription1 = new EventSubscriptionSingly(BikemapEventtype.integration_test_event);
        eventSubscriptions.add(eventSubscription1);
        eventReceiverConfiguration.setEventsToSubscribe(eventSubscriptions);

        AbstractEventProcessorService eventProcessorService = new AbstractEventProcessorService() {
            @Override
            public EventExecutionMetadata processEvent(Event event) {
                if("value1".equals(event.getFields().get("field1Key"))) {
                    return new EventExecutionMetadata();
                } else {
                    throw new RuntimeException("event's field key and or value was wrong!");
                }
            }
        };

        NonblockingEventProcessingService<BikemapEventtype> nonblockingEventProcessingService = new NonblockingEventProcessingService<BikemapEventtype>(
                testServiceIdentifier,
                eventReceiverConfiguration,
                eventProcessorService,
                BikemapEventtype.class,
                "http://localhost:9120"//TODO read from config yml
        );

        nonblockingEventProcessingService.start();

        // ----- assert subscription -------
        Thread.sleep(5000); //TODO trigger healthcheck endpoint instead of fixed waiting time
        Document document = subscrCollection.find(new Document()
                .append("serviceIdentifier",testServiceIdentifier)
                .append("eventIdentifier",BikemapEventtype.integration_test_event.name()))
                .first();

        assertThat(document,is(notNullValue()));
        assertThat(document.get("serviceIdentifier"),is(testServiceIdentifier));
        assertThat(document.get("eventIdentifier"),is(BikemapEventtype.integration_test_event.name()));

        // ---- add a event that this service should receive -----
        Event event1 = new Event();
        event1.setEventtype(BikemapEventtype.integration_test_event);
        Map<String,Object> fields = new HashMap<>();
        fields.put("field1Key","value1");
        event1.setFields(fields);
        httpService.publishEvent(event1);
        Thread.sleep(2000);

        // ------ assert that event was received and processed -------
        Document eventDoc1 = eventCollection.find(new Document()
        .append("serviceIdentifier",testServiceIdentifier)
        .append("eventIdentifier",BikemapEventtype.integration_test_event.name())
        .append("event.field1Key","value1")
        ).first();

        assertThat(eventDoc1,is(notNullValue()));
        assertThat(((Document)eventDoc1.get("processingMetadata")).get("processing_state"),is("processed"));
    }

    @Test
    public void shouldRunClientservicesAsAMicroserviceWouldBatch() throws InterruptedException {

        MongoCollection<Document> subscrCollection = mongoDatabase.getCollection(DatabaseServiceImpl.SUBSCRIPTIONS_COLLECTION_NAME);
        MongoCollection<Document> eventCollection = mongoDatabase.getCollection(DatabaseServiceImpl.EVENTS_COLLECTION_NAME);

        //remove data
        eventCollection.deleteMany(new Document().append("serviceIdentifier",testServiceIdentifier));
        subscrCollection.deleteOne(new Document().append("serviceIdentifier",testServiceIdentifier));

        EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();
        List<EventSubscription<BikemapEventtype>> eventSubscriptions = new ArrayList<>();
        EventSubscription<BikemapEventtype> eventSubscription1 = new EventSubscriptionBatch(BikemapEventtype.integration_test_event, 3, 0,1L);
        eventSubscriptions.add(eventSubscription1);
        eventReceiverConfiguration.setEventsToSubscribe(eventSubscriptions);

        AbstractEventProcessorService eventProcessorService = new AbstractEventProcessorService() {
            @Override
            public EventExecutionMetadata processEvent(Event event) {
                return new EventExecutionMetadata();

            }
        };

        NonblockingEventProcessingService<BikemapEventtype> nonblockingEventProcessingService = new NonblockingEventProcessingService<BikemapEventtype>(
                testServiceIdentifier,
                eventReceiverConfiguration,
                eventProcessorService,
                BikemapEventtype.class,
                "http://localhost:9120"//TODO read from config yml
        );

        nonblockingEventProcessingService.start();

        // ----- assert subscription -------
        Thread.sleep(5000); //TODO trigger healthcheck endpoint instead of fixed waiting time
        Document document = subscrCollection.find(new Document()
                .append("serviceIdentifier",testServiceIdentifier)
                .append("eventIdentifier",BikemapEventtype.integration_test_event.name()))
                .first();

        assertThat(document,is(notNullValue()));
        assertThat(document.get("serviceIdentifier"),is(testServiceIdentifier));
        assertThat(document.get("eventIdentifier"),is(BikemapEventtype.integration_test_event.name()));

        // ---- add 2 events that should not be processed, because they are to less -----
        Event event1 = new Event();
        event1.setEventtype(BikemapEventtype.integration_test_event);
        Map<String,Object> fields = new HashMap<>();
        fields.put("field1Key","value1");
        event1.setFields(fields);
        httpService.publishEvent(event1);
        Thread.sleep(1000);

        Event event2 = new Event();
        event2.setEventtype(BikemapEventtype.integration_test_event);
        Map<String,Object> fields2 = new HashMap<>();
        fields2.put("field1Key","value2");
        event2.setFields(fields2);
        httpService.publishEvent(event2);
        Thread.sleep(1000);

        // ------ assert that event1 was received and not processed -------
        Document eventDoc1 = eventCollection.find(new Document()
                .append("serviceIdentifier",testServiceIdentifier)
                .append("eventIdentifier",BikemapEventtype.integration_test_event.name())
                .append("event.field1Key","value1")
        ).first();

        assertThat(eventDoc1,is(notNullValue()));
        assertThat(((Document)eventDoc1.get("processingMetadata")).get("processing_state"),is("unprocessed"));

        // ------ assert that event2 was received and not processed -------
        Document eventDoc2 = eventCollection.find(new Document()
                .append("serviceIdentifier",testServiceIdentifier)
                .append("eventIdentifier",BikemapEventtype.integration_test_event.name())
                .append("event.field1Key","value2")
        ).first();

        assertThat(eventDoc2,is(notNullValue()));
        assertThat(((Document)eventDoc2.get("processingMetadata")).get("processing_state"),is("unprocessed"));

        // ------- add another event, so that there are enough events to start processing --------
        Event event3 = new Event();
        event3.setEventtype(BikemapEventtype.integration_test_event);
        Map<String,Object> fields3 = new HashMap<>();
        fields3.put("field1Key","value3");
        event3.setFields(fields3);
        httpService.publishEvent(event3);
        Thread.sleep(1000);

        // --------- assert that all 3 events are processed now ----------
        Thread.sleep(6000);

        Document eventDoc1_2 = eventCollection.find(new Document()
                .append("serviceIdentifier",testServiceIdentifier)
                .append("eventIdentifier",BikemapEventtype.integration_test_event.name())
                .append("event.field1Key","value1")
        ).first();

        assertThat(eventDoc1_2,is(notNullValue()));
        assertThat(((Document)eventDoc1_2.get("processingMetadata")).get("processing_state"),is("processed"));

        Document eventDoc2_2 = eventCollection.find(new Document()
                .append("serviceIdentifier",testServiceIdentifier)
                .append("eventIdentifier",BikemapEventtype.integration_test_event.name())
                .append("event.field1Key","value2")
        ).first();

        assertThat(eventDoc2_2,is(notNullValue()));
        assertThat(((Document)eventDoc2_2.get("processingMetadata")).get("processing_state"),is("processed"));

        Document eventDoc3_2 = eventCollection.find(new Document()
                .append("serviceIdentifier",testServiceIdentifier)
                .append("eventIdentifier",BikemapEventtype.integration_test_event.name())
                .append("event.field1Key","value3")
        ).first();

        assertThat(eventDoc3_2,is(notNullValue()));
        assertThat(((Document)eventDoc3_2.get("processingMetadata")).get("processing_state"),is("processed"));
    }
}
