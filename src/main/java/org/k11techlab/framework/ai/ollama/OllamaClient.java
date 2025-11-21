package org.k11techlab.framework.ai.ollama;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Ollama client for local LLM integration with K11 TechLab framework.
 * Provides AI-powered test generation, locator suggestions, and debugging assistance.
 */
public class OllamaClient {
    
    private final String baseUrl;
    private final String model;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public OllamaClient(String baseUrl, String model) {
        this.baseUrl = baseUrl;
        // Allow model override from system properties (useful for CI)
        this.model = System.getProperty("ai.model", model);
        // Create HTTP client with longer timeouts for LLM responses
        this.httpClient = HttpClients.custom()
            .setDefaultRequestConfig(
                org.apache.hc.client5.http.config.RequestConfig.custom()
                    .setConnectionRequestTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .setResponseTimeout(90, java.util.concurrent.TimeUnit.SECONDS) // Increased to 90s
                    .build()
            )
            .build();
        this.objectMapper = new ObjectMapper();
        Log.info("OllamaClient initialized with model: " + this.model + " (90s timeout)");
    }
    
    /**
     * Default constructor using system properties or defaults
     */
    public OllamaClient() {
        this(System.getProperty("ollama.host", "http://localhost:11434"), 
             System.getProperty("ai.model", "llama3"));
    }
    
    /**
     * Generate AI suggestion based on prompt
     */
    public String generateSuggestion(String prompt) {
        try {
            Log.info("Sending request to Ollama (max 90s)...");
            // Shorter, more focused prompt for faster response
            String enhancedPrompt = "As a Selenium expert, " + prompt + " Keep response under 200 words and be specific.";
            
            return callOllama(enhancedPrompt);
        } catch (Exception e) {
            Log.info("AI generation failed, using fallback: " + e.getMessage());
            return "// AI unavailable: " + prompt + "\n// Error: " + e.getMessage();
        }
    }
    
    /**
     * Generate locator suggestions for web elements
     */
    public String generateLocatorSuggestion(String elementDescription) {
        String prompt = "Generate 3 Selenium locators for " + elementDescription + 
                       ": By.id(), By.cssSelector(), By.xpath(). Show code only.";
        
        return generateSuggestion(prompt);
    }
    
    /**
     * Generate test case suggestions
     */
    public String generateTestSuggestion(String testScenario) {
        String prompt = "Generate TestNG test for " + testScenario + 
                       ". Include @Test, findElement, click, assert. Show code only.";
        
        return generateSuggestion(prompt);
    }
    
    /**
     * Generate debugging suggestions for test failures
     */
    public String generateDebugSuggestion(String errorMessage, String context) {
        String prompt = "Fix Selenium error: " + errorMessage.substring(0, Math.min(100, errorMessage.length())) + 
                       ". Context: " + context + ". Give 3 solutions.";
        
        return generateSuggestion(prompt);
    }
    
    /**
     * Call Ollama API
     */
    private String callOllama(String prompt) throws IOException {
        HttpPost post = new HttpPost(baseUrl + "/api/generate");
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        // Optimize for speed and conciseness
        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.1); // Lower for more focused responses
        options.put("top_p", 0.8);
        options.put("num_predict", 150); // Shorter responses for speed
        options.put("repeat_penalty", 1.1);
        requestBody.put("options", options);
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        post.setEntity(new StringEntity(jsonBody));
        post.setHeader("Content-Type", "application/json");
        
        // Set longer request timeout for LLM processing
        org.apache.hc.client5.http.config.RequestConfig requestConfig = 
            org.apache.hc.client5.http.config.RequestConfig.custom()
                .setConnectionRequestTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                .setResponseTimeout(90, java.util.concurrent.TimeUnit.SECONDS) // Match client timeout
                .build();
        post.setConfig(requestConfig);
        
        HttpClientResponseHandler<String> responseHandler = response -> {
            int status = response.getCode();
            if (status >= 200 && status < 300) {
                try {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);
                    return jsonResponse.get("response").asText();
                } catch (ParseException e) {
                    throw new IOException("Failed to parse response", e);
                }
            } else {
                throw new IOException("HTTP error code: " + status);
            }
        };
        
        return httpClient.execute(post, responseHandler);
    }
    
    /**
     * Test connection to Ollama
     */
    public boolean isAvailable() {
        try {
            Log.info("Quick AI availability check for model: " + model);
            // Quick test with very short prompt
            String response = callOllama("Say OK");
            boolean available = response != null && response.toLowerCase().contains("ok");
            Log.info("AI availability: " + available + " (model: " + model + ")");
            return available;
        } catch (Exception e) {
            Log.info("AI not available: " + e.getMessage() + " (model: " + model + ")");
            return false;
        }
    }
    
    /**
     * Get client status
     */
    public String getStatus() {
        return "OllamaClient - Model: " + model + ", Available: " + isAvailable();
    }
    
    /**
     * Close HTTP client resources
     */
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}