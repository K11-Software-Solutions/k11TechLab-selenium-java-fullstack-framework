package org.k11techlab.framework.ai.mcp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.k11techlab.framework.ai.openai.OpenAIClient;
import org.k11techlab.framework.ai.mcp.workflow.playwright.PlaywrightTestGenerator;
import org.k11techlab.framework.ai.mcp.workflow.playwright.PlaywrightWorkflow;
import org.k11techlab.framework.ai.mcp.util.PlaywrightMCPClient;
import org.k11techlab.framework.ai.mcp.workflow.selenium.SeleniumTestGenerator;
import org.k11techlab.framework.ai.mcp.workflow.selenium.SeleniumWorkflow;
import org.k11techlab.framework.ai.util.HttpUtils;
import org.k11techlab.framework.ai.util.OpenAISeleniumTestGenerator;
import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;

import java.io.IOException;
import java.util.Map;

public class GenerateAndRunTestHandler implements HttpHandler {

    private final SeleniumTestGenerator seleniumGenerator;
    private final SeleniumWorkflow seleniumWorkflow;

    private final PlaywrightTestGenerator playwrightGenerator;
    private final PlaywrightWorkflow playwrightWorkflow;

    public GenerateAndRunTestHandler() {
    OpenAIClient openAI = new OpenAIClient();

    // Selenium wiring (use your existing framework generator class)
    OpenAISeleniumTestGenerator openAISeleniumGenerator = new OpenAISeleniumTestGenerator(openAI);
    this.seleniumGenerator = new SeleniumTestGenerator(openAISeleniumGenerator);
    this.seleniumWorkflow = new SeleniumWorkflow(openAISeleniumGenerator);

    // Playwright wiring
    this.playwrightGenerator = new PlaywrightTestGenerator(openAI);
    this.playwrightWorkflow = new PlaywrightWorkflow();
   }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String scenario = HttpUtils.readBody(exchange);
        Map<String, String> qp = HttpUtils.parseQuery(exchange.getRequestURI().getRawQuery());

        String configEngine = safeProp("engine");
        String configPackage = safeProp("ai.packageName");

        String engine = qp.getOrDefault("engine",
                (configEngine != null && !configEngine.isBlank()) ? configEngine.trim() : "playwright");

        String packageName = (configPackage != null && !configPackage.isBlank())
                ? configPackage.trim()
                : "org.k11techlab.framework_unittests.ai_generated.k11softwaresolutions";

        String classBase = "GeneratedTest_" + System.currentTimeMillis();

        String testCode;
        String result;

        if ("playwright".equalsIgnoreCase(engine)) {
            // Use Playwright MCP via stdio, add debug output
            int mcpPort = 8090;
            try {
                String portStr = ConfigurationManager.getBundle().getPropertyValue("mcp.port");
                if (portStr != null && !portStr.isBlank()) {
                    mcpPort = Integer.parseInt(portStr.trim());
                }
            } catch (Exception e) {
                // fallback to default
            }
            System.out.println("[DEBUG] Starting PlaywrightMCPClient subprocess...");
            try (PlaywrightMCPClient mcp = new PlaywrightMCPClient()) {
                int id = mcp.nextId();
                String initMsg = "{\n" +
                        "  \"jsonrpc\": \"2.0\",\n" +
                        "  \"id\": " + id + ",\n" +
                        "  \"method\": \"initialize\",\n" +
                        "  \"params\": {\n" +
                        "    \"protocolVersion\": \"2025-06-18\",\n" +
                        "    \"clientInfo\": { \"name\": \"java-playwright-framework\", \"version\": \"1.0.0\" },\n" +
                        "    \"capabilities\": { }\n" +
                        "  }\n" +
                        "}\n";
                System.out.println("[DEBUG] Sending MCP initialize message:");
                System.out.println(initMsg);
                mcp.sendJsonRpc(initMsg);
                System.out.println("[DEBUG] Waiting for MCP initialize response...");
                String initResp = mcp.readResponse();
                System.out.println("[DEBUG] MCP initialize response: " + initResp);

                int callId = mcp.nextId();
                String callMsg = "{\n" +
                        "  \"jsonrpc\": \"2.0\",\n" +
                        "  \"id\": " + callId + ",\n" +
                        "  \"method\": \"tools/call\",\n" +
                        "  \"params\": {\n" +
                        "    \"tool\": \"test.generateAndRun\",\n" +
                        "    \"input\": {\n" +
                        "      \"scenario\": " + escapeJsonForMCP(scenario) + ",\n" +
                        "      \"packageName\": \"" + packageName + "\",\n" +
                        "      \"className\": \"" + classBase + "\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}\n";
                System.out.println("[DEBUG] Sending MCP tools/call message:");
                System.out.println(callMsg);
                mcp.sendJsonRpc(callMsg);
                System.out.println("[DEBUG] Waiting for MCP tools/call response...");
                String mcpResponse = mcp.readResponse();
                System.out.println("[DEBUG] MCP tools/call response: " + mcpResponse);
                testCode = ""; // You can parse test code from mcpResponse if needed
                result = mcpResponse;
            } catch (Exception e) {
                System.out.println("[DEBUG] MCP error: " + e.getMessage());
                testCode = "";
                result = "ERROR: " + e.getMessage();
            }
        } else {
            testCode = seleniumGenerator.generateTest(scenario);
            try {
                result = seleniumWorkflow.generateAndRunTest(scenario, classBase, packageName);
            } catch (Exception e) {
                result = "ERROR: " + e.getMessage();
            }
        }

        String response = "{\"result\":\"" + HttpUtils.escapeJson(result) +
                "\",\"testCode\":\"" + HttpUtils.escapeJson(testCode) +
                "\",\"engine\":\"" + HttpUtils.escapeJson(engine) + "\"}";
        HttpUtils.writeJson(exchange, 200, response);
}

private static String safeProp(String key) {
    try {
        String v = ConfigurationManager.getBundle().getPropertyValue(key);
        return v == null ? "" : v;
    } catch (Exception e) {
        return "";
    }
}

    // Escapes scenario string for JSON embedding in MCP call
    private static String escapeJsonForMCP(String s) {
        if (s == null) return "\"\"";
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': out.append("\\\\"); break;
                case '"': out.append("\\\""); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
            }
        }
        out.append('"');
        return out.toString();
    }
}
