package com.example.orgb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import repository.TemplateRepository;
import templates.EventOperatorB;
import templates.HeuristicsMiner;
import templates.BehaviouralPatternsConformance;

@SpringBootApplication
@ComponentScan(basePackages = {"controller", "pipeline", "communication", "repository"})
public class OrgBApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OrgBApplication.class, args);

        TemplateRepository templateRepository = context.getBean(TemplateRepository.class);
        templateRepository.storeTemplate("SimpleOperator", EventOperatorB.class);
        templateRepository.storeTemplate("HeuristicsMiner", HeuristicsMiner.class);
        templateRepository.storeTemplate("BehaviouralPatternsConformance", BehaviouralPatternsConformance.class);
    }

}
