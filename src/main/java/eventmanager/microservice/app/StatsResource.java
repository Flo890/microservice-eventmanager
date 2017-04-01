package eventmanager.microservice.app;

import com.codahale.metrics.annotation.Timed;
import eventmanager.microservice.model.ProcessingState;
import eventmanager.microservice.model.stats.EventFilterListData;
import eventmanager.microservice.model.stats.SetProcStatePostData;
import eventmanager.microservice.model.stats.StatsData;
import eventmanager.microservice.service.DatabaseService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by flobe on 21/03/2017.
 */
@Path("/stats")
public class StatsResource {

    private final DatabaseService databaseService;

    public StatsResource(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    /*

    has to produce a json like:
    [
     ['unprocessed','new-activity-imported',23], (Zahl ist die Anzahl der Events)
    ]

    man könnte dann das gleiche nochmal machen, nach service aufgeschlüsselt:
    [
     ['unprocessed','trail-extractor-service',25]
    ]
     */

    @GET
    @Path("processingstate_eventidentifier_count")
    @Produces(MediaType.APPLICATION_JSON)
    public StatsData getAllStats(@QueryParam("fromDate") Long fromDate, @QueryParam("toDate") Long toDate) {

        StatsData statsData = new StatsData();

        //collect data per eventIdentifier
        Map<ProcessingState,Map<String,Integer>> countsPerEvent = databaseService.getEventIdentifierCountForEachProcessingState(new Date(fromDate),new Date(toDate));
        List<List<Object>> transformedEventCounts = new ArrayList<>();
        for(Map.Entry<ProcessingState,Map<String,Integer>> aProcessingStateCount : countsPerEvent.entrySet()){
            for(Map.Entry<String,Integer> aEventIdentifierCount : aProcessingStateCount.getValue().entrySet()){
                List<Object> aCount = new ArrayList<>();
                aCount.add(aProcessingStateCount.getKey());
                aCount.add(aEventIdentifierCount.getKey());
                aCount.add(aEventIdentifierCount.getValue());
                transformedEventCounts.add(aCount);
            }
        }
        statsData.setProcessingStateEventIdentifierCount(transformedEventCounts);

        //collect data per serviceIdentifier
        Map<ProcessingState,Map<String,Integer>> countsPerService = databaseService.getServiceIdentifierCountForEachProcessingState(new Date(fromDate),new Date(toDate));
        List<List<Object>> transformedServiceCounts = new ArrayList<>();
        for(Map.Entry<ProcessingState,Map<String,Integer>> aProcessingStateCount : countsPerService.entrySet()){
            for(Map.Entry<String,Integer> aServiceIdentifierCount : aProcessingStateCount.getValue().entrySet()){
                List<Object> aCount = new ArrayList<>();
                aCount.add(aProcessingStateCount.getKey());
                aCount.add(aServiceIdentifierCount.getKey());
                aCount.add(aServiceIdentifierCount.getValue());
                transformedServiceCounts.add(aCount);
            }
        }
        statsData.setProcessingStateServiceIdentifierCount(transformedServiceCounts);

        //collect data per subscription
        Map<ProcessingState,Map<String,Integer>> countsPerSubscription = databaseService.getSubscriptionCountForEachProcessingState(new Date(fromDate),new Date(toDate));
        List<List<Object>> transformedSubscriptionsCounts = new ArrayList<>();
        for(Map.Entry<ProcessingState,Map<String,Integer>> aProcessingStateCount : countsPerSubscription.entrySet()){
            for(Map.Entry<String,Integer> aSubscriptionIdentifierCount : aProcessingStateCount.getValue().entrySet()){
                List<Object> aCount = new ArrayList<>();
                aCount.add(aProcessingStateCount.getKey());
                aCount.add(aSubscriptionIdentifierCount.getKey());
                aCount.add(aSubscriptionIdentifierCount.getValue());
                transformedSubscriptionsCounts.add(aCount);
            }
        }
        statsData.setProcessingStateSubscriptionsCount(transformedSubscriptionsCounts);


        return statsData;
    }

    /**
     *
     * @param fromDate
     * @param toDate
     * @param filterExpressions komma-separated list of equals expressions:  processingMetadata.processingState=processing,aField=aValue
     * @param sortExpressions komma-separated list of sort expressions: publishingDate:desc,aNumberField:asc
     * @param limit amount of events to return
     * @return
     */
    @GET
    @Path("event_filter_list")
    @Produces(MediaType.APPLICATION_JSON)
    public EventFilterListData getEventFilterListData(
            @QueryParam("fromDate") Long fromDate,
            @QueryParam("toDate") Long toDate,
            @QueryParam("filterExpressions") String filterExpressions,
            @QueryParam("sortExpressions") String sortExpressions,
            @QueryParam("limit") Integer limit) {

        //build filter
        Document filter = new Document();
        if(!StringUtils.isEmpty(filterExpressions)) {
            for (String aFilter : filterExpressions.split(",")) {
                String[] aFilterSplit = aFilter.split("=");
                filter.append(aFilterSplit[0], aFilterSplit[1]);
            }
        }
        filter.append("publishingDate", new Document("$gte",new Date(fromDate)).append("$lte",new Date(toDate)));

        //build sort
        Document sort = null;
        if(!StringUtils.isEmpty(sortExpressions)) {
            sort = new Document();
            for (String aSort : sortExpressions.split(",")) {
                String[] aSortSplit = aSort.split(":");
                sort.append(
                        aSortSplit[0],
                        aSortSplit[1].equals("asc") ? 1 : -1
                );
            }
        }

        //fetch data
        Iterable<Document> iterable = databaseService.getEventsNative(filter,sort, limit);
        List<Document> documents = new ArrayList<>();
        for(Document document : iterable){
            document.append("objectId",document.get("_id").toString());
            documents.add(document);
        }

         // return data
        EventFilterListData eventFilterListData = new EventFilterListData();
        eventFilterListData.setDocuments(documents);
        return eventFilterListData;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("set_processing_state")
    public String getEventFilterListData(SetProcStatePostData setProcStatePostData){
        for(String eventId : setProcStatePostData.getEventIds()){
            databaseService.overrideProcessingState(
                    eventId,
                    ProcessingState.valueOf(setProcStatePostData.getNewProcessingState())
            );
        }
        return "done";
    }
}
