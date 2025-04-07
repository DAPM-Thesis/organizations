package com.example.orgb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"controller"})
public class OrgBApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrgBApplication.class, args);
    }

}
