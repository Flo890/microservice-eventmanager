package eventmanager.microservice.model.stats;

import org.bson.Document;

import java.util.List;

/**
 * Created by flobe on 31/03/2017.
 */
public class EventFilterListData {

    private List<Document> documents;

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
