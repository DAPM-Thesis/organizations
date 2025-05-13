package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class FilterConfigDTO {
    private Map<String, Object> filters;
    private String status;
}
