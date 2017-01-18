package eventmanager.microservice.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import eventmanager.microservice.app.healthchecks.SubscribeHealthCheck;
import com.fasterxml.jackson.databind.DeserializationFeature;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import eventmanager.microservice.service.DatabaseService;
import eventmanager.microservice.service.DatabaseServiceImpl;

/**
 * Created by Flo on 07/04/2016.
 */
public class EventManagerApp extends Application<AppConfiguration> {

    public static void main(String[] args) throws Exception {
        new EventManagerApp().run(args);
    }

    @Override
    public String getName() {
        return "event-manager";
    }

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {

    }

    @Override
    public void run(AppConfiguration configuration, Environment environment) throws Exception {

        DatabaseService databaseService = new DatabaseServiceImpl(configuration.getDbConfiguration());

        ObjectMapper objectMapper = new ObjectMapper();

        final SubscriptionResource subscriptionResource = new SubscriptionResource(databaseService);
        environment.jersey().register(subscriptionResource);
        final IncomingEventResource incomingEventResource = new IncomingEventResource(databaseService, objectMapper);
        environment.jersey().register(incomingEventResource);
        final FetchAndBlockEventResource fetchAndBlockEventResource = new FetchAndBlockEventResource(databaseService);
        environment.jersey().register(fetchAndBlockEventResource);
        final FetchAndBlockEventsBatchResource fetchAndBlockEventsBatchResource = new FetchAndBlockEventsBatchResource(databaseService);
        environment.jersey().register(fetchAndBlockEventsBatchResource);
        final SetEventsOutdatedResource setEventsOutdatedResource = new SetEventsOutdatedResource(databaseService, objectMapper);
        environment.jersey().register(setEventsOutdatedResource);
        final UnblockEventResource unblockEventResource = new UnblockEventResource(databaseService);
        environment.jersey().register(unblockEventResource);
        final CountEventsOfTypesReceivedSinceResource countEventsOfTypesReceivedSinceResource = new CountEventsOfTypesReceivedSinceResource(
                databaseService
        );
        environment.jersey().register(countEventsOfTypesReceivedSinceResource);


        environment.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SubscribeHealthCheck subscribeHealthCheck = new SubscribeHealthCheck(subscriptionResource);
        environment.healthChecks().register("subscribe-to-event",subscribeHealthCheck);

    }

}
