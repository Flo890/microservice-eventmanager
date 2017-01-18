package clientservices;

import eventmanager.clientservices.configuration.EventReceiverConfiguration;
import eventmanager.clientservices.service.AbstractEventProcessorService;
import eventmanager.clientservices.service.NonblockingEventProcessingService;
import eventmanager.clientservices.service.clientservercommunication.EventmanagerCommunicationService;
import eventmanager.common.model.Event;
import eventmanager.common.model.eventreturnmetadata.EventExecutionMetadata;
import org.junit.Test;
import testutils.BikemapEventtype;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by flobe on 10/01/2017.
 */
public class NonblockingEventProcessingServiceTest {

    @Test
    public void shouldProcessEventSuccessful() throws InterruptedException {

        EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

        AbstractEventProcessorService eventProcessorService = new AbstractEventProcessorService() {
            @Override
            public EventExecutionMetadata processEvent(Event event) {
                if(event.getId().equals(1L) && event.getFields().get("aFieldKey").equals("aFieldProperty")) {
                    return new EventExecutionMetadata();
                } else {
                    return new EventExecutionMetadata();
                }
            }
        };

        EventmanagerCommunicationService mockedCommunicationService = mock(EventmanagerCommunicationService.class);
        Map<String,Object> fields = new HashMap<>();
        fields.put("aFieldKey","aFieldProperty");
        when(mockedCommunicationService.fetchAndBlockEvent("testmicroservice",BikemapEventtype.enriched_trail_osmmeta))
                .thenReturn(new Event("1",fields));
       // when(mockedCommunicationService.persistEventReturnMetadata(any(EventReturnMetadata.class)))


        NonblockingEventProcessingService<BikemapEventtype> nonblockingEventProcessingService = new NonblockingEventProcessingService(
                "testmicroservice",
                eventReceiverConfiguration,
                eventProcessorService,
                mockedCommunicationService
        );
        nonblockingEventProcessingService.start();

        //wait until finished
        Thread.sleep(2000);

        //check result
        //TODO find a way to assert this
    }

}
