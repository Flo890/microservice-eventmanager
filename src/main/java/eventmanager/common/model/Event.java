package eventmanager.common.model;

import java.util.Date;
import java.util.Map;

/**
 * Created by flobe on 01/08/2016.
 * TODO cleanup this!!
 */
public class Event<T extends Enum<T>> {

    private String id;

    private T eventtype;

    private Map<String,Object> fields;

    private Map<EventProperty,Object> metaFields;

    private Date publishedAt;

    public Event() {
    }

    public Event(String id, Map<String,Object> fields) {
        this.id = id;
    }

    public Event(T eventtype, Map<String, Object> fields) {
        this.eventtype = eventtype;
        this.fields = fields;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public T getEventtype() {
        return eventtype;
    }

    public void setEventtype(T eventtype) {
        this.eventtype = eventtype;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public Map<EventProperty, Object> getMetaFields() {
        return metaFields;
    }

    public void setMetaFields(Map<EventProperty, Object> metaFields) {
        this.metaFields = metaFields;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if(id!=null){
            stringBuilder.append(id+" / ");
        }
        stringBuilder.append(eventtype.name());
        return stringBuilder.toString();
    }
}
