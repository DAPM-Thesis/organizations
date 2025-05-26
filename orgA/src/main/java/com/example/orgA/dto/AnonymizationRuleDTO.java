package com.example.orgA.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AnonymizationRuleDTO {
    private String dataSourceId;
    private List<String> pseudonymization;
    private List<String> suppression;
    private String uniqueField;
    private String status;
}