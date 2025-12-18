package org.k11techlab.framework.ai.util;

import org.k11techlab.framework.ai.openai.OpenAIClient;
import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;

public class RunLoginTestGeneration {
    public static void main(String[] args) {
        // Ensure MCP Server is running
        OpenAISeleniumTestGenerator.ensureMCPServerRunning();

        // Initialize OpenAI Client (make sure your OpenAI key is configured)
        OpenAIClient openAI = new OpenAIClient();

        // Create generator instance
        OpenAISeleniumTestGenerator generator = new OpenAISeleniumTestGenerator(openAI);

        // Read scenario template from mcp-scenarios.properties
        java.util.Properties scenarioProps = new java.util.Properties();
        try (java.io.FileInputStream in = new java.io.FileInputStream("config/mcp-scenarios.properties")) {
            scenarioProps.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load scenario properties", e);
        }

        String scenarioTemplate = scenarioProps.getProperty("login.scenario");
        String weburl = ConfigurationManager.getBundle().getPropertyValue("weburl");
        String username = ConfigurationManager.getBundle().getPropertyValue("username");
        String password = ConfigurationManager.getBundle().getPropertyValue("password");

        // Replace placeholders in scenario template
        String scenario = scenarioTemplate
            .replace("{weburl}", weburl)
            .replace("{username}", username)
            .replace("{password}", password);

        // Generate the test class code
        String testCode = generator.generateTest(scenario);

        System.out.println("\n\n=== Generated Selenium Test ===\n\n");
        System.out.println(testCode);

        // Write the generated test code to the desired folder
        // Read output directory from mcp-config.properties
        String outputDir = ConfigurationManager.getBundle().getPropertyValue("output.dir");
        if (outputDir == null || outputDir.trim().isEmpty()) {
            outputDir = "src/test/ai_generated_tests/k11softwaresolutions";
        }
        java.io.File dir = new java.io.File(outputDir);
        if (!dir.exists()) dir.mkdirs();

        // Extract class name from generated code
        String className = "GeneratedTest";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("public class (\\w+)").matcher(testCode);
        if (m.find()) className = m.group(1);

        String outputFile = outputDir + "/" + className + ".java";
        try (java.io.FileWriter writer = new java.io.FileWriter(outputFile)) {
            writer.write(testCode);
            System.out.println("\nTest code written to: " + outputFile);
        } catch (Exception e) {
            System.err.println("Failed to write test code to file: " + e.getMessage());
        }
    }
}
