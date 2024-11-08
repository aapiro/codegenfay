package com.devfay.core;

import java.io.IOException;

public class CodeGeneratorMain {
    public static void main(String[] args) {
        String jdlFilePath = "core/src/main/java/com/devfay/core/pathjdl/test.jdl";
        String outputDir = "spring-boot-base/src/main/java/com/devfay/springbootbase";
        String packageName = "com.devfay.springbootbase";
        String templateDir = "core/src/main/resources/templates";

        try {
            JdlParser parser = new JdlParser();
            parser.parse(jdlFilePath);

            CodeGenerator generator = new CodeGenerator(parser, templateDir, packageName);

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
