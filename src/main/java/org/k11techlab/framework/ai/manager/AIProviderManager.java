package org.k11techlab.framework.ai.manager;

import org.k11techlab.framework.ai.llm.LLMInterface;
import org.k11techlab.framework.ai.ollama.OllamaClient;
import org.k11techlab.framework.ai.llmstudio.LLMStudioClient;
import org.k11techlab.framework.ai.simple.SimpleAIClient;
import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;

/**
 * AI Provider Manager for Test Automation Framework
 * Manages multiple LLM providers with automatic fallback strategy
 * 
 * Supported Providers:
 * - Ollama (primary)
 * - LM Studio (secondary) 
 * - Simple AI Client (fallback)
 * 
 * @author K11 TechLab
 * @version 1.0
 */
public class AIProviderManager {
    
    public enum Provider {
        OLLAMA("Ollama"),
        LLMSTUDIO("LM Studio"),  
        SIMPLE("Simple AI"),
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
    private final boolean enableFallback;
    private final boolean enableRAG;
    
    /**
     * Constructor with fallback enabled by default
     */
    public AIProviderManager() {
        this(true);
    }
    
    /**
     * Constructor with configurable fallback
     * @param enableFallback Whether to enable automatic fallback to other providers
     */
    public AIProviderManager(boolean enableFallback) {
        this(enableFallback, false);
    }
    
    /**
     * Constructor with configurable fallback and RAG enhancement
     * @param enableFallback Whether to enable automatic fallback to other providers
     * @param enableRAG Whether to enable RAG enhancement for better context-aware responses
     */
    public AIProviderManager(boolean enableFallback, boolean enableRAG) {
        this.enableFallback = enableFallback;
        this.enableRAG = enableRAG;
        this.currentProvider = null;
        this.currentProviderType = null;
        
        Log.info("🤖 AI Provider Manager initialized (fallback: " + enableFallback + ", RAG: " + enableRAG + ")");
    }
    
    /**
     * Get the best available AI provider
     * Priority order: Ollama -> LM Studio -> Simple AI
     */
    public LLMInterface getBestProvider() {
        if (currentProvider != null && currentProvider.isAvailable()) {
            return currentProvider;
        }
        
        Log.info("🔍 Searching for available AI providers...");
        System.out.println("🔍 Scanning AI providers for best available option...");
        
        // Try Ollama first (primary choice)
        try {
            Log.info("Testing Ollama availability...");
            OllamaClient ollama = new OllamaClient();
            if (ollama.isAvailable()) {
                currentProvider = wrapWithRAG(ollama);
                currentProviderType = enableRAG ? Provider.RAG_ENHANCED : Provider.OLLAMA;
                Log.info("✅ Using Ollama as primary AI provider" + (enableRAG ? " with RAG enhancement" : ""));
                System.out.println("✅ Ollama AI connected successfully" + (enableRAG ? " (RAG-enhanced)" : ""));
                return currentProvider;
            } else {
                Log.info("❌ Ollama not available");
                System.out.println("❌ Ollama not available");
                ollama.close(); // Fix resource leak
            }
        } catch (Exception e) {
            Log.info("Ollama initialization failed: " + e.getMessage());
            System.out.println("⚠️ Ollama initialization failed: " + e.getMessage());
        }
        
        // Try LM Studio second
        try {
            Log.info("Testing LM Studio availability...");
            LLMStudioClient llmStudio = new LLMStudioClient();
            if (llmStudio.isAvailable()) {
                currentProvider = wrapWithRAG(llmStudio);
                currentProviderType = enableRAG ? Provider.RAG_ENHANCED : Provider.LLMSTUDIO;
                Log.info("✅ Using LM Studio as secondary AI provider" + (enableRAG ? " with RAG enhancement" : ""));
                System.out.println("✅ LM Studio AI connected successfully" + (enableRAG ? " (RAG-enhanced)" : ""));
                return currentProvider;
            } else {
                Log.info("❌ LM Studio not available");
                System.out.println("❌ LM Studio not available");
                llmStudio.close(); // Fix resource leak
            }
        } catch (Exception e) {
            Log.info("LM Studio initialization failed: " + e.getMessage());
            System.out.println("⚠️ LM Studio initialization failed: " + e.getMessage());
        }
        
        // Use Simple AI as fallback if enabled
        if (enableFallback) {
            Log.info("Using Simple AI Client as fallback");
            System.out.println("🔄 Falling back to Simple AI Client" + (enableRAG ? " (RAG-enhanced)" : ""));
            SimpleAIClient simpleAI = new SimpleAIClient();
            currentProvider = wrapWithRAG(simpleAI);
            currentProviderType = enableRAG ? Provider.RAG_ENHANCED : Provider.SIMPLE;
            return currentProvider;
        }
        
        Log.error("No AI providers available");
        System.out.println("❌ No AI providers available");
        return null;
    }
    
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