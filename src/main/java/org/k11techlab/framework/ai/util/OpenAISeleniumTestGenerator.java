package org.k11techlab.framework.ai.util;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Scanner;

import org.k11techlab.framework.ai.openai.OpenAIClient;
import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;

public class OpenAISeleniumTestGenerator {

    private final OpenAIClient openAI;
    private final int maxRetries = 3;

    public OpenAISeleniumTestGenerator(OpenAIClient openAI) {
        this.openAI = openAI;
    }

    public String generateTest(String scenarioDescription) {
        String className = classNameFromScenario(scenarioDescription);
        String prompt = buildPrompt(scenarioDescription, className);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String response = openAI.generateResponse(prompt);
                String fixed = autoFixBraces(response);
                String corrected;

                try {
                    corrected = correctJavaCodeViaMCP(fixed);
                    System.out.println("[MCP code correction response]:\n" + corrected);
                } catch (Exception e) {
                    System.err.println("⚠ MCP correction failed: " + e.getMessage());
                    corrected = fixed;
                }

                // Post-process MCP code to enforce framework conventions
                String enforced = enforceFrameworkConventions(corrected);

                // If MCP code contains a valid class, extract and use its class name
                if (isValidJavaCode(enforced)) {
                    String mcpClassName = className;
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("public class (\\w+)").matcher(enforced);
                    if (m.find()) mcpClassName = m.group(1);
                    System.out.println("✅ Valid test class generated from MCP on attempt " + attempt + ", class: " + mcpClassName);
                    return enforced;
                }

                // Otherwise, try to fix and validate
                String complete = ensureCompleteTestClass(enforced, className);
                if (isValidJavaCode(complete)) {
                    System.out.println("✅ Valid test class generated on attempt " + attempt);
                    return complete;
                } else {
                    System.err.println("❌ Attempt " + attempt + " failed: Invalid Java, retrying...");
                }

            } catch (Exception e) {
                System.err.println("❌ Error generating test (attempt " + attempt + "): " + e.getMessage());
            }
        }

        // Fallback: use default working template if all attempts fail
        System.err.println("⚠ All attempts failed — using default login test template.");
        return ensureCompleteTestClass("", className);
    }

    /**
     * Enforce config-driven values, descriptive element names, and getDriver() usage in generated code.
     */
    private String enforceFrameworkConventions(String code) {
        if (code == null) return null;
        String result = code;
        // Replace direct driver.get with getDriver().get
        result = result.replaceAll("(?<![a-zA-Z0-9_])driver\\.get\\(", "getDriver().get(");
        // Replace hardcoded username/password with config values
        result = result.replaceAll("sendKeys\\(\\s*\"testuser\"\\s*\\)", "sendKeys(ConfigurationManager.getBundle().getPropertyValue(\"username\"))");
        result = result.replaceAll("sendKeys\\(\\s*\"testpass\"\\s*\\)", "sendKeys(ConfigurationManager.getBundle().getPropertyValue(\"password\"))");
        // Replace element names with descriptive ones
        result = result.replaceAll("findElement\\(\\s*\"username\"\\s*\\)", "findElement(\"Username input field\")");
        result = result.replaceAll("findElement\\(\\s*\"password\"\\s*\\)", "findElement(\"Password input field\")");
        // Optionally, add more replacements as needed
        return result;
    }

    // 🧩 Build OpenAI prompt
    private String buildPrompt(String scenarioDescription, String className) {
        return "Write a complete Java Selenium test class named " + className +
            " for the following scenario:\n" + scenarioDescription + "\n\n" +
            "Requirements:\n" +
            "- The first line must be: package org.k11techlab.framework_unittests.ai_generated;\n" +
            "- The class must extend org.k11techlab.framework.selenium.webuitestbase.BaseSeleniumTest.\n" +
            "- Use ConfigurationManager.getBundle().getPropertyValue(\"weburl\") for URL.\n" +
            "- Use elementHealer.findElement(\"element description\") instead of standard locators.\n" +
            "- Use AIElementHealer initialized with:\n" +
            "    LLMInterface llmProvider = new OpenAIClient();\n" +
            "    AIElementHealer elementHealer = new AIElementHealer(llmProvider, getDriver());\n" +
            "- Include a @Test method that: enters username, enters password, clicks login, and asserts dashboard is displayed.\n" +
            "- The class must compile without syntax errors.";
    }

    private String classNameFromScenario(String scenario) {
        if (scenario == null || scenario.trim().isEmpty()) return "GeneratedTest";
        String scenarioLower = scenario.toLowerCase();
        if (scenarioLower.contains("login") || scenarioLower.contains("user") || scenarioLower.contains("password")) {
            return "LoginFunctionalityTest";
        }
        String[] words = scenario.trim().split("\\s+");
        StringBuilder name = new StringBuilder();
        int count = 0;
        for (String word : words) {
            String clean = word.replaceAll("[^a-zA-Z0-9]", "");
            if (clean.isEmpty()) continue;
            // Skip generic words, URLs, credentials, very short/long words
            if (clean.matches("(?i)(test|on|the|should|verify|and|functionality|http|https|www|com|net|org|app|platform|user|pass|password|username|click|enter|open|dashboard|is|displayed|with|to|for|of|by|at|in|login|website|web|site|page|\\d+)") || clean.length() < 3 || clean.length() > 15) continue;
            name.append(Character.toUpperCase(clean.charAt(0))).append(clean.substring(1).toLowerCase());
            count++;
            if (count >= 2) break;
        }
        String className = name.length() > 0 ? name.toString() : "Generated";
        // Truncate if too long
        if (className.length() > 20) className = className.substring(0, 20);
        // Ensure ends with 'Test' only once
        if (!className.endsWith("Test")) className += "Test";
        // Ensure class name starts with a letter
        if (!Character.isJavaIdentifierStart(className.charAt(0))) className = "Test" + className;
        return className;
    }

    // ✅ Enhanced brace and quote auto-fix
    private String autoFixBraces(String code) {
        if (code == null) return "";
        int open = code.length() - code.replace("{", "").length();
        int close = code.length() - code.replace("}", "").length();
        int diff = open - close;

        StringBuilder fixed = new StringBuilder(code);

        if (diff > 0) for (int i = 0; i < diff; i++) fixed.append("}");

        // Fix unclosed quotes
        long quotes = fixed.chars().filter(c -> c == '"').count();
        if (quotes % 2 != 0) fixed.append("\"");

        // Fix unclosed parentheses
        long opens = fixed.chars().filter(c -> c == '(').count();
        long closes = fixed.chars().filter(c -> c == ')').count();
        if (opens > closes) fixed.append(")");

        // Auto-complete known truncations
        String codeStr = fixed.toString();
        if (codeStr.contains("Password input") && !codeStr.contains("sendKeys(")) {
            fixed.append(" field\").sendKeys(ConfigurationManager.getBundle().getPropertyValue(\"password\"));");
        }
        if (codeStr.contains("Username input") && !codeStr.contains("sendKeys(")) {
            fixed.append(" field\").sendKeys(ConfigurationManager.getBundle().getPropertyValue(\"username\"));");
        }

        return fixed.toString();
    }

    private boolean isValidJavaCode(String code) {
        if (code == null) return false;
        int open = code.length() - code.replace("{", "").length();
        int close = code.length() - code.replace("}", "").length();
        return code.contains("class ") && code.contains("@Test") && open == close;
    }

    // 🧠 Core test template builder
    private String ensureCompleteTestClass(String code, String className) {
        StringBuilder fixed = new StringBuilder();
        // Always start with package or comment
        String packageLine = "package org.k11techlab.framework_unittests.ai_generated;\n\n";
        fixed.append(packageLine);
        fixed.append("import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;\n");
        fixed.append("import org.testng.annotations.Test;\n");
        fixed.append("import org.testng.Assert;\n");
        fixed.append("import org.k11techlab.framework.selenium.webuitestbase.BaseSeleniumTest;\n");
        fixed.append("import org.k11techlab.framework.ai.healing.AIElementHealer;\n");
        fixed.append("import org.k11techlab.framework.ai.llm.LLMInterface;\n");
        fixed.append("import org.k11techlab.framework.ai.openai.OpenAIClient;\n\n");

        fixed.append("public class ").append(className).append(" extends BaseSeleniumTest {\n\n");
        fixed.append("    @Test\n");
        fixed.append("    public void testLoginFunctionality() {\n");
        fixed.append("        LLMInterface llmProvider = new OpenAIClient();\n");
        fixed.append("        AIElementHealer elementHealer = new AIElementHealer(llmProvider, getDriver());\n\n");
        fixed.append("        getDriver().get(ConfigurationManager.getBundle().getPropertyValue(\"weburl\"));\n\n");
        fixed.append("        // Click on login menu in navbar and wait for login page to load\n");
        fixed.append("        elementHealer.findElement(\"Login menu in navbar\").click();\n");
        fixed.append("        elementHealer.findElement(\"Username input field\").sendKeys(ConfigurationManager.getBundle().getPropertyValue(\"username\"));\n");
        fixed.append("        elementHealer.findElement(\"Password input field\").sendKeys(ConfigurationManager.getBundle().getPropertyValue(\"password\"));\n");
        fixed.append("        elementHealer.findElement(\"Login button\").click();\n\n");
        fixed.append("        Assert.assertTrue(elementHealer.findElement(\"Dashboard\").isDisplayed(), \"Dashboard should be visible after login\");\n");
        fixed.append("    }\n");
        fixed.append("}\n");

        return fixed.toString();
    }

    // 🔧 MCP correction method
    private String correctJavaCodeViaMCP(String code) throws Exception {
        int port = getMCPPort();
        URL url = new URL("http://localhost:" + port + "/mcp/correct-code");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(code.getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() == 200) {
            try (Scanner scanner = new Scanner(conn.getInputStream(), "UTF-8")) {
                scanner.useDelimiter("\\A");
                String json = scanner.hasNext() ? scanner.next() : "";
                if (json.contains("correctedCode")) {
                    return json.substring(json.indexOf(":") + 2, json.lastIndexOf("\""))
                            .replace("\\n", "\n").replace("\\\"", "\"");
                }
                return json;
            }
        } else {
            throw new RuntimeException("MCP correction failed: HTTP " + conn.getResponseCode());
        }
    }

    private int getMCPPort() {
        try {
            String portStr = ConfigurationManager.getBundle().getPropertyValue("mcp.port");
            return portStr != null ? Integer.parseInt(portStr.trim()) : 8080;
        } catch (Exception e) {
            System.err.println("⚠ Failed to load MCP port, using default 8080.");
            return 8080;
        }
    }

    // 🚀 Start MCP server
    public static void ensureMCPServerRunning() {
        new Thread(() -> {
            try {
                org.k11techlab.framework.ai.mcp.MCPServer server = new org.k11techlab.framework.ai.mcp.MCPServer();
                server.start(8080);
                System.out.println("✅ MCP Server started on port 8080");
            } catch (Exception e) {
                System.err.println("[MCPServer] Already running or failed to start: " + e.getMessage());
            }
        }, "MCPServer-Background").start();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}
    }
}
