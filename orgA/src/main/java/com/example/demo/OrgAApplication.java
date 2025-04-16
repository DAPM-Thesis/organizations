package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import pipeline.Pipeline;
import pipeline.PipelineBuilder;
import pipeline.processingelement.ProcessingElementReference;
import pipeline.processingelement.ProcessingElementType;
import pipeline.service.PipelineExecutionService;

@SpringBootApplication
@ComponentScan(basePackages = {"controller", "pipeline", "communication"})
public class OrgAApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OrgAApplication.class, args);

        String orgAID = "orgA";
        String orgBID = "orgB";
        String orgAHost = "http://localhost:8082";
        String orgBHost = "http://localhost:8083";

        // Currently the processElementID's don't matter
        ProcessingElementReference sourceRef = new ProcessingElementReference(orgAID, orgAHost, "SimpleSource", 1, ProcessingElementType.SOURCE);
        ProcessingElementReference operatorRef = new ProcessingElementReference(orgBID, orgBHost, "SimpleOperator", 1, ProcessingElementType.OPERATOR);
        ProcessingElementReference sinkRef = new ProcessingElementReference(orgAID, orgAHost, "SimpleSink", 1, ProcessingElementType.SINK);

        PipelineBuilder configService = context.getBean(PipelineBuilder.class);

        Pipeline pipeline =  configService.createPipeline(orgAID)
                .addProcessingElement(sourceRef)
                .addProcessingElement(operatorRef)
                .addProcessingElement(sinkRef)
                .connect(sourceRef, operatorRef)
                .connect(operatorRef, sinkRef)
                .configure()
                .getCurrentPipeline();

        PipelineExecutionService executionService = context.getBean(PipelineExecutionService.class);
        executionService.start(pipeline);
    }
}
