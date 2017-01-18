package eventmanager.microservice.app;

import com.codahale.metrics.annotation.Timed;
import eventmanager.common.model.Event;
import eventmanager.common.model.EventServiceResponse;
import eventmanager.common.model.EventUngeneric;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import eventmanager.microservice.service.DatabaseService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;

/**
 * Created by flobe on 11/12/2016.
 */
@Path("/fetch-and-block-event")
public class FetchAndBlockEventResource {

    private static final Logger LOGGER = LogManager.getLogger(FetchAndBlockEventResource.class);

    private final DatabaseService databaseService;

    public FetchAndBlockEventResource(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public EventServiceResponse fetchAndBlockEvent(@QueryParam("serviceIdentifier") String serviceIdentifier, @QueryParam("eventIdentifier") String eventIdentifier){
        //looping because the block can fail if an other eventmanager.microservice.service trys to block the same event concurrent.
        //should usually not occur but can

        try {
            EventUngeneric eventUngeneric = databaseService.fetchAndBlockEventForProcessing(serviceIdentifier, eventIdentifier);

            return new EventServiceResponse(true, eventUngeneric, null);

        } catch (Exception e){
            LOGGER.error("could not fetch event for "+serviceIdentifier+" / "+eventIdentifier,e);
            return new EventServiceResponse(false,null,"call failed due to exception: "+e.getMessage());
        }

    }

}
