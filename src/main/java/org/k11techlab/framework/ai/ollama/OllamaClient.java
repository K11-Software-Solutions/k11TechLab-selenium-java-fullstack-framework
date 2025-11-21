package org.k11techlab.framework.ai.ollama;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpGet;
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
     * Test connection to Ollama with enhanced diagnostics
     */
    public boolean isAvailable() {
        try {
            Log.info("🔍 AI availability check starting...");
            System.out.println("🔍 Testing Ollama connection to: " + baseUrl + " with model: " + model);
            
            // First, test basic connectivity
            if (!testBasicConnectivity()) {
                System.out.println("❌ Basic connectivity failed to Ollama service");
                return false;
            }
            
            // Then test model availability
            if (!testModelAvailability()) {
                System.out.println("❌ Model '" + model + "' not available");
                return false;
            }
            
            // Finally, test actual generation
            Log.info("Quick AI availability check for model: " + model);
            String response = callOllama("Respond with just: READY");
            boolean available = response != null && !response.trim().isEmpty() && response.length() > 3;
            
            if (available) {
                Log.info("✅ AI availability: " + available + " (model: " + model + ")");
                System.out.println("✅ Ollama AI fully operational with model: " + model);
                System.out.println("🤖 AI Response Preview: " + 
                    (response.length() > 100 ? response.substring(0, 100) + "..." : response));
            } else {
                Log.info("❌ AI generation test failed (model: " + model + ")");
                System.out.println("❌ AI generation test failed - response: " + response);
            }
            
            return available;
        } catch (Exception e) {
            String error = "AI not available: " + e.getMessage() + " (model: " + model + ")";
            Log.info(error);
            System.out.println("❌ " + error);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Test basic connectivity to Ollama service
     */
    private boolean testBasicConnectivity() {
        try {
            HttpGet get = new HttpGet(baseUrl + "/api/tags");
            
            org.apache.hc.client5.http.config.RequestConfig requestConfig = 
                org.apache.hc.client5.http.config.RequestConfig.custom()
                    .setConnectionRequestTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .setResponseTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build();
            get.setConfig(requestConfig);
            
            HttpClientResponseHandler<Boolean> responseHandler = response -> {
                int status = response.getCode();
                System.out.println("🔗 Ollama connectivity test - Status: " + status);
                return status >= 200 && status < 300;
            };
            
            return httpClient.execute(get, responseHandler);
        } catch (Exception e) {
            System.out.println("🔗 Connectivity test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Test if the specified model is available
     */
    private boolean testModelAvailability() {
        try {
            HttpGet get = new HttpGet(baseUrl + "/api/tags");
            
            HttpClientResponseHandler<Boolean> responseHandler = response -> {
                try {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    System.out.println("📋 Available models response: " + responseBody);
                    
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);
                    JsonNode models = jsonResponse.get("models");
                    
                    if (models != null && models.isArray()) {
                        for (JsonNode modelNode : models) {
                            String modelName = modelNode.get("name").asText();
                            System.out.println("📦 Found model: " + modelName);
                            if (modelName.startsWith(model)) {
                                System.out.println("✅ Target model '" + model + "' found!");
                                return true;
                            }
                        }
                    }
                    
                    System.out.println("❌ Target model '" + model + "' not found in available models");
                    return false;
                    
                } catch (Exception e) {
                    System.out.println("❌ Failed to parse models response: " + e.getMessage());
                    return false;
                }
            };
            
            return httpClient.execute(get, responseHandler);
        } catch (Exception e) {
            System.out.println("📋 Model availability test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get client status with detailed information
     */
    public String getStatus() {
        try {
            boolean available = isAvailable();
            return String.format("OllamaClient - URL: %s, Model: %s, Available: %s", 
                baseUrl, model, available);
        } catch (Exception e) {
            return String.format("OllamaClient - URL: %s, Model: %s, Error: %s", 
                baseUrl, model, e.getMessage());
        }
    }
    
    /**
     * Get detailed diagnostic information
     */
    public String getDiagnostics() {
        StringBuilder diag = new StringBuilder();
        diag.append("=== OLLAMA CLIENT DIAGNOSTICS ===\n");
        diag.append("Base URL: ").append(baseUrl).append("\n");
        diag.append("Target Model: ").append(model).append("\n");
        
        try {
            diag.append("Basic Connectivity: ").append(testBasicConnectivity() ? "✅ OK" : "❌ FAILED").append("\n");
            diag.append("Model Available: ").append(testModelAvailability() ? "✅ OK" : "❌ FAILED").append("\n");
        } catch (Exception e) {
            diag.append("Diagnostics Error: ").append(e.getMessage()).append("\n");
        }
        
        diag.append("===============================");
        return diag.toString();
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