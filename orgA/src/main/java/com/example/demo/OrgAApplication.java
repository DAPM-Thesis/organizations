package com.example.demo;

import impl.pipe1.MyEventAlgorithm;
import impl.pipe1.MyEventOperator;
import impl.pipe1.MyEventSource;
import impl.pipe1.MySink;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import pipeline.Pipeline;
import pipeline.PipelineBuilder;
import pipeline.accesscontrolled.processingelement.AccessControlledProcessingElement;
import pipeline.accesscontrolled.processingelement.ProcessingElementToken;
import pipeline.processingelement.ProcessingElement;
import pipeline.processingelement.ProcessingElementReference;
import pipeline.processingelement.ProcessingElementType;
import pipeline.service.PipelineExecutionService;

@SpringBootApplication
@ComponentScan(basePackages = {"controller", "pipeline", "communication", "impl.pipe1"})
public class OrgAApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OrgAApplication.class, args);

        String orgAID = "orgA";
        String orgBID = "orgB";

        var sourceToken = new ProcessingElementToken("sourceTokenValue", orgAID);
        var operatorToken = new ProcessingElementToken("operatorTokenValue", orgBID);
        var sinkToken = new ProcessingElementToken("sinkTokenValue", orgAID);

        var sourceACPE = new AccessControlledProcessingElement<ProcessingElement>(new MyEventSource(), sourceToken);
        var operatorACPE = new AccessControlledProcessingElement<ProcessingElement>(new MyEventOperator(new MyEventAlgorithm()), operatorToken);
        var sinkACPE = new AccessControlledProcessingElement<ProcessingElement>(new MySink(), sinkToken);

        PipelineBuilder builder = context.getBean(PipelineBuilder.class);
        Pipeline pipeline = builder.createPipeline(orgAID)
                .addACPE(sourceACPE)
                .addACPE(operatorACPE)
                .addACPE(sinkACPE)
                .connect(sourceACPE, operatorACPE)
                .connect(operatorACPE, sinkACPE)
                .getCurrentPipeline();

        PipelineExecutionService executionService = context.getBean(PipelineExecutionService.class);
        executionService.start(pipeline);
    }
}
