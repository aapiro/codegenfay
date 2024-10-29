package com.devfay.core;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class CodeGenerator {
    private static final Logger logger = Logger.getLogger(CodeGenerator.class.getName());

    public CodeGenerator() {}

    public void generateCode(Map<String, JdlParser.EntityDefinition> entities, String outputDir, String packageName, String templateDir) {
        entities.forEach((entityName, entity) -> {
            Map<String, String> model = new HashMap<>();
            model.put("package", packageName);
            model.put("entityName", entityName);
            model.put("entityNameLowerCase", entityName.toLowerCase());
            model.put("attributes", generateAttributesSection(entity.attributes));
            model.put("relationships", generateRelationshipsSection(entity));

            processTemplates(model, outputDir, entityName, templateDir);
        });
    }

    private void processTemplates(Map<String, String> model, String outputDir, String entityName, String templateDir) {
        try (Stream<Path> paths = Files.list(Paths.get(templateDir))) {
            paths.filter(path -> path.toString().endsWith(".jft"))
                    .forEach(path -> processSingleTemplate(path, model, outputDir, entityName));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al procesar las plantillas en el directorio: " + templateDir, e);
        }
    }

    private void processSingleTemplate(Path templatePath, Map<String, String> model, String outputDir, String entityName) {
        try {
            String templateContent = new String(Files.readAllBytes(templatePath));
            String filledContent = fillTemplate(templateContent, model);
            String outputFilePath = determineOutputPath(templatePath.getFileName().toString(), outputDir, entityName);

            try (FileWriter writer = new FileWriter(outputFilePath)) {
                writer.write(filledContent);
                logger.info("Archivo generado: " + outputFilePath);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al procesar la plantilla: " + templatePath.getFileName(), e);
        }
    }

    private String fillTemplate(String templateContent, Map<String, String> model) {
        for (Map.Entry<String, String> entry : model.entrySet()) {
            templateContent = templateContent.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return templateContent;
    }

    private String generateAttributesSection(Map<String, String> attributes) {
        StringBuilder attributesSection = new StringBuilder();
        attributes.forEach((name, type) ->
                attributesSection.append("    private ").append(type).append(" ").append(name).append(";\n"));
        return attributesSection.toString();
    }

    private String generateRelationshipsSection(JdlParser.EntityDefinition entity) {
        StringBuilder relationshipsSection = new StringBuilder();
        Set<String> uniqueRelationshipNames = new HashSet<>(); // Almacena los nombres de relaciones únicos

        for (JdlParser.EntityRelationship rel : entity.relationships) {
            // Usa 'fromAttribute' como nombre si está definido; si no, usa 'toEntity' en minúsculas
            String fromAttributeName = !rel.fromAttribute.isEmpty() ? rel.fromAttribute : rel.toEntity.toLowerCase();

            // Verifica si el nombre de relación ya fue añadido
            if (uniqueRelationshipNames.contains(fromAttributeName)) {
                continue; // Salta esta relación si el nombre ya existe
            }
            uniqueRelationshipNames.add(fromAttributeName); // Agrega el nombre al conjunto

            // Obtiene la plantilla de la relación con etiquetas para reemplazar
            String relationshipCode = getRelationshipTemplate(rel.relationshipType, entity.name.toLowerCase(), fromAttributeName, rel.toEntity);
            relationshipsSection.append(relationshipCode);
        }

        return relationshipsSection.toString();
    }

    private String getRelationshipTemplate(String relationshipType, String entityName, String fromAttributeName, String toEntity) {
        // Definimos las plantillas de relaciones como cadenas de texto con etiquetas
        String oneToOneTemplate = """
        @OneToOne
        @JoinColumn(name = "${fromAttributeName}_id")
        private ${toEntity} ${fromAttributeName};

    """;

        String oneToManyTemplate = """
        @OneToMany(mappedBy = "${entityName}")
        private List<${toEntity}> ${fromAttributeName}s;

    """;

        String manyToOneTemplate = """
        @ManyToOne
        @JoinColumn(name = "${fromAttributeName}_id")
        private ${toEntity} ${fromAttributeName};

    """;

        String manyToManyTemplate = """
        @ManyToMany
        @JoinTable(
            name = "${entityName}_${toEntity}",
            joinColumns = @JoinColumn(name = "${entityName}_id"),
            inverseJoinColumns = @JoinColumn(name = "${fromAttributeName}_id")
        )
        private Set<${toEntity}> ${fromAttributeName}s;

    """;

        // Selecciona la plantilla adecuada según el tipo de relación
        String template;
        switch (relationshipType) {
            case "OneToOne":
                template = oneToOneTemplate;
                break;
            case "OneToMany":
                template = oneToManyTemplate;
                break;
            case "ManyToOne":
                template = manyToOneTemplate;
                break;
            case "ManyToMany":
                template = manyToManyTemplate;
                break;
            default:
                template = "";
                break;
        }

        // Llamamos a replacePlaceholders para reemplazar las etiquetas en la plantilla seleccionada
        return replacePlaceholders(template, Map.of(
                "fromAttributeName", fromAttributeName,
                "entityName", entityName,
                "toEntity", toEntity
        ));
    }

    // Método para reemplazar las etiquetas en una plantilla
    private String replacePlaceholders(String template, Map<String, String> values) {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
    private String determineOutputPath(String templateName, String outputDir, String entityName) throws IOException {
        String subDir = templateName.contains("Repository") ? "/repository/" :
                templateName.contains("Service") ? "/service/" :
                        templateName.contains("Controller") ? "/web/rest/" :
                                "/domain/";
        String outputFileName = templateName.contains("Controller") ? entityName + "Controller.java" :
                templateName.contains("Repository") ? entityName + "Repository.java" :
                        templateName.contains("Service") ? entityName + "Service.java" :
                                entityName + ".java";

        String fullOutputPath = outputDir + subDir + outputFileName;
        Files.createDirectories(Paths.get(outputDir + subDir));
        return fullOutputPath;
    }
}
