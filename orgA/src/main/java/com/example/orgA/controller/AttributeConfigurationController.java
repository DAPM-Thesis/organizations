package com.example.orgA.controller;
import com.example.orgA.dto.*;
import com.example.orgA.entity.*;
import com.example.orgA.service.StreamConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/config/attribute")
@Tag(name = "Attribute Configuration API", description = "Manage attribute configurations")
public class AttributeConfigurationController {
    private final StreamConfigurationService service;
    public AttributeConfigurationController(StreamConfigurationService service) {
        this.service = service;
    }

    @Operation(summary = "Save Attribute", description = "Store the Attributes for specific pipeline into DB")
    @PostMapping("/save")
    public ResponseEntity<String> saveAttributes(@RequestBody AttributeSettingDTO dto) {
        AttributeSetting saved = service.saveAttributes(dto);
        return ResponseEntity.ok("Saved Successfully!!!");
    }
    @Operation(summary = "Get attribute by id", description = "Fetch attributes using its db ID")
    @GetMapping("/retrieve/{id}")
    public ResponseEntity<AttributeSetting> getAttributeSetting(@PathVariable Long id) {
        AttributeSetting setting = service.getAttributeSettingById(id);
        if (setting != null) {
            return ResponseEntity.ok(setting);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @Operation(summary = "Update attributes", description = "Update attribute for specific source by its db id")
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateAttributes(
            @PathVariable Long id, @RequestBody AttributeSettingDTO dto) {
        AttributeSetting updated = service.updateAttributeSetting(id, dto);
        return ResponseEntity.ok("Updated Successfully!!!");
    }
    @Operation(summary = "Change Status of attributes", description = "Update attributes status for specific source by its db id")
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> updateAttributeStatus(@PathVariable Long id, @RequestBody StatusUpdateDTO dto) {
        boolean result = service.updateAttributeStatus(id, dto.getStatus());
        return result ?
                ResponseEntity.ok("Attribute status updated.") :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Attribute not found.");
    }
    @Operation(summary = "Delete attributes", description = "Permanently delete attributes from the db for specific source by its db id")
    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<String> deleteAttributes(@PathVariable Long id) {
        boolean deleted = service.deleteAttributeSetting(id);
        if (deleted) {
            return ResponseEntity.ok("Attribute Setting permanently deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Attribute setting not found.");
        }
    }
}
