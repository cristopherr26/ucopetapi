package com.uco.ucopetapi.service.product;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Component
public class CatalogMergePatch {

    public static final String MEDIA_TYPE = "application/merge-patch+json";

    public static final Set<String> PRODUCT_FIELDS = Set.of(
            "name", "description", "imageUrl", "price", "taxCategory", "sellable", "category");

    public static final Set<String> SERVICE_FIELDS = Set.of(
            "name", "description", "imageUrl", "price", "taxCategory", "sellable", "category",
            "purchasable", "headquarterIds");

    private final ObjectMapper objectMapper;

    public CatalogMergePatch(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T apply(T current, JsonNode patch, Set<String> editable, Class<T> type) {
        if (patch == null || !patch.isObject()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El cuerpo del parche tiene que ser un objeto JSON");
        }
        rejectReadOnly(patch, editable);
        try {
            return objectMapper.treeToValue(merge(objectMapper.valueToTree(current), patch), type);
        } catch (JacksonException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El parche tiene un valor no permitido en '" + ex.getPathReference() + "'");
        }
    }

    private static void rejectReadOnly(JsonNode patch, Set<String> editable) {
        Set<String> rejected = new TreeSet<>(patch.propertyNames());
        rejected.removeAll(editable);
        if (!rejected.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Por PATCH no se pueden cambiar estos campos: " + String.join(", ", rejected)
                            + ". Se cambian con: " + String.join(", ", new TreeSet<>(editable)));
        }
    }

    private JsonNode merge(JsonNode target, JsonNode patch) {
        if (!patch.isObject()) {
            return patch;
        }
        ObjectNode merged = target != null && target.isObject()
                ? (ObjectNode) target.deepCopy()
                : objectMapper.createObjectNode();
        for (Map.Entry<String, JsonNode> change : patch.properties()) {
            if (change.getValue().isNull()) {
                merged.remove(change.getKey());
            } else {
                merged.set(change.getKey(), merge(merged.get(change.getKey()), change.getValue()));
            }
        }
        return merged;
    }
}
