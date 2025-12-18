package org.k11techlab.framework.ai.util;

import org.k11techlab.framework.ai.openai.OpenAIClient;

public class DynamicSeleniumTestWorkflowDemo {
    public static void main(String[] args) throws Exception {
        // Ensure MCP server is running before test generation
        OpenAISeleniumTestGenerator.ensureMCPServerRunning();

        OpenAIClient openAI = new OpenAIClient();
        OpenAISeleniumTestGenerator generator = new OpenAISeleniumTestGenerator(openAI);
        DynamicSeleniumTestWorkflow workflow = new DynamicSeleniumTestWorkflow(generator);

            // Read config values from MCP config
            String weburl = org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager.getBundle().getPropertyValue("weburl");
            String username = org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager.getBundle().getPropertyValue("username");
            String password = org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager.getBundle().getPropertyValue("password");

            // Test for K11 Software Solutions login demo page
            String k11Scenario = "Open " + weburl + ", enter username '" + username + "' and password '" + password + "', click login, assert dashboard is displayed.";
            String k11ClassName = "K11SoftwareSolutionsLoginTest";
            String k11PackageName = "org.k11techlab.framework_unittests.ai_generated";

            String k11Result = workflow.generateAndRunTest(k11Scenario, k11ClassName, k11PackageName);
            System.out.println("K11 Software Solutions Login Test Result:\n" + k11Result);

            // Optionally, run the original test as well
            String scenario = "Open " + weburl + ", enter username '" + username + "' and password '" + password + "', click login, assert dashboard is displayed.";
            String className = "GeneratedLoginTest";
            String packageName = "org.k11techlab.framework_unittests.ai_generated";

            String result = workflow.generateAndRunTest(scenario, className, packageName);
            System.out.println("Default Login Test Result:\n" + result);
    }
}
