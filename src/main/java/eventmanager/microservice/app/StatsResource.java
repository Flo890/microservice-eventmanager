package eventmanager.microservice.app;

import com.codahale.metrics.annotation.Timed;
import eventmanager.microservice.model.ProcessingState;
import eventmanager.microservice.model.stats.StatsData;
import eventmanager.microservice.service.DatabaseService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    @Timed
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

}
