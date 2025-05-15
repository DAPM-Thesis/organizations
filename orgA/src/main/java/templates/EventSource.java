package templates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import communication.message.impl.event.Event;
import org.springframework.web.reactive.function.client.WebClient;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.source.WebSource;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashSet;

public class EventSource extends WebSource<Event> {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://stream.wikimedia.org/v2/stream/recentchange")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EventSource(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected Flux<Event> process() {
        return webClient.get()
                .retrieve()
                .bodyToFlux(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .handle((incomingEvent, sink) -> {
                    try {
                        System.out.println(incomingEvent);
                        JsonNode root = objectMapper.readTree(incomingEvent);
                        JsonNode titleNode = root.get("title");
                        JsonNode typeNode = root.get("type");
                        JsonNode timestampNode = root.get("timestamp");
                        if (titleNode != null && typeNode != null && timestampNode != null) {
                            String title = titleNode.asText();
                            String type = typeNode.asText();
                            String timestamp = timestampNode.asText();
                            sink.next(new Event(title, type, timestamp, new HashSet<>() {}));
                        }
                    } catch (JsonProcessingException e) {
                        sink.error(new RuntimeException(e));
                    }
                });
    }
}
