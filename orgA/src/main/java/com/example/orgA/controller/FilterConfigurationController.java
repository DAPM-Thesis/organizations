package com.example.orgA.controller;

import com.example.orgA.dto.FilterConfigDTO;
import com.example.orgA.dto.StatusUpdateDTO;
import com.example.orgA.entity.FilterConfig;
import com.example.orgA.service.StreamConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/filter")
@Tag(name = "Filter Configuration API", description = "Manage filter configurations")
public class FilterConfigurationController {
    private final StreamConfigurationService service;
    public FilterConfigurationController(StreamConfigurationService service) {
        this.service = service;
    }

    @Operation(summary = "Save Filter Rules", description = "Store the filters for specific pipeline into DB")
    @PostMapping("/save")
    public ResponseEntity<String> saveFilter(@RequestBody FilterConfigDTO dto) {
        service.saveFilter(dto);
        return ResponseEntity.ok("Filter saved successfully");
    }
    @Operation(summary = "Get filters by id", description = "Fetch filters using its db ID")
    @GetMapping("/retrieve/{id}")
    public ResponseEntity<FilterConfig> getFilter(@PathVariable Long id) {
        FilterConfig config = service.getFilterById(id);
        if (config != null) {
            return ResponseEntity.ok(config);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @Operation(summary = "Update Filter", description = "Update filters for specific source by its db id")
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateFilter(@PathVariable Long id, @RequestBody FilterConfigDTO dto) {
        service.updateFilter(id, dto);
        return ResponseEntity.ok("Filter updated successfully");
    }
    // Status Change
    @Operation(summary = "Change Status of filters", description = "Update filter status for specific source by its db id")
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> updateFilterStatus(@PathVariable Long id, @RequestBody StatusUpdateDTO dto) {
        boolean result = service.updateFilterStatus(id, dto.getStatus());
        return result ?
                ResponseEntity.ok("Filter status updated.") :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Filter not found.");
    }
    @Operation(summary = "Delete filter", description = "Permanently delete filter from the db for specific source by its db id")
    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<String> deleteFilter(@PathVariable Long id) {
        boolean deleted = service.deleteFilter(id);
        if (deleted) {
            return ResponseEntity.ok("Filter permanently deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Filter config not found");
        }
    }
}
