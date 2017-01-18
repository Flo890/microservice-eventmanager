package eventmanager.common.model;

import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by flobe on 12/01/2017.
 */
public class EventUngeneric {

    private String id;

    private String eventIdentifier;

    private Map<String,Object> eventFields;

    private Map<EventProperty,Object> metaFields;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventIdentifier() {
        return eventIdentifier;
    }

    public void setEventIdentifier(String eventIdentifier) {
        this.eventIdentifier = eventIdentifier;
    }

    public Map<String, Object> getEventFields() {
        return eventFields;
    }

    public void setEventFields(Map<String, Object> eventFields) {
        this.eventFields = eventFields;
    }

    public Map<EventProperty, Object> getMetaFields() {
        return metaFields;
    }

    public void setMetaFields(Map<EventProperty, Object> metaFields) {
        this.metaFields = metaFields;
    }

    public static EventUngeneric fromDocument(Document document){
        EventUngeneric eventUngeneric = new EventUngeneric();
        eventUngeneric.setEventIdentifier(document.getString("eventIdentifier"));
        eventUngeneric.setId(document.get("_id").toString());
        eventUngeneric.setEventFields((Map<String,Object>) document.get("event"));
        Map<EventProperty,Object> metaFields = new HashMap<>();
        for(EventProperty aEventProp : EventProperty.values()){
            Document metaDoc = (Document)document.get("metaFields");
            if(metaDoc.containsKey(aEventProp.name())){
                metaFields.put(aEventProp,metaDoc.get(aEventProp.name()));
            }
        }
        eventUngeneric.setMetaFields(metaFields);

        return eventUngeneric;
    }

}
