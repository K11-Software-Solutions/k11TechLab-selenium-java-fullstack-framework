package org.k11techlab.framework.ai.mcp.workflow.playwright;

import org.k11techlab.framework.ai.openai.OpenAIClient;
import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;

public class PlaywrightTestGenerator {
    private final OpenAIClient openAI;

    public PlaywrightTestGenerator(OpenAIClient openAI) {
        this.openAI = openAI;
    }

    public String generateTest(String scenario, String classNameBase, String packageName) {
        String className = classNameBase.endsWith("Test") ? classNameBase : classNameBase + "Test";

        String prompt;
        String promptDir = "mcp-prompts/k11softwaresolutions";
        try {
            String configPromptDir = ConfigurationManager.getBundle().getPropertyValue("prompt.dir");
            if (configPromptDir != null && !configPromptDir.isEmpty()) {
                promptDir = configPromptDir;
            }
        } catch (Exception e) {
            // fallback to default if config not found
        }
        java.nio.file.Path promptPath = java.nio.file.Paths.get(promptDir, "logintest_prompt.txt");
        if (java.nio.file.Files.exists(promptPath)) {
            try {
                prompt = new String(java.nio.file.Files.readAllBytes(promptPath));
                prompt = prompt.replace("{packageName}", packageName)
                               .replace("{className}", className)
                               .replace("{scenario}", scenario);
            } catch (Exception e) {
                // fallback to default prompt if error reading file
                prompt = getDefaultPrompt(packageName, className, scenario);
            }
        } else {
            prompt = getDefaultPrompt(packageName, className, scenario);
        }

        String code = openAI.generateResponse(prompt);
        // Remove markdown code block markers if present
        code = code.replaceAll("(?s)```[a-zA-Z]*\\s*", "").replaceAll("```", "");
        // Remove any package statement from the generated code
        code = code.replaceFirst("(?m)^package\\s+[^;]+;\\s*", "");
        // Fix ConfigurationManager import/package dynamically from config
        String configManagerPackage = "org.k11techlab.framework.selenium.webuitestengine.configManager";
        try {
            String configPkg = ConfigurationManager.getBundle().getPropertyValue("configmanager.package");
            if (configPkg != null && !configPkg.isEmpty()) {
                configManagerPackage = configPkg;
            }
        } catch (Exception e) {
            // fallback to default if config not found
        }
        code = code.replaceAll("import\\s+org\\.k11techlab\\.framework\\.ConfigurationManager;", "import " + configManagerPackage + ".ConfigurationManager;");
        code = code.replace("org.k11techlab.framework.ConfigurationManager", configManagerPackage + ".ConfigurationManager");
        // Always add ConfigurationManager import if missing
        String configManagerImport = "import " + configManagerPackage + ".ConfigurationManager;";
        if (!code.contains(configManagerImport)) {
            // Insert after the last import statement
            int lastImport = code.lastIndexOf("import ");
            if (lastImport != -1) {
                int importEnd = code.indexOf(";", lastImport) + 1;
                code = code.substring(0, importEnd) + "\n" + configManagerImport + code.substring(importEnd);
            } else {
                // If no import, insert after package
                int pkgEnd = code.indexOf(";") + 1;
                code = code.substring(0, pkgEnd) + "\n" + configManagerImport + code.substring(pkgEnd);
            }
        }
        // Ensure BrowserType import is present if used
        if (code.contains("BrowserType.")) {
            if (!code.contains("import com.microsoft.playwright.BrowserType;")) {
                code = code.replace("import com.microsoft.playwright.Browser;", "import com.microsoft.playwright.Browser;\nimport com.microsoft.playwright.BrowserType;");
            }
        }
        return "package " + packageName + ";\n\n" + code.trim();
    }

    private String getDefaultPrompt(String packageName, String className, String scenario) {
        return "Generate a complete, compilable Java TestNG test class using Playwright for Java.\n"
                + "Package: " + packageName + "\n"
                + "Class: " + className + "\n"
                + "MUST include:\n"
                + "- All required imports (Playwright, TestNG, ConfigurationManager, etc).\n"
                + "- A @BeforeClass setUp() method that launches Playwright, a Chromium browser in headless mode, and creates a Page. Store Playwright, Browser, and Page as fields.\n"
                + "- A @Test method that:\n"
                + "  - Reads weburl, username, and password using ConfigurationManager.getBundle().getPropertyValue('weburl'), etc.\n"
                + "  - Navigates to the weburl.\n"
                + "  - Clicks the dashboard link if present (e.g., page.isVisible and page.click for 'a:has-text('Dashboard')').\n"
                + "  - Fills the username and password fields and clicks the login button (use generic selectors if not specified).\n"
                + "  - Asserts that the dashboard is visible after login (e.g., page.isVisible('text=Dashboard')).\n"
                + "- A @AfterClass tearDown() method that closes the browser and Playwright.\n"
                + "- Output ONLY Java code, no markdown.\n"
                + "- The code must be complete and not truncated.\n\nScenario:\n" + scenario;
}
}
