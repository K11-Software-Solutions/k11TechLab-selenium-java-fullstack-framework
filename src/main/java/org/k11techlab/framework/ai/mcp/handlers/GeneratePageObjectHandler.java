package org.k11techlab.framework.ai.mcp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class GeneratePageObjectHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject req = new JSONObject(body);
        // If promptFile is provided, load JSON from file
        if (req.has("promptFile")) {
            String promptFile = req.getString("promptFile");
            try {
                java.nio.file.Path promptPath = java.nio.file.Paths.get("mcp-prompts", promptFile);
                String promptContent = new String(java.nio.file.Files.readAllBytes(promptPath), StandardCharsets.UTF_8);
                int start = promptContent.indexOf('{');
                int end = promptContent.indexOf('}', start);
                while (end != -1 && end + 1 < promptContent.length() && promptContent.charAt(end + 1) != '\n') {
                    end = promptContent.indexOf('}', end + 1);
                }
                if (start != -1 && end != -1) {
                    req = new JSONObject(promptContent.substring(start, end + 1));
                }
            } catch (Exception e) {
                exchange.sendResponseHeaders(500, -1);
                return;
            }
        }
        StringBuilder code = new StringBuilder();
        if (req.has("pageObjects")) {
            // Multiple page objects
            org.json.JSONArray arr = req.getJSONArray("pageObjects");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject po = arr.getJSONObject(i);
                code.append(generatePageObjectCode(po));
                code.append("\n\n");
            }
        } else if (req.has("className") && req.has("fields")) {
            code.append(generatePageObjectCode(req));
        } else {
            // Invalid input
            exchange.sendResponseHeaders(400, -1);
            return;
        }
        byte[] resp = code.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(200, resp.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp);
        }
    }

    private String generatePageObjectCode(JSONObject req) {
        String className = req.optString("className", "LoginPage");
        JSONObject fields = req.optJSONObject("fields");
        StringBuilder code = new StringBuilder();
        code.append("public class ").append(className).append(" {\n");
        code.append("    private final com.microsoft.playwright.Page page;\n\n");
        code.append("    public ").append(className).append("(com.microsoft.playwright.Page page) { this.page = page; }\n\n");
        for (String key : fields.keySet()) {
            JSONObject field = fields.getJSONObject(key);
            String method = field.optString("method", "fill");
            String id = field.optString("id", "");
            String name = field.optString("name", "");
            String text = field.optString("text", "");
            code.append("    public void ").append(key).append("(String value) {\n");
            code.append("        String selector = null;\n");
            if (!id.isEmpty()) code.append("        if (page.querySelector(\"#" + id + "\") != null) selector = \"#" + id + "\";\n");
            if (!name.isEmpty()) code.append("        else if (page.querySelector(\"input[name='" + name + "']\") != null) selector = \"input[name='" + name + "']\";\n");
            if (!text.isEmpty()) code.append("        else if (page.querySelector(\"text=" + text + "\") != null) selector = \"text=" + text + "\";\n");
            code.append("        if (selector != null) page.").append(method).append("(selector, value);\n");
            code.append("    }\n\n");
        }
        code.append("}\n");
        return code.toString();
    }
    }

