
package org.k11techlab.framework.ai.mcp.workflow.playwright;

import org.k11techlab.framework.ai.openai.OpenAIClient;
import org.k11techlab.framework.ai.engine.AICompileRepairEngine;
import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;

import java.io.File;
import org.k11techlab.framework.selenium.webuitestengine.commonUtil.fileHandler.FileUtil;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class PlaywrightWorkflow {
    public static void main(String[] args) throws Exception {
        String scenario = ConfigurationManager.getString("scenario", "Login to the dashboard and verify the dashboard is visible");
        String classNameBase = ConfigurationManager.getString("classNameBase", "GeneratedPlaywrightLogin");
        String packageName = ConfigurationManager.getString("ai.packageName", "org.k11techlab.framework_unittests.ai_generated.k11softwaresolutions");
        String outputDir = ConfigurationManager.getString("output.dir", "src/test/ai_generated_tests/k11softwaresolutions");
        String relPath = outputDir.replace("src/test/java/", "");
        if (relPath.contains("/")) {
            String[] parts = relPath.split("/");
            StringBuilder subPkg = new StringBuilder();
            boolean found = false;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("ai_generated")) {
                    found = true;
                }
                if (found && i > 0) {
                    if (!parts[i].isEmpty()) {
                        subPkg.append(".").append(parts[i]);
                    }
                }
            }
            String subPkgStr = subPkg.toString();
            if (!packageName.endsWith(subPkgStr)) {
                packageName = packageName + subPkgStr;
            }
        }
        String mcpLogDir = ConfigurationManager.getString("log.dir", "mcp_testlog");
        String mavenCmd = ConfigurationManager.getString("maven.cmd.path", "mvn");
        String reportSrc = ConfigurationManager.getString("report.src", "test-output/emailable-report.html");
        String destDir = ConfigurationManager.getString("maven.test.dir", "src/test/ai_generated");
        int maxFixAttempts = Integer.parseInt(ConfigurationManager.getString("maxFixAttempts", "2"));
        // Empty the mcp_testlog directory before test run
        FileUtil.emptyDirectory(mcpLogDir);
        new File(mcpLogDir).mkdirs();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String logFile = mcpLogDir + "/" + classNameBase + "_" + timestamp + "_workflow.log";
        String testFileName = classNameBase + "Test.java";
        String testFilePath = outputDir + "/" + testFileName;
        StringBuilder log = new StringBuilder();

        // 1. Ensure Page Object exists
        log.append("=== Page Object Check ===\n");
        String pageObjectName = "LoginPage";
        String pageObjectFile = outputDir + "/" + pageObjectName + ".java";
        File poFile = new File(pageObjectFile);
        if (!poFile.exists()) {
            log.append("Page object not found, generating via MCP endpoint...\n");
            try {
                // Example: hardcoded fields for demo; in real use, make this dynamic or configurable
                String json = "{" +
                        "\"className\":\"" + pageObjectName + "\"," +
                        "\"fields\": {" +
                        "\"enterUsername\": {\"id\":\"username\", \"name\":\"username\", \"text\":\"Username\", \"method\":\"fill\"}," +
                        "\"enterPassword\": {\"id\":\"password\", \"name\":\"password\", \"text\":\"Password\", \"method\":\"fill\"}," +
                        "\"clickLogin\": {\"id\":\"login\", \"name\":\"login\", \"text\":\"Login\", \"method\":\"click\"}" +
                        "}}";
                java.net.URL url = new java.net.URL("http://localhost:8090/mcp/generate-page-object");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
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
                log.append("Page object generated and saved to: ").append(pageObjectFile).append("\n");
            } catch (Exception e) {
                log.append("Failed to generate page object: ").append(e.getMessage()).append("\n");
            }
        } else {
            log.append("Page object already exists: ").append(pageObjectFile).append("\n");
        }

        // 2. Generate test using page object
        log.append("=== Test Generation (Page Object) ===\n");
        OpenAIClient openAI = new OpenAIClient();
        PlaywrightTestGenerator generator = new PlaywrightTestGenerator(openAI);
        // Instruct generator to use the page object (could be prompt-based or template-based)
        String code = "import " + packageName + ".LoginPage;\n" +
                "// ...existing imports...\n" +
                "public class " + classNameBase + "Test {\n" +
                "    // ...existing setup...\n" +
                "    @Test\n" +
                "    public void testLogin() {\n" +
                "        LoginPage loginPage = new LoginPage(page);\n" +
                "        loginPage.enterUsername(\"testuser\");\n" +
                "        loginPage.enterPassword(\"testpass\");\n" +
                "        loginPage.clickLogin();\n" +
                "        assert loginPage.isDashboardVisible();\n" +
                "    }\n" +
                "}";
        try {
            File dir = new File(outputDir);
            dir.mkdirs();
            FileWriter writer = new FileWriter(testFilePath);
            writer.write(code);
            writer.close();
            log.append("Test saved to: ").append(testFilePath).append("\n");
        } catch (Exception e) {
            log.append("Failed to save generated test: ").append(e.getMessage()).append("\n");
        }

        // 2. Copy test to src/test/java for Maven
        log.append("\n=== Copy to src/test/java ===\n");
        File destDirFile = new File(destDir);
        destDirFile.mkdirs();
        File destFile = new File(destDirFile, testFileName);
        try {
            Files.copy(Paths.get(testFilePath), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.append("Copied to: ").append(destFile.getAbsolutePath()).append("\n");
        } catch (Exception e) {
            log.append("Failed to copy test file: ").append(e.getMessage()).append("\n");
        }

        // 3. Compile/run + AI repair loop
        log.append("\n=== Test Execution & AI Repair ===\n");
        AICompileRepairEngine repairEngine = new AICompileRepairEngine(openAI, maxFixAttempts);
        Path javaFilePath = destFile.toPath();
        String lastOutput = "";
        for (int attempt = 0; attempt <= maxFixAttempts; attempt++) {
            RunResult rr = runMavenTest(mavenCmd, packageName + "." + classNameBase + "Test");
            lastOutput = rr.output;
            log.append("\n--- Attempt ").append(attempt + 1).append(" ---\n");
            log.append(lastOutput).append("\n");
            if (rr.exitCode == 0) {
                log.append("Test PASSED on attempt ").append(attempt + 1).append("\n");
                break;
            }
            if (attempt == maxFixAttempts) {
                log.append("Test FAILED after ").append(maxFixAttempts + 1).append(" attempts\n");
                break;
            }
            String brokenCode = new String(Files.readAllBytes(javaFilePath), StandardCharsets.UTF_8);
            String fixedCode = repairEngine.fixJavaCompilationErrors(
                    brokenCode, rr.output, scenario, packageName, classNameBase + "Test");
            fixedCode = normalize(fixedCode, packageName, classNameBase + "Test");
            repairEngine.writeFixedCode(javaFilePath, fixedCode);
            log.append("AI attempted to repair code.\n");
        }

        // 4. Download/copy test report
        log.append("\n=== Report Download ===\n");
        String reportDest = mcpLogDir + "/" + classNameBase + "_" + timestamp + "_report.html";
        String reportDest2 = "mcp_testreport/" + classNameBase + "_" + timestamp + "_report.html";
        try {
            Files.copy(Paths.get(reportSrc), Paths.get(reportDest), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.append("Report copied to: ").append(reportDest).append("\n");
            // Also copy to mcp_testreport
            new File("mcp_testreport").mkdirs();
            Files.copy(Paths.get(reportSrc), Paths.get(reportDest2), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.append("Report copied to: ").append(reportDest2).append("\n");
        } catch (Exception e) {
            log.append("Failed to copy report: ").append(e.getMessage()).append("\n");
        }

        // 5. Save master log
        try (FileWriter logWriter = new FileWriter(logFile)) {
            logWriter.write(log.toString());
        } catch (Exception e) {
            System.err.println("Failed to save workflow log: " + e.getMessage());
        }
        System.out.println("\nWorkflow complete. Log and report saved in: " + mcpLogDir);
    }

    private static RunResult runMavenTest(String mavenCmd, String testClass) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                mavenCmd,
                "-q",
                "-Dtest=" + testClass,
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
        try (var is = p.getInputStream()) {
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

    private static String normalize(String code, String packageName, String className) {
        if (code == null) return "";
        String c = code.replaceFirst("(?m)^package\\s+[^;]+;\\s*", "");
        c = "package " + packageName + ";\n\n" + c.trim();
        c = c.replaceAll("(?s)public\\s+class\\s+\\w+", "public class " + className);
        return c;
    }

    private static class RunResult {
        final int exitCode;
        final String output;
        RunResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
