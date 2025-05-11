package templates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import communication.message.impl.event.Event;
import communication.message.impl.event.Attribute;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.source.SimpleSource;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SourceA extends SimpleSource<Event> {
    private final BlockingQueue<Event> queue = new LinkedBlockingQueue<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public SourceA(Configuration configuration) {
        super(configuration);

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();

        String url = "http://localhost:8081/ingest/start";
        Request request = new Request.Builder().url(url).build();

        EventSourceListener listener = new EventSourceListener() {
            @Override public void onOpen(EventSource source, Response response) { }

            @Override
            public void onEvent(EventSource source, String id, String type, String data) {
                try {
                    // 1) parse JSON into our inner DTO
                    EventDto dto = mapper.readValue(data, EventDto.class);
                    // 2) convert DTO attributes into domain Attributes
                    Set<Attribute<?>> attrs = dto.attributes().stream()
                            .map(adto -> new Attribute<>(adto.name(), adto.value()))
                            .collect(Collectors.toSet());

                    // 3) build the real Event
                    Event e = new Event(
                            dto.caseID(),
                            dto.activity(),
                            dto.timestamp(),
                            attrs
                    );
                    // 4) hand it off
                    queue.put(e);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override public void onFailure(EventSource source, Throwable t, Response response) {
                t.printStackTrace();
            }
        };

        EventSources.createFactory(client).newEventSource(request, listener);
    }

    @Override
    protected Event process() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public static record EventDto(
            String caseID,
            String activity,
            String timestamp,
            @JsonIgnoreProperties(ignoreUnknown = true)
            java.util.List<AttributeDto> attributes
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record AttributeDto(
            String name,
            Object value
    ) {}
    public static record CustomPayload(){}
}
