package org.k11techlab.framework.ai.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Client for interacting with Ollama local LLM server.
 * Ollama runs on localhost:11434 by default.
 */
public class OllamaClient implements LLMInterface {
    
    private static final String DEFAULT_OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String DEFAULT_MODEL = "llama3";
    
    private final String ollamaUrl;
    private final String model;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    
    public OllamaClient() {
        this(DEFAULT_OLLAMA_URL, DEFAULT_MODEL);
    }
    
    public OllamaClient(String ollamaUrl, String model) {
        this.ollamaUrl = ollamaUrl;
        this.model = model;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClients.createDefault();
    }
    
    @Override
    public String generateResponse(String prompt) {
        return generateResponse(prompt, 0.7f, 2000);
    }
    
    @Override
    public String generateResponse(String prompt, float temperature, int maxTokens) {
        try {
            // Build request payload for Ollama
            String requestPayload = buildOllamaRequest(prompt, temperature, maxTokens);
            
            // Create HTTP request
            HttpPost httpPost = new HttpPost(ollamaUrl);
            httpPost.setEntity(new StringEntity(requestPayload, StandardCharsets.UTF_8));
            httpPost.setHeader("Content-Type", "application/json");
            
            Log.info("Sending request to Ollama: " + model);
            
            // Execute request
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                return parseOllamaResponse(responseBody);
            }
            
        } catch (IOException e) {
            Log.error("Error communicating with Ollama: " + e.getMessage());
            return "Error: Could not connect to Ollama. Please ensure Ollama is running and the model is available.";
        }
    }
    
    private String buildOllamaRequest(String prompt, float temperature, int maxTokens) throws IOException {
        // Ollama request format
        ObjectMapper mapper = new ObjectMapper();
        var requestNode = mapper.createObjectNode();
        requestNode.put("model", model);
        requestNode.put("prompt", prompt);
        requestNode.put("stream", false);
        
        var options = mapper.createObjectNode();
        options.put("temperature", temperature);
        options.put("num_predict", maxTokens);
        requestNode.set("options", options);
        
        return mapper.writeValueAsString(requestNode);
    }
    
    private String parseOllamaResponse(String responseBody) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        
        if (jsonNode.has("error")) {
            String error = jsonNode.get("error").asText();
            Log.error("Ollama returned error: " + error);
            return "Error: " + error;
        }
        
        if (jsonNode.has("response")) {
            return jsonNode.get("response").asText();
        }
        
        Log.info("Unexpected Ollama response format: " + responseBody);
        return "Error: Unexpected response format from Ollama";
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // Test connectivity with a simple request
            String testPrompt = "Hello";
            String response = generateResponse(testPrompt);
            return !response.startsWith("Error:");
        } catch (Exception e) {
            Log.info("Ollama availability check failed: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getModelInfo() {
        return String.format("Ollama Model: %s (URL: %s)", model, ollamaUrl);
    }
    
    /**
     * Check if Ollama server is running
     * @return true if server responds to ping
     */
    public boolean isServerRunning() {
        try {
            HttpPost httpPost = new HttpPost("http://localhost:11434/api/tags");
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return response.getCode() == 200;
            }
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * List available models in Ollama
     * @return JSON string with available models
     */
    public String listAvailableModels() {
        try {
            HttpPost httpPost = new HttpPost("http://localhost:11434/api/tags");
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            Log.error("Error listing Ollama models: " + e.getMessage());
            return "{}";
        }
    }
    
    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            Log.info("Error closing HTTP client: " + e.getMessage());
        }
    }
}