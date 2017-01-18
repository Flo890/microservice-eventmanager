package eventmanager.clientservices.configuration;

/**
 * Created by flobe on 17/01/2017.
 */
public class EventSubscriptionBatch extends EventSubscription {

    private final Integer minBatchSize;

    private final Integer flushIfOlderThanMinutes;


    public EventSubscriptionBatch(Enum eventtype, Integer minBatchSize, Integer flushIfOlderThanMinutes) {
        this(eventtype, minBatchSize, flushIfOlderThanMinutes,10000L);
    }

    public EventSubscriptionBatch(Enum eventtype, Integer minBatchSize, Integer flushIfOlderThanMinutes, Long lookForWorkIntervall) {
        super(EventFetchType.batch, eventtype, lookForWorkIntervall);
        this.minBatchSize = minBatchSize;
        this.flushIfOlderThanMinutes = flushIfOlderThanMinutes;
    }

    public Integer getMinBatchSize() {
        return minBatchSize;
    }

    public Integer getFlushIfOlderThanMinutes() {
        return flushIfOlderThanMinutes;
    }
}
