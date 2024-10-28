package com.devfay.core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EntityGenerator {
    private final Configuration cfg;

    public EntityGenerator() {
        cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassForTemplateLoading(getClass(), "/templates");
    }

    public void generateEntities(Map<String, Map<String, String>> entities, String outputDir, String packageName) throws IOException, TemplateException {
        Template template = cfg.getTemplate("EntityTemplate.ftl");

        for (Map.Entry<String, Map<String, String>> entry : entities.entrySet()) {
            String entityName = entry.getKey();
            Map<String, Object> model = new HashMap<>();
            model.put("package", packageName);
            model.put("entityName", entityName);
            model.put("attributes", entry.getValue());

            File dir = new File(outputDir + "/domain");
            dir.mkdirs();

            try (FileWriter writer = new FileWriter(new File(dir, entityName + ".java"))) {
                template.process(model, writer);
            }
        }
    }
}
