package controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import templates.EventAlgorithmB;
import templates.EventOperatorB;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/pipelineBuilder")
public class ProcessElementController {
    // Currently no checks are performed on organizationID and processElementID

    // Pretend these instances already exist
    EventAlgorithmB eventAlgorithmB = new EventAlgorithmB();
    EventOperatorB operatorB = new EventOperatorB(eventAlgorithmB);

    private String orgABroker = "localhost:29082";

    @PostMapping("/publisher/broker/{broker}/topic/{topic}")
    public void connectPublisher(@PathVariable String broker, @PathVariable String topic) {
        String decodedTopic = URLDecoder.decode(topic, StandardCharsets.UTF_8);
        String decodedBroker = URLDecoder.decode(broker, StandardCharsets.UTF_8);
        operatorB.registerProducer(decodedTopic, decodedBroker);
    }

    @PostMapping("/subscriber/broker/{broker}/topic/{topic}")
    public void connectSubscriber(@PathVariable String broker, @PathVariable String topic) {
        String decodedTopic = URLDecoder.decode(topic, StandardCharsets.UTF_8);
        String decodedBroker = URLDecoder.decode(broker, StandardCharsets.UTF_8);
        operatorB.registerConsumer(decodedTopic, decodedBroker);
    }

    @PostMapping("/start")
    public void startSource() {
    }
}
