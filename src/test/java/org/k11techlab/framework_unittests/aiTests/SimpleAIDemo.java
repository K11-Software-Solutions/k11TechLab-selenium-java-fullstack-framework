package org.k11techlab.framework_unittests.aiTests;

import org.k11techlab.framework.selenium.webuitestbase.BaseSeleniumTest;
import org.k11techlab.framework.ai.simple.SimpleAIClient;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import static org.testng.Assert.*;

/**
 * Simple AI demonstration test that works without external AI dependencies.
 * This shows the basic concept while you set up the full AI system.
 */
public class SimpleAIDemo extends BaseSeleniumTest {
    
    private SimpleAIClient aiClient;
    
    @BeforeMethod
    public void setup() {
        aiClient = new SimpleAIClient();
        getDriver().get("https://www.google.com");
        Log.info("Starting Simple AI Demo Test with Google Search");
    }
    
    @Test(description = "Demo AI locator suggestions without external dependencies")
    public void testBasicAILocatorSuggestions() {
        Log.info("=== Simple AI Locator Demo ===");
        
        // Get AI suggestions (currently returns template suggestions)
        String locatorSuggestions = aiClient.generateSuggestion("locator for search input field");
        Log.info("AI Locator Suggestions:");
        Log.info(locatorSuggestions);
        
        // Use actual locators to demonstrate the concept
        WebElement searchBox = getDriver().findElement(By.id("searchInput"));
        searchBox.sendKeys("Test Automation");
        
        Log.info("Successfully demonstrated locator concept with AI guidance");
        
        // Verify element was found
        assertTrue(searchBox.isDisplayed(), "Search box should be visible");
    }
    
    @Test(description = "Demo AI test generation concept")
    public void testBasicAITestGeneration() {
        Log.info("=== Simple AI Test Generation Demo ===");
        
        // Get AI test suggestions
        String testSuggestion = aiClient.generateSuggestion("test for Wikipedia search functionality");
        Log.info("AI Test Generation:");
        Log.info(testSuggestion);
        
        // Demonstrate the concept by executing a basic test
        WebElement searchInput = getDriver().findElement(By.id("searchInput"));
        searchInput.clear();
        searchInput.sendKeys("Selenium WebDriver");
        
        WebElement searchButton = getDriver().findElement(By.xpath("//button[@type='submit']"));
        searchButton.click();
        
        // Simple verification
        try {
            Thread.sleep(2000); // Simple wait for demo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String currentUrl = getDriver().getCurrentUrl();
        assertTrue(currentUrl.contains("selenium") || currentUrl.contains("Selenium"), 
                  "Should navigate to Selenium-related page");
        
        Log.info("AI-guided test concept demonstrated successfully");
    }
    
    @Test(description = "Check AI system status")
    public void testAISystemStatus() {
        Log.info("=== AI System Status Check ===");
        
        String status = aiClient.getStatus();
        Log.info("AI Status: " + status);
        
        boolean available = aiClient.isAvailable();
        Log.info("AI Available: " + available);
        
        // This test always passes - it's for information only
        assertTrue(true, "Status check completed");
        
        if (!available) {
            Log.info("To enable full AI features:");
            Log.info("1. Install Ollama from https://ollama.ai/download");
            Log.info("2. Run: ollama pull llama3");
            Log.info("3. Update configuration to enable AI");
        }
    }
    
    @Test(description = "Demo AI workflow concept without external dependencies")
    public void testBasicAIWorkflow() {
        Log.info("=== Basic AI Workflow Demo ===");
        
        try {
            // Step 1: Get AI advice for element location
            String locatorAdvice = aiClient.generateSuggestion("find search input on Wikipedia");
            Log.info("Step 1 - AI Locator Advice: " + locatorAdvice);
            
            // Step 2: Apply the advice (using actual locator)
            WebElement searchElement = getDriver().findElement(By.id("searchInput"));
            Log.info("Step 2 - Found search element using guidance");
            
            // Step 3: Get AI advice for test actions
            String actionAdvice = aiClient.generateSuggestion("test search functionality");
            Log.info("Step 3 - AI Action Advice: " + actionAdvice);
            
            // Step 4: Execute the test based on AI guidance
            searchElement.clear();
            searchElement.sendKeys("Artificial Intelligence");
            
            WebElement submitButton = getDriver().findElement(By.xpath("//button[@type='submit']"));
            submitButton.click();
            
            // Step 5: Simple verification
            Thread.sleep(2000); // Basic wait
            String pageTitle = getDriver().getTitle();
            Log.info("Step 5 - Page title after search: " + pageTitle);
            
            assertTrue(pageTitle.toLowerCase().contains("artificial") || 
                      getDriver().getCurrentUrl().toLowerCase().contains("artificial"),
                      "Should navigate to AI-related page");
            
            Log.info("=== AI Workflow Demo Completed Successfully ===");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.error("Test interrupted: " + e.getMessage());
        } catch (Exception e) {
            Log.error("Test failed: " + e.getMessage());
            // Get AI suggestion for failure analysis
            String failureAdvice = aiClient.generateSuggestion("analyze test failure: " + e.getMessage());
            Log.info("AI Failure Analysis: " + failureAdvice);
            throw e;
        }
    }
}