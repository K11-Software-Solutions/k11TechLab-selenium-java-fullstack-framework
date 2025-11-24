package org.k11techlab.framework.ai.llmstudio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.k11techlab.framework.ai.llm.LLMInterface;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * LLM Studio Client for AI-powered test automation
 * Provides integration with LM Studio local LLM server
 * 
 * Features:
 * - Multiple model support
 * - Configurable parameters
 * - Robust error handling
 * - Health check diagnostics
 * 
 * @author K11 TechLab
 * @version 1.0
 */
public class LLMStudioClient implements LLMInterface {
    
    private final String baseUrl;
    private final String model;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final int timeout;
    
    /**
     * Constructor with default configuration
     */
    public LLMStudioClient() {
        this("http://localhost:1234", "local-model", 90);
    }
    
    /**
     * Constructor with custom configuration
     * @param baseUrl LM Studio server URL (default: http://localhost:1234)
     * @param model Model name (default: local-model)
     * @param timeoutSeconds Request timeout in seconds
     */
    public LLMStudioClient(String baseUrl, String model, int timeoutSeconds) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.model = model;
        this.timeout = timeoutSeconds;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        
        Log.info("🎯 LM Studio Client initialized - URL: " + this.baseUrl + ", Model: " + this.model);
    }
    
    @Override
    public String generateResponse(String prompt) {
        return generateResponse(prompt, 0.7f, 200);
    }
    
    @Override
    public String generateResponse(String prompt, float temperature, int maxTokens) {
        try {
            Log.info("📨 Sending request to LM Studio (max " + timeout + "s)...");
            System.out.println("🔮 LM Studio generating response for prompt: " + 
                (prompt.length() > 100 ? prompt.substring(0, 100) + "..." : prompt));
            
            String response = callLMStudio(prompt, temperature, maxTokens);
            
            if (response != null && !response.trim().isEmpty()) {
                System.out.println("✅ LM Studio response generated successfully");
                System.out.println("🤖 Response preview: " + 
                    (response.length() > 150 ? response.substring(0, 150) + "..." : response));
                return response;
            } else {
                Log.info("Empty response from LM Studio");
                return "// LM Studio returned empty response for: " + prompt;
            }
            
        } catch (Exception e) {
            Log.error("LM Studio generation failed: " + e.getMessage());
            System.out.println("❌ LM Studio generation failed: " + e.getMessage());
            return "// LM Studio unavailable: " + prompt + "\n// Error: " + e.getMessage();
        }
    }
    
    /**
     * Generate AI suggestion for Selenium testing
     */
    public String generateSuggestion(String prompt) {
        try {
            Log.info("Sending request to LM Studio (max " + timeout + "s)...");
            String enhancedPrompt = "As a Selenium test automation expert, " + prompt + 
                                  " Provide clear, actionable advice. Keep response under 200 words and be specific.";
            
            return callLMStudio(enhancedPrompt, 0.1f, 150);
        } catch (Exception e) {
            Log.info("LM Studio generation failed, using fallback: " + e.getMessage());
            return "// LM Studio unavailable: " + prompt + "\n// Error: " + e.getMessage();
        }
    }
    
    /**
     * Generate locator suggestions for web elements
     */
    public String generateLocatorSuggestion(String elementDescription) {
        String prompt = "Generate 3 robust Selenium locators for " + elementDescription + 
                       ": By.id(), By.cssSelector(), By.xpath(). Show code examples only.";
        
        return generateSuggestion(prompt);
    }
    
    /**
     * Generate test case suggestions
     */
    public String generateTestSuggestion(String testScenario) {
        String prompt = "Create a complete Selenium WebDriver test method for: " + testScenario + 
                       ". Include setup, actions, assertions, and proper waits. Use Java and TestNG annotations.";
        
        return generateSuggestion(prompt);
    }
    
    /**
     * Main LM Studio API call method
     */
    private String callLMStudio(String prompt, float temperature, int maxTokens) throws IOException {
        HttpPost post = new HttpPost(baseUrl + "/v1/chat/completions");
        
        // Build OpenAI-compatible request format (LM Studio uses OpenAI API format)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        // Messages format for chat completion
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        requestBody.put("messages", new Map[]{message});
        
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("stream", false);
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        post.setEntity(new StringEntity(jsonBody));
        post.setHeader("Content-Type", "application/json");
        
        // Set request timeout
        org.apache.hc.client5.http.config.RequestConfig requestConfig = 
            org.apache.hc.client5.http.config.RequestConfig.custom()
                .setConnectionRequestTimeout(20, TimeUnit.SECONDS)
                .setResponseTimeout(timeout, TimeUnit.SECONDS)
                .build();
        post.setConfig(requestConfig);
        
        HttpClientResponseHandler<String> responseHandler = response -> {
            int status = response.getCode();
            if (status >= 200 && status < 300) {
                try {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);
                    
                    // Extract content from OpenAI format response
                    JsonNode choices = jsonResponse.get("choices");
                    if (choices != null && choices.isArray() && choices.size() > 0) {
                        JsonNode firstChoice = choices.get(0);
                        JsonNode messageNode = firstChoice.get("message");
                        if (messageNode != null) {
                            JsonNode content = messageNode.get("content");
                            if (content != null) {
                                return content.asText().trim();
                            }
                        }
                    }
                    
                    return "// LM Studio response parsing failed";
                } catch (Exception e) {
                    return "// LM Studio response error: " + e.getMessage();
                }
            } else {
                return "// LM Studio HTTP error: " + status;
            }
        };
        
        return httpClient.execute(post, responseHandler);
    }
    
    @Override
    public boolean isAvailable() {
        try {
            Log.info("🔍 LM Studio availability check starting...");
            System.out.println("🔍 Testing LM Studio connection to: " + baseUrl + " with model: " + model);
            
            // First, test basic connectivity to server
            if (!testBasicConnectivity()) {
                System.out.println("❌ Basic connectivity failed to LM Studio service");
                return false;
            }
            
            // Then test model availability
            if (!testModelAvailability()) {
                System.out.println("❌ Model '" + model + "' not available in LM Studio");
                return false;
            }
            
            // Finally, test actual generation
            Log.info("Quick LM Studio availability check for model: " + model);
            String response = generateResponse("Respond with just: READY", 0.1f, 10);
            boolean available = response != null && !response.trim().isEmpty() && response.length() > 3;
            
            if (available) {
                Log.info("✅ LM Studio availability: " + available + " (model: " + model + ")");
                System.out.println("✅ LM Studio AI fully operational with model: " + model);
                System.out.println("🤖 LM Studio Response Preview: " + 
                    (response.length() > 100 ? response.substring(0, 100) + "..." : response));
            } else {
                Log.info("❌ LM Studio generation test failed (model: " + model + ")");
                System.out.println("❌ LM Studio generation test failed - response: " + response);
            }
            
            return available;
            
        } catch (Exception e) {
            Log.error("LM Studio availability check failed: " + e.getMessage());
            System.out.println("❌ LM Studio availability check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Test basic connectivity to LM Studio server
     */
    public boolean testBasicConnectivity() {
        try {
            HttpGet get = new HttpGet(baseUrl + "/v1/models");
            
            HttpClientResponseHandler<Boolean> responseHandler = response -> {
                int status = response.getCode();
                System.out.println("🔗 LM Studio connectivity test - Status: " + status);
                return status >= 200 && status < 300;
            };
            
            return httpClient.execute(get, responseHandler);
            
        } catch (Exception e) {
            System.out.println("❌ LM Studio connectivity failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Test if specified model is available
     */
    public boolean testModelAvailability() {
        try {
            HttpGet get = new HttpGet(baseUrl + "/v1/models");
            
            HttpClientResponseHandler<Boolean> responseHandler = response -> {
                try {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    System.out.println("📋 Available models response: " + responseBody);
                    
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);
                    JsonNode data = jsonResponse.get("data");
                    
                    if (data != null && data.isArray()) {
                        for (JsonNode modelNode : data) {
                            String modelId = modelNode.get("id").asText();
                            System.out.println("📦 Found model: " + modelId);
                            if (modelId.equals(model) || modelId.contains(model) || model.equals("local-model")) {
                                System.out.println("✅ Target model '" + model + "' found!");
                                return true;
                            }
                        }
                    }
                    
                    System.out.println("⚠️ Target model '" + model + "' not found in available models");
                    return false;
                    
                } catch (Exception e) {
                    System.out.println("❌ Model availability check failed: " + e.getMessage());
                    return false;
                }
            };
            
            return httpClient.execute(get, responseHandler);
            
        } catch (Exception e) {
            System.out.println("❌ Model availability test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get detailed diagnostics information
     */
    public String getDiagnostics() {
        StringBuilder diagnostics = new StringBuilder();
        diagnostics.append("=== LM STUDIO CLIENT DIAGNOSTICS ===\n");
        diagnostics.append("Base URL: ").append(baseUrl).append("\n");
        diagnostics.append("Target Model: ").append(model).append("\n");
        diagnostics.append("Timeout: ").append(timeout).append(" seconds\n");
        
        try {
            boolean connectivity = testBasicConnectivity();
            diagnostics.append("Basic Connectivity: ").append(connectivity ? "✅ OK" : "❌ FAILED").append("\n");
            
            boolean modelAvailable = testModelAvailability();
            diagnostics.append("Model Available: ").append(modelAvailable ? "✅ OK" : "❌ FAILED").append("\n");
            
        } catch (Exception e) {
            diagnostics.append("Diagnostics Error: ").append(e.getMessage()).append("\n");
        }
        
        diagnostics.append("=====================================");
        return diagnostics.toString();
    }
    
    /**
     * Get status information for logging
     */
    public String getStatus() {
        return String.format("LM Studio Client - URL: %s, Model: %s, Available: %s", 
                           baseUrl, model, isAvailable() ? "✅" : "❌");
    }
    
    @Override
    public void close() {
        if (httpClient != null) {
            try {
                httpClient.close();
                Log.info("🔒 LM Studio client connection closed");
            } catch (IOException e) {
                Log.error("Error closing LM Studio client: " + e.getMessage());
            }
        }
    }
    
    @Override
    public String getModelInfo() {
        return String.format("LM Studio - Model: %s, URL: %s", model, baseUrl);
    }
}