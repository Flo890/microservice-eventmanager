package eventmanager.microservice.app.healthchecks;

import eventmanager.microservice.app.SubscriptionResource;
import com.codahale.metrics.health.HealthCheck;
import microservicecommons.interservicecommunication.model.SyncServiceResponse;

/**
 * Created by flobe on 06/12/2016.
 */
public class SubscribeHealthCheck extends HealthCheck {

    private final SubscriptionResource subscriptionResource;

    public SubscribeHealthCheck(SubscriptionResource subscriptionResource) {
        this.subscriptionResource = subscriptionResource;
    }

    @Override
    protected Result check() throws Exception {

        SyncServiceResponse syncServiceResponse = subscriptionResource.subscribeToEvent(
                "healthcheck",
                "healthcheck-event"
        );
        if(syncServiceResponse == null){
            return Result.unhealthy("subscribing to event returned null");
        }
        if(!syncServiceResponse.isSuccess()){
            return Result.unhealthy("subscribing to event returned with success false");
        }
        return Result.healthy();
    }
}
