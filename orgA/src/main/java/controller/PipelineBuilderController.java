package controller;

import ch.qos.logback.core.subst.Token;
import communication.API.HTTPClient;
import communication.API.PEInstanceResponse;
import communication.config.ConsumerConfig;
import communication.config.ProducerConfig;
import communication.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pipeline.PipelineBuilder;
import pipeline.processingelement.Sink;
import pipeline.processingelement.Source;
import pipeline.processingelement.accesscontrolled.PEToken;
import pipeline.processingelement.operator.Operator;
import repository.PEInstanceRepository;
import repository.TemplateRepository;
import utils.IDGenerator;
import utils.JsonUtil;

import pipeline.processingelement.accesscontrolled.AccessControlledProcessingElement;
import pipeline.heartbeat.HeartbeatManager;
import pipeline.heartbeat.VerificationStrategy;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/pipelineBuilder")
public class PipelineBuilderController {
    @Value("${organization.broker.orgA}")
    private String orgABroker;

    private final TemplateRepository templateRepository = new TemplateRepository();
    private final PEInstanceRepository peInstanceRepository = new PEInstanceRepository();
    private HTTPClient httpClient;
    @Autowired
    public PipelineBuilderController(HTTPClient httpClient) { this.httpClient = httpClient; }
    @PostMapping("/source/templateID/{templateID}")
    public ResponseEntity<PEInstanceResponse> configureSource(@PathVariable String templateID) {
        String decodedTemplateID = JsonUtil.decode(templateID);
        String instanceID = IDGenerator.generateInstanceID();
        PEToken token = new PEToken(null, instanceID, httpClient);
        Source<Message> source = templateRepository.createInstanceFromTemplate(decodedTemplateID, token);
        if (source != null) {
            String topic = IDGenerator.generateTopic();
            ProducerConfig producerConfig = new ProducerConfig(orgABroker, topic);
            source.registerProducer(producerConfig);
            peInstanceRepository.storeInstance(instanceID, source);

            return ResponseEntity.ok(new PEInstanceResponse
                    .Builder(decodedTemplateID, instanceID)
                    .producerConfig(producerConfig)
                    .build());
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/operator/templateID/{templateID}")
    public ResponseEntity<PEInstanceResponse> createOperator(@PathVariable String templateID, @RequestBody List<ConsumerConfig> consumerConfigs) {
        String decodedTemplateID = JsonUtil.decode(templateID);
        String instanceID = IDGenerator.generateInstanceID();
        PEToken token = new PEToken(null, instanceID, httpClient);
        Operator<Message, Message> operator = templateRepository.createInstanceFromTemplate(decodedTemplateID, token);
        if (operator != null) {
            for (ConsumerConfig config : consumerConfigs) {
                operator.registerConsumer(config);
            }
            String topic = IDGenerator.generateTopic();
            ProducerConfig producerConfig = new ProducerConfig(orgABroker, topic);
            operator.registerProducer(producerConfig);

            peInstanceRepository.storeInstance(instanceID, operator);
            return ResponseEntity.ok(new PEInstanceResponse
                    .Builder(decodedTemplateID, instanceID)
                    .producerConfig(producerConfig)
                    .build());
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/sink/templateID/{templateID}")
    public ResponseEntity<PEInstanceResponse> createSink(@PathVariable String templateID, @RequestBody List<ConsumerConfig> consumerConfigs) {
        String decodedTemplateID = JsonUtil.decode(templateID);
        String instanceID = IDGenerator.generateInstanceID();
        PEToken token = new PEToken(null, instanceID, httpClient);
        Sink sink = templateRepository.createInstanceFromTemplate(decodedTemplateID, token);
        if (sink != null) {
            for (ConsumerConfig config : consumerConfigs) {
                sink.registerConsumer(config);
            }

            String hbTopic = "hb-sink-" + instanceID;          // any unique topic name
            ProducerConfig dummy = new ProducerConfig(orgABroker, hbTopic);

            peInstanceRepository.storeInstance(instanceID, sink);
            return ResponseEntity.ok(new PEInstanceResponse
                    .Builder(decodedTemplateID, instanceID)
                    .producerConfig(dummy)
                    .build());
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PutMapping("/start/instance/{instanceID}")
    public ResponseEntity<Void> startSource(@PathVariable String instanceID) {
        Source<Message> source = peInstanceRepository.getInstance(instanceID);
        if (source != null) {
            source.start();
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/heartbeat/publisher/instance/{instanceID}")
    public ResponseEntity<Void> addHeartbeatPublisher(
            @PathVariable String instanceID,
            @RequestBody ProducerConfig cfg)
    {
        AccessControlledProcessingElement pe =
                peInstanceRepository.getInstance(instanceID);
        if (pe == null) return ResponseEntity.notFound().build();

        /* create HeartbeatManager lazily */
        if (pe.getHeartbeatManager() == null) {
            pe.attachHeartbeatManager(
                    new HeartbeatManager(
                            PipelineBuilder.HB_PERIOD,
                            VerificationStrategy.anyWithin(PipelineBuilder.HB_TOLERANCE),
                            cfg,              // producer
                            null,             // no consumer yet
                            java.util.Set.of() ));
            System.out.println("ATTACH-hb-publisher-"+instanceID+" PE: "+pe.getClass());
        } else {
            pe.getHeartbeatManager().registerProducer(cfg);
            System.out.println("REGISTER-hb-publisher-"+instanceID+" PE: "+pe.getClass());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/heartbeat/consumer/instance/{instanceID}")
    public ResponseEntity<Void> addHeartbeatConsumer(
            @PathVariable String instanceID,
            @RequestBody ConsumerConfig cfg)
    {
        AccessControlledProcessingElement pe =
                peInstanceRepository.getInstance(instanceID);
        if (pe == null) return ResponseEntity.notFound().build();

        if (pe.getHeartbeatManager() == null) {
            pe.attachHeartbeatManager(
                    new HeartbeatManager(
                            PipelineBuilder.HB_PERIOD,
                            VerificationStrategy.allWithin(PipelineBuilder.HB_TOLERANCE),
                            null,             // no producer
                            cfg,              // consumer
                            java.util.Set.of() ));
            System.out.println("ATTACH-hb-consumer-"+instanceID+" PE: "+pe.getClass());
        } else {
            pe.getHeartbeatManager().registerConsumer(cfg);
            System.out.println("REGISTER-hb-consumer-"+instanceID+" PE: "+pe.getClass());
        }
        return ResponseEntity.ok().build();
    }
}
