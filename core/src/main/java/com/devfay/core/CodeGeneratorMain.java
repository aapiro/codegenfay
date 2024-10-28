package com.devfay.core;

import freemarker.template.TemplateException;
import java.io.IOException;

public class CodeGeneratorMain {
    public static void main(String[] args) throws IOException {
        String jdlPath = "core/src/main/java/com/devfay/core/pathjdl/test.jdl";
        String outputDir = "spring-boot-base/src/main/java/com/devfay/springbootbase";
        String packageName = "com.devfay.springbootbase";
        String templateDir = "core/src/main/resources/templates"; // Directorio con todas las plantillas

        // 1. Parsear el archivo JDL
        JdlParser parser = new JdlParser();
        parser.parse(jdlPath);

        // 2. Generar código usando todas las plantillas en el directorio
        CodeGenerator codeGenerator = new CodeGenerator(templateDir);
        codeGenerator.generateCode(parser.getEntities(), outputDir, packageName,templateDir);
    }
}
