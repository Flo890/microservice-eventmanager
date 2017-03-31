package eventmanager.microservice.model.stats;

import java.util.List;

/**
 * Created by flobe on 21/03/2017.
 */
public class StatsData {

    List<List<Object>> processingStateEventIdentifierCount;

    List<List<Object>> processingStateServiceIdentifierCount;

    List<List<Object>> processingStateSubscriptionsCount;

    public List<List<Object>> getProcessingStateEventIdentifierCount() {
        return processingStateEventIdentifierCount;
    }

    public void setProcessingStateEventIdentifierCount(List<List<Object>> processingStateEventIdentifierCount) {
        this.processingStateEventIdentifierCount = processingStateEventIdentifierCount;
    }

    public List<List<Object>> getProcessingStateServiceIdentifierCount() {
        return processingStateServiceIdentifierCount;
    }

    public void setProcessingStateServiceIdentifierCount(List<List<Object>> processingStateServiceIdentifierCount) {
        this.processingStateServiceIdentifierCount = processingStateServiceIdentifierCount;
    }

    public List<List<Object>> getProcessingStateSubscriptionsCount() {
        return processingStateSubscriptionsCount;
    }

    public void setProcessingStateSubscriptionsCount(List<List<Object>> processingStateSubscriptionsCount) {
        this.processingStateSubscriptionsCount = processingStateSubscriptionsCount;
    }
}