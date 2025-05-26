package com.example.orgA.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttributeSettingDTO {
    private String caseId;
    private String activity;
    private String timeStamp;
    private List<String> attributes;
    private String status;  // "active", "archived", or "deleted"
}
