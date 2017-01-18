package eventmanager.common.model;

/**
 * Created by flobe on 12/01/2017.
 */
public class EventFactory<T extends Enum<T>> {

    public Event fromEventUngeneric(EventUngeneric eventUngeneric, Class<T> eventtypeClass){
        Event<T> event = new Event<T>();
        event.setId(eventUngeneric.getId());
        try {
            T eventtype = T.valueOf(eventtypeClass, eventUngeneric.getEventIdentifier());
            event.setEventtype(eventtype);
        } catch (IllegalArgumentException e){
            throw new RuntimeException("received event with unknown eventIdentifier: "+eventUngeneric.getEventIdentifier());
        }
        event.setFields(eventUngeneric.getEventFields());
        event.setMetaFields(eventUngeneric.getMetaFields());

        return event;
    }

}
