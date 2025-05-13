package templates;

import com.example.demo.config.SpringContext;
import com.example.demo.mongo.AnonymizationMappingService;
import com.example.demo.preProcessingElements.AnonymizationProcess;
import com.example.demo.preProcessingElements.AttributeSettingProcess;
import com.example.demo.preProcessingElements.FiltrationProcess;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import communication.message.impl.event.Event;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.source.SimpleSource;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SourceA extends SimpleSource<Event> {

    private static final String SSE_URL      = "https://stream.wikimedia.org/v2/stream/recentchange";
    private static final long   FILTERING_ID = 2L;
    private static final long   ATTRIBUTE_ID = 2L;
    private static final String SOURCE_ID    = "wiki-edit";

    private final BlockingQueue<Event>        eventQueue;
    private final EventSource                 eventSource;
    private final ObjectMapper                mapper;
    private final FiltrationProcess           filtrationProcess;
    private final AttributeSettingProcess     attributeProcess;
    private final AnonymizationProcess        anonymizationProcess;
    private final AnonymizationMappingService anonymizationMappingService;

    public SourceA(Configuration configuration) {
        super(configuration);

        // 1) initialize queue + Jackson
        this.eventQueue = new LinkedBlockingQueue<>();
        this.mapper     = new ObjectMapper();

        // 2) fetch your Spring beans / processes
        this.filtrationProcess        = FiltrationProcess.fromFilterId(FILTERING_ID);
        this.attributeProcess         = AttributeSettingProcess.fromSettingId(ATTRIBUTE_ID);
        this.anonymizationProcess     = AnonymizationProcess.fromDataSourceId(SOURCE_ID);
        this.anonymizationMappingService = SpringContext
                .getBean(AnonymizationMappingService.class);

        // 3) build the EventHandler exactly as in SSEStreamSource
        EventHandler handler = new EventHandler() {
            @Override public void onOpen() {}
            @Override public void onClosed() {}
            @Override public void onComment(String comment) {}

            @Override
            public void onError(Throwable t) {
                System.err.println("Ingestion-Service Error (SSE handler): " + t.getMessage());
                t.printStackTrace();
            }

            @Override
            public void onMessage(String event, MessageEvent messageEvent) {
                try {
                    // parse raw JSON
                    JsonNode json = mapper.readTree(messageEvent.getData());

                    // optional: store raw payload
                    anonymizationMappingService.saveRawData(SOURCE_ID, json);

                    // 1) filtration
                    if (!filtrationProcess.shouldPass(json)) {
                        return;
                    }

                    // 2) anonymization
                    json = anonymizationProcess.apply(json);

                    // 3) attribute-setting → may throw NPE
                    Event dapmEvent = attributeProcess.extractEvent(json);

                    // 4) enqueue for downstream
                    eventQueue.put(dapmEvent);

                } catch (Exception e) {
                    System.err.println("Error processing messageEvent: " + e.getMessage());
                    System.err.println("Payload: " + messageEvent.getData());
                    e.printStackTrace();
                }
            }
        };

        // 4) wire up the source
        this.eventSource = new EventSource.Builder(handler, URI.create(SSE_URL)).build();
    }

    @Override
    protected Event process() {
        try {
            // block until next ingested event
            return eventQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public void start() {
        // kick off the SSE connection
        eventSource.start();
    }

    public void stop() {
        eventSource.close();
    }
}
