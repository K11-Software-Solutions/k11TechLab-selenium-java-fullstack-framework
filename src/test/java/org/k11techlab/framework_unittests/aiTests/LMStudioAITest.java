package org.k11techlab.framework_unittests.aiTests;

import org.k11techlab.framework.ai.manager.AIProviderManager;
import org.k11techlab.framework.ai.llm.LLMInterface;
import org.k11techlab.framework.selenium.webuitestbase.BaseSeleniumTest;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;

/**
 * LM Studio Integration Test
 * Tests LM Studio AI provider functionality
 * 
 * @author K11 TechLab
 * @version 1.0
 */
public class LMStudioAITest extends BaseSeleniumTest {
    
    private AIProviderManager aiManager;
    private LLMInterface llmStudioProvider;
    
    @BeforeClass
    public void setupAI() {
        Log.info("🚀 Setting up LM Studio AI Test");
        aiManager = new AIProviderManager(); // Enable fallback
    }
    
    @Test(priority = 1, description = "Test LM Studio specific provider")
    public void testLMStudioSpecificProvider() {
        Log.info("🎯 Testing LM Studio specific provider");
        System.out.println("\n=== LM STUDIO SPECIFIC PROVIDER TEST ===");
        
        llmStudioProvider = aiManager.getProvider(AIProviderManager.Provider.LLMSTUDIO);
        
        if (llmStudioProvider != null) {
            System.out.println("✅ LM Studio provider obtained successfully");
            Log.info("LM Studio provider obtained: " + aiManager.getCurrentProviderInfo());
            
            // Test availability
            boolean available = llmStudioProvider.isAvailable();
            System.out.println("🔍 LM Studio availability: " + (available ? "✅ Available" : "❌ Not Available"));
            
            if (available) {
                // Test basic response generation
                String response = llmStudioProvider.generateResponse("Say 'LM Studio is working'");
                System.out.println("🤖 LM Studio Response: " + response);
                assert response != null && !response.trim().isEmpty() : "LM Studio should return a response";
            } else {
                System.out.println("⚠️ LM Studio not available - likely fallback to other provider");
                Log.info("LM Studio fallback scenario - provider type: " + aiManager.getCurrentProviderType());
            }
        } else {
            System.out.println("❌ No AI provider available");
            Log.error("No AI provider available in LM Studio test");
        }
    }
    
    @Test(priority = 2, description = "Test AI Manager with multiple providers")
    public void testAIManagerProviderSelection() {
        Log.info("🔄 Testing AI Manager provider selection logic");
        System.out.println("\n=== AI PROVIDER MANAGER TEST ===");
        
        // Get best available provider
        LLMInterface bestProvider = aiManager.getBestProvider();
        if (bestProvider != null) {
            System.out.println("✅ Best provider found: " + aiManager.getCurrentProviderInfo());
            
            // Test the provider
            String response = bestProvider.generateResponse("Generate a simple Selenium locator example");
            System.out.println("🔧 Selenium Suggestion: " + response);
            
        } else {
            System.out.println("❌ No AI providers available");
        }
        
        // Show diagnostics
        System.out.println("\n--- PROVIDER DIAGNOSTICS ---");
        System.out.println(aiManager.getAllProviderDiagnostics());
    }
    
    @Test(priority = 3, description = "Test LM Studio specific features")
    public void testLMStudioFeatures() {
        Log.info("🎪 Testing LM Studio specific features");
        System.out.println("\n=== LM STUDIO FEATURES TEST ===");
        
        LLMInterface provider = aiManager.getProvider(AIProviderManager.Provider.LLMSTUDIO);
        
        if (provider != null && aiManager.getCurrentProviderType() == AIProviderManager.Provider.LLMSTUDIO) {
            System.out.println("✅ Using actual LM Studio provider");
            
            // Test locator generation
            String locatorSuggestion = provider.generateResponse(
                "Generate 3 Selenium locators for a login button: By.id, By.cssSelector, By.xpath", 
                0.3f, 100
            );
            System.out.println("🎯 LM Studio Locator Suggestions:\n" + locatorSuggestion);
            
            // Test with different temperature
            String creativeSuggestion = provider.generateResponse(
                "Suggest 3 creative test automation scenarios for an e-commerce website",
                0.8f, 150
            );
            System.out.println("🎨 LM Studio Creative Suggestions:\n" + creativeSuggestion);
            
        } else {
            System.out.println("ℹ️ LM Studio not available - using fallback provider");
            System.out.println("Current provider: " + aiManager.getCurrentProviderInfo());
            
            // Still test functionality with fallback
            if (provider != null) {
                String response = provider.generateResponse("Generate a basic test case structure");
                System.out.println("📋 Fallback Response: " + response);
            }
        }
    }
    
    @Test(priority = 4, description = "Test provider switching and fallback")
    public void testProviderSwitchingAndFallback() {
        Log.info("🔄 Testing provider switching and fallback logic");
        System.out.println("\n=== PROVIDER SWITCHING TEST ===");
        
        // Try each provider type
        AIProviderManager.Provider[] providers = {
            AIProviderManager.Provider.OLLAMA,
            AIProviderManager.Provider.LLMSTUDIO,
            AIProviderManager.Provider.SIMPLE
        };
        
        for (AIProviderManager.Provider providerType : providers) {
            System.out.println("\n--- Testing " + providerType.getDisplayName() + " ---");
            
            LLMInterface provider = aiManager.getProvider(providerType);
            if (provider != null) {
                System.out.println("✅ " + providerType.getDisplayName() + " provider obtained");
                System.out.println("Current: " + aiManager.getCurrentProviderInfo());
                
                // Quick test
                String response = provider.generateResponse("Respond with: " + providerType.getDisplayName() + " working");
                System.out.println("🤖 Response: " + response);
            } else {
                System.out.println("❌ " + providerType.getDisplayName() + " provider not available");
            }
        }
    }
    
    @AfterClass
    public void tearDownAI() {
        Log.info("🧹 Cleaning up AI resources");
        if (aiManager != null) {
            aiManager.close();
        }
        System.out.println("✅ LM Studio AI test completed");
    }
}