package controller;

import communication.API.SourceResponse;
import communication.message.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pipeline.processingelement.Instance;
import pipeline.processingelement.Source;
import repository.PEInstanceRepository;
import repository.TemplateRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/pipelineBuilder")
public class ProcessElementController {
    private String orgABroker = "localhost:29092";
    private TemplateRepository templateRepository = new TemplateRepository();
    private PEInstanceRepository peInstanceRepository = new PEInstanceRepository();

    @PostMapping("/source/templateID/{templateID}/instanceNumber/{instanceNumber}")
    public ResponseEntity<SourceResponse> connectSource(@PathVariable String templateID, @PathVariable int instanceNumber) {
        String decodedTemplateID = URLDecoder.decode(templateID, StandardCharsets.UTF_8);
        String topic = createTopic();
        String instanceID = peInstanceRepository.storeInstanceDetails(decodedBroker, decodedTopic, false);
        return ResponseEntity.ok(new SourceResponse(decodedTemplateID, instanceNumber, orgABroker, topic, instanceID));
    }

    @PostMapping("/source/instance/templateID/{templateID}/instanceNumber/{instanceNumber}/instanceID/{instanceID}")
    public String createSourceInstance(@PathVariable String templateID, @PathVariable String instanceID) {
        String decodedTemplateID = URLDecoder.decode(templateID, StandardCharsets.UTF_8);
        String decodedInstanceID = URLDecoder.decode(instanceID, StandardCharsets.UTF_8);

        Source<Message> source =  templateRepository.createInstanceFromTemplate(decodedTemplateID);
        Instance instanceDetails = peInstanceRepository.getInstanceDetails(decodedInstanceID);
        if(instanceDetails != null) {
            if(instanceDetails.isPublisher()) source.registerProducer(instanceDetails.brokerURL(), instanceDetails.topic());
        }
        peInstanceRepository.storeInstance(source);
        return "ResponseEntity.ok()";
    }

    @PostMapping("/operator/{role}/templateID/{templateID}/instanceNumber/{instanceNumber}/broker/{broker}/topic/{topic}")
    public String connectOperator(@PathVariable String role, @PathVariable String templateID, @PathVariable int instanceNumber, @PathVariable String broker, @PathVariable String topic) {
        String decodedTemplateID = URLDecoder.decode(templateID, StandardCharsets.UTF_8);
        String decodedTopic = URLDecoder.decode(topic, StandardCharsets.UTF_8);
        String decodedBroker = URLDecoder.decode(broker, StandardCharsets.UTF_8);
        boolean setProducer = Objects.equals(role, "producer");
        String instanceID = peInstanceRepository.storeInstanceDetails(decodedBroker, decodedTopic, setProducer);

        return "ResponseEntity.ok()";
    }

    @PostMapping("/start")
    public void startSource() {
        srcA.start();
    }

    private String createTopic() {
        return "Topic-" + UUID.randomUUID();
    }
}
