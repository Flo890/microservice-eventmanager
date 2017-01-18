package eventmanager.microservice.app;

import com.codahale.metrics.annotation.Timed;
import eventmanager.common.model.eventreturnmetadata.EventReturnMetadata;
import microservicecommons.interservicecommunication.model.SyncServiceResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import eventmanager.microservice.service.DatabaseService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by flobe on 11/12/2016.
 */
@Path("/unblock-event")
public class UnblockEventResource {

    private static final Logger LOGGER = LogManager.getLogger(UnblockEventResource.class);

    private final DatabaseService databaseService;

    public UnblockEventResource(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @POST
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public SyncServiceResponse unblockEvent(EventReturnMetadata eventReturnMetadata){

        try {
            databaseService.unblockEvent(eventReturnMetadata.getEventId(), eventReturnMetadata);
            return new SyncServiceResponse(true, null);
        } catch (Exception e){
            LOGGER.error("could not unblock event",e);
            return new SyncServiceResponse(false,"call failed due to exception: "+e.getMessage());
        }
    }

}
