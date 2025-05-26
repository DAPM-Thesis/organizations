package com.example.orgA;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import repository.TemplateRepository;
import templates.*;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.orgA","controller", "pipeline", "communication", "repository"})
public class OrgAApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OrgAApplication.class, args);

        TemplateRepository templateRepository = context.getBean(TemplateRepository.class);
        templateRepository.storeTemplate("SimpleSource", SourceA.class);
        templateRepository.storeTemplate("SimpleSink", SinkA.class);
        templateRepository.storeTemplate("EventSource", EventSource.class);
        templateRepository.storeTemplate("PetriNetSink", PetriNetSink.class);
        templateRepository.storeTemplate("LanguageFilter", LanguageFilter.class);
    }
}
