package org.k11techlab.framework.ai.healing;

import org.k11techlab.framework.ai.llm.LLMInterface;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI-Powered Self-Healing Element Locator
 * 
 * Features:
 * - Intelligent locator generation using AI
 * - Self-healing when elements change
 * - Dynamic locator adaptation
 * - Fallback strategy management
 * - Learning from failures
 * 
 * @author K11 TechLab
 * @version 1.0
 */
public class AIElementHealer {
    
    private final LLMInterface aiProvider;
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final List<LocatorStrategy> healingHistory;
    
    public AIElementHealer(LLMInterface aiProvider, WebDriver driver) {
        this.aiProvider = aiProvider;
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.healingHistory = new ArrayList<>();
    }
    
    /**
     * Find element with AI-powered healing capabilities
     */
    public WebElement findElement(String elementDescription) {
        return findElement(elementDescription, null);
    }
    
    /**
     * Find element with AI-powered healing and custom timeout
     */
    public WebElement findElement(String elementDescription, Duration timeout) {
        WebDriverWait customWait = timeout != null ? 
            new WebDriverWait(driver, timeout) : wait;
        
        Log.info("🔍 AI Element Search: " + elementDescription);
        
        // Step 1: Try standard locator strategies
        List<By> standardLocators = generateStandardLocators(elementDescription);
        WebElement element = tryLocators(standardLocators, customWait, "Standard");
        if (element != null) return element;
        
        // Step 2: Try AI-generated locators
        if (aiProvider != null) {
            List<By> aiLocators = generateAILocators(elementDescription);
            element = tryLocators(aiLocators, customWait, "AI-Generated");
            if (element != null) {
                recordSuccessfulStrategy(elementDescription, element);
                return element;
            }
        }
        
        // Step 3: Try advanced healing strategies
        element = tryAdvancedHealing(elementDescription, customWait);
        if (element != null) return element;
        
        throw new NoSuchElementException("AI healing failed for: " + elementDescription);
    }
    
    /**
     * Generate standard locator strategies based on element description
     */
    private List<By> generateStandardLocators(String description) {
        List<By> locators = new ArrayList<>();
        String lowerDesc = description.toLowerCase();
        
        // Search-related elements
        if (lowerDesc.contains("search")) {
            if (lowerDesc.contains("input") || lowerDesc.contains("field")) {
                locators.add(By.name("q"));
                locators.add(By.name("search"));
                locators.add(By.id("search"));
                locators.add(By.className("search-input"));
                locators.add(By.cssSelector("input[type='search']"));
                locators.add(By.xpath("//input[contains(@placeholder, 'search') or contains(@placeholder, 'Search')]"));
            }
            if (lowerDesc.contains("button")) {
                locators.add(By.name("btnK"));
                locators.add(By.name("submit"));
                locators.add(By.cssSelector("button[type='submit']"));
                locators.add(By.xpath("//button[contains(text(), 'Search')]"));
                locators.add(By.xpath("//input[@type='submit']"));
            }
        }
        
        // Form elements
        if (lowerDesc.contains("email")) {
            locators.add(By.name("email"));
            locators.add(By.id("email"));
            locators.add(By.cssSelector("input[type='email']"));
            locators.add(By.xpath("//input[contains(@placeholder, 'email') or contains(@name, 'email')]"));
        }
        
        if (lowerDesc.contains("password")) {
            locators.add(By.name("password"));
            locators.add(By.id("password"));
            locators.add(By.cssSelector("input[type='password']"));
        }
        
        if (lowerDesc.contains("login") || lowerDesc.contains("submit")) {
            locators.add(By.cssSelector("button[type='submit']"));
            locators.add(By.xpath("//button[contains(text(), 'Login') or contains(text(), 'Submit')]"));
            locators.add(By.className("login-button"));
        }
        
        // Navigation elements
        if (lowerDesc.contains("link")) {
            locators.add(By.tagName("a"));
            locators.add(By.partialLinkText(extractKeyword(description)));
        }
        
        return locators;
    }
    
    /**
     * Generate AI-powered locators
     */
    private List<By> generateAILocators(String description) {
        List<By> locators = new ArrayList<>();
        
        try {
            String prompt = String.format(
                "Generate 5 diverse Selenium locator strategies for '%s'. " +
                "Return ONLY the locator expressions in this exact format:\n" +
                "By.xpath(\"//xpath/expression\")\n" +
                "By.cssSelector(\"css.selector\")\n" +
                "By.id(\"element-id\")\n" +
                "By.name(\"element-name\")\n" +
                "By.className(\"class-name\")\n" +
                "Make them specific and realistic for %s.",
                description, description
            );
            
            String aiResponse = aiProvider.generateResponse(prompt, 0.1f, 200);
            Log.info("🤖 AI Locator Response: " + aiResponse);
            
            locators = parseAILocators(aiResponse);
            
        } catch (Exception e) {
            Log.error("AI locator generation failed: " + e.getMessage());
        }
        
        return locators;
    }
    
    /**
     * Parse AI response into By locators
     */
    private List<By> parseAILocators(String aiResponse) {
        List<By> locators = new ArrayList<>();
        
        try {
            // Parse By.xpath patterns
            Pattern xpathPattern = Pattern.compile("By\\.xpath\\([\"'](.*?)[\"']\\)");
            Matcher xpathMatcher = xpathPattern.matcher(aiResponse);
            while (xpathMatcher.find()) {
                try {
                    locators.add(By.xpath(xpathMatcher.group(1)));
                } catch (Exception e) {
                    Log.info("Invalid xpath from AI: " + xpathMatcher.group(1));
                }
            }
            
            // Parse By.cssSelector patterns
            Pattern cssPattern = Pattern.compile("By\\.cssSelector\\([\"'](.*?)[\"']\\)");
            Matcher cssMatcher = cssPattern.matcher(aiResponse);
            while (cssMatcher.find()) {
                try {
                    locators.add(By.cssSelector(cssMatcher.group(1)));
                } catch (Exception e) {
                    Log.info("Invalid CSS selector from AI: " + cssMatcher.group(1));
                }
            }
            
            // Parse By.id patterns
            Pattern idPattern = Pattern.compile("By\\.id\\([\"'](.*?)[\"']\\)");
            Matcher idMatcher = idPattern.matcher(aiResponse);
            while (idMatcher.find()) {
                locators.add(By.id(idMatcher.group(1)));
            }
            
            // Parse By.name patterns
            Pattern namePattern = Pattern.compile("By\\.name\\([\"'](.*?)[\"']\\)");
            Matcher nameMatcher = namePattern.matcher(aiResponse);
            while (nameMatcher.find()) {
                locators.add(By.name(nameMatcher.group(1)));
            }
            
            // Parse By.className patterns
            Pattern classPattern = Pattern.compile("By\\.className\\([\"'](.*?)[\"']\\)");
            Matcher classMatcher = classPattern.matcher(aiResponse);
            while (classMatcher.find()) {
                locators.add(By.className(classMatcher.group(1)));
            }
            
        } catch (Exception e) {
            Log.error("Error parsing AI locators: " + e.getMessage());
        }
        
        return locators;
    }
    
    /**
     * Try advanced healing strategies
     */
    private WebElement tryAdvancedHealing(String description, WebDriverWait customWait) {
        Log.info("🛠️ Attempting advanced AI healing...");
        
        if (aiProvider == null) return null;
        
        try {
            // Get page context for better healing
            String pageTitle = driver.getTitle();
            String pageUrl = driver.getCurrentUrl();
            
            String healingPrompt = String.format(
                "I need to find '%s' on a webpage titled '%s' at URL '%s'. " +
                "The standard locators failed. Analyze this context and suggest 3 alternative " +
                "Selenium locator strategies that might work. Consider partial matches, " +
                "contains() functions, and flexible selectors.",
                description, pageTitle, pageUrl
            );
            
            String healingResponse = aiProvider.generateResponse(healingPrompt, 0.3f, 250);
            Log.info("🤖 AI Healing Response: " + healingResponse);
            
            List<By> healingLocators = parseAILocators(healingResponse);
            return tryLocators(healingLocators, customWait, "AI-Healing");
            
        } catch (Exception e) {
            Log.error("Advanced healing failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Try a list of locators and return first successful element
     */
    private WebElement tryLocators(List<By> locators, WebDriverWait customWait, String strategyType) {
        for (By locator : locators) {
            try {
                WebElement element = customWait.until(ExpectedConditions.presenceOfElementLocated(locator));
                Log.info("✅ " + strategyType + " locator success: " + locator);
                System.out.println("✅ Found element using " + strategyType + ": " + locator);
                return element;
            } catch (Exception e) {
                Log.info("❌ " + strategyType + " locator failed: " + locator);
            }
        }
        return null;
    }
    
    /**
     * Record successful strategy for learning
     */
    private void recordSuccessfulStrategy(String description, WebElement element) {
        try {
            LocatorStrategy strategy = new LocatorStrategy(
                description,
                element.getTagName(),
                element.getAttribute("id"),
                element.getAttribute("class"),
                element.getText()
            );
            healingHistory.add(strategy);
            
            Log.info("📚 Recorded successful strategy for: " + description);
        } catch (Exception e) {
            Log.error("Failed to record strategy: " + e.getMessage());
        }
    }
    
    /**
     * Extract keyword from description for partial matching
     */
    private String extractKeyword(String description) {
        String[] words = description.toLowerCase().split("\\s+");
        for (String word : words) {
            if (word.length() > 3 && !word.equals("button") && !word.equals("field") && !word.equals("input")) {
                return word;
            }
        }
        return words.length > 0 ? words[0] : "link";
    }
    
    /**
     * Get AI-powered debugging information for failed locators
     */
    public String getDiagnostics(String description) {
        if (aiProvider == null) {
            return "AI not available for diagnostics";
        }
        
        try {
            String pageSource = driver.getPageSource();
            String truncatedSource = pageSource.length() > 500 ? 
                pageSource.substring(0, 500) + "..." : pageSource;
            
            String diagnosticPrompt = String.format(
                "Diagnose why element '%s' cannot be found. Page source excerpt: %s. " +
                "Suggest what might have changed and provide 3 alternative locator approaches.",
                description, truncatedSource
            );
            
            return aiProvider.generateResponse(diagnosticPrompt, 0.2f, 300);
            
        } catch (Exception e) {
            return "Diagnostics failed: " + e.getMessage();
        }
    }
    
    /**
     * Inner class to track successful locator strategies
     */
    private static class LocatorStrategy {
        final String description;
        final String tagName;
        final String id;
        final String className;
        final String text;
        
        LocatorStrategy(String description, String tagName, String id, String className, String text) {
            this.description = description;
            this.tagName = tagName;
            this.id = id;
            this.className = className;
            this.text = text;
        }
    }
}