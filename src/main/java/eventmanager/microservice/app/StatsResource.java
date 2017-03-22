package eventmanager.microservice.app;

import com.codahale.metrics.annotation.Timed;
import eventmanager.microservice.model.ProcessingState;
import eventmanager.microservice.model.stats.StatsData;
import eventmanager.microservice.service.DatabaseService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
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
    public StatsData getAllStats() {
        //collect data
        Map<ProcessingState,Map<String,Integer>> counts = databaseService.getEventIdentifierCountForEachProcessingState();

        List<List<Object>> transformedCounts = new ArrayList<>();
        for(Map.Entry<ProcessingState,Map<String,Integer>> aProcessingStateCount : counts.entrySet()){
            for(Map.Entry<String,Integer> aEventIdentifierCount : aProcessingStateCount.getValue().entrySet()){
                List<Object> aCount = new ArrayList<>();
                aCount.add(aProcessingStateCount.getKey());
                aCount.add(aEventIdentifierCount.getKey());
                aCount.add(aEventIdentifierCount.getValue());
                transformedCounts.add(aCount);
            }
        }
        StatsData statsData = new StatsData();
        statsData.setProcessingStateEventIdentifierCount(transformedCounts);
        return statsData;
    }

}
