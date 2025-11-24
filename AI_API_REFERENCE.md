# 📚 AI Framework API Reference

## Overview

This document provides detailed API reference for the AI-powered self-healing test automation framework.

## 🧠 Core Classes

### AIElementHealer

The main class for AI-powered element location and self-healing capabilities.

#### Constructor

```java
public AIElementHealer(LLMInterface aiProvider, WebDriver driver)
```

**Parameters:**
- `aiProvider`: The AI provider interface for generating locator strategies
- `driver`: WebDriver instance for element interaction

#### Methods

##### findElement(String description)

```java
public WebElement findElement(String description)
```

Finds a single element using AI-powered natural language description.

**Parameters:**
- `description`: Natural language description of the element (e.g., "submit button", "email input field")

**Returns:** `WebElement` or `null` if not found

**Example:**
```java
WebElement button = healer.findElement("login button");
WebElement input = healer.findElement("search input box");
```

##### findElement(String description, String context)

```java
public WebElement findElement(String description, String context)
```

Finds element with additional context for better accuracy.

**Parameters:**
- `description`: Element description
- `context`: Additional context (e.g., "in header navigation", "on checkout page")

**Example:**
```java
WebElement cart = healer.findElement("cart icon", "in top navigation bar");
```

##### findElements(String description)

```java
public List<WebElement> findElements(String description)
```

Finds multiple elements matching the description.

**Returns:** `List<WebElement>` of matching elements

**Example:**
```java
List<WebElement> products = healer.findElements("product cards");
List<WebElement> links = healer.findElements("navigation links");
```

##### getDiagnostics(String elementDescription)

```java
public String getDiagnostics(String elementDescription)
```

Provides AI-powered diagnostic information about why an element search failed.

**Returns:** Diagnostic information and suggestions

**Example:**
```java
String diagnostics = healer.getDiagnostics("missing button");
System.out.println(diagnostics);
```

##### getHealingHistory()

```java
public List<LocatorStrategy> getHealingHistory()
```

Returns history of healing attempts for analysis and debugging.

**Returns:** List of locator strategies that were attempted

---

### AIProviderManager

Manages multiple AI providers with automatic fallback capabilities.

#### Constructor

```java
public AIProviderManager()
```

Initializes the provider manager and scans for available AI providers.

#### Methods

##### getBestProvider()

```java
public LLMInterface getBestProvider()
```

Returns the best available AI provider based on availability and performance.

**Provider Priority:**
1. Ollama (if available)
2. LM Studio (if available) 
3. Simple AI (fallback)

**Returns:** `LLMInterface` instance

##### getProvider(String name)

```java
public LLMInterface getProvider(String name)
```

Returns a specific AI provider by name.

**Parameters:**
- `name`: Provider name ("ollama", "lmstudio", "simple")

**Returns:** `LLMInterface` instance or `null` if not available

##### getDiagnostics()

```java
public String getDiagnostics()
```

Provides diagnostic information about all AI providers.

**Returns:** Formatted diagnostic report

##### close()

```java
public void close()
```

Closes all AI provider connections and releases resources.

---

### LLMInterface

Interface that all AI providers must implement.

#### Methods

##### generateResponse(String prompt)

```java
String generateResponse(String prompt)
```

Generates AI response for the given prompt.

**Parameters:**
- `prompt`: The prompt/question for the AI

**Returns:** AI-generated response string

##### generateResponse(String prompt, float temperature, int maxTokens)

```java
String generateResponse(String prompt, float temperature, int maxTokens)
```

Generates AI response with specific parameters.

**Parameters:**
- `prompt`: The prompt/question for the AI
- `temperature`: Creativity level (0.0-1.0, lower = more focused)
- `maxTokens`: Maximum response length

**Returns:** AI-generated response string

##### isAvailable()

```java
boolean isAvailable()
```

Checks if the AI provider is available and responsive.

**Returns:** `true` if available, `false` otherwise

##### getModelInfo()

```java
String getModelInfo()
```

Returns information about the AI model being used.

**Returns:** Model information string

##### close()

```java
void close()
```

Closes the AI provider connection and releases resources.

---

## 🔧 Configuration Classes

### OllamaClient

AI provider implementation for Ollama.

#### Configuration Properties

```properties
# ollama-config.properties
ollama.baseUrl=http://localhost:11434
ollama.model=llama3
ollama.timeout=30
ollama.maxRetries=3
```

#### Methods

```java
// Check if Ollama is running
public boolean isOllamaRunning()

// Get available models
public List<String> getAvailableModels()

// Test model response
public String testModel(String testPrompt)
```

### LLMStudioClient

AI provider implementation for LM Studio.

#### Configuration Properties

```properties
# llmstudio-config.properties
llmstudio.baseUrl=http://localhost:1234
llmstudio.timeout=30
llmstudio.maxRetries=3
```

#### Methods

```java
// Check server status
public boolean isServerRunning()

// Get loaded model info
public String getLoadedModel()

// Test connection
public boolean testConnection()
```

---

## 🏗️ Data Classes

### LocatorStrategy

Represents a locator strategy attempt.

```java
public class LocatorStrategy {
    private String description;
    private By locator;
    private boolean successful;
    private long executionTime;
    private String errorMessage;
    
    // Getters and setters...
}
```

**Fields:**
- `description`: Human-readable description of what was searched
- `locator`: The Selenium `By` locator that was generated
- `successful`: Whether this strategy found the element
- `executionTime`: Time taken to execute this strategy (ms)
- `errorMessage`: Error message if the strategy failed

---

## 🎯 Usage Patterns

### Basic Element Finding

```java
// Initialize healer
AIElementHealer healer = new AIElementHealer(aiProvider, driver);

// Find elements
WebElement button = healer.findElement("submit button");
WebElement input = healer.findElement("email input");
WebElement link = healer.findElement("forgot password link");
```

### Error Handling Pattern

```java
try {
    WebElement element = healer.findElement("target element");
    if (element != null) {
        element.click();
    } else {
        // Element not found, get diagnostics
        String diagnostics = healer.getDiagnostics("target element");
        Log.info("Diagnostics: " + diagnostics);
        
        // Fallback to traditional locator
        element = driver.findElement(By.id("fallback-id"));
        element.click();
    }
} catch (Exception e) {
    Log.error("Element interaction failed: " + e.getMessage());
    throw e;
}
```

### Multi-Strategy Pattern

```java
String[] strategies = {
    "login button",
    "sign in button", 
    "authenticate button",
    "user login control"
};

WebElement authButton = null;
for (String strategy : strategies) {
    authButton = healer.findElement(strategy);
    if (authButton != null) {
        Log.info("Found element using strategy: " + strategy);
        break;
    }
}

if (authButton != null) {
    authButton.click();
} else {
    throw new NoSuchElementException("Authentication button not found with any strategy");
}
```

### Performance Monitoring Pattern

```java
long startTime = System.currentTimeMillis();
WebElement element = healer.findElement("search button");
long healingTime = System.currentTimeMillis() - startTime;

Log.info(String.format("AI healing completed in %d ms", healingTime));

// Get healing history for analysis
List<LocatorStrategy> history = healer.getHealingHistory();
for (LocatorStrategy strategy : history) {
    Log.info(String.format("Strategy: %s, Success: %s, Time: %d ms", 
        strategy.getDescription(), 
        strategy.isSuccessful(), 
        strategy.getExecutionTime()));
}
```

---

## 🚨 Exception Handling

### Custom Exceptions

```java
// AI provider not available
throw new AIProviderException("No AI provider available");

// AI healing failed
throw new HealingException("AI healing failed for: " + description);

// Configuration error
throw new ConfigurationException("Invalid AI provider configuration");
```

### Exception Hierarchy

```
AIFrameworkException
├── AIProviderException
├── HealingException
├── ConfigurationException
└── TimeoutException
```

---

## 🔍 Debugging and Logging

### Enable Debug Logging

```java
// System property
System.setProperty("ai.framework.debug", "true");

// Programmatically
Logger aiLogger = LoggerFactory.getLogger("org.k11techlab.framework.ai");
aiLogger.setLevel(Level.DEBUG);
```

### Log Output Examples

```
INFO  AIProviderManager - Scanning for AI providers...
INFO  OllamaClient - Ollama connection successful: llama3 model loaded
DEBUG AIElementHealer - Generating locators for: 'submit button'
DEBUG AIElementHealer - Trying locator: By.id("submit-btn")
INFO  AIElementHealer - Element found using: By.name("submit")
```

---

## 🎯 Best Practices

### Element Description Guidelines

```java
// ✅ Good - Specific and clear
healer.findElement("email input field");
healer.findElement("submit button");
healer.findElement("user profile dropdown");

// ✅ Better - Include context
healer.findElement("search button in header");
healer.findElement("add to cart button on product page");

// ❌ Avoid - Too vague
healer.findElement("button");
healer.findElement("input");
```

### Performance Tips

- Reuse `AIElementHealer` instances
- Cache AI providers across tests
- Use specific descriptions to reduce AI processing time
- Monitor healing history to optimize descriptions

### Memory Management

```java
@AfterClass
public void cleanup() {
    if (aiManager != null) {
        aiManager.close();
    }
}
```

This completes the comprehensive API reference for the AI self-healing framework.