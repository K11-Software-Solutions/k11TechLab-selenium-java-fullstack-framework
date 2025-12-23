package org.k11techlab.framework_unittests.ai_mcp;

import org.k11techlab.framework.ai.mcp.MCPServer;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MCPServerIntegrationTest {
  
    private MCPServer server;
    private int port = 9000;
    private boolean startedServer = false;


    @BeforeClass
    public void startServer() throws Exception {
        // Use ConfigurationManager to get port
        String portStr = ConfigurationManager.getString("mcp.port", "9000");
        if (portStr != null && !portStr.isEmpty()) {
            port = Integer.parseInt(portStr.trim());
        }

        // Check if server is already running (Apache HttpClient)
        boolean alreadyRunning = false;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet get = new HttpGet("http://localhost:" + port + "/mcp/health");
            get.setConfig(org.apache.http.client.config.RequestConfig.custom()
                .setConnectTimeout(500)
                .setSocketTimeout(500)
                .build());
            try (CloseableHttpResponse response = httpClient.execute(get)) {
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 200) {
                    alreadyRunning = true;
                }
            }
        } catch (Exception e) {
            // Not running
        }

        if (!alreadyRunning) {
            server = new MCPServer();
            server.start(port);
            startedServer = true;
            // Give the server a moment to start
            Thread.sleep(500);
        }
    }


    @AfterClass
    public void stopServer() {
        if (startedServer && server != null) server.stop();
    }

    private String httpGet(String path) throws Exception {
        String url = "http://localhost:" + port + path;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(get)) {
                int responseCode = response.getStatusLine().getStatusCode();
                StringBuilder content = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent(), "utf-8"))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                }
                assertEquals(responseCode, 200, "Expected HTTP 200 for " + path + ", got " + responseCode + ": " + content);
                return content.toString();
            }
        }
    }

    private String httpPost(String path, String jsonBody) throws Exception {
        String url = "http://localhost:" + port + path;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            if (jsonBody != null) {
                post.setEntity(new StringEntity(jsonBody, "UTF-8"));
            }
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                int responseCode = response.getStatusLine().getStatusCode();
                StringBuilder content = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent(), "utf-8"))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                }
                assertEquals(responseCode, 200, "Expected HTTP 200 for POST " + path + ", got " + responseCode + ": " + content);
                return content.toString();
            }
        }
    }

    @Test
    public void testHealthEndpoint() throws Exception {
        String response = httpGet("/mcp/health");
        assertTrue(response.toLowerCase().contains("ok") || response.length() > 0, "Health endpoint should return OK or non-empty");
    }


    @Test
    public void testCompletionEndpoint() throws Exception {
        // Example payload, adjust as needed for your API
        String payload = "{\"prompt\":\"Hello, world!\"}";
        String response = httpPost("/mcp/completion", payload);
        assertTrue(response.length() > 0, "Completion endpoint should return non-empty response");
    }


    @Test
    public void testContextEndpoint() throws Exception {
        // Example payload, adjust as needed for your API
        String payload = "{\"contextId\":\"test\"}";
        String response = httpPost("/mcp/context", payload);
        assertTrue(response.length() > 0, "Context endpoint should return non-empty response");
    }

    @Test
    public void testContextListEndpoint() throws Exception {
        String response = httpGet("/mcp/context/list");
        assertTrue(response.length() > 0, "Context list endpoint should return non-empty response");
    }


    @Test
    public void testWorkflowEndpoint() throws Exception {
        // Example payload, adjust as needed for your API
        String payload = "{\"workflow\":\"test\"}";
        String response = httpPost("/mcp/workflow", payload);
        assertTrue(response.length() > 0, "Workflow endpoint should return non-empty response");
    }


    @Test
    public void testCorrectCodeEndpoint() throws Exception {
        // Example payload, adjust as needed for your API
        String payload = "{\"code\":\"System.out.println(\\\"Hello\\\");\"}";
        String response = httpPost("/mcp/correct-code", payload);
        assertTrue(response.length() > 0, "Correct code endpoint should return non-empty response");
    }


    @Test
    public void testGenerateAndRunSeleniumTestEndpoint() throws Exception {
        // Example payload for new endpoint structure
        String payload = "{\"webUrl\":\"https://k11softwaresolutions-platform.vercel.app/\",\"promptFile\":\"mcp-prompts/k11softwaresolutions/tests/logintest_creation_prompt.txt\"}";
        String response = httpPost("/mcp/generate-and-run-selenium-test", payload);
        assertTrue(response.length() > 0, "Generate and run selenium test endpoint should return non-empty response");
    }

   @Test
        public void testGeneratePageObjectEndpoint() throws Exception {
            // Use a minimal valid payload for page object generation
            String payload = "{\"webUrl\":\"https://k11softwaresolutions-platform.vercel.app/\",\"promptFile\":\"mcp-prompts/k11softwaresolutions/pageobjects/pageobject_creation_prompt_multi.txt\"}";
            String response = httpPost("/mcp/generate-page-object", payload);
            assertTrue(response.length() > 0, "Generate page object endpoint should return non-empty response");
        }

    @Test
    public void testGenerateAndRunPlaywrightTestEndpoint() throws Exception {
        // Example payload for new endpoint structure
        String payload = "{\"webUrl\":\"https://k11softwaresolutions-platform.vercel.app/\",\"promptFile\":\"mcp-prompts/k11softwaresolutions/tests/logintest_creation_prompt.txt\"}";
        String response = httpPost("/mcp/generate-and-run-playwright-test", payload);
        assertTrue(response.length() > 0, "Generate and run playwright test endpoint should return non-empty response");
    }
}
