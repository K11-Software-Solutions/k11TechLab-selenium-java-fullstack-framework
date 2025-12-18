package org.k11techlab.framework.ai.mcp.workflow.playwright;

import org.k11techlab.framework.ai.openai.OpenAIClient;

public class PlaywrightTestGenerator {
    private final OpenAIClient openAI;

    public PlaywrightTestGenerator(OpenAIClient openAI) {
        this.openAI = openAI;
    }

    public String generateTest(String scenario, String classNameBase, String packageName) {
        String className = classNameBase.endsWith("Test") ? classNameBase : classNameBase + "Test";

        String prompt =
                "Generate a complete Java TestNG test class using Playwright for Java.\n" +
                "Package: " + packageName + "\n" +
                "Class: " + className + "\n" +
                "MUST include:\n" +
                "try (Playwright playwright = Playwright.create()) { ... }\n" +
                "Use Chromium headless with BrowserType.LaunchOptions.\n" +
                "Use env vars WEBURL, USERNAME, PASSWORD.\n" +
                "Output ONLY Java code.\n\nScenario:\n" + scenario;

        String code = openAI.generateResponse(prompt);
        code = code.replaceFirst("(?m)^package\\s+[^;]+;\\s*", "");
        return "package " + packageName + ";\n\n" + code.trim();
    }
}
