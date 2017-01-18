package eventmanager.clientservices.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by flobe on 10/01/2017.
 */
public class EventReceiverConfiguration<T extends Enum<T>> {

    private List<EventSubscription<T>> eventsToSubscribe = new ArrayList<>();

    public List<EventSubscription<T>> getEventsToSubscribe() {
        return eventsToSubscribe;
    }

    public void setEventsToSubscribe(List<EventSubscription<T>> eventsToSubscribe) {
        this.eventsToSubscribe = eventsToSubscribe;
    }
}
