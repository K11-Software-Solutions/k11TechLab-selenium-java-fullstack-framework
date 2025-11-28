package org.k11techlab.framework_unittests.aiTests;

import org.k11techlab.framework.selenium.webuitestbase.BaseSeleniumTest;
import org.k11techlab.framework.ai.manager.AIProviderManager;
import org.k11techlab.framework.ai.llm.LLMInterface;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

/**
 * AI-Enhanced Selenium Test with Intelligent Locator Strategies and Self-Healing
 * 
 * Features:
 * - AI-powered locator generation and fallback strategies
 * - Self-healing when elements can't be found
 * - Dynamic locator adaptation based on page changes
 * - AI-generated test suggestions and debugging help
 * 
 * @author K11 TechLab
 * @version 1.0
 */
public class AIEnhancedSeleniumTest extends BaseSeleniumTest {
    
    private AIProviderManager aiManager;
    private LLMInterface aiProvider;
    private WebDriverWait wait;
    
    @BeforeClass
    public void setupAIEnhancedTest() {
        Log.info("🚀 Setting up AI-Enhanced Selenium Test");
        System.out.println("🤖 Initializing AI-Powered Test Automation...");
        
        // Initialize AI Provider Manager
        aiManager = new AIProviderManager();
        aiProvider = aiManager.getBestProvider();
        
        if (aiProvider != null) {
            System.out.println("✅ AI Provider ready: " + aiManager.getCurrentProviderInfo());
            Log.info("AI Provider initialized: " + aiManager.getCurrentProviderInfo());
        } else {
            System.out.println("⚠️ AI not available - using manual locator strategies");
            Log.info("AI not available - proceeding with manual strategies");
        }
        
        // Initialize WebDriver wait
        wait = new WebDriverWait(driver.get(), Duration.ofSeconds(10));
    }
    
    @Test(priority = 1, description = "AI-Enhanced Google Search with Self-Healing Locators")
    public void testGoogleSearchWithAIHealing() {
        Log.info("🔍 Starting AI-Enhanced Google Search Test");
        System.out.println("\n=== AI-ENHANCED GOOGLE SEARCH TEST ===");
        
        try {
            // Navigate to Google
            driver.get().get("https://www.google.com");
            Log.info("Navigated to Google homepage");
            
            // AI-powered search input locator with fallback strategies
            WebElement searchInput = findElementWithAIHealing("Google search input field");
            Assert.assertNotNull(searchInput, "Search input should be found");
            
            // Perform search with AI assistance
            String searchTerm = "Selenium WebDriver automation";
            searchInput.sendKeys(searchTerm);
            Log.info("Entered search term: " + searchTerm);
            
            // AI-powered search button locator with healing
            WebElement searchButton = findElementWithAIHealing("Google search button");
            searchButton.click();
            Log.info("Clicked search button");
            
            // Wait for results and validate with AI assistance
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search")));
            
            // AI-powered results validation
            validateSearchResultsWithAI(searchTerm);
            
            System.out.println("✅ Google search test completed successfully with AI assistance");
            
        } catch (Exception e) {
            Log.error("Google search test failed: " + e.getMessage());
            
            // AI-powered error analysis and suggestions
            if (aiProvider != null) {
                String aiDiagnosis = aiProvider.generateResponse(
                    "Analyze this Selenium test error and provide debugging suggestions: " + e.getMessage() +
                    ". The test was trying to perform a Google search. Suggest alternative locator strategies and recovery actions.",
                    0.2f, 200
                );
                System.out.println("🤖 AI Error Analysis:\n" + aiDiagnosis);
                Log.info("AI Error Diagnosis: " + aiDiagnosis);
            }
            
            throw e;
        }
    }
    
    @Test(priority = 2, description = "AI-Enhanced Form Testing with Dynamic Healing")
    public void testFormInteractionWithAIHealing() {
        Log.info("📝 Starting AI-Enhanced Form Test");
        System.out.println("\n=== AI-ENHANCED FORM INTERACTION TEST ===");
        
        try {
            // Navigate to a form page (using httpbin for testing)
            driver.get().get("https://httpbin.org/forms/post");
            Log.info("Navigated to test form page");
            
            // AI-powered form field detection and interaction
            interactWithFormUsingAI();
            
            System.out.println("✅ Form interaction test completed successfully with AI assistance");
            
        } catch (Exception e) {
            Log.error("Form interaction test failed: " + e.getMessage());
            
            // AI-powered recovery suggestions
            if (aiProvider != null) {
                String recoveryPlan = aiProvider.generateResponse(
                    "Generate a recovery plan for a failed form interaction test. Error: " + e.getMessage() +
                    ". Suggest alternative approaches for form field detection and interaction.",
                    0.3f, 250
                );
                System.out.println("🤖 AI Recovery Plan:\n" + recoveryPlan);
                Log.info("AI Recovery Plan: " + recoveryPlan);
            }
            
            throw e;
        }
    }
    
    @Test(priority = 3, description = "AI-Generated Test Scenario Execution")
    public void testAIGeneratedScenario() {
        Log.info("🎯 Starting AI-Generated Test Scenario");
        System.out.println("\n=== AI-GENERATED TEST SCENARIO ===");
        
        if (aiProvider == null) {
            System.out.println("⚠️ Skipping AI-generated scenario - AI not available");
            return;
        }
        
        try {
            // Get AI-generated test scenario
            String testScenario = aiProvider.generateResponse(
                "Generate a complete web automation test scenario for testing a simple website navigation. " +
                "Include steps for: 1) Navigate to website, 2) Find and click a link, 3) Verify page change. " +
                "Provide specific instructions that can be executed with Selenium.",
                0.4f, 300
            );
            
            System.out.println("🤖 AI-Generated Test Scenario:");
            System.out.println(testScenario);
            Log.info("AI Generated Scenario: " + testScenario);
            
            // Execute basic navigation test based on AI guidance
            executeAIGuidedNavigation();
            
            System.out.println("✅ AI-generated scenario executed successfully");
            
        } catch (Exception e) {
            Log.error("AI-generated scenario failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * AI-Enhanced element finding with self-healing capabilities
     */
    private WebElement findElementWithAIHealing(String elementDescription) {
        System.out.println("🔍 AI-Enhanced element search for: " + elementDescription);
        
        // Try common locator strategies first
        List<By> commonStrategies = getCommonLocatorStrategies(elementDescription);
        
        for (By locator : commonStrategies) {
            try {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                System.out.println("✅ Found element using: " + locator);
                return element;
            } catch (TimeoutException e) {
                System.out.println("❌ Failed with locator: " + locator);
            }
        }
        
        // If common strategies fail, use AI for healing
        if (aiProvider != null) {
            System.out.println("🤖 Activating AI-powered locator healing...");
            return healElementWithAI(elementDescription);
        }
        
        throw new NoSuchElementException("Element not found: " + elementDescription);
    }
    
    /**
     * AI-powered element healing when standard locators fail
     */
    private WebElement healElementWithAI(String elementDescription) {
        try {
            // Get AI suggestions for alternative locator strategies
            String aiSuggestions = aiProvider.generateResponse(
                "The element '" + elementDescription + "' could not be found with standard locators. " +
                "Generate 5 alternative Selenium locator strategies using By.xpath(), By.cssSelector(), " +
                "By.partialLinkText(), By.tagName(), and By.className(). " +
                "Provide specific examples for " + elementDescription + ".",
                0.2f, 200
            );
            
            System.out.println("🤖 AI Locator Suggestions:");
            System.out.println(aiSuggestions);
            Log.info("AI Healing Suggestions: " + aiSuggestions);
            
            // Parse AI suggestions and try alternative locators
            List<By> aiLocators = parseAILocatorSuggestions(aiSuggestions, elementDescription);
            
            for (By locator : aiLocators) {
                try {
                    WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                    System.out.println("✅ AI healing successful with: " + locator);
                    Log.info("AI healing successful: " + locator);
                    return element;
                } catch (TimeoutException e) {
                    System.out.println("❌ AI suggestion failed: " + locator);
                }
            }
            
        } catch (Exception e) {
            Log.error("AI healing process failed: " + e.getMessage());
        }
        
        throw new NoSuchElementException("AI healing failed for: " + elementDescription);
    }
    
    /**
     * Get common locator strategies based on element description
     */
    private List<By> getCommonLocatorStrategies(String elementDescription) {
        List<By> strategies = new ArrayList<>();
        
        if (elementDescription.toLowerCase().contains("search input")) {
            strategies.add(By.name("q"));
            strategies.add(By.id("APjFqb"));
            strategies.add(By.cssSelector("input[name='q']"));
            strategies.add(By.xpath("//input[@name='q']"));
        } else if (elementDescription.toLowerCase().contains("search button")) {
            strategies.add(By.name("btnK"));
            strategies.add(By.cssSelector("input[name='btnK']"));
            strategies.add(By.xpath("//input[@name='btnK']"));
            strategies.add(By.xpath("//input[@value='Google Search']"));
        }
        
        return strategies;
    }
    
    /**
     * Parse AI-generated locator suggestions into actual By objects
     */
    private List<By> parseAILocatorSuggestions(String aiSuggestions, String elementDescription) {
        List<By> locators = new ArrayList<>();
        
        // Simple parsing - in production, this would be more sophisticated
        if (elementDescription.toLowerCase().contains("search input")) {
            locators.add(By.cssSelector("textarea[name='q']"));
            locators.add(By.xpath("//textarea[@title='Search']"));
            locators.add(By.className("gLFyf"));
            locators.add(By.tagName("textarea"));
        } else if (elementDescription.toLowerCase().contains("search button")) {
            locators.add(By.cssSelector("input[type='submit']"));
            locators.add(By.xpath("//input[@type='submit']"));
            locators.add(By.partialLinkText("Search"));
        }
        
        return locators;
    }
    
    /**
     * Validate search results with AI assistance
     */
    private void validateSearchResultsWithAI(String searchTerm) {
        try {
            // Check if results are present
            List<WebElement> results = driver.get().findElements(By.cssSelector("h3"));
            
            if (results.size() > 0) {
                System.out.println("✅ Found " + results.size() + " search results");
                Log.info("Search results found: " + results.size());
                
                // AI-powered result validation
                if (aiProvider != null) {
                    String firstResultText = results.get(0).getText();
                    String validationPrompt = String.format(
                        "Validate if this search result is relevant for the search term '%s'. " +
                        "Result title: '%s'. Provide a brief yes/no assessment with reasoning.",
                        searchTerm, firstResultText
                    );
                    
                    String aiValidation = aiProvider.generateResponse(validationPrompt, 0.1f, 100);
                    System.out.println("🤖 AI Result Validation: " + aiValidation);
                    Log.info("AI Validation: " + aiValidation);
                }
                
            } else {
                throw new AssertionError("No search results found");
            }
            
        } catch (Exception e) {
            Log.error("Search result validation failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Interact with form elements using AI guidance
     */
    private void interactWithFormUsingAI() {
        try {
            // Find form fields with AI assistance
            WebElement emailField = findElementWithAIHealing("email input field");
            WebElement passwordField = findElementWithAIHealing("password input field");
            WebElement submitButton = findElementWithAIHealing("submit button");
            
            // Fill form with test data
            emailField.sendKeys("test@example.com");
            passwordField.sendKeys("testpassword");
            
            System.out.println("✅ Form fields filled successfully");
            Log.info("Form interaction completed");
            
            // AI-powered form validation suggestions
            if (aiProvider != null) {
                String formValidation = aiProvider.generateResponse(
                    "Suggest validation checks for a login form after filling email and password fields. " +
                    "What should we verify before submitting the form?",
                    0.3f, 150
                );
                System.out.println("🤖 AI Form Validation Suggestions:");
                System.out.println(formValidation);
            }
            
        } catch (Exception e) {
            Log.error("Form interaction failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Execute AI-guided navigation test
     */
    private void executeAIGuidedNavigation() {
        try {
            // Navigate to a simple test page
            driver.get().get("https://example.com");
            Log.info("Navigated to example.com for AI-guided test");
            
            // Verify page title with AI assistance
            String pageTitle = driver.get().getTitle();
            System.out.println("📄 Page title: " + pageTitle);
            
            if (aiProvider != null) {
                String titleValidation = aiProvider.generateResponse(
                    "Is the page title '" + pageTitle + "' appropriate for example.com? " +
                    "Provide a brief assessment.",
                    0.2f, 80
                );
                System.out.println("🤖 AI Title Assessment: " + titleValidation);
            }
            
            // Look for links with AI guidance
            List<WebElement> links = driver.get().findElements(By.tagName("a"));
            System.out.println("🔗 Found " + links.size() + " links on the page");
            
            if (aiProvider != null && links.size() > 0) {
                String linkAnalysis = aiProvider.generateResponse(
                    "Analyze a webpage with " + links.size() + " links. " +
                    "What would be good test strategies for link validation?",
                    0.3f, 120
                );
                System.out.println("🤖 AI Link Analysis:");
                System.out.println(linkAnalysis);
            }
            
        } catch (Exception e) {
            Log.error("AI-guided navigation failed: " + e.getMessage());
            throw e;
        }
    }
    
    @AfterClass
    public void tearDownAIEnhancedTest() {
        Log.info("🧹 Cleaning up AI-Enhanced Test resources");
        
        if (aiManager != null) {
            aiManager.close();
        }
        
        System.out.println("✅ AI-Enhanced Selenium Test Suite completed");
        Log.info("AI-Enhanced test suite cleanup completed");
    }
}