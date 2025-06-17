package templates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;
import org.springframework.web.reactive.function.client.WebClient;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.source.WebSource;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EventSource extends WebSource<Event> {
    private static volatile boolean shutdownScheduled = false;
    private static final long START_TIME = System.currentTimeMillis();
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://stream.wikimedia.org/v2/stream/recentchange")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EventSource(Configuration configuration) {
        super(configuration);
        //scheduleAutoTerminate();
    }

    @Override
    protected Flux<Event> process() {
        return webClient.get()
                .retrieve()
                .bodyToFlux(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .handle((incomingEvent, sink) -> {
                    try {
                        JsonNode root = objectMapper.readTree(incomingEvent);
                        JsonNode titleNode = root.get("user");
                        JsonNode typeNode = root.get("type");
                        JsonNode timestampNode = root.get("timestamp");
                        JsonNode metaNode = root.get("meta");
                        JsonNode domain = metaNode.get("domain");
                        if (titleNode != null && typeNode != null && timestampNode != null && domain != null) {
                            String title = titleNode.asText();
                            String type = typeNode.asText();
                            String timestamp = timestampNode.asText();
                            Set<Attribute<?>> attributes = new HashSet<>();
                            Attribute<String> language = new Attribute<>("domain", domain.asText());
                            attributes.add(language);
                            sink.next(new Event(title, type, timestamp, attributes));
                        }
                    } catch (JsonProcessingException e) {
                        sink.error(new RuntimeException(e));
                    }
                });
    }

    private void scheduleAutoTerminate() {
        if (shutdownScheduled) {
            return;
        }
        shutdownScheduled = true;

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "EventSource-AutoShutdown");
            t.setDaemon(true);
            return t;
        });

        long now = System.currentTimeMillis();
        long elapsedSinceStart = now - START_TIME;
        long delayUntilThirtySeconds = 30_000 - elapsedSinceStart;
        if (delayUntilThirtySeconds < 0) {
            // If more than 30 s have already passed, schedule immediately
            delayUntilThirtySeconds = 0;
        }

        scheduler.schedule(() -> {
            try {
                System.out.println("30 s passed—sleeping for another 30 s to trigger missed‐heartbeat…");
                Thread.sleep(30_000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }

            System.out.println("60 s since start—calling terminate() on EventSource.");
            boolean didTerminate = this.terminate();
            if (didTerminate) {
                System.out.println("EventSource terminated successfully after hold.");
            } else {
                System.err.println("EventSource failed to terminate.");
            }
        }, delayUntilThirtySeconds, TimeUnit.MILLISECONDS);
    }
}


