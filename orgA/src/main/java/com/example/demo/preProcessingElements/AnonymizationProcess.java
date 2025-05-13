// AnonymizationProcess.java
package com.dapm2.ingestion_service.preProcessingElements;

import com.dapm2.ingestion_service.config.SpringContext;
import com.dapm2.ingestion_service.mongo.AnonymizationMappingService;
import com.dapm2.ingestion_service.entity.AnonymizationRule;
import com.dapm2.ingestion_service.service.AnonymizationRuleService;
import com.dapm2.ingestion_service.utils.AppConstants;
import com.dapm2.ingestion_service.utils.JsonNodeUtils;   // ← new import
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Wraps and pseudonymizes JSON; now honors nested paths.
 */
public class AnonymizationProcess {
    private final String dataSourceId;
    private final List<String> pseudoFields;
    private final List<String> supFields;
    private final String uniqueField;
    private final AnonymizationMappingService mappingService;

    private AnonymizationProcess(String dataSourceId,
                                 List<String> pseu,
                                 List<String> sup,
                                 String uniq,
                                 AnonymizationMappingService mappingService) {
        this.dataSourceId    = dataSourceId;
        this.pseudoFields    = pseu;
        this.supFields       = sup;
        this.uniqueField     = uniq;
        this.mappingService  = mappingService;
    }

    public static AnonymizationProcess fromDataSourceId(String dataSourceId) {
        AnonymizationRuleService svc = SpringContext.getBean(AnonymizationRuleService.class);
        AnonymizationRule rule = svc.getRuleByDataSourceId(dataSourceId);
        List<String> pseu = rule != null ? rule.getPseudonymization() : List.of();
        List<String> sup  = rule != null ? rule.getSuppression()      : List.of();
        String uniq      = rule != null ? rule.getUniqueField()       : null;

        var mappingSvc = SpringContext.getBean(AnonymizationMappingService.class);
        return new AnonymizationProcess(dataSourceId, pseu, sup, uniq, mappingSvc);
    }

    public JsonNode apply(JsonNode json) {
        if (pseudoFields.isEmpty() && supFields.isEmpty()) {
            return json;
        }

        // keep a raw copy
        JsonNode raw = json.deepCopy();
        ObjectNode node = (ObjectNode) json;

        // only proceed if uniqueField exists
        JsonNode uniqNode = getNodeByPath(raw, uniqueField);
        if (uniqueField != null && !uniqNode.isMissingNode()) {
            String mappingId = UUID.randomUUID().toString();

            // 1) pseudonymize each field
            for (String field : pseudoFields) {
                JsonNode origNode = getNodeByPath(raw, field);
                String originalValue = origNode.isMissingNode() ? "" : origNode.asText();
                String pseudoValue   = mappingService.pseudonym(dataSourceId, uniqueField, field, raw);
                setNodeByPath(node, field, pseudoValue);
            }

            // 2) suppress each field
            for (String field : supFields) {
                removeNodeByPath(node, field);
            }

            // 3) build wrapper
            ObjectNode wrapper = JsonNodeFactory.instance.objectNode();
            wrapper.put(AppConstants.MAPPING_Table_ID, mappingId);
            wrapper.set(AppConstants.Raw_Data, raw);
            wrapper.set(AppConstants.Anonymized_Data, node);
            mappingService.saveRawDataAnonymization(dataSourceId, wrapper);

            String mappingRef = AppConstants.ANONYMIZE_STATUS_TRUE + ";" + dataSourceId + ";" + mappingId;
            node.put(AppConstants.MAPPING_Table_REFERENCE, mappingRef);
        }
        return node;
    }

    /** Walks a dotted path via JsonNodeUtils. */
    private JsonNode getNodeByPath(JsonNode json, String path) {
        return JsonNodeUtils.getNodeByPath(json, path);
    }

    /** Sets a nested value, creating intermediate objects if needed. */
    private void setNodeByPath(ObjectNode root, String path, String value) {
        String[] parts = path.split("\\.");
        ObjectNode node = root;
        for (int i = 0; i < parts.length - 1; i++) {
            JsonNode child = node.get(parts[i]);
            if (!(child instanceof ObjectNode)) {
                child = JsonNodeFactory.instance.objectNode();
                node.set(parts[i], child);
            }
            node = (ObjectNode) child;
        }
        node.put(parts[parts.length - 1], value);
    }

    /** Removes a nested field if present. */
    private void removeNodeByPath(ObjectNode root, String path) {
        String[] parts = path.split("\\.");
        ObjectNode node = root;
        for (int i = 0; i < parts.length - 1; i++) {
            JsonNode child = node.path(parts[i]);
            if (!(child instanceof ObjectNode)) {
                return;
            }
            node = (ObjectNode) child;
        }
        node.remove(parts[parts.length - 1]);
    }
}
