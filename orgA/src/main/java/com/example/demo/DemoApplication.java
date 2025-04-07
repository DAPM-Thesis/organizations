package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import pipeline.PipelineBuilder;
import pipeline.processingelement.ProcessingElementReference;
import pipeline.processingelement.ProcessingElementType;

@SpringBootApplication
@ComponentScan(basePackages = {"controller"})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

        String organizationID = "orgA";

        // Currently the processElementID's don't matter
        ProcessingElementReference sourceRef = new ProcessingElementReference(organizationID, 1, ProcessingElementType.SOURCE);
        ProcessingElementReference operatorRef = new ProcessingElementReference("orgB", 2, ProcessingElementType.OPERATOR);
        ProcessingElementReference sinkRef = new ProcessingElementReference(organizationID, 3, ProcessingElementType.SINK);
        PipelineBuilder pipelineBuilder = new PipelineBuilder();
        pipelineBuilder.createPipeline(organizationID)
                .addProcessingElement(sourceRef)
                .addProcessingElement(operatorRef)
                .addProcessingElement(sinkRef)
                .connect(sourceRef, operatorRef)
                .connect(operatorRef, sinkRef);

        pipelineBuilder.start();
    }
}
