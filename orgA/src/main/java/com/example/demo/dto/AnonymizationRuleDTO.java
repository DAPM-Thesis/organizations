package com.dapm2.ingestion_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AnonymizationRuleDTO {
    private String dataSourceId;
    private List<String> pseudonymization;
    private List<String> suppression;
    private String uniqueField;
    private String status;
}
