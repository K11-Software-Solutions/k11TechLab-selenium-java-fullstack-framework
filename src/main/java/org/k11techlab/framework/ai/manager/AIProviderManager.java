package org.k11techlab.framework.ai.manager;

import org.k11techlab.framework.ai.llm.LLMInterface;
import org.k11techlab.framework.ai.ollama.OllamaClient;
import org.k11techlab.framework.ai.openai.OpenAIClient;
import org.k11techlab.framework.ai.llmstudio.LLMStudioClient;
import org.k11techlab.framework.ai.simple.SimpleAIClient;
import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import java.util.List;
import java.util.ArrayList;
import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;

/**
 * AI Provider Manager for Test Automation Framework
 * Manages multiple LLM providers with automatic fallback strategy
 * 
 * Supported Providers:
 * - Ollama (primary)
 * - LM Studio (secondary)
 * - Simple AI Client (fallback)
 * - OpenAI (if configured)
 * 
 * @author K11 Tech Lab
 * @version 1.0
 */
public class AIProviderManager {
        /**
         * Get the best available AI provider
         * Priority order is configurable via ai.provider.priority in config (e.g. OLLAMA,SIMPLE)
         */
        public LLMInterface getBestProvider() {
            if (currentProvider != null && currentProvider.isAvailable()) {
                return currentProvider;
            }

            Log.info("🔍 Searching for available AI providers...");
            System.out.println("🔍 Scanning AI providers for best available option...");

            // Read provider priority from config
            String priorityConfig = ConfigurationManager.getString("ai.provider.priority", "OLLAMA,SIMPLE");
            String[] priorities = priorityConfig.split(",");
            List<String> providerOrder = new ArrayList<>();
            for (String p : priorities) {
                String trimmed = p.trim().toUpperCase();
                if (!trimmed.isEmpty()) providerOrder.add(trimmed);
            }
            // Always ensure SIMPLE is last fallback
            if (!providerOrder.contains("SIMPLE")) providerOrder.add("SIMPLE");

            for (String provider : providerOrder) {
                try {
                    switch (provider) {
                        case "OPENAI": {
                            Log.info("Testing OpenAI availability...");
                            OpenAIClient openai = new OpenAIClient();
                            try {
                                if (openai.isAvailable()) {
                                    currentProvider = openai;
                                    currentProviderType = Provider.OPENAI;
                                    Log.info("✅ Using OpenAI as AI provider");
                                    System.out.println("✅ OpenAI connected successfully");
                                    return currentProvider;
                                } else {
                                    Log.info("❌ OpenAI not available");
                                    System.out.println("❌ OpenAI not available");
                                    openai.close();
                                }
                            } catch (Exception e) {
                                openai.close();
                                throw e;
                            }
                            break;
                        }
                        case "OLLAMA": {
                            Log.info("Testing Ollama availability...");
                            OllamaClient ollama = new OllamaClient();
                            try {
                                if (ollama.isAvailable()) {
                                    currentProvider = ollama;
                                    currentProviderType = Provider.OLLAMA;
                                    Log.info("✅ Using Ollama as AI provider");
                                    System.out.println("✅ Ollama AI connected successfully");
                                    return currentProvider;
                                } else {
                                    Log.info("❌ Ollama not available");
                                    System.out.println("❌ Ollama not available");
                                    ollama.close();
                                }
                            } catch (Exception e) {
                                ollama.close();
                                throw e;
                            }
                            break;
                        }
                        case "LLMSTUDIO": {
                            Log.info("Testing LM Studio availability...");
                            LLMStudioClient llmStudio = new LLMStudioClient();
                            try {
                                if (llmStudio.isAvailable()) {
                                    currentProvider = llmStudio;
                                    currentProviderType = Provider.LLMSTUDIO;
                                    Log.info("✅ Using LM Studio as AI provider");
                                    System.out.println("✅ LM Studio AI connected successfully");
                                    return currentProvider;
                                } else {
                                    Log.info("❌ LM Studio not available");
                                    System.out.println("❌ LM Studio not available");
                                    llmStudio.close();
                                }
                            } catch (Exception e) {
                                llmStudio.close();
                                throw e;
                            }
                            break;
                        }
                        case "SIMPLE": {
                            Log.info("Using Simple AI Client as fallback");
                            SimpleAIClient simpleAI = new SimpleAIClient();
                            try {
                                currentProvider = simpleAI;
                                currentProviderType = Provider.SIMPLE;
                                Log.info("✅ Using Simple AI Client as fallback");
                                System.out.println("🔄 Falling back to Simple AI Client");
                                return currentProvider;
                            } catch (Exception e) {
                                simpleAI.close();
                                throw e;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.info(provider + " initialization failed: " + e.getMessage());
                    System.out.println("⚠️ " + provider + " initialization failed: " + e.getMessage());
                }
            }

            Log.error("No AI providers available");
            System.out.println("❌ No AI providers available");
            return null;
        }
    public enum Provider {
        OLLAMA("Ollama"),
        LLMSTUDIO("LM Studio"),
        SIMPLE("Simple AI"),
        OPENAI("OpenAI"),
        RAG_ENHANCED("RAG-Enhanced AI");

        private final String displayName;

        Provider(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private LLMInterface currentProvider;
    private Provider currentProviderType;
    private final boolean enableFallback = true;
    private final boolean enableRAG = false;
// ...existing code...
// ...existing code...
    
    /**
     * Wrap an AI provider with RAG enhancement if enabled
     */
    private LLMInterface wrapWithRAG(LLMInterface baseProvider) {
        if (enableRAG && baseProvider != null) {
            Log.info("🧠 Wrapping provider with RAG enhancement");
            return new RAGEnhancedAIClient(baseProvider);
        }
        return baseProvider;
    }
    
    /**
     * Get RAG-enhanced provider (always wraps with RAG regardless of global setting)
     */
    public LLMInterface getRAGEnhancedProvider() {
        LLMInterface baseProvider = getBestProvider();
        if (baseProvider != null) {
            Log.info("🧠 Creating RAG-enhanced provider");
            return new RAGEnhancedAIClient(baseProvider);
        }
        return null;
    }
    
    /**
     * Get specific provider by type
     */
    public LLMInterface getProvider(Provider providerType) {
        Log.info("🎯 Requesting specific provider: " + providerType.getDisplayName());
        
        switch (providerType) {
            case OLLAMA:
                try {
                    OllamaClient ollama = new OllamaClient();
                    if (ollama.isAvailable()) {
                        currentProvider = ollama;
                        currentProviderType = providerType;
                        Log.info("✅ " + providerType.getDisplayName() + " provider ready");
                        return ollama;
                    } else {
                        Log.info("❌ " + providerType.getDisplayName() + " not available");
                        if (enableFallback) {
                            return getBestProvider();
                        }
                        return null;
                    }
                } catch (Exception e) {
                    Log.error("Ollama provider failed: " + e.getMessage());
                    return enableFallback ? getBestProvider() : null;
                }
                
            case LLMSTUDIO:
                try {
                    LLMStudioClient llmStudio = new LLMStudioClient();
                    if (llmStudio.isAvailable()) {
                        currentProvider = llmStudio;
                        currentProviderType = providerType;
                        Log.info("✅ " + providerType.getDisplayName() + " provider ready");
                        return llmStudio;
                    } else {
                        Log.info("❌ " + providerType.getDisplayName() + " not available");
                        if (enableFallback) {
                            return getBestProvider();
                        }
                        return null;
                    }
                } catch (Exception e) {
                    Log.error("LM Studio provider failed: " + e.getMessage());
                    return enableFallback ? getBestProvider() : null;
                }
                
            case SIMPLE:
                currentProvider = new SimpleAIClient();
                currentProviderType = providerType;
                Log.info("✅ " + providerType.getDisplayName() + " provider ready");
                return currentProvider;
                
            default:
                Log.error("Unknown provider type: " + providerType);
                return enableFallback ? getBestProvider() : null;
        }
    }
    
    /**
     * Get current provider information
     */
    public String getCurrentProviderInfo() {
        if (currentProvider == null) {
            return "No provider selected";
        }
        
        String status = currentProvider.isAvailable() ? "✅ Available" : "❌ Unavailable";
        return String.format("%s - %s", currentProviderType.getDisplayName(), status);
    }
    
    /**
     * Get current provider type
     */
    public Provider getCurrentProviderType() {
        return currentProviderType;
    }
    
    /**
     * Force refresh of current provider
     */
    public void refreshProvider() {
        Log.info("🔄 Refreshing AI provider...");
        currentProvider = null;
        currentProviderType = null;
        getBestProvider();
    }
    
    /**
     * Get comprehensive diagnostics for all providers
     */
    public String getAllProviderDiagnostics() {
        StringBuilder diagnostics = new StringBuilder();
        diagnostics.append("=== AI PROVIDER MANAGER DIAGNOSTICS ===\n");
        diagnostics.append("Current Provider: ").append(getCurrentProviderInfo()).append("\n");
        diagnostics.append("Fallback Enabled: ").append(enableFallback).append("\n\n");
        
        // Test Ollama
        diagnostics.append("--- OLLAMA STATUS ---\n");
        try {
            OllamaClient ollama = new OllamaClient();
            diagnostics.append("Available: ").append(ollama.isAvailable() ? "✅" : "❌").append("\n");
            if (ollama instanceof org.k11techlab.framework.ai.ollama.OllamaClient) {
                diagnostics.append(((org.k11techlab.framework.ai.ollama.OllamaClient) ollama).getDiagnostics()).append("\n");
            }
        } catch (Exception e) {
            diagnostics.append("Error: ").append(e.getMessage()).append("\n");
        }
        
        diagnostics.append("\n--- LM STUDIO STATUS ---\n");
        try {
            LLMStudioClient llmStudio = new LLMStudioClient();
            diagnostics.append("Available: ").append(llmStudio.isAvailable() ? "✅" : "❌").append("\n");
            diagnostics.append(llmStudio.getDiagnostics()).append("\n");
        } catch (Exception e) {
            diagnostics.append("Error: ").append(e.getMessage()).append("\n");
        }
        
        diagnostics.append("\n--- SIMPLE AI STATUS ---\n");
        try {
            SimpleAIClient simple = new SimpleAIClient();
            diagnostics.append("Available: ").append(simple.isAvailable() ? "✅" : "❌").append("\n");
        } catch (Exception e) {
            diagnostics.append("Error: ").append(e.getMessage()).append("\n");
        }
        
        diagnostics.append("\n========================================");
        return diagnostics.toString();
    }
    
    /**
     * Close current provider resources
     */
    public void close() {
        if (currentProvider != null && currentProvider instanceof AutoCloseable) {
            try {
                ((AutoCloseable) currentProvider).close();
                Log.info("🔒 AI provider resources closed");
            } catch (Exception e) {
                Log.info("Error closing AI provider: " + e.getMessage());
            }
        }
        currentProvider = null;
        currentProviderType = null;
    }
}