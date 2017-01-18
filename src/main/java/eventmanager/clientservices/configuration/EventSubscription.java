package eventmanager.clientservices.configuration;

import java.util.Date;

/**
 * Created by flobe on 12/01/2017.
 */
public abstract class EventSubscription<EventtypesClass extends Enum<EventtypesClass>> {

    private final EventtypesClass eventtype;

    private final Long lookForWorkIntervall;

    private final EventFetchType eventFetchType;


    protected EventSubscription(EventFetchType eventFetchType, EventtypesClass eventtype, Long lookForWorkIntervall) {
        this.eventtype = eventtype;
        this.lookForWorkIntervall = lookForWorkIntervall;
        this.eventFetchType = eventFetchType;
    }

    public EventtypesClass getEventtype() {
        return eventtype;
    }

    public Long getLookForWorkIntervall() {
        return lookForWorkIntervall;
    }

    public EventFetchType getEventFetchType() {
        return eventFetchType;
    }


}
