package controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pipeline.processingelement.Sink;
import templates.SinkA;
import templates.SourceA;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/{organizationID}/{processElementID}")
public class ProcessElementController {
    // Currently no checks are performed on organizationID and processElementID

    // Pretend this instance already existed
    SourceA srcA = new SourceA();
    SinkA sinkA = new SinkA();

    @PostMapping("/publisher/broker/{broker}/topic/{topic}")
    public void connectPublisher(@PathVariable String organizationID, @PathVariable String processElementID, @PathVariable String broker, @PathVariable String topic) {
        String decodedTopic = URLDecoder.decode(topic, StandardCharsets.UTF_8);
        String decodedBroker = URLDecoder.decode(broker, StandardCharsets.UTF_8);
        srcA.registerProducer(decodedTopic, decodedBroker);
    }

    @PostMapping("/subscriber/broker/{broker}/topic/{topic}")
    public void connectSubscriber(@PathVariable String organizationID, @PathVariable String processElementID, @PathVariable String broker, @PathVariable String topic) {
        String decodedTopic = URLDecoder.decode(topic, StandardCharsets.UTF_8);
        String decodedBroker = URLDecoder.decode(broker, StandardCharsets.UTF_8);
        sinkA.registerConsumer(decodedTopic, decodedBroker);
    }

    @PostMapping("/start")
    public void startSource(@PathVariable String organizationID, @PathVariable String processElementID) {
        srcA.start();
    }
}
