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
            aiClient = new OllamaClient(); // Uses system properties for CI compatibility
            getDriver().get("https://www.google.com");
            Log.info("🚀 Quick AI Test Setup Complete");
        } catch (Exception e) {
            Log.error("Setup failed: " + e.getMessage());
            aiClient = null;
        }
    }
    
    @Test(timeOut = 120000)
    public void testQuickAISuggestion() {
        Log.info("🔍 Testing Quick AI Response");
        
        if (aiClient != null && aiClient.isAvailable()) {
            // Super simple, fast AI request
            String suggestion = aiClient.generateSuggestion("Give me 2 CSS selectors for Google search box");
            
            System.out.println("\n🤖 QUICK AI RESPONSE:");
            System.out.println("====================");
            System.out.println(suggestion);
            System.out.println("====================");
            
            assertNotNull(suggestion);
            assertFalse(suggestion.contains("AI unavailable"));
            assertTrue(suggestion.length() > 20);
            
            Log.info("✅ Quick AI test successful!");
        } else {
            Log.info("⚠️ AI not available - skipping test");
        }
    }
}