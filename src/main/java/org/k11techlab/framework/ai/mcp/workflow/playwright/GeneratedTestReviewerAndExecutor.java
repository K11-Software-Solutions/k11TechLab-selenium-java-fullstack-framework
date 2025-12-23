package org.k11techlab.framework.ai.mcp.workflow.playwright;

import org.k11techlab.framework.ai.openai.OpenAIClient;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class GeneratedTestReviewerAndExecutor {
    public static void main(String[] args) {
        // Path to the generated test file
        String testFilePath = "src/test/ai_generated_tests/k11softwaresolutions/LoginFunctionalityTest.java";
        String testClassName = "org.k11techlab.framework_unittests.ai_generated.LoginFunctionalityTest";
        
        // 1. Review the generated test using OpenAI
        String code = null;
        try {
            code = new String(Files.readAllBytes(Paths.get(testFilePath)));
        } catch (IOException e) {
            System.err.println("Failed to read test file: " + e.getMessage());
            return;
        }
        OpenAIClient openAI = new OpenAIClient();
        String reviewPrompt = "Review the following Java TestNG test class for correctness, best practices, and possible improvements. Provide a concise summary and suggestions.\n\n" + code;
        String review = openAI.generateResponse(reviewPrompt);
        System.out.println("\n--- AI Review of Generated Test ---\n");
        System.out.println(review);

        // 2. Copy the generated test to src/test/java for Maven discovery
        System.out.println("\n--- Copying Generated Test to src/test/java for Maven discovery ---\n");
        String destDir = "src/test/java/org/k11techlab/framework_unittests/ai_generated";
        java.io.File destDirFile = new java.io.File(destDir);
        if (!destDirFile.exists()) destDirFile.mkdirs();
        java.io.File destFile = new java.io.File(destDirFile, "LoginFunctionalityTest.java");
        try {
            Files.copy(Paths.get(testFilePath), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copied to: " + destFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to copy test file: " + e.getMessage());
            return;
        }

        // 3. Execute the test using Maven (TestNG)
        System.out.println("\n--- Executing Generated Test with Maven ---\n");
        try {
            // Read Maven path from mcp-config.properties
            java.util.Properties props = new java.util.Properties();
            try (java.io.FileInputStream fis = new java.io.FileInputStream("config/mcp-config.properties")) {
                props.load(fis);
            }
            String mavenCmd = props.getProperty("maven.cmd.path", "mvn");
            ProcessBuilder pb = new ProcessBuilder(
                mavenCmd, "-Dtest=org.k11techlab.framework_unittests.ai_generated.LoginFunctionalityTest", "test"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("\nTest execution finished with exit code: " + exitCode);
        } catch (Exception e) {
            System.err.println("Failed to execute test: " + e.getMessage());
        }
    }
}
