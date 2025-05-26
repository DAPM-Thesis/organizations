package com.example.orgA.controller;

import com.example.orgA.dto.AnonymizationRuleDTO;
import com.example.orgA.dto.StatusUpdateDTO;
import com.example.orgA.entity.AnonymizationRule;
import com.example.orgA.service.AnonymizationRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/anonymization")
@Tag(name = "Anonymization API", description = "Manage anonymization rules")
public class AnonymizationRuleController {

    @Autowired
    private AnonymizationRuleService service;

    @Operation(summary = "Save Anonymization Rules", description = "Store the rules for a specific source")
    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody AnonymizationRuleDTO request) {
        service.saveRule(request);
        return ResponseEntity.ok("Anonymization rule saved successfully!");
    }

    @Operation(summary = "Get all rules", description = "Retrieve all stored rules")
    @GetMapping("/retrieve/all")
    public ResponseEntity<List<AnonymizationRule>> getAll() {
        return ResponseEntity.ok(service.getAllRules());
    }

    @Operation(summary = "Get rule by ID", description = "Fetch an anonymization rule using its DB ID")
    @GetMapping("/retrieve/{id}")
    public ResponseEntity<AnonymizationRule> getById(@PathVariable Long id) {
        AnonymizationRule rule = service.getRuleById(id);
        return rule != null
                ? ResponseEntity.ok(rule)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @Operation(summary = "Get rule by dataSourceId", description = "Fetch a rule using its data source ID")
    @GetMapping("/retrieve/by-data-source")
    public ResponseEntity<AnonymizationRule> getByDataSource(@RequestBody Map<String, String> body) {
        String dataSourceId = body.get("dataSourceId");
        AnonymizationRule rule = service.getRuleByDataSourceId(dataSourceId);
        return rule != null
                ? ResponseEntity.ok(rule)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @Operation(summary = "Update Anonymization Rule", description = "Update rule for a source by DB ID")
    @PutMapping("/update/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @RequestBody AnonymizationRuleDTO request
    ) {
        service.updateRule(id, request);
        return ResponseEntity.ok("Anonymization rule updated successfully!");
    }

    @Operation(summary = "Change Status", description = "Update rule status for a source by DB ID")
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> updateAnonymizationStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateDTO dto
    ) {
        boolean result = service.updateRuleStatus(id, dto.getStatus());
        return result
                ? ResponseEntity.ok("Anonymization rule status updated.")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Anonymization rule not found.");
    }

    @Operation(summary = "Delete rule", description = "Permanently delete a rule by DB ID")
    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        boolean deleted = service.deleteRule(id);
        return deleted
                ? ResponseEntity.ok("Anonymization rule deleted successfully!")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Anonymization rule not found.");
    }
}
