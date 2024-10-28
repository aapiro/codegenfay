package com.devfay.core;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class JdlParser {

    private final Map<String, EntityDefinition> entities = new HashMap<>();

    public void parse(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentEntity = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("entity")) {
                    String[] parts = line.split(" ");
                    currentEntity = parts[1];
                    entities.put(currentEntity, new EntityDefinition(currentEntity, new HashMap<>()));
                }
                else if (currentEntity != null && (line.contains("String") || line.contains("Long") || line.contains("Instant"))) {
                    String[] parts = line.split(" ");
                    String attributeName = parts[0];
                    String attributeType = parts[1];
                    entities.get(currentEntity).attributes.put(attributeName, attributeType);
                }
                else if (line.startsWith("relationship")) {
                    parseRelationship(line, reader);
                }
                else if (line.isEmpty()) {
                    currentEntity = null;
                }
            }
        }
    }

    private void parseRelationship(String line, BufferedReader reader) throws IOException {
        Pattern pattern = Pattern.compile("relationship\\s+(OneToOne|OneToMany|ManyToOne|ManyToMany)\\s*\\{");
        Matcher matcher = pattern.matcher(line);

        if (!matcher.find()) {
            System.out.println("Formato de relación no válido: " + line);
            return;
        }

        String relationshipType = matcher.group(1);
        String relationshipLine;

        while ((relationshipLine = reader.readLine()) != null) {
            relationshipLine = relationshipLine.trim();
            if (relationshipLine.equals("}")) break;

            Pattern relPattern = Pattern.compile("([\\w]+)\\{?(\\w+)?}?\\s+to\\s+([\\w]+)\\{?(\\w+)?}?");
            Matcher relMatcher = relPattern.matcher(relationshipLine);

            if (relMatcher.find()) {
                String fromEntity = relMatcher.group(1);
                String fromAttribute = relMatcher.group(2) != null ? relMatcher.group(2) : "";
                String toEntity = relMatcher.group(3);
                String toAttribute = relMatcher.group(4) != null ? relMatcher.group(4) : "";

                EntityRelationship relationship = new EntityRelationship(fromEntity, toEntity, toAttribute, relationshipType);

                entities.get(fromEntity).addRelationship(relationship);
            } else {
                System.out.println("Formato de relación no válido: " + relationshipLine);
            }
        }
    }

    public static class EntityDefinition {
        public final String name;
        public final Map<String, String> attributes;
        public final List<EntityRelationship> relationships = new ArrayList<>();

        public EntityDefinition(String name, Map<String, String> attributes) {
            this.name = name;
            this.attributes = attributes;
        }

        public void addRelationship(EntityRelationship relationship) {
            relationships.add(relationship);
        }
    }

    public static class EntityRelationship {
        public final String fromEntity;
        public final String toEntity;
        public final String toAttribute;
        public final String relationshipType;

        public EntityRelationship(String fromEntity, String toEntity, String toAttribute, String relationshipType) {
            this.fromEntity = fromEntity;
            this.toEntity = toEntity;
            this.toAttribute = toAttribute;
            this.relationshipType = relationshipType;
        }
    }
}
