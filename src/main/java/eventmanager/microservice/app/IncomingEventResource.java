package eventmanager.microservice.app;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eventmanager.common.model.EventProperty;
import microservicecommons.interservicecommunication.model.SyncServiceResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import eventmanager.microservice.service.DatabaseService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by flobe on 31/07/2016.
 */
@Path("/publish-event")
public class IncomingEventResource {

    private static final Logger LOGGER = LogManager.getLogger(IncomingEventResource.class);

    private final DatabaseService databaseService;

    private final ObjectMapper objectMapper;

    public IncomingEventResource(DatabaseService databaseService, ObjectMapper objectMapper) {
        this.databaseService = databaseService;
        this.objectMapper = objectMapper;
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public SyncServiceResponse processIncomingEvent(@QueryParam("eventIdentifier") String eventIdentifier, @QueryParam("eventFields") String eventFieldsString, @QueryParam("metaFields") String metaFieldsString){

        try {
            Map<String, Object> eventFields = null;
            if(eventFieldsString!=null) {
                eventFields = objectMapper.readValue(eventFieldsString, Map.class);
            }

            Map<EventProperty, Object> metaFields = null;
            if(metaFieldsString!=null) {
                Map<String, Object> metaFieldsParsed = objectMapper.readValue(metaFieldsString, Map.class);
                metaFields = new HashMap<>();
                for (Map.Entry<String, Object> aField : metaFieldsParsed.entrySet()) {
                    try {
                        EventProperty eventProperty = EventProperty.valueOf(aField.getKey());
                        metaFields.put(eventProperty, aField.getValue());
                    } catch (IllegalArgumentException e) {
                        LOGGER.warn("received invalid event metafield property: " + aField.getKey());
                        continue;
                    }
                }
            }



            Integer dispatchCount = databaseService.dispatchIncomingEvent(
                    eventIdentifier,
                    eventFields,
                    metaFields
            );
            LOGGER.debug("published event "+eventIdentifier+" to "+dispatchCount+" services");
            return new SyncServiceResponse(true, "dispatched event to " + dispatchCount + " services");
        } catch (Exception e){
            LOGGER.error("processing incoming event failed: "+eventIdentifier+" / "+eventFieldsString+" / "+metaFieldsString,e);
            return new SyncServiceResponse(false,"call failed due to exception: "+e.getMessage());
        }
    }

}
