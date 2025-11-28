package org.k11techlab.framework_unittests.aiTests;

import org.k11techlab.framework.selenium.webuitestbase.BaseSeleniumTest;
import org.k11techlab.framework.ai.manager.AIProviderManager;
import org.k11techlab.framework.ai.llm.LLMInterface;
import org.k11techlab.framework.ai.ollama.OllamaClient;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

/**
 * AI Provider Diagnostics Test
 * Comprehensive testing and debugging for AI provider connectivity
 * 
 * @author K11 TechLab
 * @version 1.0
 */
public class AIProviderDiagnosticsTest extends BaseSeleniumTest {
    
    private AIProviderManager aiManager;
    
    @BeforeClass
    public void setupDiagnostics() {
        System.out.println("\n🔧 AI Provider Diagnostics Starting...");
        Log.info("Starting comprehensive AI provider diagnostics");
    }
    
    @Test(priority = 1, description = "Test System Properties and Environment")
    public void testSystemConfiguration() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔧 SYSTEM CONFIGURATION DIAGNOSTICS");
        System.out.println("=".repeat(60));
        
        // Environment variables
        System.out.println("📋 Environment Variables:");
        System.out.println("  CI: " + System.getenv("CI"));
        System.out.println("  OLLAMA_HOST: " + System.getenv("OLLAMA_HOST"));
        System.out.println("  OLLAMA_MODEL: " + System.getenv("OLLAMA_MODEL"));
        System.out.println("  OLLAMA_AVAILABLE: " + System.getenv("OLLAMA_AVAILABLE"));
        
        // System properties
        System.out.println("\n⚙️ Java System Properties:");
        System.out.println("  ai.test.mode: " + System.getProperty("ai.test.mode", "not set"));
        System.out.println("  ai.provider: " + System.getProperty("ai.provider", "not set"));
        System.out.println("  ai.model: " + System.getProperty("ai.model", "not set"));
        System.out.println("  ai.ollama.url: " + System.getProperty("ai.ollama.url", "not set"));
        
        System.out.println("✅ System configuration check completed");
    }
    
    @Test(priority = 2, description = "Test Ollama Direct Connection")
    public void testOllamaDirectConnection() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🦙 OLLAMA DIRECT CONNECTION TEST");
        System.out.println("=".repeat(60));
        
        try {
            // Test with system properties if available
            String ollamaUrl = System.getProperty("ai.ollama.url", "http://localhost:11434");
            String model = System.getProperty("ai.model", "tinyllama:1.1b");
            
            System.out.println("🔗 Testing Ollama connection:");
            System.out.println("  URL: " + ollamaUrl);
            System.out.println("  Model: " + model);
            
            OllamaClient ollama = new OllamaClient(ollamaUrl, model);
            
            // Test availability
            System.out.println("🔍 Testing Ollama availability...");
            boolean available = ollama.isAvailable();
            System.out.println("  Available: " + (available ? "✅ YES" : "❌ NO"));
            
            if (available) {
                // Test model info
                System.out.println("📦 Testing model info...");
                String modelInfo = ollama.getModelInfo();
                System.out.println("  Model Info: " + modelInfo);
                
                // Test simple generation
                System.out.println("🤖 Testing response generation...");
                String response = ollama.generateResponse("Say 'Hello from Ollama'");
                System.out.println("  Response: " + (response != null && !response.trim().isEmpty() ? "✅ SUCCESS" : "❌ FAILED"));
                System.out.println("  Response Preview: " + (response != null ? response.substring(0, Math.min(100, response.length())) : "null"));
                
            } else {
                System.out.println("❌ Ollama not available - cannot perform further tests");
                
                // Try to diagnose the issue
                System.out.println("\n🔍 Diagnosing connection issues...");
                try {
                    java.net.URL url = new java.net.URL(ollamaUrl + "/api/tags");
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("GET");
                    int responseCode = conn.getResponseCode();
                    System.out.println("  HTTP Response Code: " + responseCode);
                } catch (Exception e) {
                    System.out.println("  Connection Error: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Ollama connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test(priority = 3, description = "Test AI Provider Manager")
    public void testAIProviderManager() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🤖 AI PROVIDER MANAGER TEST");
        System.out.println("=".repeat(60));
        
        try {
            System.out.println("🔧 Initializing AI Provider Manager...");
            aiManager = new AIProviderManager(); // Enable fallback for CI
            
            // Test best provider selection
            System.out.println("🎯 Testing best provider selection...");
            LLMInterface bestProvider = aiManager.getBestProvider();
            
            if (bestProvider != null) {
                System.out.println("✅ Best provider found: " + bestProvider.getModelInfo());
                
                // Test availability
                boolean available = bestProvider.isAvailable();
                System.out.println("  Available: " + (available ? "✅ YES" : "❌ NO"));
                
                if (available) {
                    // Test response generation
                    System.out.println("🧪 Testing response generation...");
                    String response = bestProvider.generateResponse("Generate 3 Selenium locators for a submit button");
                    System.out.println("  Response received: " + (response != null && !response.trim().isEmpty() ? "✅ SUCCESS" : "❌ FAILED"));
                    
                    if (response != null && response.length() > 0) {
                        System.out.println("  Response preview (first 200 chars):");
                        System.out.println("  " + response.substring(0, Math.min(200, response.length())));
                    }
                }
            } else {
                System.out.println("❌ No AI provider available");
            }
            
            // Test specific Ollama provider
            System.out.println("\n🦙 Testing specific Ollama provider...");
            LLMInterface ollamaProvider = aiManager.getProvider(AIProviderManager.Provider.OLLAMA);
            if (ollamaProvider != null) {
                System.out.println("✅ Ollama provider obtained");
                System.out.println("  Available: " + (ollamaProvider.isAvailable() ? "✅ YES" : "❌ NO"));
            } else {
                System.out.println("❌ Ollama provider not available");
            }
            
        } catch (Exception e) {
            System.out.println("❌ AI Provider Manager test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test(priority = 4, description = "Test Fallback Behavior")
    public void testFallbackBehavior() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🛡️ FALLBACK BEHAVIOR TEST");
        System.out.println("=".repeat(60));
        
        try {
            System.out.println("🔧 Testing fallback behavior with all providers...");
            
            // Create manager with fallback enabled
            AIProviderManager fallbackManager = new AIProviderManager();
            
            // Get any available provider
            LLMInterface provider = fallbackManager.getBestProvider();
            
            if (provider != null) {
                System.out.println("✅ Fallback provider available: " + provider.getModelInfo());
                
                // Test response generation with fallback
                String response = provider.generateResponse("Test fallback response");
                System.out.println("  Fallback response: " + (response != null ? "✅ SUCCESS" : "❌ FAILED"));
                
            } else {
                System.out.println("❌ Even fallback provider failed - this should not happen");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Fallback test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test(priority = 5, description = "Generate Diagnostics Summary")
    public void generateDiagnosticsSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 DIAGNOSTICS SUMMARY");
        System.out.println("=".repeat(60));
        
        System.out.println("🎯 Recommendations based on test results:");
        
        String testMode = System.getProperty("ai.test.mode", "not set");
        String ciEnv = System.getenv("CI");
        
        if ("true".equals(ciEnv)) {
            System.out.println("🏗️ CI Environment detected:");
            System.out.println("  ✅ Use fallback mode for reliable testing");
            System.out.println("  ✅ Mock LM Studio for API compatibility testing");
            System.out.println("  ⚠️ Ollama may timeout due to model download constraints");
        } else {
            System.out.println("💻 Local Development Environment:");
            System.out.println("  ✅ Install and run Ollama locally for best experience");
            System.out.println("  ✅ Consider LM Studio for advanced AI features");
            System.out.println("  ✅ Simple AI fallback always available");
        }
        
        System.out.println("\n📋 Next Steps:");
        System.out.println("  1. Review diagnostics output above");
        System.out.println("  2. Fix any connection issues identified");
        System.out.println("  3. Run SelfHealingDemoTest to validate functionality");
        System.out.println("  4. Check troubleshooting guide if issues persist");
        
        System.out.println("\n✅ AI Provider Diagnostics completed!");
    }
}