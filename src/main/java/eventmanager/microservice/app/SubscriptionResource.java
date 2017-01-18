package eventmanager.microservice.app;

import com.codahale.metrics.annotation.Timed;

import microservicecommons.interservicecommunication.model.SyncServiceResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import eventmanager.microservice.service.DatabaseService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;


/**
 * Created by Flo on 07/04/2016.
 */
@Path("/subscribe-to-event")
public class SubscriptionResource {

    private static final Logger LOGGER = LogManager.getLogger(SubscriptionResource.class);

    private DatabaseService databaseService;

    public SubscriptionResource(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public SyncServiceResponse subscribeToEvent(@QueryParam("serviceIdentifier") String serviceIdentifier, @QueryParam("eventIdentifier") String eventIdentifier) {

        if(serviceIdentifier == null){
            return new SyncServiceResponse(false,"parameter serviceIdentifier was null");
        }
        if(eventIdentifier == null){
            return new SyncServiceResponse(false,"parameter eventIdentifier was null");
        }

        try {
            databaseService.addSubscription(serviceIdentifier, eventIdentifier);
            return new SyncServiceResponse(true,"subscription added.");
        } catch (Exception e){
            LOGGER.error("could not add subscription: "+serviceIdentifier+" / "+eventIdentifier,e);
            return new SyncServiceResponse(false,"call failed due to exception: "+e.getMessage());
        }
    }

}
