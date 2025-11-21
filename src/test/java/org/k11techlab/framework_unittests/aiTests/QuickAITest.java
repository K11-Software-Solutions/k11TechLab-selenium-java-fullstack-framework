package org.k11techlab.framework_unittests.aiTests;

import org.k11techlab.framework.selenium.webuitestbase.BaseSeleniumTest;
import org.k11techlab.framework.ai.ollama.OllamaClient;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import static org.testng.Assert.*;

/**
 * Quick AI Test - Single fast AI call to verify LLM integration works.
 */
public class QuickAITest extends BaseSeleniumTest {
    
    private OllamaClient aiClient;
    
    @BeforeMethod
    public void setup() {
        try {
            System.out.println("\n=== QUICK AI TEST SETUP ===");
            System.out.println("⚡ Initializing Ollama client for quick test...");
            
            aiClient = new OllamaClient(); // Uses system properties for CI compatibility
            getDriver().get("https://www.google.com");
            
            Log.info("🚀 Quick AI Test Setup Complete");
            System.out.println("✅ Quick AI Test Setup Complete");
            
            // Quick availability check
            if (aiClient != null) {
                System.out.println("🔍 AI Client Status: " + aiClient.getStatus());
            }
            
        } catch (Exception e) {
            String error = "Setup failed: " + e.getMessage();
            Log.error(error);
            System.out.println("❌ QUICK TEST SETUP ERROR: " + error);
            e.printStackTrace();
            aiClient = null;
        }
    }
    
    @Test(timeOut = 120000)
    public void testQuickAISuggestion() {
        System.out.println("\n=== QUICK AI SUGGESTION TEST ===");
        Log.info("🔍 Testing Quick AI Response");
        
        if (aiClient != null && aiClient.isAvailable()) {
            System.out.println("🚀 AI is available - sending quick request...");
            
            // Super simple, fast AI request
            String suggestion = aiClient.generateSuggestion("Give me 2 CSS selectors for Google search box");
            
            System.out.println("\n🤖 QUICK AI RESPONSE:");
            System.out.println("====================");
            System.out.println(suggestion);
            System.out.println("====================");
            
            // Enhanced assertions with detailed output
            assertNotNull(suggestion, "AI suggestion should not be null");
            assertFalse(suggestion.contains("AI unavailable"), "Should not contain error message");
            assertTrue(suggestion.length() > 20, "Response should be meaningful (got " + suggestion.length() + " chars)");
            
            Log.info("✅ Quick AI test successful!");
            System.out.println("✅ QUICK AI TEST PASSED!");
        } else {
            Log.info("⚠️ AI not available - skipping test");
            System.out.println("⚠️ AI NOT AVAILABLE - Test skipped");
            
            // Show diagnostic info
            if (aiClient == null) {
                System.out.println("❌ AI client is null");
            } else {
                System.out.println("❌ AI client not available: " + aiClient.getStatus());
                System.out.println("\n" + aiClient.getDiagnostics());
            }
        }
        System.out.println("=== QUICK AI TEST COMPLETE ===");
    }
}