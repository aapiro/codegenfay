package com.devfay.core;

import java.io.IOException;
import java.util.Map;

public class CodeGeneratorMain {
    public static void main(String[] args) {
        String jdlFilePath = "core/src/main/java/com/devfay/core/pathjdl/test.jdl";
        String outputDir = "spring-boot-base/src/main/java/com/devfay/springbootbase";
        String packageName = "com.devfay.springbootbase";
        String templateDir = "core/src/main/resources/templates"; // Directorio con todas las plantillas

        // Instancia el parser y procesa el archivo JDL
        try {
            // 1. Parsear el archivo JDL
            JdlParser parser = new JdlParser();
            parser.parse(jdlFilePath);

            // Obtener las definiciones de las entidades, con sus atributos y relaciones
            Map<String, JdlParser.EntityDefinition> entities = parser.getEntities();

            // 2. Crear una instancia de CodeGenerator
            CodeGenerator generator = new CodeGenerator(parser,templateDir, packageName);

            // 3. Generar el código para cada entidad
            generator.generateCode(outputDir);

            System.out.println("Generación de código completada exitosamente.");

        } catch (IOException e) {
            System.err.println("Error de entrada/salida: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
