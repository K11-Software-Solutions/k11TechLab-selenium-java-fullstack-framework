package org.k11techlab.framework_unittests.aiTests;

import org.k11techlab.framework.selenium.webuitestbase.BaseSeleniumTest;
import org.k11techlab.framework.ai.manager.AIProviderManager;
import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.ai.rag.KnowledgeBase;
import org.k11techlab.framework.ai.llm.LLMInterface;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;

/**
 * RAG (Retrieval-Augmented Generation) Enhanced AI Test
 * Demonstrates knowledge-enhanced AI responses for test automation
 * 
 * @author K11 TechLab
 * @version 1.0
 */
public class RAGEnhancedAITest extends BaseSeleniumTest {
    
    private AIProviderManager aiManager;
    private RAGEnhancedAIClient ragAI;
    private LLMInterface baseAI;
    
    @BeforeClass
    public void setupRAGTesting() {
        System.out.println("\n🧠 Setting up RAG-Enhanced AI Testing Environment...");
        Log.info("Initializing RAG-enhanced AI test environment");
        
        try {
            // Initialize AI provider manager with RAG enabled
            aiManager = new AIProviderManager(); // fallback=true, RAG=true
            
            // Get base AI provider for comparison
            baseAI = new AIProviderManager().getBestProvider();
            
            // Get RAG-enhanced provider
            ragAI = (RAGEnhancedAIClient) aiManager.getRAGEnhancedProvider();
            
            if (ragAI != null) {
                System.out.println("✅ RAG-Enhanced AI Client initialized successfully");
                System.out.println("🧠 Knowledge Base: " + ragAI.getKnowledgeBaseInfo());
            } else {
                System.out.println("❌ RAG-Enhanced AI initialization failed");
            }
            
        } catch (Exception e) {
            Log.error("RAG AI setup failed: " + e.getMessage());
            System.out.println("❌ RAG setup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test(priority = 1, description = "Compare Base AI vs RAG-Enhanced AI Responses")
    public void testBaseVsRAGComparison() throws InterruptedException {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🎯 BASE AI vs RAG-ENHANCED AI COMPARISON");
        System.out.println("=".repeat(70));
        
        String[] testQueries = {
            "Generate locators for a submit button",
            "Help me troubleshoot NoSuchElementException",
            "Show me an example login test",
            "What are best practices for element waiting?",
            "How to handle StaleElementReferenceException?"
        };
        
        for (String query : testQueries) {
            demonstrateAIComparison(query);
            Thread.sleep(2000); // Brief pause between queries
        }
    }
    
    @Test(priority = 2, description = "Test RAG Locator Generation with Context")
    public void testRAGLocatorGeneration() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📍 RAG-ENHANCED LOCATOR GENERATION");
        System.out.println("=".repeat(70));
        
        // Test various element types with context
        String[] elementDescriptions = {
            "search input field",
            "login submit button", 
            "navigation menu",
            "modal close button",
            "dropdown menu"
        };
        
        for (String elementDesc : elementDescriptions) {
            System.out.println("\n🔍 Generating locators for: " + elementDesc);
            
            if (ragAI != null) {
                String response = ragAI.generateLocatorSuggestions(elementDesc, "e-commerce website");
                System.out.println("🧠 RAG-Enhanced Response:");
                System.out.println(response);
                System.out.println("-".repeat(50));
            }
        }
    }
    
    @Test(priority = 3, description = "Test RAG Troubleshooting Assistance")
    public void testRAGTroubleshootingAssistance() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔧 RAG-ENHANCED TROUBLESHOOTING ASSISTANCE");
        System.out.println("=".repeat(70));
        
        // Test various error scenarios
        String[] errorScenarios = {
            "TimeoutException: Element not found after 10 seconds",
            "NoSuchElementException: Unable to locate element by ID",
            "StaleElementReferenceException: Element is no longer valid",
            "ElementNotInteractableException: Element is not clickable"
        };
        
        for (String error : errorScenarios) {
            System.out.println("\n🚨 Troubleshooting Error: " + error);
            
            if (ragAI != null) {
                String solution = ragAI.generateTroubleshootingSuggestions(error, "Selenium WebDriver test");
                System.out.println("🧠 RAG-Enhanced Solution:");
                System.out.println(solution);
                System.out.println("-".repeat(50));
            }
        }
    }
    
    @Test(priority = 4, description = "Test RAG Test Code Generation")
    public void testRAGTestCodeGeneration() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🧪 RAG-ENHANCED TEST CODE GENERATION");
        System.out.println("=".repeat(70));
        
        // Test various test scenarios
        String[] testScenarios = {
            "User login with valid credentials",
            "Add item to shopping cart",
            "Fill and submit contact form",
            "Navigate through multi-step checkout"
        };
        
        for (String scenario : testScenarios) {
            System.out.println("\n📝 Generating test for: " + scenario);
            
            if (ragAI != null) {
                String testCode = ragAI.generateTestCodeSuggestions(scenario, "functional test");
                System.out.println("🧠 RAG-Enhanced Test Code:");
                System.out.println(testCode);
                System.out.println("-".repeat(50));
            }
        }
    }
    
    @Test(priority = 5, description = "Test Custom Knowledge Addition")
    public void testCustomKnowledgeAddition() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📚 CUSTOM KNOWLEDGE ADDITION TEST");
        System.out.println("=".repeat(70));
        
        if (ragAI != null) {
            // Add custom knowledge about project-specific elements
            ragAI.addCustomKnowledge(
                "custom-login-pattern",
                "For our application, login elements use specific IDs: username-input, password-input, login-submit-btn. " +
                "The login form has class 'auth-form' and is inside a container with ID 'login-container'.",
                "project-specific-patterns",
                KnowledgeBase.DocumentCategory.LOCATOR_PATTERNS,
                "login", "authentication", "project", "custom"
            );
            
            System.out.println("✅ Added custom knowledge about project login patterns");
            
            // Test query that should use the custom knowledge
            String response = ragAI.generateLocatorSuggestions("login form elements", "our project");
            System.out.println("\n🧠 Response using custom knowledge:");
            System.out.println(response);
        }
    }
    
    @Test(priority = 6, description = "Test RAG Performance Metrics")
    public void testRAGPerformanceMetrics() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📊 RAG PERFORMANCE METRICS");
        System.out.println("=".repeat(70));
        
        if (ragAI != null && baseAI != null) {
            String testQuery = "Generate 5 locators for a submit button with error handling";
            
            // Test base AI performance
            long baseStartTime = System.currentTimeMillis();
            String baseResponse = baseAI.generateResponse(testQuery);
            long baseEndTime = System.currentTimeMillis();
            long baseResponseTime = baseEndTime - baseStartTime;
            
            // Test RAG AI performance
            long ragStartTime = System.currentTimeMillis();
            String ragResponse = ragAI.generateResponse(testQuery);
            long ragEndTime = System.currentTimeMillis();
            long ragResponseTime = ragEndTime - ragStartTime;
            
            // Compare results
            System.out.println("⚡ Performance Comparison:");
            System.out.println("  Base AI Response Time: " + baseResponseTime + "ms");
            System.out.println("  RAG AI Response Time: " + ragResponseTime + "ms");
            System.out.println("  Time Overhead: " + (ragResponseTime - baseResponseTime) + "ms");
            
            System.out.println("\n📏 Response Quality Comparison:");
            System.out.println("  Base AI Response Length: " + (baseResponse != null ? baseResponse.length() : 0) + " chars");
            System.out.println("  RAG AI Response Length: " + (ragResponse != null ? ragResponse.length() : 0) + " chars");
            
            if (ragResponse != null && baseResponse != null) {
                boolean ragHasMoreDetails = ragResponse.length() > baseResponse.length() * 1.2;
                boolean ragHasSourceRefs = ragResponse.contains("Knowledge Sources");
                
                System.out.println("  RAG Enhancement Detected: " + 
                    (ragHasMoreDetails ? "✅ More detailed" : "❓ Similar detail") + 
                    (ragHasSourceRefs ? ", ✅ Has source references" : ", ❓ No source references"));
            }
        }
    }
    
    /**
     * Demonstrate comparison between base AI and RAG-enhanced AI
     */
    private void demonstrateAIComparison(String query) {
        System.out.println("\n🤔 Query: " + query);
        System.out.println("-".repeat(50));
        
        // Base AI Response
        if (baseAI != null) {
            System.out.println("🤖 Base AI Response:");
            String baseResponse = baseAI.generateResponse(query);
            if (baseResponse != null && baseResponse.length() > 200) {
                System.out.println(baseResponse.substring(0, 200) + "...");
            } else {
                System.out.println(baseResponse);
            }
        }
        
        System.out.println("\n🧠 RAG-Enhanced AI Response:");
        
        // RAG-Enhanced Response
        if (ragAI != null) {
            String ragResponse = ragAI.generateResponse(query);
            if (ragResponse != null && ragResponse.length() > 300) {
                System.out.println(ragResponse.substring(0, 300) + "...");
            } else {
                System.out.println(ragResponse);
            }
        }
        
        System.out.println("=".repeat(50));
    }
    
    @AfterClass
    public void teardownRAGTesting() {
        System.out.println("\n🔧 RAG Testing Summary:");
        
        if (ragAI != null) {
            System.out.println("✅ RAG-Enhanced AI Tests Completed");
            System.out.println("📊 Knowledge Base Final State: " + ragAI.getKnowledgeBaseInfo());
            ragAI.close();
        }
        
        if (baseAI != null) {
            baseAI.close();
        }
        
        System.out.println("🎯 RAG testing demonstrates enhanced context-aware responses!");
        System.out.println("💡 Benefits: Better locators, detailed troubleshooting, comprehensive examples");
    }
}