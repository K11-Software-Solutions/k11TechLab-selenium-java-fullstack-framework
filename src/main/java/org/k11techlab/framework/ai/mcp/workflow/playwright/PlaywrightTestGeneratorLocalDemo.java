package org.k11techlab.framework.ai.mcp.workflow.playwright;

import org.k11techlab.framework.ai.openai.OpenAIClient;

/**
 * Local demo runner that injects a canned response from a subclassed OpenAIClient
 * so we can run the PlaywrightTestGenerator without network/API keys.
 */
public class PlaywrightTestGeneratorLocalDemo {
    // Local stub that returns a minimal Playwright Java TestNG skeleton
    static class LocalOpenAI extends OpenAIClient {
        @Override
        public String generateResponse(String prompt, float temperature, int maxTokens) {
            return "package demo;\n\n" +
                   "import com.microsoft.playwright.*;\n" +
                   "import org.testng.annotations.Test;\n\n" +
                   "public class SamplePlaywrightTest {\n" +
                   "  @Test\n" +
                   "  public void testLogin() {\n" +
                   "    try (Playwright playwright = Playwright.create()) {\n" +
                   "      Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));\n" +
                   "      Page page = browser.newPage();\n" +
                   "      String weburl = System.getenv(\"WEBURL\");\n" +
                   "      page.navigate(weburl != null ? weburl : \"https://example.com\");\n" +
                   "      // login steps using USERNAME/PASSWORD env vars\n" +
                   "    }\n" +
                   "  }\n" +
                   "}\n";
        }

        @Override
        public String generateResponse(String prompt) {
            return generateResponse(prompt, 0.7f, 200);
        }
    }

    public static void main(String[] args) {
        LocalOpenAI local = new LocalOpenAI();
        PlaywrightTestGenerator gen = new PlaywrightTestGenerator(local);
        String scenario = (args.length > 0) ? args[0] : "Login to the dashboard and verify the dashboard is visible";
        String code = gen.generateTest(scenario, "GeneratedPlaywrightLogin", "org.k11techlab.framework_unittests.ai_generated");
        System.out.println("\n--- Generated Playwright Test ---\n");
        System.out.println(code);
    }
}
