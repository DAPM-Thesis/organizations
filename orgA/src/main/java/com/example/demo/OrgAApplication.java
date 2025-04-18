package com.example.demo;

import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import draft_validation.ChannelReference;
import draft_validation.PipelineDraft;
import draft_validation.ProcessingElementReference;
import draft_validation.SubscriberReference;
import draft_validation.parsing.DraftParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import pipeline.Pipeline;
import pipeline.PipelineBuilder;
import pipeline.service.PipelineExecutionService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootApplication
@ComponentScan(basePackages = {"controller", "pipeline", "communication"})
public class OrgAApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OrgAApplication.class, args);

        String orgID = "orgA";
        String contents;
        try {
           contents = Files.readString(Paths.get("orgA/src/main/resources/simple_pipeline.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PipelineDraft pipelineDraft = (new DraftParser()).deserialize(contents);

        PipelineBuilder pipelineBuilder = context.getBean(PipelineBuilder.class);

        Pipeline pipeline =  pipelineBuilder.buildPipeline(orgID, pipelineDraft);

        PipelineExecutionService executionService = context.getBean(PipelineExecutionService.class);
        executionService.start(pipeline);
    }
}
