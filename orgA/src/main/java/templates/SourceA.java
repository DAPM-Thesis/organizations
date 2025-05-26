package templates;

import com.example.orgA.preProcessingElements.AnonymizationProcess;
import com.example.orgA.preProcessingElements.AttributeSettingProcess;
import com.example.orgA.preProcessingElements.FiltrationProcess;
import communication.message.impl.event.Event;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.source.SimpleSource;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;
import java.time.Duration;

public class SourceA extends SimpleSource<Event> {

    private static final String SSE_URL      = "https://stream.wikimedia.org/v2/stream/recentchange";
    private static final long   FILTERING_ID = 2L;
    private static final long   ATTRIBUTE_ID = 2L;
    private static final String SOURCE_ID    = "wiki-edit";

    private FiltrationProcess       filtrationProcess;
    private AnonymizationProcess    anonymizationProcess;
    private AttributeSettingProcess attributeProcess;

    public SourceA(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected Event process() {
        // lazy-init on first call
        if (filtrationProcess == null) {
            System.out.println("Lazy init of pipeline steps");
            this.filtrationProcess    = FiltrationProcess   .fromFilterId(FILTERING_ID);
            this.anonymizationProcess = AnonymizationProcess.fromDataSourceId(SOURCE_ID);
            this.attributeProcess     = AttributeSettingProcess.fromSettingId(ATTRIBUTE_ID);
        }

        System.out.println("SourceA into!!");

        JsonNode json = WebClient.create()
                .get()
                .uri(SSE_URL)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .blockFirst(Duration.ofSeconds(10));

        if (json == null) {
            throw new RuntimeException("Timed out waiting for a Wikipedia edit event");
        }

        System.out.println("Filtration Step into!!");
        if (!filtrationProcess.shouldPass(json)) {
            System.out.println("  → filtered out, pulling next…");
            return null;  // will cause SimpleSource to call process() again
        }

        json = anonymizationProcess.apply(json);

        Event dapmEvent = attributeProcess.extractEvent(json);
        return dapmEvent;
    }
}
