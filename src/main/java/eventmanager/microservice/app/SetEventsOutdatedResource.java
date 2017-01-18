package eventmanager.microservice.app;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import microservicecommons.interservicecommunication.model.SyncServiceResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import eventmanager.microservice.service.DatabaseService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * Created by flobe on 11/12/2016.
 */
@Path("/set-events-outdated")
public class SetEventsOutdatedResource {

    private static final Logger LOGGER = LogManager.getLogger(SetEventsOutdatedResource.class);

    private final DatabaseService databaseService;

    private final ObjectMapper objectMapper;

    public SetEventsOutdatedResource(DatabaseService databaseService, ObjectMapper objectMapper) {
        this.databaseService = databaseService;
        this.objectMapper = objectMapper;
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public SyncServiceResponse setEventsOutdated(@QueryParam("keyFields") String keyFieldsString){

        try {
            Map<String,Object> keyFields = objectMapper.readValue(keyFieldsString,Map.class);
            Integer count = databaseService.setEventsOutdatedByEventFields(keyFields);

            return new SyncServiceResponse(true, "", count);
        } catch (Exception e){
            LOGGER.error("could not set events outdated for keys "+keyFieldsString);
            return new SyncServiceResponse(false, "call failed due to exception: "+e.getMessage());
        }
    }

}
