package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import pipeline.PipelineBuilder;
import pipeline.processingelement.ProcessingElementReference;
import pipeline.processingelement.ProcessingElementType;

@SpringBootApplication
@ComponentScan(basePackages = {"controller", "pipeline", "communication"})
public class OrgAApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OrgAApplication.class, args);

        String organizationID = "orgA";

        // Currently the processElementID's don't matter
        ProcessingElementReference sourceRef = new ProcessingElementReference(organizationID, 1, ProcessingElementType.SOURCE);
        ProcessingElementReference operatorRef = new ProcessingElementReference("orgB", 2, ProcessingElementType.OPERATOR);
        ProcessingElementReference sinkRef = new ProcessingElementReference(organizationID, 3, ProcessingElementType.SINK);
        PipelineBuilder pipelineBuilder = context.getBean(PipelineBuilder.class);
        pipelineBuilder.createPipeline(organizationID)
                .addProcessingElement(sourceRef)
                .addProcessingElement(operatorRef)
                .addProcessingElement(sinkRef)
                .connect(sourceRef, operatorRef)
                .connect(operatorRef, sinkRef);

        pipelineBuilder.start();
    }
}
