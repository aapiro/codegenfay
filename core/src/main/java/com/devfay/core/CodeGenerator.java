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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CodeGenerator {
    private final Configuration cfg;

    public CodeGenerator(String templateDir) throws IOException {
        cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setDirectoryForTemplateLoading(new File(templateDir));
    }

    public void generateCode(Map<String, JdlParser.EntityDefinition> entities, String outputDir, String packageName, String templateDir) {
        entities.forEach((entityName, entityDefinition) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("package", packageName);
            model.put("entity", entityDefinition);
            model.put("entityName", entityName);
            model.put("entityNameLowerCase", entityName.toLowerCase());
            model.put("attributes", entityDefinition.attributes);
            model.put("relationships", entityDefinition.relationships); // Aseguramos incluir las relaciones aquí

            processTemplates(model, outputDir, entityName, templateDir);
        });
    }

    private void processTemplates(Map<String, Object> model, String outputDir, String entityName, String templateDir) {
        try (Stream<Path> paths = Files.list(Paths.get(templateDir))) {
            paths.filter(path -> path.toString().endsWith(".ftl"))
                    .forEach(path -> processSingleTemplate(path, model, outputDir, entityName));
        } catch (IOException e) {
            e.printStackTrace();  // Puedes reemplazarlo con un logger
        }
    }

    private void processSingleTemplate(Path templatePath, Map<String, Object> model, String outputDir, String entityName) {
        try {
            Template template = cfg.getTemplate(templatePath.getFileName().toString());
            String outputFilePath = determineOutputPath(templatePath.getFileName().toString(), outputDir, entityName);
            try (FileWriter writer = new FileWriter(outputFilePath)) {
                template.process(model, writer);
                System.out.println("Archivo generado: " + outputFilePath);
            }
        } catch (IOException | TemplateException e) {
            e.printStackTrace();  // Puedes reemplazarlo con un logger
        }
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
