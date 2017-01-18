package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import eventmanager.clientservices.service.clientservercommunication.EventmanagerCommunicationService;
import eventmanager.clientservices.service.clientservercommunication.EventmanagerCommunicationServiceHttp;
import eventmanager.common.model.Event;
import eventmanager.common.model.EventFactory;
import eventmanager.microservice.app.EventManagerApp;
import eventmanager.microservice.service.DatabaseServiceImpl;
import org.bson.Document;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import testutils.BikemapEventtype;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by flobe on 13/01/2017.
 * This Integration test tests if the server / app component fits together with the client's http communication service
 * running it requires that:
 *  - no other instance of the eventmanager app (or any other process blocking its port) is running
 *  - a mongodb fitting the below configuration is running
 *  TODO check + automatize services setup
 */
public class HttpAppIntegrationTest {

    private static MongoDatabase mongoDatabase;

    private static final String testServiceIdentifier = "integration-test-service";

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
    }

    @BeforeClass
    public static void deletePreviousStuffFromMongodb(){
        MongoCollection<Document> collection = mongoDatabase.getCollection(DatabaseServiceImpl.SUBSCRIPTIONS_COLLECTION_NAME);
        collection.deleteMany(new Document()
        .append("serviceIdentifier",testServiceIdentifier));

        MongoCollection<Document> collection2 = mongoDatabase.getCollection(DatabaseServiceImpl.EVENTS_COLLECTION_NAME);
        collection2.deleteMany(new Document()
                .append("serviceIdentifier",testServiceIdentifier));
    }

    @Test
    public void shouldSubscribeToEvent(){
        EventmanagerCommunicationService httpService = new EventmanagerCommunicationServiceHttp<BikemapEventtype>(
                "http://localhost:9120",
                BikemapEventtype.class,
                new EventFactory<BikemapEventtype>(),
                new ObjectMapper());
        httpService.subscribeServiceToEvent(
                testServiceIdentifier,
                BikemapEventtype.integration_test_event
        );

        MongoCollection<Document> collection = mongoDatabase.getCollection(DatabaseServiceImpl.SUBSCRIPTIONS_COLLECTION_NAME);
        Document document = collection.find(new Document()
        .append("serviceIdentifier",testServiceIdentifier)
        .append("eventIdentifier",BikemapEventtype.integration_test_event.name()))
                .first();

        assertThat(document,is(notNullValue()));
        assertThat(document.get("serviceIdentifier"),is(testServiceIdentifier));
        assertThat(document.get("eventIdentifier"),is(BikemapEventtype.integration_test_event.name()));

    }

    @Test
    public void shouldPublishEvent(){
        EventmanagerCommunicationService httpService = new EventmanagerCommunicationServiceHttp<BikemapEventtype>(
                "http://localhost:9120",
                BikemapEventtype.class,
                new EventFactory<BikemapEventtype>(),
                new ObjectMapper());
        Map<String,Object> fields = new HashMap<>();
        fields.put("aKey","aValue");
        httpService.publishEvent(new Event(
                BikemapEventtype.integration_test_event,
                fields
        ));

        MongoCollection<Document> collection = mongoDatabase.getCollection(DatabaseServiceImpl.EVENTS_COLLECTION_NAME);
        Document document = collection.find(new Document()
                .append("serviceIdentifier",testServiceIdentifier)
                .append("eventIdentifier",BikemapEventtype.integration_test_event.name())
                .append("event.aKey","aValue"))
                .first();

        assertThat(document,is(notNullValue()));
        assertThat(document.get("processingMetadata"),is(notNullValue()));
        assertThat(((Document)document.get("processingMetadata")).get("processing_state"),is("unprocessed"));
    }

    @Test
    public void shouldSetEventOutdated(){
        shouldPublishEvent();

        EventmanagerCommunicationService httpService = new EventmanagerCommunicationServiceHttp<BikemapEventtype>(
                "http://localhost:9120",
                BikemapEventtype.class,
                new EventFactory<BikemapEventtype>(),
                new ObjectMapper());
        Map<String,Object> fields = new HashMap<>();
        fields.put("aKey","aValue");

        httpService.setEventsOutdated(fields);

        MongoCollection<Document> collection = mongoDatabase.getCollection(DatabaseServiceImpl.EVENTS_COLLECTION_NAME);
        Document document = collection.find(new Document()
                .append("serviceIdentifier",testServiceIdentifier)
                .append("eventIdentifier",BikemapEventtype.integration_test_event.name())
                .append("event.aKey","aValue"))
                .first();

        assertThat(document,is(notNullValue()));
        assertThat(document.get("processingMetadata"),is(notNullValue()));
        assertThat(((Document)document.get("processingMetadata")).get("processing_state"),is("outdated"));
    }

    //TODO test other methods of http communication service


}
