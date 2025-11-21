package org.k11techlab.framework_unittests.aiTests;

import org.k11techlab.framework.selenium.webuitestbase.BaseSeleniumTest;
import org.k11techlab.framework.ai.simple.SimpleAIClient;
import org.k11techlab.framework.ai.ollama.OllamaClient;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.openqa.selenium.By;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import static org.testng.Assert.*;

/**
 * Full AI Testing Demo with Real Ollama Integration!
 * This test demonstrates the complete AI Testing Assistant capabilities using local LLM.
 */
public class FullAIDemo extends BaseSeleniumTest {
    
    private OllamaClient ollamaClient;
    private SimpleAIClient fallbackClient;
    
    @BeforeMethod
    public void setup() {
        try {
            System.out.println("\n=== FULL AI DEMO SETUP ===");
            System.out.println("🚀 Initializing AI Testing Assistant...");
            
            // Initialize Ollama client with real LLM (supports system property overrides for CI)
            ollamaClient = new OllamaClient(); // Uses system properties or defaults
            fallbackClient = new SimpleAIClient();
            
            // Navigate to test page
            getDriver().get("https://www.google.com");
            Log.info("🚀 Starting Full AI Testing Demo with REAL Ollama LLM on Google Search!");
            System.out.println("🌐 Navigated to Google Search for AI testing");
            
            // Test AI connection with detailed output
            System.out.println("🔍 Testing AI connection...");
            if (ollamaClient.isAvailable()) {
                String status = ollamaClient.getStatus();
                Log.info("✅ Ollama AI Status: " + status);
                System.out.println("✅ AI AVAILABLE: " + status);
            } else {
                Log.info("⚠️ Ollama not available, using fallback client");
                System.out.println("⚠️ AI NOT AVAILABLE - using fallback client");
                
                // Show detailed diagnostics when AI is not available
                System.out.println("\n" + ollamaClient.getDiagnostics());
            }
            System.out.println("=== SETUP COMPLETE ===");
            
        } catch (Exception e) {
            String error = "Failed to initialize Ollama client: " + e.getMessage();
            Log.error(error);
            System.out.println("❌ SETUP ERROR: " + error);
            e.printStackTrace();
            ollamaClient = null;
        }
    }
    
    @Test(timeOut = 120000) // 120 second timeout for LLM responses
    public void testAIEnhancedLocatorGeneration() {
        System.out.println("\n=== TEST: AI LOCATOR GENERATION ===");
        Log.info("🔍 Testing AI-Enhanced Locator Generation with Real LLM");
        
        if (ollamaClient != null && ollamaClient.isAvailable()) {
            System.out.println("🤖 Requesting locator suggestions from REAL AI...");
            // Test AI-generated locators with real LLM
            String suggestion = ollamaClient.generateLocatorSuggestion("Google search input field and search button");
            Log.info("🤖 REAL AI Locator Suggestions for Google Search:\n" + suggestion);
            
            // Also output to console for immediate visibility
            System.out.println("\n🤖 REAL AI LOCATOR SUGGESTIONS:");
            System.out.println("==================================");
            System.out.println(suggestion);
            System.out.println("==================================");
            
            assertNotNull(suggestion, "AI suggestion should not be null");
            assertTrue(suggestion.length() > 100, "Real AI should give detailed responses (got " + suggestion.length() + " chars)");
            assertTrue(suggestion.contains("By.") || suggestion.contains("locator"), "Response should contain locator information");
            
            Log.info("✅ Real AI locator generation working!");
            System.out.println("✅ AI LOCATOR TEST PASSED!");
        } else {
            // Fallback to simple client
            System.out.println("⚠️ AI not available, using fallback client");
            String suggestion = fallbackClient.generateSuggestion("Generate locator for search input field");
            Log.info("⚠️ Using fallback client: " + suggestion);
            System.out.println("⚠️ FALLBACK RESPONSE: " + suggestion);
        }
        System.out.println("=== LOCATOR TEST COMPLETE ===");
    }
    
    @Test(timeOut = 120000) // 120 second timeout for LLM responses
    public void testAITestCaseGeneration() {
        Log.info("📝 Testing AI Test Case Generation with Real LLM");
        
        if (ollamaClient != null && ollamaClient.isAvailable()) {
            // Generate complete test case with real AI
            String testCase = ollamaClient.generateTestSuggestion("Google search functionality with result validation and edge cases");
            Log.info("🤖 REAL AI Generated Test Case for Google Search:\n" + testCase);
            
            // Also output to console for immediate visibility
            System.out.println("\n🤖 REAL AI TEST CASE GENERATION:");
            System.out.println("==================================");
            System.out.println(testCase);
            System.out.println("==================================");
            
            assertNotNull(testCase);
            assertTrue(testCase.length() > 200); // Real AI gives comprehensive test cases
            assertTrue(testCase.contains("@Test") || testCase.contains("test"));
            
            Log.info("✅ Real AI test generation working!");
        } else {
            String testCase = fallbackClient.generateSuggestion("Generate test for search functionality");
            Log.info("⚠️ Using fallback client: " + testCase);
        }
    }
    
    @Test(timeOut = 120000) // 120 second timeout for LLM responses
    public void testAIAssistedDebugging() {
        Log.info("🐛 Testing AI-Assisted Debugging with Real LLM");
        
        try {
            // Intentionally cause an issue to test AI debugging
            getDriver().findElement(By.id("non-existent-button"));
        } catch (Exception e) {
            if (ollamaClient != null && ollamaClient.isAvailable()) {
                // Get real AI debugging help
                String debugSuggestion = ollamaClient.generateDebugSuggestion(
                    e.getMessage(), 
                    "Google search homepage, looking for non-existent button element"
                );
                Log.info("🤖 REAL AI Debug Analysis:\n" + debugSuggestion);
                
                // Also output to console for immediate visibility
                System.out.println("\n🤖 REAL AI DEBUGGING ASSISTANCE:");
                System.out.println("==================================");
                System.out.println(debugSuggestion);
                System.out.println("==================================");
                
                assertNotNull(debugSuggestion);
                assertTrue(debugSuggestion.length() > 100);
                
                Log.info("✅ Real AI debugging assistance working!");
            } else {
                String debugSuggestion = fallbackClient.generateSuggestion("Debug this error: " + e.getMessage());
                Log.info("⚠️ Using fallback client: " + debugSuggestion);
            }
        }
    }
    
    @Test(timeOut = 120000) // 120 second timeout for LLM responses
    public void testAIPageAnalysis() {
        Log.info("📊 Testing AI Page Analysis with Real LLM");
        
        if (ollamaClient != null && ollamaClient.isAvailable()) {
            // Get comprehensive page analysis from real AI
            String analysis = ollamaClient.generateSuggestion(
                "Analyze the Google search homepage for test automation opportunities. " +
                "Suggest 5 different test scenarios covering search functionality, UI elements, " +
                "mobile responsiveness, accessibility, and edge cases like empty searches."
            );
            Log.info("🤖 REAL AI Page Analysis for Google Search:\n" + analysis);
            
            // Also output to console for immediate visibility
            System.out.println("\n🤖 REAL AI PAGE ANALYSIS:");
            System.out.println("==================================");
            System.out.println(analysis);
            System.out.println("==================================");
            
            assertNotNull(analysis);
            assertTrue(analysis.length() > 300); // Real AI gives detailed analysis
            
            Log.info("✅ Real AI page analysis working!");
        } else {
            String analysis = fallbackClient.generateSuggestion("Analyze this page for testability");
            Log.info("⚠️ Using fallback client: " + analysis);
        }
    }

}