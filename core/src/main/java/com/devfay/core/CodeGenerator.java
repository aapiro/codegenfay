package com.devfay.core;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class CodeGenerator {
    private final JdlParser parser;
    private final String templateDir; // Ruta al directorio de plantillas
    private final String packageName;

    public CodeGenerator(JdlParser parser, String templateDir, String packageName) {
        this.parser = parser;
        this.templateDir = templateDir;
        this.packageName = packageName;
    }

    public void generateCode(String outputDir) throws IOException {
        // Genera entidades, repositorios, servicios y controladores
        generateEntities(outputDir);
        // Genera enums además de los otros archivos
        generateEnums(outputDir);
    }

    private void generateEntities(String outputDir) throws IOException {
        for (JdlParser.EntityDefinition entityDef : parser.getEntities().values()) {
            Map<String, String> model = Map.of(
                    "package", packageName,  // Ajusta según tu estructura de paquetes
                    "entityName", entityDef.name,
                    "entityNameLowerCase", entityDef.name.toLowerCase(),
                    "attributes", generateAttributes(entityDef),
                    "relationships", generateRelationships(entityDef)
            );

            // Procesa plantillas de entidad, repositorio, servicio y controlador
            processTemplate("EntityTemplate.jft", model, outputDir + "/domain/" + entityDef.name + ".java");
            processTemplate("RepositoryTemplate.jft", model, outputDir + "/repository/" + entityDef.name + "Repository.java");
            processTemplate("ServiceTemplate.jft", model, outputDir + "/service/" + entityDef.name + "Service.java");
            processTemplate("ControllerTemplate.jft", model, outputDir + "/web/rest/" + entityDef.name + "Controller.java");
        }
    }

    private void generateEnums(String outputDir) throws IOException {
        for (JdlParser.EnumDefinition enumDef : parser.getEnums().values()) {
            Map<String, String> model = Map.of(
                    "package", packageName,
                    "enumName", enumDef.name,
                    "enumValues", String.join(", ", enumDef.values)
            );

            // Procesa la plantilla de enum
            processTemplate("EnumTemplate.jft", model, outputDir + "/domain/" + enumDef.name + ".java");
        }
    }

    private void processTemplate(String templateName, Map<String, String> model, String outputPath) throws IOException {
        Path templatePath = Paths.get(templateDir, templateName);
        String templateContent = new String(Files.readAllBytes(templatePath));

        // Realiza el reemplazo de placeholders en la plantilla
        for (Map.Entry<String, String> entry : model.entrySet()) {
            templateContent = templateContent.replace("${" + entry.getKey() + "}", entry.getValue());
        }

        // Escribe el archivo de salida
        Files.createDirectories(Paths.get(outputPath).getParent());
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(templateContent);
            System.out.println("Archivo generado: " + outputPath);
        }
    }

    private String generateAttributes(JdlParser.EntityDefinition entityDef) {
        StringBuilder attributesSection = new StringBuilder();
        entityDef.attributes.forEach((name, type) ->
                attributesSection.append("    private ").append(type).append(" ").append(name).append(";\n"));
        return attributesSection.toString();
    }

    private String generateRelationships(JdlParser.EntityDefinition entityDef) {
        StringBuilder relationshipsSection = new StringBuilder();
        for (JdlParser.EntityRelationship rel : entityDef.relationships) {
            String fromAttributeName = !rel.fromAttribute.isEmpty() ? rel.fromAttribute : rel.toEntity.toLowerCase();
            relationshipsSection.append(getRelationshipTemplate(rel.relationshipType, entityDef.name.toLowerCase(), fromAttributeName, rel.toEntity));
        }
        return relationshipsSection.toString();
    }

    private String getRelationshipTemplate(String relationshipType, String entityName, String fromAttributeName, String toEntity) {
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

        // Selecciona y reemplaza etiquetas en la plantilla de relación
        String template = switch (relationshipType) {
            case "OneToOne" -> oneToOneTemplate;
            case "OneToMany" -> oneToManyTemplate;
            case "ManyToOne" -> manyToOneTemplate;
            case "ManyToMany" -> manyToManyTemplate;
            default -> "";
        };

        return template.replace("${fromAttributeName}", fromAttributeName)
                .replace("${entityName}", entityName)
                .replace("${toEntity}", toEntity);
    }
}
