package eventmanager.common.model.eventreturnmetadata;

import org.bson.Document;

import java.util.Date;

/**
 * Created by flobe on 12/01/2017.
 * holds metrics about a event execution; e.g. api calls amount
 */
public class EventExecutionMetadata {

    private Integer aProperty;

    public EventExecutionMetadata() {
        aProperty = 1;
    }

    public Document toDocument(){
        return new Document();
    }

    public Integer getaProperty() {
        return aProperty;
    }

    public void setaProperty(Integer aProperty) {
        this.aProperty = aProperty;
    }
}
