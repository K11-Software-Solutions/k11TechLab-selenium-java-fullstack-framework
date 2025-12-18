package org.k11techlab.framework.ai.util;

import org.k11techlab.framework.ai.openai.OpenAIClient;
import org.testng.TestNG;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Generates, compiles, and runs a TestNG-based Selenium test using OpenAI.
 */
public class DynamicSeleniumTestWorkflow {
    private final OpenAISeleniumTestGenerator generator;

    public DynamicSeleniumTestWorkflow(OpenAISeleniumTestGenerator generator) {
        this.generator = generator;
    }

    /**
     * Generates, writes, compiles, and runs a Selenium test from a scenario.
     *
     * @param scenarioDescription The test scenario in plain English.
     * @param className           The desired name for the test class.
     * @param packageName         The package for the generated test class.
     * @return A summary message of what was executed.
     * @throws Exception If anything fails during generation, compilation, or execution.
     */
    public String generateAndRunTest(String scenarioDescription, String className, String packageName) throws Exception {
        // 1. Generate test code
        String testCode = generator.generateTest(scenarioDescription);

        // 2. Replace the class name if needed (in case OpenAI picked a different name)
        String finalTestCode = replaceClassName(testCode, className);

        // 3. Write to .java file
        String sourceDir = "src/test/java/" + packageName.replace('.', '/');
        File sourceDirFile = new File(sourceDir);
        if (!sourceDirFile.exists()) sourceDirFile.mkdirs();

        String filePath = sourceDir + "/" + className + ".java";
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(finalTestCode);
        }

        // 4. Compile .java to .class
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int result = compiler.run(null, null, null, "-d", "target/test-classes", filePath);
        if (result != 0) {
            return "❌ Compilation failed for " + className + ".java";
        }

        // 5. Load compiled class
        File classesDir = new File("target/test-classes");
        URLClassLoader classLoader = new URLClassLoader(
            new URL[]{classesDir.toURI().toURL()},
            this.getClass().getClassLoader()
        );
        Class<?> testClass = classLoader.loadClass(packageName + "." + className);

        // 6. Run with TestNG
        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[]{testClass});
        testng.setDefaultSuiteName("GeneratedSuite");
        testng.setDefaultTestName("GeneratedTest");
        testng.setVerbose(1);
        testng.run();

        return "✅ TestNG execution completed for class: " + className;
    }

    /**
     * Replaces the class name in the generated code if needed.
     */
    private String replaceClassName(String code, String className) {
        StringBuilder updatedCode = new StringBuilder();
        String classPattern = "public class ";
        for (String line : code.split("\n")) {
            if (line.trim().startsWith(classPattern)) {
                int idx = line.indexOf(classPattern);
                int start = idx + classPattern.length();
                int end = line.indexOf(" ", start);
                int brace = line.indexOf("{", start);
                if (end == -1 || (brace != -1 && brace < end)) end = brace;
                if (end == -1) end = line.length();
                String newLine = line.substring(0, start) + className + line.substring(end);
                updatedCode.append(newLine).append("\n");
            } else {
                updatedCode.append(line).append("\n");
            }
        }
        return updatedCode.toString();
    }
}
