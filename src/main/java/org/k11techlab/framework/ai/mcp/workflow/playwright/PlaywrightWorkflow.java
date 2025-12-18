package org.k11techlab.framework.ai.mcp.workflow.playwright;

import org.k11techlab.framework.ai.openai.OpenAIClient;
import org.k11techlab.framework.ai.engine.AICompileRepairEngine;
import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class PlaywrightWorkflow {

    private final PlaywrightTestGenerator generator;
    private final AICompileRepairEngine repairEngine;

    public PlaywrightWorkflow(
            PlaywrightTestGenerator generator,
            OpenAIClient openAI,
            int maxFixAttempts) {

        this.generator = generator;
        this.repairEngine = new AICompileRepairEngine(openAI, maxFixAttempts);
    }

    public String generateAndRunTest(
            String scenario,
            String classNameBase,
            String packageName) throws Exception {

        String className = classNameBase.endsWith("Test")
                ? classNameBase
                : classNameBase + "Test";

        String packageDir = packageName.replace('.', '/');

        // 1️⃣ Generate code
        String code = generator.generateTest(scenario, classNameBase, packageName);
        code = normalize(code, packageName, className);

        // 2️⃣ Write Java file
        Path javaFilePath = Paths.get(
                "src/test/java",
                packageDir,
                className + ".java"
        );
        Files.createDirectories(javaFilePath.getParent());
        Files.write(javaFilePath, code.getBytes(StandardCharsets.UTF_8));

        String lastOutput = "";

        // 3️⃣ Compile / run + repair loop
        for (int attempt = 0; attempt <= repairEngine.getMaxAttempts(); attempt++) {

            RunResult rr = runMavenTest(className);
            lastOutput = rr.output;

            if (rr.exitCode == 0) {
                return "Maven exit code: 0\n" + lastOutput;
            }

            if (attempt == repairEngine.getMaxAttempts()) {
                return "Maven exit code: " + rr.exitCode +
                        "\n" + lastOutput +
                        "\n\nAUTO-REPAIR exhausted (" + repairEngine.getMaxAttempts() + " attempts)";
            }

            // 4️⃣ Read broken code (Java 11 safe)
            String brokenCode = new String(
                    Files.readAllBytes(javaFilePath),
                    StandardCharsets.UTF_8
            );

            // 5️⃣ Ask OpenAI to fix compilation errors
            String fixedCode = repairEngine.fixJavaCompilationErrors(
                    brokenCode,
                    rr.output,
                    scenario,
                    packageName,
                    className
            );

            fixedCode = normalize(fixedCode, packageName, className);

            repairEngine.writeFixedCode(javaFilePath, fixedCode);
        }

        return lastOutput; // should never reach
    }

    // -------------------------
    // Helpers
    // -------------------------

    private static String normalize(String code, String packageName, String className) {
        if (code == null) return "";

        String c = code.replaceFirst("(?m)^package\\s+[^;]+;\\s*", "");
        c = "package " + packageName + ";\n\n" + c.trim();
        c = c.replaceAll("(?s)public\\s+class\\s+\\w+", "public class " + className);
        return c;
    }

    private static RunResult runMavenTest(String className) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(
                "mvn",
                "-q",
                "-Dtest=" + className,
                "test"
        );

        pb.directory(new File(System.getProperty("user.dir")));
        pb.redirectErrorStream(true);

        Map<String, String> env = pb.environment();
        env.put("WEBURL", safeProp("weburl"));
        env.put("USERNAME", safeProp("username"));
        env.put("PASSWORD", safeProp("password"));

        Process p = pb.start();

        String out;
        try (InputStream is = p.getInputStream()) {
            out = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        int exit = p.waitFor();
        return new RunResult(exit, out);
    }

    private static String safeProp(String key) {
        try {
            String v = ConfigurationManager.getBundle().getPropertyValue(key);
            return v == null ? "" : v;
        } catch (Exception e) {
            return "";
        }
    }

    // Java 11-compatible result holder
    private static class RunResult {
        final int exitCode;
        final String output;

        RunResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
