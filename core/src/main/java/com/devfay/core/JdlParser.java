package com.devfay.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdlParser {
    private final Map<String, EntityDefinition> entities = new HashMap<>();
    private final Map<String, EnumDefinition> enums = new HashMap<>(); // Mapa para almacenar enums

    public void parse(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentEntity = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("entity")) {
                    currentEntity = parseEntity(line);
                } else if (currentEntity != null && (line.contains("String") || line.contains("Long") || line.contains("Instant") || line.contains("Language"))) {
                    parseAttribute(currentEntity, line);
                } else if (line.startsWith("relationship")) {
                    parseRelationship(line, reader);
                } else if (line.startsWith("enum")) {
                    parseEnum(line, reader);
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
        String relationshipType = line.split(" ")[1];
        String relationshipLine;

        while ((relationshipLine = reader.readLine()) != null) {
            relationshipLine = relationshipLine.trim();

            if (relationshipLine.isEmpty() || relationshipLine.equals("{") || relationshipLine.equals("}") || relationshipLine.startsWith("//") || relationshipLine.startsWith("/**")) {
                continue;
            }

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

            if (relationshipLine.endsWith("}")) {
                break;
            }
        }
    }

    private void parseEnum(String line, BufferedReader reader) throws IOException {
        String enumName = line.split(" ")[1]; // Obtiene el nombre del enum
        EnumDefinition enumDefinition = new EnumDefinition(enumName);

        String enumLine;
        while ((enumLine = reader.readLine()) != null) {
            enumLine = enumLine.trim();

            // Detener la lectura cuando se encuentra "}"
            if (enumLine.equals("}")) {
                break;
            }

            // Divide la línea por comas y agrega cada valor individualmente
            if (!enumLine.isEmpty()) {
                String[] values = enumLine.replace(",", "").split("\\s+");
                for (String value : values) {
                    if (!value.isEmpty()) {
                        enumDefinition.values.add(value.trim());
                    }
                }
            }
        }

        enums.put(enumName, enumDefinition); // Agrega el enum al mapa de enums
    }
    public Map<String, EntityDefinition> getEntities() {
        return entities;
    }

    public Map<String, EnumDefinition> getEnums() {
        return enums;
    }

    // Clase para definir las entidades
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

    // Clase para definir las relaciones entre entidades
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

    // Clase para definir un Enum
    public static class EnumDefinition {
        public final String name;
        public final List<String> values = new ArrayList<>();

        public EnumDefinition(String name) {
            this.name = name;
        }
    }
}
