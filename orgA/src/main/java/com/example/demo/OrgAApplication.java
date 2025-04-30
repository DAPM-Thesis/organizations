package com.example.demo;

import candidate_validation.*;
import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import candidate_validation.parsing.CandidateParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import pipeline.Pipeline;
import pipeline.PipelineBuilder;
import pipeline.service.PipelineExecutionService;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
@ComponentScan(basePackages = {"controller", "pipeline", "communication"})
public class OrgAApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OrgAApplication.class, args);

        String orgID = "orgA";
        String contents;
        try {
           contents = Files.readString(Paths.get("orgA/src/main/resources/heuristics_miner_pipeline.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        URI configURI = Paths.get("orgA/src/main/resources/config_schemas").toUri();
        PipelineCandidate pipelineCandidate = new PipelineCandidate(contents, configURI);
        ValidatedPipeline validatedPipeline = new ValidatedPipeline(pipelineCandidate);

        PipelineBuilder pipelineBuilder = context.getBean(PipelineBuilder.class);

        Pipeline pipeline =  pipelineBuilder.buildPipeline(orgID, validatedPipeline);

        PipelineExecutionService executionService = context.getBean(PipelineExecutionService.class);
        executionService.start(pipeline);
    }
}
