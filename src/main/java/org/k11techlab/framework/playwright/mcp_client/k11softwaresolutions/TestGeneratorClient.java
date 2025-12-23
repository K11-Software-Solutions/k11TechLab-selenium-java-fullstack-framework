package org.k11techlab.framework.playwright.mcp_client.k11softwaresolutions;

import java.io.*;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;

public class TestGeneratorClient {
    public static void main(String[] args) throws Exception {
        String appName = "k11softwaresolutions";
        // Load config values using ConfigurationManager
        String configMcpPort = ConfigurationManager.getString("mcp.port", "8091");
        String mcpUrl = "http://localhost:" + configMcpPort + "/mcp/generate-and-run-playwright-test";

        // Fetch app-specific properties
        String webUrl = ConfigurationManager.getString(appName + ".weburl", ConfigurationManager.getString("weburl", ""));
        String promptDir = ConfigurationManager.getString(appName + ".prompt.testdir", "mcp-prompts/" + appName + "/tests");
        String promptFile = ConfigurationManager.getString(appName + ".prompt.logintest", "logintest_creation_prompt.txt");
        String standardsPrompt = ConfigurationManager.getString(appName + ".prompt.framework", "mcp-prompts/framework_standards_prompt.txt");

        // Compose prompt file path
        String promptFilePath = promptDir.endsWith("/") ? promptDir + promptFile : promptDir + "/" + promptFile;

        // Build JSON prompt (now includes standardsPrompt)
        String prompt = String.format("{ \"webUrl\": \"%s\", \"promptFile\": \"%s\", \"standardsPrompt\": \"%s\" }", webUrl, promptFilePath, standardsPrompt);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(mcpUrl);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(prompt, "UTF-8"));

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                int code = response.getStatusLine().getStatusCode();
                System.out.println("Response Code: " + code);
                StringBuilder responseText = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        responseText.append(responseLine.trim());
                    }
                }
                System.out.println("Response: " + responseText.toString());
            }
        }
    }
}
