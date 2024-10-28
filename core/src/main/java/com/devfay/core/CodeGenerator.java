package com.devfay.core;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CodeGenerator {
    private final Configuration cfg;

    public CodeGenerator(String templateDir) throws IOException {
        cfg = new Configuration(Configuration.VERSION_2_3_31);

        File templateDirectory = new File(templateDir);
        if (!templateDirectory.exists() || !templateDirectory.isDirectory()) {
            throw new IOException("El directorio de plantillas no existe o no es un directorio válido: " + templateDir);
        }

        cfg.setDirectoryForTemplateLoading(templateDirectory);
    }

    public void generateCode(Map<String, Map<String, String>> entities, Map<String, List<JdlParser.Relationship>> relationships, String outputDir, String packageName, String templateDir) {
        entities.forEach((entityName, attributes) -> {
            var model = Map.of(
                    "package", packageName,
                    "entityName", entityName,
                    "entityNameLowerCase", entityName.toLowerCase(),
                    "attributes", attributes,
                    "relationships", relationships.getOrDefault(entityName, List.of())
            );
            processTemplates(model, outputDir, entityName, templateDir);
        });
    }

    private void processTemplates(Map<String, Object> model, String outputDir, String entityName, String templateDir) {
        try (Stream<Path> paths = Files.list(Paths.get(templateDir))) {
            paths.filter(path -> path.toString().endsWith(".ftl"))
                    .forEach(path -> processSingleTemplate(path, model, outputDir, entityName));
        } catch (IOException e) {
            e.printStackTrace();  // Idealmente, reemplazar con un logger
        }
    }

    private void processSingleTemplate(Path templatePath, Map<String, Object> model, String outputDir, String entityName) {
        try {
            Template template = cfg.getTemplate(templatePath.getFileName().toString());
            var outputFilePath = determineOutputPath(templatePath.getFileName().toString(), outputDir, entityName);
            try (FileWriter writer = new FileWriter(outputFilePath)) {
                template.process(model, writer);
                System.out.println("Archivo generado: " + outputFilePath);
            }
        } catch (IOException | TemplateException e) {
            e.printStackTrace();  // Reemplazar con un logger
        }
    }

    private String determineOutputPath(String templateName, String outputDir, String entityName) throws IOException {
        var subDirMap = Map.of(
                "Repository", "/repository/",
                "Service", "/service/",
                "Controller", "/web/rest/"
        );

        var subDir = subDirMap.entrySet().stream()
                .filter(entry -> templateName.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("/domain/");

        var outputFileName = templateName.contains("Controller") ? entityName + "Controller.java" :
                templateName.contains("Repository") ? entityName + "Repository.java" :
                        templateName.contains("Service") ? entityName + "Service.java" :
                                entityName + ".java";

        var fullOutputPath = outputDir + subDir + outputFileName;
        Files.createDirectories(Paths.get(outputDir + subDir));
        return fullOutputPath;
    }
}
