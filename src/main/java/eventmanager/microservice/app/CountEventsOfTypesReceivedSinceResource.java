package eventmanager.microservice.app;

import com.codahale.metrics.annotation.Timed;
import microservicecommons.interservicecommunication.model.SyncServiceResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import eventmanager.microservice.service.DatabaseService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by flobe on 25/12/2016.
 */
@Path("/count-events-of-types-received-since")
public class CountEventsOfTypesReceivedSinceResource {

    private static final Logger LOGGER = LogManager.getLogger(CountEventsOfTypesReceivedSinceResource.class);

    private final DatabaseService databaseService;

    public CountEventsOfTypesReceivedSinceResource(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public SyncServiceResponse fetchAndBlockEvent(@QueryParam("types") String types, @QueryParam("since") Long since) {

        List<String> typesList = Arrays.asList(types.split(";"));

        Integer count = null;
        try {
            count = databaseService.countEventsOfTypesReceivedSince(typesList,since);
        } catch (Exception e) {
            LOGGER.error("could not count events of tpyes "+types+" since "+since,e);
            return new SyncServiceResponse(false,e.getMessage());
        }

        return new SyncServiceResponse(true,count.toString());

    }

}
