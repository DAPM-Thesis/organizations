package com.example.orgA.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * Register a mixin on the core PEInstanceResponse class
     * so that Jackson will ignore the "timestamp" property when serializing.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer ignoreTimestampMixin() {
        return builder ->
                builder.mixIn(
                        communication.API.response.PEInstanceResponse.class,
                        PEInstanceResponseMixin.class
                );
    }

    /**
     * A no-op abstract class just to carry @JsonIgnoreProperties
     */
    @JsonIgnoreProperties({"timestamp"})
    private static abstract class PEInstanceResponseMixin { }
}
