package org.k11techlab.framework.ai.mcp.workflow.playwright;

import java.io.File;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PageObjectCreator {
    public static void ensurePageObject(String outputDir, String className, String json, StringBuilder log) {
        String pageObjectFile = outputDir + "/" + className + ".java";
        File poFile = new File(pageObjectFile);
        if (!poFile.exists()) {
            log.append(className).append(" object not found, generating via MCP endpoint...\n");
            try {
                URL url = new URL("http://localhost:8090/mcp/generate-page-object");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
                String poCode;
                try (java.io.InputStream is = conn.getInputStream()) {
                    poCode = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
                try (FileWriter writer = new FileWriter(pageObjectFile)) {
                    writer.write(poCode);
                }
                log.append(className).append(" object generated and saved to: ").append(pageObjectFile).append("\n");
            } catch (Exception e) {
                log.append("Failed to generate ").append(className).append(" object: ").append(e.getMessage()).append("\n");
            }
        } else {
            log.append(className).append(" object already exists: ").append(pageObjectFile).append("\n");
        }
    }
}
