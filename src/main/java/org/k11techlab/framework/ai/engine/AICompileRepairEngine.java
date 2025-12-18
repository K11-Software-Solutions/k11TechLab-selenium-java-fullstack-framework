package org.k11techlab.framework.ai.engine;

import org.k11techlab.framework.ai.openai.OpenAIClient;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Compile-repair loop helper:
 * - given broken Java code + compiler output
 * - asks OpenAI to return fixed compilable Java code
 * - writes it back to disk
 */
public class AICompileRepairEngine {

    private final OpenAIClient openAI;
    private final int maxAttempts;

    public AICompileRepairEngine(OpenAIClient openAI, int maxAttempts) {
        this.openAI = openAI;
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Ask the model to fix compilation errors. Returns fixed Java source as a String.
     */
    public String fixJavaCompilationErrors(
            String brokenCode,
            String compilerOutput,
            String scenario,
            String packageName,
            String className
    ) {
        String prompt =
                "You are a senior Java automation engineer.\n" +
                "Fix this Java TestNG test that uses Playwright for Java so it COMPILES.\n\n" +

                "Constraints:\n" +
                "- MUST include: try (Playwright playwright = Playwright.create()) { ... }\n" +
                "- MUST launch Chromium headless using BrowserType.LaunchOptions (no StartOptions)\n" +
                "- MUST use TestNG @Test\n" +
                "- MUST include all necessary imports\n" +
                "- MUST keep package: " + packageName + "\n" +
                "- MUST keep public class name: " + className + "\n" +
                "- Use env vars WEBURL, USERNAME, PASSWORD\n" +
                "- Output ONLY valid Java code (no markdown, no explanations)\n\n" +

                "Scenario:\n" + scenario + "\n\n" +

                "Compiler output:\n" + compilerOutput + "\n\n" +

                "Broken code:\n" + brokenCode + "\n";

        String fixed = openAI.generateResponse(prompt);

        // Normalize: enforce correct package + class name even if model deviates
        fixed = stripPackageLine(fixed);
        fixed = "package " + packageName + ";\n\n" + fixed.trim();

        // If model accidentally changed class name, try to force it back (best-effort).
        fixed = fixed.replaceAll("(?s)public\\s+class\\s+\\w+", "public class " + className);

        return fixed;
    }

    /**
     * Write fixed code to file. Returns the written path.
     */
    public Path writeFixedCode(Path javaFilePath, String fixedCode) throws Exception {
        Files.createDirectories(javaFilePath.getParent());
        Files.write(javaFilePath, fixedCode.getBytes(StandardCharsets.UTF_8));
        return javaFilePath;
    }

    private static String stripPackageLine(String code) {
        if (code == null) return "";
        return code.replaceFirst("(?m)^package\\s+[^;]+;\\s*", "");
    }
}
