package com.devfay.core;

import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CodeGeneratorMain {
    public static void main(String[] args) {
        String jdlPath = "core/src/main/java/com/devfay/core/pathjdl/test.jdl";
        String outputDir = "spring-boot-base/src/main/java/com/devfay/springbootbase";
        String packageName = "com.devfay.springbootbase";
        String templateDir = "core/src/main/resources/templates"; // Directorio con todas las plantillas

        // Instancia el parser y procesa el archivo JDL
        JdlParser parser = new JdlParser();
        try {
            parser.parse(jdlPath);

            // Obtén las entidades y relaciones del parser
            Map<String, Map<String, String>> entities = parser.getEntities();
            Map<String, List<JdlParser.Relationship>> relationships = parser.getRelationships();

            // Instancia y ejecuta el generador de código con entidades y relaciones
            CodeGenerator generator = new CodeGenerator(templateDir);
            generator.generateCode(entities, relationships, outputDir, packageName, templateDir);

            System.out.println("Generación de código completada.");
        } catch (IOException e) {
            System.err.println("Error durante la generación de código: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
