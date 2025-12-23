package org.k11techlab.framework.ai.mcp.workflow.playwright;

import org.k11techlab.framework.ai.openai.OpenAIClient;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MCPTestWorkflow {
    public static void main(String[] args) {
        // Load config with k11softwaresolutions prefix
        java.util.Properties props = new java.util.Properties();
        try (java.io.FileInputStream fis = new java.io.FileInputStream("config/mcp-config.properties")) {
            props.load(fis);
        } catch (Exception e) {
            System.err.println("Failed to load config: " + e.getMessage());
            return;
        }
        String scenario = props.getProperty("scenario", "Login to the dashboard and verify the dashboard is visible");
        String classNameBase = props.getProperty("classNameBase", "GeneratedPlaywrightLogin");
        String packageName = props.getProperty("ai.packageName", "org.k11techlab.framework_unittests.ai_generated");
        String outputDir = props.getProperty("output.dir", "src/test/ai_generated_tests/k11softwaresolutions");
        String mcpLogDir = props.getProperty("log.dir", "mcp_testlog");
        String mavenCmd = props.getProperty("maven.cmd.path", "mvn");
        String reportSrc = props.getProperty("report.src", "test-output/emailable-report.html");
        new File(mcpLogDir).mkdirs();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String logPrefix = mcpLogDir + "/" + classNameBase + "_" + timestamp;
        String testFileName = classNameBase + "Test.java";
        String testFilePath = outputDir + "/" + testFileName;

        // 1. Generate test
        StringBuilder log = new StringBuilder();
        log.append("=== Test Generation ===\n");
        OpenAIClient openAI = new OpenAIClient();
        PlaywrightTestGenerator generator = new PlaywrightTestGenerator(openAI);
        String code = generator.generateTest(scenario, classNameBase, packageName);
        log.append("Generated test code:\n").append(code).append("\n\n");
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

        // 2. Review test
        log.append("\n=== AI Review ===\n");
        String reviewPrompt = props.getProperty("review.prompt", "Review the following Java TestNG test class for correctness, best practices, and possible improvements. Provide a concise summary and suggestions.\n\n") + code;
        String review = openAI.generateResponse(reviewPrompt);
        log.append(review).append("\n\n");
        try (FileWriter reviewWriter = new FileWriter(logPrefix + "_review.txt")) {
            reviewWriter.write(review);
        } catch (Exception e) {
            log.append("Failed to save review: ").append(e.getMessage()).append("\n");
        }

        // 3. Copy test to src/test/java for Maven
        log.append("\n=== Copy to src/test/java ===\n");
        String destDir = props.getProperty("maven.test.dir", "src/test/java/org/k11techlab/framework_unittests/ai_generated");
        File destDirFile = new File(destDir);
        destDirFile.mkdirs();
        File destFile = new File(destDirFile, testFileName);
        try {
            Files.copy(Paths.get(testFilePath), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.append("Copied to: ").append(destFile.getAbsolutePath()).append("\n");
        } catch (IOException e) {
            log.append("Failed to copy test file: ").append(e.getMessage()).append("\n");
        }

        // 4. Execute test with Maven
        log.append("\n=== Test Execution ===\n");
        StringBuilder testOutput = new StringBuilder();
        int exitCode = -1;
        try {
            ProcessBuilder pb = new ProcessBuilder(
                mavenCmd, "-Dtest=" + packageName + "." + classNameBase + "Test", "test"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                testOutput.append(line).append("\n");
            }
            exitCode = process.waitFor();
            log.append("Test execution finished with exit code: ").append(exitCode).append("\n");
        } catch (Exception e) {
            log.append("Failed to execute test: ").append(e.getMessage()).append("\n");
        }
        try (FileWriter execWriter = new FileWriter(logPrefix + "_execution.txt")) {
            execWriter.write(testOutput.toString());
        } catch (Exception e) {
            log.append("Failed to save execution log: ").append(e.getMessage()).append("\n");
        }

        // 5. Download/copy test report
        log.append("\n=== Report Download ===\n");
        String reportDest = logPrefix + "_report.html";
        try {
            Files.copy(Paths.get(reportSrc), Paths.get(reportDest), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.append("Report copied to: ").append(reportDest).append("\n");
        } catch (IOException e) {
            log.append("Failed to copy report: ").append(e.getMessage()).append("\n");
        }

        // 6. Save master log
        try (FileWriter logWriter = new FileWriter(logPrefix + "_workflow.log")) {
            logWriter.write(log.toString());
        } catch (Exception e) {
            System.err.println("Failed to save workflow log: " + e.getMessage());
        }
        System.out.println("\nWorkflow complete. Logs and reports saved in: " + mcpLogDir);
    }
}
