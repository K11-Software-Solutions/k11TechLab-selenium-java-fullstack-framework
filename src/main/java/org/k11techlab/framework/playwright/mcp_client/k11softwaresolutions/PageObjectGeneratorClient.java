package org.k11techlab.framework.playwright.mcp_client.k11softwaresolutions;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;

public class PageObjectGeneratorClient {
    public static void main(String[] args) throws Exception {
        // App name prefix

        String appName = "k11softwaresolutions";
        // Load config values using ConfigurationManager
        String configMcpPort = ConfigurationManager.getString("mcp.port", "8091");
        String mcpUrl = "http://localhost:" + configMcpPort + "/mcp/generate-page-object";

        // Fetch app-specific properties
        String webUrl = ConfigurationManager.getString(appName + ".weburl", ConfigurationManager.getString("weburl", ""));
        String promptDir = ConfigurationManager.getString(appName + ".prompt.pagedir", "mcp-prompts/" + appName + "/pageobjects");
        String promptFile = ConfigurationManager.getString(appName + ".prompt.pages", "pageobject_creation_prompt_multi.txt");

        // Compose prompt file path
        String promptFilePath = promptDir.endsWith("/") ? promptDir + promptFile : promptDir + "/" + promptFile;

        // Build JSON prompt
        String prompt = String.format("{ \"webUrl\": \"%s\", \"promptFile\": \"%s\" }", webUrl, promptFilePath);

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
