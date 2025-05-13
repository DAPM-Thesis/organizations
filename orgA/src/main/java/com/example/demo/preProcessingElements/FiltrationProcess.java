package com.dapm2.ingestion_service.preProcessingElements;

import com.dapm2.ingestion_service.config.SpringContext;
import com.dapm2.ingestion_service.entity.FilterConfig;
import com.dapm2.ingestion_service.service.StreamConfigurationService;
import com.dapm2.ingestion_service.utils.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Apply simple equals‐based filters; now supports nested JSON paths
 * and does all lookups null‐safely.
 */
public class FiltrationProcess {

    private final Map<String, Object> filters;

    private FiltrationProcess(Map<String, Object> filters) {
        this.filters = filters;
    }

    public static FiltrationProcess fromFilterId(Long id) {
        StreamConfigurationService service =
                SpringContext.getBean(StreamConfigurationService.class);
        ObjectMapper mapper = SpringContext.getBean(ObjectMapper.class);

        FilterConfig config = service.getFilterById(id);
        if (config == null || config.getFilters() == null) {
            throw new RuntimeException("No FilterConfig found with id: " + id);
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed =
                    mapper.readValue(config.getFilters(), Map.class);
            return new FiltrationProcess(parsed);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse filters", e);
        }
    }

    /**
     * Returns true only if every key in `filters` matches the JSON at that path.
     * Uses null‐safe lookups and avoids any direct .get(...).textValue() calls.
     */
    public boolean shouldPass(JsonNode eventJson) {
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String path          = entry.getKey();
            Object expectedValue = entry.getValue();

            // 1) null‐safe fetch
            JsonNode actualNode = JsonNodeUtils.getNodeByPath(eventJson, path);
            if (actualNode.isMissingNode() || actualNode.isNull()) {
                return false;
            }

            // 2) boolean filter
            if (expectedValue instanceof Boolean) {
                boolean actualBool = actualNode.asBoolean(false);
                if (actualBool != (Boolean) expectedValue) {
                    return false;
                }
                continue;
            }

            // 3) string or other primitive – compare text
            String actualText = actualNode.asText(null);
            if (actualText == null
                    || !actualText.equals(expectedValue.toString())) {
                return false;
            }
        }
        return true;
    }
}
