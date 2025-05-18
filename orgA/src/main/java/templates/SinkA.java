package templates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import communication.message.Message;
import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;
import okhttp3.OkHttpClient;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.Sink;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SinkA extends Sink {

    public SinkA(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void observe(Message message, int portNumber) {
//        if (message instanceof Event) {
//            Event e = (Event) message;
//            System.out.println("SinkA:- Received Event on port " + portNumber + ":");
//            System.out.println("  caseID   = " + e.getCaseID());
//            System.out.println("  activity = " + e.getActivity());
//            System.out.println("  timestamp= " + e.getTimestamp());
//            System.out.println("  attributes:");
//            for (Attribute<?> attr : e.getAttributes()) {
//                System.out.println("    • " + attr.getName() + " = " + attr.getValue());
//            }
//        } else {
//            System.out.println("SinkA:- Received non-Event message: " + message);
//        }
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
        Event e = (Event) message;
        System.out.println(this + " received: " + "  caseID:   " + e.getCaseID()+",  activity:   "
                + e.getActivity() +",  timestamp:" + e.getTimestamp()+ " on port " + portNumber);
        // Convert the full Event object to JSON string
        String eventJson = String.format(
                "{\"caseID\":\"%s\",\"activity\":\"%s\",\"timestamp\":\"%s\"}",
                e.getCaseID(),
                e.getActivity(),
                e.getTimestamp().toString(),
                e.getAttributes()
        );
        try {
            eventJson = new ObjectMapper().writeValueAsString(e);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }

        // Wrap it in a minimal payload: { "result": "..." }
        String payload = null;
        try {
            payload = String.format("{\"result\": %s}", new ObjectMapper().writeValueAsString(eventJson));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForObject("http://localhost:8081/sink/mining-result/save", request, String.class);
            System.out.println("Event sent to ingestion-service for DB storage.");
        } catch (Exception ex) {
            System.err.println("Failed to send event to ingestion-service: " + ex.getMessage());
        }
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }
}
