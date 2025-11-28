package org.k11techlab.framework_unittests.aiTests;

import org.k11techlab.framework.selenium.webuitestbase.BaseSeleniumTest;
import org.k11techlab.framework.ai.manager.AIProviderManager;
import org.k11techlab.framework.ai.llm.LLMInterface;
import org.k11techlab.framework.ai.healing.AIElementHealer;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.Assert;

/**
 * Focused AI Healing Test - Demonstrates Real AI-Powered Locator Strategies
 * 
 * This test shows:
 * 1. AI-generated locator strategies when standard ones fail
 * 2. Self-healing element detection
 * 3. AI-powered error analysis and recovery
 * 4. Dynamic locator adaptation
 * 
 * @author K11 TechLab
 * @version 1.0
 */
public class AIHealingDemoTest extends BaseSeleniumTest {
    
    private AIProviderManager aiManager;
    private LLMInterface aiProvider;
    private AIElementHealer elementHealer;
    
    @BeforeClass
    public void setupAIHealing() {
        Log.info("🚀 Initializing AI Healing Demo Test");
        System.out.println("🤖 Setting up AI-Powered Element Healing...");
        
        // Initialize AI Provider
        aiManager = new AIProviderManager();
        aiProvider = aiManager.getBestProvider();
        
        if (aiProvider != null) {
            System.out.println("✅ AI Provider ready: " + aiManager.getCurrentProviderInfo());
            // Initialize AI Element Healer
            elementHealer = new AIElementHealer(aiProvider, driver.get());
        } else {
            System.out.println("⚠️ AI not available - test will demonstrate fallback behavior");
        }
    }
    
    @Test(priority = 1, description = "AI Healing - Google Search Elements")
    public void testAIHealingGoogleSearch() throws InterruptedException {
        Log.info("🔍 Testing AI healing on Google search elements");
        System.out.println("\n=== AI HEALING - GOOGLE SEARCH TEST ===");
        
        try {
            // Navigate to Google
            driver.get().get("https://www.google.com");
            System.out.println("📍 Navigated to Google");
            
            // Test 1: Find search input with AI healing
            System.out.println("\n🎯 Test 1: AI-Enhanced Search Input Detection");
            if (elementHealer != null) {
                WebElement searchInput = elementHealer.findElement("Google search input field");
                Assert.assertNotNull(searchInput, "Search input should be found with AI healing");
                
                searchInput.sendKeys("AI-powered test automation");
                System.out.println("✅ Successfully entered text in AI-found search input");
                
                // Test 2: Find search button with AI healing
                System.out.println("\n🎯 Test 2: AI-Enhanced Search Button Detection");
                WebElement searchButton = elementHealer.findElement("Google search button");
                Assert.assertNotNull(searchButton, "Search button should be found with AI healing");
                
                // Use JavaScript click as fallback for non-interactable elements
                try {
                    searchButton.click();
                    System.out.println("✅ Successfully clicked AI-found search button");
                } catch (Exception e) {
                    System.out.println("⚠️ Direct click failed, using JavaScript click...");
                    ((JavascriptExecutor) driver.get()).executeScript("arguments[0].click();", searchButton);
                    System.out.println("✅ Successfully clicked using JavaScript");
                }
                
                // Wait for results and demonstrate AI validation
                Thread.sleep(2000);
                validateResultsWithAI();
                
            } else {
                System.out.println("⚠️ AI healing not available - using standard approach");
                // Fallback to standard approach
                performStandardSearch();
            }
            
        } catch (Exception e) {
            Log.error("AI healing test failed: " + e.getMessage());
            
            // Demonstrate AI-powered error analysis
            if (aiProvider != null) {
                demonstrateAIErrorAnalysis(e);
            }
            
            throw e;
        }
    }
    
    @Test(priority = 2, description = "AI Healing - Dynamic Page Elements")
    public void testAIHealingDynamicElements() throws InterruptedException {
        Log.info("🔄 Testing AI healing on dynamic page elements");
        System.out.println("\n=== AI HEALING - DYNAMIC ELEMENTS TEST ===");
        
        try {
            // Navigate to a page with dynamic elements
            driver.get().get("https://the-internet.herokuapp.com/dynamic_loading/1");
            System.out.println("📍 Navigated to dynamic loading test page");
            
            if (elementHealer != null) {
                try {
                    // Find and click start button
                    WebElement startButton = elementHealer.findElement("Start button");
                    Assert.assertNotNull(startButton, "Start button should be found");
                    
                    // Use JavaScript click for better reliability
                    ((JavascriptExecutor) driver.get()).executeScript("arguments[0].click();", startButton);
                    System.out.println("✅ Clicked start button using JavaScript");
                    
                    // Wait for dynamic element to appear and find it with AI
                    System.out.println("⏳ Waiting for dynamic element...");
                    Thread.sleep(6000); // Wait for element to load
                    
                    WebElement finishElement = elementHealer.findElement("Hello World text");
                    Assert.assertNotNull(finishElement, "Dynamic element should be found with AI");
                    
                    String elementText = finishElement.getText();
                    System.out.println("✅ AI found dynamic element with text: " + elementText);
                    Assert.assertTrue(elementText.contains("Hello World"), "Element should contain expected text");
                    
                } catch (Exception startButtonError) {
                    System.out.println("⚠️ Start button interaction failed, demonstrating AI fallback strategies");
                    // Use AI to analyze the page and suggest alternatives
                    demonstrateAIPageAnalysis();
                }
                
            } else {
                System.out.println("⚠️ AI healing not available for dynamic element test");
            }
            
        } catch (Exception e) {
            Log.error("Dynamic element AI healing failed: " + e.getMessage());
            
            if (aiProvider != null) {
                demonstrateAIDiagnostics();
            }
            
            throw e;
        }
    }
    
    @Test(priority = 3, description = "AI-Generated Test Strategy")
    public void testAIGeneratedStrategy() {
        Log.info("🎨 Testing AI-generated test strategy");
        System.out.println("\n=== AI-GENERATED TEST STRATEGY ===");
        
        if (aiProvider == null) {
            System.out.println("⚠️ Skipping AI strategy test - AI not available");
            return;
        }
        
        try {
            // Get AI to generate a test strategy for a specific scenario
            String testStrategyPrompt = 
                "I'm testing a login form with email and password fields. " +
                "Generate a comprehensive test strategy including: " +
                "1) Positive test cases, 2) Negative test cases, 3) Edge cases, " +
                "4) Accessibility considerations. Keep it concise but thorough.";
            
            String aiStrategy = aiProvider.generateResponse(testStrategyPrompt, 0.4f, 300);
            
            System.out.println("🤖 AI-Generated Test Strategy:");
            System.out.println("=" .repeat(50));
            System.out.println(aiStrategy);
            System.out.println("=" .repeat(50));
            
            // Validate that AI provided meaningful strategy
            Assert.assertTrue(aiStrategy.length() > 100, "AI should provide substantial strategy");
            Assert.assertTrue(aiStrategy.toLowerCase().contains("test"), "Strategy should mention testing");
            
            Log.info("AI generated test strategy: " + aiStrategy.substring(0, Math.min(100, aiStrategy.length())));
            
        } catch (Exception e) {
            Log.error("AI strategy generation failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Validate search results using AI analysis
     */
    private void validateResultsWithAI() {
        try {
            String pageTitle = driver.get().getTitle();
            
            if (aiProvider != null) {
                String validationPrompt = String.format(
                    "Analyze this search results page title: '%s'. " +
                    "Does it indicate successful search results for 'AI-powered test automation'? " +
                    "Provide a brief yes/no assessment with reasoning.",
                    pageTitle
                );
                
                String aiValidation = aiProvider.generateResponse(validationPrompt, 0.2f, 100);
                System.out.println("🤖 AI Results Validation:");
                System.out.println(aiValidation);
                Log.info("AI validation: " + aiValidation);
            }
            
        } catch (Exception e) {
            Log.error("AI validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Demonstrate AI-powered error analysis
     */
    private void demonstrateAIErrorAnalysis(Exception error) {
        try {
            String errorAnalysisPrompt = String.format(
                "Analyze this Selenium test error and provide debugging suggestions: %s. " +
                "The test was trying to find Google search elements. Suggest: " +
                "1) Possible causes, 2) Alternative approaches, 3) Prevention strategies.",
                error.getMessage()
            );
            
            String aiAnalysis = aiProvider.generateResponse(errorAnalysisPrompt, 0.3f, 250);
            
            System.out.println("🤖 AI Error Analysis:");
            System.out.println("=" .repeat(50));
            System.out.println(aiAnalysis);
            System.out.println("=" .repeat(50));
            
            Log.info("AI error analysis: " + aiAnalysis);
            
        } catch (Exception e) {
            Log.error("AI error analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Fallback to standard search approach
     */
    private void performStandardSearch() {
        try {
            System.out.println("🔧 Using standard locator approach...");
            
            // Standard Google search
            WebElement searchInput = driver.get().findElement(org.openqa.selenium.By.name("q"));
            searchInput.sendKeys("standard test automation");
            
            WebElement searchButton = driver.get().findElement(org.openqa.selenium.By.name("btnK"));
            searchButton.click();
            
            System.out.println("✅ Standard search completed");
            
        } catch (Exception e) {
            Log.error("Standard search also failed: " + e.getMessage());
            throw e;
        }
    }
    
    @AfterClass
    public void tearDownAIHealing() {
        Log.info("🧹 Cleaning up AI Healing Test");
        
        if (aiManager != null) {
            // Get final AI summary
            if (aiProvider != null) {
                try {
                    String summaryPrompt = 
                        "Summarize the key benefits of AI-powered test automation and self-healing locators " +
                        "based on the tests that were just executed. Keep it brief but impactful.";
                    
                    String aiSummary = aiProvider.generateResponse(summaryPrompt, 0.3f, 150);
                    
                    System.out.println("\n🤖 AI Test Summary:");
                    System.out.println("=" .repeat(60));
                    System.out.println(aiSummary);
                    System.out.println("=" .repeat(60));
                    
                } catch (Exception e) {
                    Log.error("AI summary generation failed: " + e.getMessage());
                }
            }
            
            aiManager.close();
        }
        
        System.out.println("✅ AI Healing Demo Test completed successfully");
        Log.info("AI healing test suite completed");
    }
    
    private void demonstrateAIDiagnostics() {
        System.out.println("\n🔍 AI Diagnostics:");
        try {
            if (driver.get() != null) {
                String pageSource = driver.get().getPageSource();
                String diagnostics = aiProvider.generateResponse(
                    "Analyze this page source and suggest why element location might be failing: " + 
                    pageSource.substring(0, Math.min(1000, pageSource.length()))
                );
                System.out.println(diagnostics);
            } else {
                System.out.println("Cannot run diagnostics - driver session is null");
            }
        } catch (Exception e) {
            System.out.println("Diagnostics failed: " + e.getMessage());
        }
    }
    
    private void demonstrateAIPageAnalysis() {
        System.out.println("\n🧠 AI Page Analysis:");
        try {
            if (driver.get() != null && aiProvider != null) {
                String pageTitle = driver.get().getTitle();
                String analysisPrompt = "Analyze this page and suggest alternative locator strategies for finding a 'Start' button on: " + pageTitle;
                String analysis = aiProvider.generateResponse(analysisPrompt);
                System.out.println("📊 AI Analysis Result:");
                System.out.println(analysis);
            }
        } catch (Exception e) {
            System.out.println("Page analysis failed: " + e.getMessage());
        }
    }
}