package com.example.orgc;

import candidate_validation.PipelineCandidate;
import candidate_validation.ValidatedPipeline;
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
@ComponentScan(basePackages = {"controller", "pipeline", "communication", "repository"})
public class OrgCApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OrgCApplication.class, args);

        String pipelineID = "orgC_pipeline";
        String contents;
        try {
            contents = Files.readString(Paths.get("orgC/src/main/resources/concrete_pipeline.json"));
            //contents = Files.readString(Paths.get("orgC/src/main/resources/simple_pipeline.json"));
            //contents = Files.readString(Paths.get("orgC/src/main/resources/heuristics_miner_pipeline.json"));
            //contents = Files.readString(Paths.get("orgC/src/main/resources/pipelines/concrete_complex.json"));


            //contents = Files.readString(Paths.get("orgC/src/main/resources/pipelines/simple_2source.json"));
            //contents = Files.readString(Paths.get("orgC/src/main/resources/pipelines/concrete_1.json"));
            //contents = Files.readString(Paths.get("orgC/src/main/resources/pipelines/concrete_2.json"));
            //contents = Files.readString(Paths.get("orgC/src/main/resources/pipelines/concrete_3.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        URI configURI = Paths.get("orgC/src/main/resources/config_schemas").toUri();
        PipelineCandidate pipelineCandidate = new PipelineCandidate(contents, configURI);
        ValidatedPipeline validatedPipeline = new ValidatedPipeline(pipelineCandidate);

        PipelineBuilder pipelineBuilder = context.getBean(PipelineBuilder.class);
        pipelineBuilder.buildPipeline(pipelineID, validatedPipeline);

        PipelineExecutionService executionService = context.getBean(PipelineExecutionService.class);
        executionService.start(pipelineID);

        // terminate the pipeline after 30 secs:
        /*
        try {
            Thread.sleep(120_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        executionService.terminate(pipelineID);

         */


    }
}
