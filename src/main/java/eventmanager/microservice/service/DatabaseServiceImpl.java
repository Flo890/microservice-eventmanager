package eventmanager.microservice.service;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import eventmanager.common.model.EventProperty;
import eventmanager.common.model.EventUngeneric;
import eventmanager.common.model.eventreturnmetadata.EventReturnMetadata;
import eventmanager.microservice.exception.DatabaseException;
import eventmanager.microservice.model.ProcessingState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by flobe on 31/07/2016.
 */
public class DatabaseServiceImpl implements DatabaseService {

    private static final Logger LOGGER = LogManager.getLogger(DatabaseServiceImpl.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Map<String,String> dbConfig;

    public static final String EVENTS_COLLECTION_NAME = "received_events";

    public static final String SUBSCRIPTIONS_COLLECTION_NAME = "event_subscriptions";

    public static final Integer defaultTimeoutSecs = 600;

    protected MongoDatabase mongoDatabase;

    public DatabaseServiceImpl(Map<String,String> dbConfiguration) throws SQLException {
        this.dbConfig = dbConfiguration;
        connect();
    }

    private void connect(){
        MongoClient mongoClient = new MongoClient(dbConfig.get("hostname") , Integer.valueOf(dbConfig.get("port")));
        mongoDatabase = mongoClient.getDatabase(dbConfig.get("dbName"));
        //TODO make this throw exception in case of failure
        //boolean auth = mongoDatabase. authenticate("username", "password".toCharArray());
    }


    @Override
    public void addSubscription(String serviceIdentifier, String eventIdentifier) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(SUBSCRIPTIONS_COLLECTION_NAME);

        Document filter = new Document();
        filter.append("serviceIdentifier",serviceIdentifier);
        filter.append("eventIdentifier",eventIdentifier);

        Document document = new Document();
        document.append("serviceIdentifier",serviceIdentifier);
        document.append("eventIdentifier",eventIdentifier);

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(true);
        collection.replaceOne(
                filter,
                document,
                updateOptions
        );
    }

    @Override
    public Integer dispatchIncomingEvent(String eventIdentifier, Map<String,Object> eventFields, Map<EventProperty,Object> metaFields) {
        // -------- load services that have subscribed to the event ---------
        MongoCollection<Document> subscriptionsCollection = mongoDatabase.getCollection(SUBSCRIPTIONS_COLLECTION_NAME);

        Document subscrFilter = new Document()
                .append("eventIdentifier",eventIdentifier);

        Set<String> serviceIdentifiers = new HashSet<>();
        subscriptionsCollection.find(subscrFilter).forEach((Block<? super Document>) (Document document) -> {
            serviceIdentifiers.add((String) document.get("serviceIdentifier"));
        });

        if(serviceIdentifiers.isEmpty()){
            return 0;
        }

        // ---------- add event entry for subscription ----------
        MongoCollection<Document> eventsCollection = mongoDatabase.getCollection(EVENTS_COLLECTION_NAME);

        Document eventDoc = new Document();
        if(eventFields!=null) {
            for (Map.Entry<String, Object> aField : eventFields.entrySet()) {
                eventDoc.append(aField.getKey(), aField.getValue());
            }
        }

        Document metaFieldsDoc = new Document();
        if(metaFields!=null) {
            for (Map.Entry<EventProperty, Object> eventProp : metaFields.entrySet()) {
                metaFieldsDoc.append(eventProp.getKey().name(), eventProp.getValue());
            }
        }
        if(!metaFieldsDoc.containsKey(EventProperty.timeout.name())){
            metaFieldsDoc.append("timeout",defaultTimeoutSecs);
        }

        List<Document> documents = new ArrayList<>();
        for(String serviceIdentifier : serviceIdentifiers){
            Document document = new Document()
                    .append("eventIdentifier",eventIdentifier)
                    .append("serviceIdentifier",serviceIdentifier)
                    .append("publishingDate",new Date())
                    .append("processingMetadata",new Document()
                            .append("processing_state",ProcessingState.unprocessed.name())
                    )
                    .append("event",eventDoc)
                    .append("metaFields",metaFieldsDoc);
            documents.add(document);
        }
        eventsCollection.insertMany(documents);

        return documents.size();
    }

    @Override
    public EventUngeneric fetchAndBlockEventForProcessing(String serviceIdentifier, String eventIdentifier) {
        MongoCollection<Document> eventsCollection = mongoDatabase.getCollection(EVENTS_COLLECTION_NAME);

        Document filter = new Document()
                .append("serviceIdentifier",serviceIdentifier)
                .append("eventIdentifier",eventIdentifier)
                .append("processingMetadata.processing_state",ProcessingState.unprocessed.name()
                );

        Document update = new Document()
                .append("$set",new Document()
                        .append("processingMetadata.processing_state", ProcessingState.processing.name()
                ));

        FindOneAndUpdateOptions findOneAndUpdateOptions = new FindOneAndUpdateOptions();
        findOneAndUpdateOptions.sort(new Document()
        .append("publishingDate",-1));

        Document document = eventsCollection.findOneAndUpdate(
                filter,
                update,
                findOneAndUpdateOptions
        );

        if(document==null){
            return null;
        }
        return EventUngeneric.fromDocument(document);
    }

    @Override
    public void unblockEvent(String eventId, EventReturnMetadata eventReturnMetadata) {
        MongoCollection<Document> eventsCollection = mongoDatabase.getCollection(EVENTS_COLLECTION_NAME);

        Document filter = new Document()
                .append("_id",new ObjectId(eventId))
                .append("processingMetadata.processing_state",ProcessingState.processing.name()
                );

        ProcessingState newProcessingState = null;
        switch(eventReturnMetadata.getEventReturnState()){
            case completed_successful:
                newProcessingState = ProcessingState.processed;
                break;
            case failed_withexception:
                newProcessingState = ProcessingState.failed;
                break;
            case terminated:
                newProcessingState = ProcessingState.terminated;
                break;
        }
        if(newProcessingState == null){
            throw new NullPointerException("return state could not be converted to processing state");
        }

        Document subdoc = new Document()
                .append("processingMetadata",new Document()
                        .append("processing_state",newProcessingState.name())
                        .append("start_time",eventReturnMetadata.getStartTime())
                        .append("end_time",eventReturnMetadata.getEndTime())

                );
        if(eventReturnMetadata.getEventExecutionMetadata()!=null){
            subdoc.append("event_execution_metadata",eventReturnMetadata.getEventExecutionMetadata().toDocument());
        }
        Document update = new Document()
                .append("$set",subdoc);

        UpdateResult updateResult = eventsCollection.updateOne(filter,update);
        if(updateResult.getMatchedCount()<1L){
            throw new DatabaseException("unblock event "+eventId+" did not update any document");
        }
    }

    @Override
    public List<EventUngeneric> fetchBatchOfEvents(String serviceIdentifier, String eventIdentifier, Integer minBatchSize, java.util.Date flushIfOlderThan) {
        MongoCollection<Document> eventsCollection = mongoDatabase.getCollection(EVENTS_COLLECTION_NAME);

        Document filter = new Document()
                .append("serviceIdentifier",serviceIdentifier)
                .append("eventIdentifier",eventIdentifier)
                .append("processingMetadata",new Document()
                        .append("processing_state",ProcessingState.unprocessed.name())
                );

        Document blockUpdate = new Document()
                .append("$set",new Document()
                        .append("processingMetadata",new Document()
                          .append("processing_state", ProcessingState.processing.name())
                        ));

        Document sort = new Document()
                .append("publishingDate",-1);

        List<Document> documents = new ArrayList<>();
        eventsCollection.find(filter).sort(sort).forEach((Block<? super Document>) (Document document) -> {
            documents.add(document);
        });

        if(
                documents.size()>=minBatchSize ||
                (documents.size()>0 && documents.get(0).getDate("publishingDate").before(flushIfOlderThan))){
            //block events
            List<ObjectId> ids = new ArrayList<>();
            documents.forEach(document -> ids.add(document.getObjectId("_id")));
            Document updateFilter = new Document()
                    .append("_id",new Document()
                        .append("$in",ids)
                    );

            eventsCollection.updateMany(updateFilter,blockUpdate);

            //return them
            List<EventUngeneric> eventsUngeneric = new ArrayList<>();
            documents.forEach(document -> eventsUngeneric.add(EventUngeneric.fromDocument(document)));
            return eventsUngeneric;

        } else {
            //not enough / old enough documents available to flush
            return new ArrayList<>();
        }
    }

    public Integer setEventsOutdatedByEventFields(Map<String,Object> fields) {
        MongoCollection<Document> eventsCollection = mongoDatabase.getCollection(EVENTS_COLLECTION_NAME);

        Document filter = new Document();
        for(Map.Entry<String,Object> aField : fields.entrySet()){
            filter.append("event."+aField.getKey(),aField.getValue());
        }
        //add condition that processing state must be unprocessed
        filter.append("processingMetadata.processing_state",ProcessingState.unprocessed.name());

        Document update = new Document()
                .append("$set",new Document()
                        .append("processingMetadata",new Document()
                            .append("processing_state", ProcessingState.outdated.name()))
                );

        UpdateResult updateResult = eventsCollection.updateMany(filter,update);
        return Long.valueOf(updateResult.getMatchedCount()).intValue();
    }

    public Integer countEventsOfTypesReceivedSince(List<String> eventIdentifiers, Long since) {
        MongoCollection<Document> eventsCollection = mongoDatabase.getCollection(EVENTS_COLLECTION_NAME);

        Document filter = new Document();
        filter.append("eventIdentifier",new Document()
            .append("$in",eventIdentifiers)
        )
        .append("publishingDate",new Document()
                .append("$gte",since)
        );

        return new Long(eventsCollection.count(filter)).intValue();
    }

    // ----------- FOR STATS ENDPOINTS ------------

    public Map<ProcessingState,Map<String,Integer>> getEventIdentifierCountForEachProcessingState(Date fromDate, Date toDate){
        MongoCollection<Document> eventsCollection = mongoDatabase.getCollection(EVENTS_COLLECTION_NAME);

        Map<ProcessingState,Map<String,Integer>> counts = new HashMap<>();
        for(ProcessingState aProcessingState : ProcessingState.values()) {
            Map<String,Integer> eventIdentifiersCount = new HashMap<>();
            for(String aEventIdentifier : getAllExistingEventIdentifiers()) {
                Document filter = new Document();
                filter.append("eventIdentifier", aEventIdentifier);
                filter.append("processingMetadata.processing_state", aProcessingState.name());
                filter.append("publishingDate",new Document().append("$gte",fromDate).append("$lte",toDate));
                Integer count = new Long(eventsCollection.count(filter)).intValue();
                eventIdentifiersCount.put(aEventIdentifier,count);
            }
            counts.put(aProcessingState,eventIdentifiersCount);
        }
        return counts;
    }

    public Map<ProcessingState,Map<String,Integer>> getServiceIdentifierCountForEachProcessingState(Date fromDate, Date toDate){
        MongoCollection<Document> eventsCollection = mongoDatabase.getCollection(EVENTS_COLLECTION_NAME);

        Map<ProcessingState,Map<String,Integer>> counts = new HashMap<>();
        for(ProcessingState aProcessingState : ProcessingState.values()) {
            Map<String,Integer> serviceIdentifiersCount = new HashMap<>();
            for(String aServiceIdentifier : getAllExistingServiceIdentifiers()) {
                Document filter = new Document();
                filter.append("serviceIdentifier", aServiceIdentifier);
                filter.append("processingMetadata.processing_state", aProcessingState.name());
                filter.append("publishingDate",new Document().append("$gte",fromDate).append("$lte",toDate));
                Integer count = new Long(eventsCollection.count(filter)).intValue();
                serviceIdentifiersCount.put(aServiceIdentifier,count);
            }
            counts.put(aProcessingState,serviceIdentifiersCount);
        }
        return counts;
    }

    public Map<ProcessingState,Map<String,Integer>> getSubscriptionCountForEachProcessingState(Date fromDate, Date toDate){
        MongoCollection<Document> eventsCollection = mongoDatabase.getCollection(EVENTS_COLLECTION_NAME);

        Map<ProcessingState,Map<String,Integer>> counts = new HashMap<>();
        for(ProcessingState aProcessingState : ProcessingState.values()) {
            Map<String,Integer> subscriptionsCount = new HashMap<>();
            for(Map.Entry<String,List<String>> aServiceIdentifier : getAllExistingSubscriptions().entrySet()) {
                for(String aEventIdentifier : aServiceIdentifier.getValue()){
                    Document filter = new Document();
                    filter.append("serviceIdentifier", aServiceIdentifier.getKey());
                    filter.append("eventIdentifier", aEventIdentifier);
                    filter.append("processingMetadata.processing_state", aProcessingState.name());
                    filter.append("publishingDate",new Document().append("$gte",fromDate).append("$lte",toDate));
                    Integer count = new Long(eventsCollection.count(filter)).intValue();
                    String subscriptionDisplayName = aEventIdentifier+"@"+aServiceIdentifier.getKey();
                    subscriptionsCount.put(subscriptionDisplayName,count);
                }
            }
            counts.put(aProcessingState,subscriptionsCount);
        }
        return counts;
    }

    private List<String> getAllExistingServiceIdentifiers() {
        MongoCollection<Document> eventsCollection = mongoDatabase.getCollection(EVENTS_COLLECTION_NAME);

        Iterable<String> serviceIdentifiersIterable = eventsCollection.distinct("serviceIdentifier",String.class);

        List<String> distinctServiceIdentifiers = new ArrayList<>();
        serviceIdentifiersIterable.forEach(s -> distinctServiceIdentifiers.add(s));

        return distinctServiceIdentifiers;
    }

    private List<String> getAllExistingEventIdentifiers(){
        MongoCollection<Document> eventsCollection = mongoDatabase.getCollection(EVENTS_COLLECTION_NAME);

        Iterable<String> eventIdentifiersIterable = eventsCollection.distinct("eventIdentifier",String.class);

        List<String> distinctEventIdentifiers = new ArrayList<>();
        eventIdentifiersIterable.forEach(s -> distinctEventIdentifiers.add(s));

        return distinctEventIdentifiers;
    }

    /**
     *
     * @return Map serviceIdentifier -> list of subscribed eventIdentifiers
     */
    private Map<String,List<String>> getAllExistingSubscriptions(){
        MongoCollection<Document> subscriptionsCollection = mongoDatabase.getCollection(SUBSCRIPTIONS_COLLECTION_NAME);
        Iterable<Document> subscriptions = subscriptionsCollection.find();

        Map<String,List<String>> transformedSubscriptions = new HashMap<>();
        for(Document aDocument : subscriptions){
            String serviceIdentifier = aDocument.getString("serviceIdentifier");
            if(!transformedSubscriptions.containsKey(serviceIdentifier)){
                transformedSubscriptions.put(serviceIdentifier,new ArrayList<>());
            }
            transformedSubscriptions.get(serviceIdentifier).add(aDocument.getString("eventIdentifier"));
        }

        return transformedSubscriptions;
    }

    /**
     *
     * @param filter
     * @param sort can be null
     * @return
     */
    public Iterable<Document> getEventsNative(Document filter, Document sort, Integer limit){
        MongoCollection<Document> eventsCollection = mongoDatabase.getCollection(EVENTS_COLLECTION_NAME);
        FindIterable<Document> iterable = eventsCollection.find(filter).limit(limit);
        if(sort == null){
            return iterable;
        } else {
            return iterable.sort(sort);
        }
    }

    public boolean overrideProcessingState(String eventId, ProcessingState newProcessingState){
        MongoCollection<Document> eventsCollection = mongoDatabase.getCollection(EVENTS_COLLECTION_NAME);

        Document filter = new Document()
                .append("_id",new ObjectId(eventId));

        Document update = new Document().append("$set",new Document()
                .append("processingMetadata",new Document()
                        .append("processing_state", newProcessingState.name()))
        );

        UpdateResult updateResult = eventsCollection.updateOne(filter,update);
        if(updateResult.getMatchedCount() != 1L){
            return false;
        }
        return true;
    }

}
