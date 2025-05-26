package com.example.orgA.preProcessingElements;

import com.example.orgA.config.SpringContext;
import com.example.orgA.entity.AttributeSetting;
import com.example.orgA.service.StreamConfigurationService;
import com.example.orgA.utils.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Processes incoming JSON payloads into Event messages,
 * dynamically extracting both flat (primitive) and nested fields.
 */
public class AttributeSettingProcess {

    private final String caseIdField;
    private final String activityField;
    private final String timestampField;

    public AttributeSettingProcess(String caseIdField,
                                   String activityField,
                                   String timestampField) {
        this.caseIdField   = caseIdField;
        this.activityField = activityField;
        this.timestampField= timestampField;
    }

    /**
     * Fetch the three core JSON‐paths from the DB and build a processor.
     */
    public static AttributeSettingProcess fromSettingId(Long id) {
        StreamConfigurationService svc =
                SpringContext.getBean(StreamConfigurationService.class);
        AttributeSetting setting = svc.getAttributeSettingById(id);
        if (setting == null) {
            throw new IllegalStateException("No AttributeSetting for id " + id);
        }
        return new AttributeSettingProcess(
                setting.getCaseId(),
                setting.getActivity(),
                setting.getTimeStamp()
        );
    }

    /**
     * Turn the given JSON into an Event:
     *  1) extract caseId/activity/timestamp null-safely
     *  2) deep-copy & remove those fields (even nested)
     *  3) recurse the remainder into Attributes
     */
    public Event extractEvent(JsonNode json) {
        // 1) core fields
        String caseId   = JsonNodeUtils.getTextByPath(json, caseIdField,   "unknown_case");
        String activity = JsonNodeUtils.getTextByPath(json, activityField, "unknown_activity");
        String timestamp= JsonNodeUtils.getTextByPath(json, timestampField, "unknown_timestamp");

        // 2) strip core fields from a fresh copy
        if (!(json instanceof ObjectNode)) {
            throw new IllegalArgumentException("Expected JSON root to be an object");
        }
        ObjectNode copy = ((ObjectNode) json).deepCopy();
        removeByPath(copy, caseIdField);
        removeByPath(copy, activityField);
        removeByPath(copy, timestampField);

        // 3) convert everything left into Attributes
        Set<Attribute<?>> attrs = buildAttributes(copy);

        return new Event(caseId, activity, timestamp, attrs);
    }

    /**
     * Remove a field at an arbitrary dot-path from the given ObjectNode.
     */
    private void removeByPath(ObjectNode root, String path) {
        String[] parts = path.split("\\.");
        ObjectNode parent = root;
        for (int i = 0; i < parts.length - 1; i++) {
            JsonNode child = parent.get(parts[i]);
            if (!(child instanceof ObjectNode)) {
                return; // nothing to remove
            }
            parent = (ObjectNode) child;
        }
        parent.remove(parts[parts.length - 1]);
    }

    /**
     * Recursively walk a JSON node and collect Attributes.
     */
    private Set<Attribute<?>> buildAttributes(JsonNode node) {
        Set<Attribute<?>> out = new LinkedHashSet<>();
        node.fieldNames().forEachRemaining(name -> {
            JsonNode child = node.get(name);

            if (child.isObject()) {
                // nested → recurse
                Set<Attribute<?>> nested = buildAttributes(child);
                Map<String,Attribute<?>> map = nested.stream()
                        .collect(Collectors.toMap(Attribute::getName, Function.identity()));
                out.add(new Attribute<>(name, null, map));

            } else if (child.isArray()) {
                // array → either list of primitives or index‐keyed nested
                List<Object> list = new ArrayList<>();
                Map<String,Attribute<?>> map = new LinkedHashMap<>();

                for (int i = 0; i < child.size(); i++) {
                    JsonNode el = child.get(i);
                    if (el.isObject()) {
                        Set<Attribute<?>> elNested = buildAttributes(el);
                        Map<String,Attribute<?>> em = elNested.stream()
                                .collect(Collectors.toMap(Attribute::getName, Function.identity()));
                        map.put(String.valueOf(i), new Attribute<>(String.valueOf(i), null, em));
                    } else {
                        list.add(extractPrimitive(el));
                    }
                }
                if (!map.isEmpty()) {
                    out.add(new Attribute<>(name, null, map));
                } else {
                    out.add(new Attribute<>(name, list));
                }

            } else {
                // primitive → linear
                out.add(new Attribute<>(name, extractPrimitive(child)));
            }
        });
        return out;
    }

    /**
     * Convert a JSON primitive into a Java object.
     */
    private Object extractPrimitive(JsonNode n) {
        if (n.isNumber())  return n.numberValue();
        if (n.isBoolean()) return n.booleanValue();
        return n.asText();
    }
}
