package com.devfay.core;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class JdlParser {
    private final Map<String, Map<String, String>> entities = new HashMap<>();
    private final Map<String, List<Relationship>> relationships = new HashMap<>();

    public void parse(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentEntity = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("entity")) {
                    // Parse entity definition
                    String[] parts = line.split(" ");
                    currentEntity = parts[1];
                    entities.put(currentEntity, new HashMap<>());

                } else if (line.startsWith("relationship")) {
                    // Parse relationship type and details
                    String relationshipType = line.split(" ")[1];
                    line = reader.readLine().trim();

                    String[] parts = line.replace("{", "").replace("}", "").split(" to ");
                    String fromEntity = parts[0].split("\\{")[0].trim();
                    String toEntity = parts[1].split("\\{")[0].trim();
                    String toEntityAttribute = parts[1].contains("{") ? parts[1].split("\\{")[1].replace("}", "").trim() : null;

                    addRelationship(fromEntity, toEntity, toEntityAttribute, relationshipType);

                } else if (currentEntity != null && (line.contains("String") || line.contains("Long") || line.contains("Instant"))) {
                    // Parse entity attributes
                    String[] parts = line.split(" ");
                    String attributeName = parts[0];
                    String attributeType = parts[1];
                    entities.get(currentEntity).put(attributeName, attributeType);

                } else if (line.isEmpty()) {
                    // Reset entity context on blank lines
                    currentEntity = null;
                }
            }
        }
    }

    private void addRelationship(String fromEntity, String toEntity, String toEntityAttribute, String relationshipType) {
        if (relationshipType == null || relationshipType.isEmpty()) {
            throw new IllegalArgumentException("El tipo de relación no puede ser nulo o vacío.");
        }
        Relationship relationship = new Relationship(fromEntity, toEntity, toEntityAttribute, relationshipType);
        relationships.computeIfAbsent(fromEntity, k -> new ArrayList<>()).add(relationship);
    }


    // Inner class to represent relationships
    public static class Relationship {
        public final String fromEntity;
        public final String toEntity;
        public final String toEntityAttribute;
        public final String relationshipType;

        public Relationship(String fromEntity, String toEntity, String toEntityAttribute, String relationshipType) {
            this.fromEntity = fromEntity;
            this.toEntity = toEntity;
            this.toEntityAttribute = toEntityAttribute;
            this.relationshipType = relationshipType;
        }
    }
}
