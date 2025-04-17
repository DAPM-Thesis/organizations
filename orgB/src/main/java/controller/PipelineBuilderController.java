package controller;

import communication.API.PEInstanceResponse;
import communication.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pipeline.processingelement.InstanceMetaData;
import pipeline.processingelement.Sink;
import pipeline.processingelement.Source;
import pipeline.processingelement.operator.Operator;
import repository.PEInstanceRepository;
import repository.TemplateRepository;
import utils.IDGenerator;
import utils.JsonUtil;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/pipelineBuilder")
public class PipelineBuilderController {
    @Value("${organization.broker.orgB}")
    private String orgBBroker;

    private final TemplateRepository templateRepository = new TemplateRepository();
    private final PEInstanceRepository peInstanceRepository = new PEInstanceRepository();

    @PostMapping("/source/templateID/{templateID}/instanceNumber/{instanceNumber}")
    public ResponseEntity<PEInstanceResponse> configureSource(@PathVariable String templateID, @PathVariable int instanceNumber) {
        String decodedTemplateID = JsonUtil.decode(templateID);
        String topic = IDGenerator.generateTopic();
        String instanceMetaDataID = peInstanceRepository.storeInstanceMetaData(decodedTemplateID, instanceNumber, orgBBroker, topic, true);

        Source<Message> source = templateRepository.createInstanceFromTemplate(decodedTemplateID);
        if (source != null) {
            source.registerProducer(orgBBroker, topic);
            String instanceID = peInstanceRepository.storeInstance(source, new String[]{instanceMetaDataID});

            return ResponseEntity.ok(new PEInstanceResponse.Builder(decodedTemplateID, instanceNumber)
                    .broker(orgBBroker)
                    .topic(topic)
                    .instanceID(instanceID)
                    .build());
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/operator/consumer/templateID/{templateID}/instanceNumber/{instanceNumber}/broker/{broker}/topic/{topic}")
    public ResponseEntity<PEInstanceResponse> storeOperatorConsumer(@PathVariable String templateID, @PathVariable int instanceNumber, @PathVariable String broker, @PathVariable String topic) {
        String decodedTemplateID = JsonUtil.decode(templateID);
        String decodedTopic = JsonUtil.decode(topic);
        String decodedBroker = JsonUtil.decode(broker);
        String instanceMetaDataID = peInstanceRepository.storeInstanceMetaData(decodedTemplateID, instanceNumber, decodedBroker, decodedTopic, false);

        return ResponseEntity.ok(new PEInstanceResponse.Builder(decodedTemplateID, instanceNumber)
                .instanceMetaDataID(instanceMetaDataID)
                .build());
    }

    @PostMapping("/operator/producer/templateID/{templateID}/instanceNumber/{instanceNumber}")
    public ResponseEntity<PEInstanceResponse> storeOperatorProducer(@PathVariable String templateID, @PathVariable int instanceNumber) {
        String decodedTemplateID = JsonUtil.decode(templateID);
        String topic = IDGenerator.generateTopic();
        String instanceMetaDataID = peInstanceRepository.storeInstanceMetaData(decodedTemplateID, instanceNumber, orgBBroker, topic, true);

        return ResponseEntity.ok(new PEInstanceResponse.Builder(decodedTemplateID, instanceNumber)
                .broker(orgBBroker)
                .topic(topic)
                .instanceMetaDataID(instanceMetaDataID)
                .build());
    }

    @PostMapping("/operator/templateID/{templateID}/instance/{instanceMetaDataIDS}")
    public ResponseEntity<Void> createOperator(@PathVariable String templateID, @PathVariable("instanceMetaDataIDS") String instanceMetaDataIDS) {
        String decodedTemplateID = JsonUtil.decode(templateID);
        Operator<Message, Message> operator = templateRepository.createInstanceFromTemplate(decodedTemplateID);
        if (operator != null) {
            String decodedInstanceMetaDataIDS = JsonUtil.decode(instanceMetaDataIDS);
            String[] instanceMetaDataIDList = decodedInstanceMetaDataIDS.split(",");
            for (String instanceID : instanceMetaDataIDList) {
                InstanceMetaData metadata = peInstanceRepository.getInstanceMetaData(instanceID);
                if (metadata != null) {
                    if (metadata.isProducer()) operator.registerProducer(metadata.brokerURL(), metadata.topic());
                    else operator.registerConsumer(metadata.brokerURL(), metadata.topic());
                }
            }
            peInstanceRepository.storeInstance(operator, instanceMetaDataIDList);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/sink/templateID/{templateID}/instanceNumber/{instanceNumber}/broker/{broker}/topic/{topic}")
    public ResponseEntity<PEInstanceResponse> storeSinkConsumer(@PathVariable String templateID, @PathVariable int instanceNumber, @PathVariable String broker, @PathVariable String topic) {
        String decodedTemplateID = JsonUtil.decode(templateID);
        String decodedTopic = JsonUtil.decode(topic);
        String decodedBroker = JsonUtil.decode(broker);
        String instanceMetaDataID = peInstanceRepository.storeInstanceMetaData(decodedTemplateID, instanceNumber, decodedBroker, decodedTopic, false);

        return ResponseEntity.ok(new PEInstanceResponse.Builder(decodedTemplateID, instanceNumber)
                .instanceMetaDataID(instanceMetaDataID)
                .build());
    }

    @PostMapping("/sink/templateID/{templateID}/instance/{instanceMetaDataIDS}")
    public ResponseEntity<Void> createSink(@PathVariable String templateID, @PathVariable("instanceMetaDataIDS") String instanceMetaDataIDS) {
        String decodedTemplateID = JsonUtil.decode(templateID);
        Sink sink = templateRepository.createInstanceFromTemplate(decodedTemplateID);
        if (sink != null) {
            String decodedInstanceMetaDataIDS = JsonUtil.decode(instanceMetaDataIDS);
            String[] instanceMetaDataIDList = decodedInstanceMetaDataIDS.split(",");
            for (String instanceID : instanceMetaDataIDList) {
                InstanceMetaData metadata = peInstanceRepository.getInstanceMetaData(instanceID);
                if (metadata != null) {
                    sink.registerConsumer(metadata.brokerURL(), metadata.topic());
                }
            }
            peInstanceRepository.storeInstance(sink, instanceMetaDataIDList);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/start/instance/{instanceID}")
    public ResponseEntity<Void> startSource(@PathVariable String instanceID) {
        Source<Message> source = peInstanceRepository.getInstance(instanceID);
        if (source != null) {
            source.start();
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body(null);
    }
}

