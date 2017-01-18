package eventmanager.clientservices.configuration;

/**
 * Created by flobe on 17/01/2017.
 */
public class EventSubscriptionSingly extends EventSubscription {
    public EventSubscriptionSingly(Enum eventtype) {
        super(EventFetchType.singly,eventtype,10000L);
    }
}
