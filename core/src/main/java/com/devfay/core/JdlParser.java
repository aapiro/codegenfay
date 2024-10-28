package com.devfay.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JdlParser {
    private final Map<String, Map<String, String>> entities = new HashMap<>();

    public void parse(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentEntity = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("entity")) {
                    String[] parts = line.split(" ");
                    currentEntity = parts[1];
                    entities.put(currentEntity, new HashMap<>());
                } else if (currentEntity != null && line.contains("String") || line.contains("Long") || line.contains("Instant")) {
                    String[] parts = line.split(" ");
                    String attributeName = parts[0];
                    String attributeType = parts[1];
                    entities.get(currentEntity).put(attributeName, attributeType);
                } else if (line.isEmpty()) {
                    currentEntity = null; // reset entity when a blank line is encountered
                }
            }
        }
    }

    public Map<String, Map<String, String>> getEntities() {
        return entities;
    }
}
