package eventmanager.microservice.app;

import com.codahale.metrics.annotation.Timed;
import eventmanager.common.model.Event;
import eventmanager.common.model.EventUngeneric;
import eventmanager.common.model.MultiEventServiceResponse;
import eventmanager.microservice.exception.DatabaseException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import eventmanager.microservice.service.DatabaseService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by flobe on 11/12/2016.
 */
@Path("/fetch-and-block-events-batch")
public class FetchAndBlockEventsBatchResource {

    private static final Logger LOGGER = LogManager.getLogger(FetchAndBlockEventsBatchResource.class);

    private final DatabaseService databaseService;

    public FetchAndBlockEventsBatchResource(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public MultiEventServiceResponse fetchAndBlockEventBatch(
            @QueryParam("serviceIdentifier") String serviceIdentifier,
            @QueryParam("eventIdentifier") String eventIdentifier,
            @QueryParam("minBatchSize") Integer minBatchSize,
            @QueryParam("flushIfOlderThan") Long flushIfOlderThan){

        try {
            List<EventUngeneric> events = databaseService.fetchBatchOfEvents(
                    serviceIdentifier,
                    eventIdentifier,
                    minBatchSize,
                    new Date(flushIfOlderThan)
            );
            return new MultiEventServiceResponse(true, events, null);
        } catch (Exception e){
            LOGGER.error("could not fetch events batch: "+serviceIdentifier+" / "+eventIdentifier+" / "+minBatchSize+" / "+flushIfOlderThan,e);
            return new MultiEventServiceResponse(false,null,"call failed due to exception: "+e.getMessage());
        }
    }

}
