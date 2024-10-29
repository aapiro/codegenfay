package com.devfay.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdlParser {
    private final Map<String, EntityDefinition> entities = new HashMap<>();

    public void parse(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentEntity = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("entity")) {
                    currentEntity = parseEntity(line);
                } else if (currentEntity != null && (line.contains("String") || line.contains("Long") || line.contains("Instant"))) {
                    parseAttribute(currentEntity, line);
                } else if (line.startsWith("relationship")) {
                    parseRelationship(line, reader);
                } else if (line.isEmpty() || line.startsWith("paginate") || line.startsWith("dto") || line.startsWith("service")) {
                    currentEntity = null; // Reset cuando alcanzamos un bloque no relevante
                }
            }
        }
    }

    private String parseEntity(String line) {
        String[] parts = line.split(" ");
        String entityName = parts[1];
        entities.put(entityName, new EntityDefinition(entityName));
        return entityName;
    }

    private void parseAttribute(String entityName, String line) {
        String[] parts = line.split(" ");
        String attributeName = parts[0];
        String attributeType = parts[1];
        entities.get(entityName).attributes.put(attributeName, attributeType);
    }

    private void parseRelationship(String line, BufferedReader reader) throws IOException {
        String relationshipType = line.split(" ")[1]; // Obtiene el tipo de relación
        String relationshipLine;

        while ((relationshipLine = reader.readLine()) != null) {
            relationshipLine = relationshipLine.trim();

            // Ignorar líneas vacías, solo `{` o `}`
            if (relationshipLine.isEmpty() || relationshipLine.equals("{") || relationshipLine.equals("}") || relationshipLine.startsWith("//") || relationshipLine.startsWith("/**")) {
                continue;
            }

            // Patrón para capturar relaciones con atributos opcionales en `{}` y `()`
            Pattern pattern = Pattern.compile("(\\w+)\\{?(\\w+)?(?:\\(([^\\)]+)\\))?\\}? to (\\w+)\\{?(\\w+)?(?:\\(([^\\)]+)\\))?\\}?");
            Matcher matcher = pattern.matcher(relationshipLine);

            if (matcher.matches()) {
                String fromEntity = matcher.group(1);
                String fromAttribute = matcher.group(2) != null ? matcher.group(2) : "";
                String fromAttributeKey = matcher.group(3) != null ? matcher.group(3) : "";
                String toEntity = matcher.group(4);
                String toAttribute = matcher.group(5) != null ? matcher.group(5) : "";
                String toAttributeKey = matcher.group(6) != null ? matcher.group(6) : "";

                EntityRelationship relationship = new EntityRelationship(fromEntity, toEntity, relationshipType, fromAttribute, toAttribute, fromAttributeKey, toAttributeKey);
                entities.get(fromEntity).addRelationship(relationship);
                entities.get(toEntity).addRelationship(relationship); // Opcional para bidireccionalidad
            } else {
                System.out.println("Formato de relación no válido: " + relationshipLine);
            }

            // Detener si encontramos la llave de cierre `}`
            if (relationshipLine.endsWith("}")) {
                break;
            }
        }
    }

    public Map<String, EntityDefinition> getEntities() {
        return entities;
    }

    public static class EntityDefinition {
        public final String name;
        public final Map<String, String> attributes = new HashMap<>();
        public final List<EntityRelationship> relationships = new ArrayList<>();

        public EntityDefinition(String name) {
            this.name = name;
        }

        public void addRelationship(EntityRelationship relationship) {
            relationships.add(relationship);
        }
    }

    public static class EntityRelationship {
        public final String fromEntity;
        public final String toEntity;
        public final String relationshipType;
        public final String fromAttribute;
        public final String toAttribute;
        public final String fromAttributeKey;
        public final String toAttributeKey;

        public EntityRelationship(String fromEntity, String toEntity, String relationshipType, String fromAttribute, String toAttribute, String fromAttributeKey, String toAttributeKey) {
            this.fromEntity = fromEntity;
            this.toEntity = toEntity;
            this.relationshipType = relationshipType;
            this.fromAttribute = fromAttribute;
            this.toAttribute = toAttribute;
            this.fromAttributeKey = fromAttributeKey;
            this.toAttributeKey = toAttributeKey;
        }
    }
}
