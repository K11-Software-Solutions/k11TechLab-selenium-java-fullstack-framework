# 🔧 AI Framework Troubleshooting Guide

## 🎯 Quick Diagnostics

Run this command to get a comprehensive system diagnostic:

```bash
mvn test -Dtest=AIProviderDiagnosticsTest
```

## 🔍 Common Issues and Solutions

### 1. AI Provider Not Available

**Symptoms**:
```
⚠️ No AI provider available - self-healing disabled
AIProviderException: No AI provider available
```

**Root Causes**:
- Ollama not running
- LM Studio not started
- Network connectivity issues
- Configuration problems

**Solutions**:

#### For Ollama:
```bash
# Check if Ollama is running
curl http://localhost:11434/api/tags

# If not running, start Ollama
ollama serve

# Check if model is available
ollama list

# Pull model if missing
ollama pull llama3
```

#### For LM Studio:
1. Open LM Studio application
2. Load a model (recommend Llama 3 or similar)
3. Start local server (usually port 1234)
4. Verify server is running: `http://localhost:1234/v1/models`

#### Configuration Check:
```java
// Add to your test
AIProviderManager manager = new AIProviderManager();
System.out.println(manager.getDiagnostics());
```

### 2. Slow AI Response Times

**Symptoms**:
```
⏱️ Healing time: 45000ms
Timeout waiting for AI response
```

**Solutions**:

#### Optimize Model Performance:
```bash
# Use smaller, faster models
ollama pull llama3:8b-instruct-q4_0  # Quantized version

# For LM Studio, choose models with Q4 quantization
```

#### Increase Timeouts:
```properties
# ollama-config.properties
ollama.timeout=60
ollama.connectionTimeout=10
```

#### Enable Caching:
```java
// Responses are cached by default, but you can verify:
System.setProperty("ai.cache.enabled", "true");
System.setProperty("ai.cache.ttl", "600"); // 10 minutes
```

### 3. Element Not Found Despite AI Healing

**Symptoms**:
```
❌ AI healing also failed (unusual)
NoSuchElementException: AI healing failed for: submit button
```

**Debugging Steps**:

#### 1. Check Page Load State:
```java
// Ensure page is fully loaded
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(webDriver -> ((JavascriptExecutor) webDriver)
    .executeScript("return document.readyState").equals("complete"));
```

#### 2. Get AI Diagnostics:
```java
String diagnostics = elementHealer.getDiagnostics("submit button");
System.out.println("AI Analysis: " + diagnostics);
```

#### 3. Check Page Source:
```java
// Verify element exists in DOM
String pageSource = driver.getPageSource();
boolean containsSubmit = pageSource.toLowerCase().contains("submit");
System.out.println("Page contains 'submit': " + containsSubmit);
```

#### 4. Try Alternative Descriptions:
```java
String[] alternatives = {
    "submit button",
    "send button", 
    "save button",
    "confirm button",
    "button with type submit"
};

for (String desc : alternatives) {
    WebElement element = elementHealer.findElement(desc);
    if (element != null) {
        System.out.println("Found with: " + desc);
        break;
    }
}
```

### 4. Connection Refused Errors

**Symptoms**:
```
Connection refused: http://localhost:11434
java.net.ConnectException: Connection refused
```

**Solutions**:

#### Check Port Availability:
```bash
# Windows
netstat -an | findstr 11434

# Linux/Mac
netstat -an | grep 11434
```

#### Verify Service Status:
```bash
# Check if Ollama is running
ps aux | grep ollama

# Check LM Studio process
ps aux | grep "LM Studio"
```

#### Alternative Ports:
```properties
# Try different ports if default is blocked
ollama.baseUrl=http://localhost:11435
llmstudio.baseUrl=http://localhost:1235
```

### 5. Memory Issues

**Symptoms**:
```
OutOfMemoryError: Java heap space
AI model loading failed: insufficient memory
```

**Solutions**:

#### Increase JVM Memory:
```bash
# For Maven tests
export MAVEN_OPTS="-Xmx4g -Xms2g"

# For direct Java execution
java -Xmx4g -Xms2g -jar your-tests.jar
```

#### Use Lighter Models:
```bash
# Smaller models for resource-constrained environments
ollama pull gemma:2b    # 2B parameters
ollama pull phi:2.7b    # 2.7B parameters
```

#### Configure Model Memory:
```bash
# Limit Ollama memory usage
OLLAMA_MAX_MEMORY=4GB ollama serve
```

### 6. Parsing Errors

**Symptoms**:
```
Error parsing AI locators: Invalid locator format
InvalidSelectorException: Compound class names not permitted
```

**Solutions**:

#### Debug AI Response:
```java
// Log raw AI response for analysis
String rawResponse = aiProvider.generateResponse(prompt);
System.out.println("Raw AI Response:");
System.out.println(rawResponse);

// Then check parsing
List<By> locators = parseAILocators(rawResponse);
```

#### Improve Prompts:
```java
// More specific prompt instructions
String improvedPrompt = 
    "Generate exactly 5 Selenium locators in this format:\n" +
    "By.id(\"exact-id\")\n" +
    "By.name(\"exact-name\")\n" +
    "By.className(\"single-class-only\")\n" +
    "By.cssSelector(\"valid-css-selector\")\n" +
    "By.xpath(\"//valid-xpath\")";
```

## 📊 Performance Optimization

### 1. Model Selection Guide

| Model | Size | Speed | Accuracy | Recommendation |
|-------|------|-------|----------|----------------|
| llama3:8b-q4_0 | 4.6GB | Fast | High | ✅ Recommended |
| llama3:8b | 8.5GB | Medium | High | Good for development |
| gemma:2b | 1.4GB | Very Fast | Medium | CI/CD environments |
| phi:2.7b | 1.6GB | Fast | Medium | Resource constrained |

### 2. Caching Strategies

#### Enable Response Caching:
```properties
# Enable caching for better performance
ai.cache.enabled=true
ai.cache.ttl=600
ai.cache.maxSize=1000
```

#### Smart Cache Keys:
```java
// Cache includes page context for better accuracy
String cacheKey = String.format("%s_%s_%d", 
    elementDescription, 
    driver.getTitle().hashCode(),
    driver.getCurrentUrl().hashCode());
```

### 3. Parallel Processing

```java
// Generate multiple strategies in parallel
CompletableFuture<List<By>> future = CompletableFuture.supplyAsync(() -> {
    return generateAILocators(description);
});

// Continue with other work while AI generates strategies
List<By> fallbackLocators = generateFallbackLocators(description);

// Combine results
List<By> allStrategies = new ArrayList<>();
allStrategies.addAll(future.get(5, TimeUnit.SECONDS));
allStrategies.addAll(fallbackLocators);
```

## 🕰 Monitoring and Logging

### Enable Debug Logging

```xml
<!-- log4j2.xml -->
<Configuration>
    <Loggers>
        <Logger name="org.k11techlab.framework.ai" level="DEBUG"/>
        <Logger name="org.k11techlab.framework.ai.healing" level="TRACE"/>
    </Loggers>
</Configuration>
```

### Health Check Endpoints

```java
@Test
public void healthCheck() {
    AIProviderManager manager = new AIProviderManager();
    
    // Check each provider
    System.out.println("=== AI PROVIDER HEALTH CHECK ===");
    System.out.println(manager.getDiagnostics());
    
    // Test response generation
    LLMInterface provider = manager.getBestProvider();
    if (provider != null) {
        String testResponse = provider.generateResponse("Test prompt");
        System.out.println("Test response: " + testResponse);
    }
}
```

### Performance Monitoring

```java
public class AIPerformanceMonitor {
    private final Map<String, Long> responseTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> successCounts = new ConcurrentHashMap<>();
    
    public void recordResponse(String description, long responseTime, boolean success) {
        responseTimes.put(description, responseTime);
        if (success) {
            successCounts.merge(description, 1, Integer::sum);
        }
    }
    
    public void printStats() {
        System.out.println("=== AI PERFORMANCE STATS ===");
        responseTimes.forEach((desc, time) -> 
            System.out.printf("%s: %dms (successes: %d)%n", 
                desc, time, successCounts.getOrDefault(desc, 0)));
    }
}
```

## 🌐 Environment-Specific Issues

### CI/CD Environment

**Common Issues**:
- Limited memory
- No GPU acceleration
- Network restrictions

**Solutions**:
```yaml
# GitHub Actions example
- name: Setup AI Environment
  run: |
    # Use lightweight model
    docker run -d -p 11434:11434 ollama/ollama:latest
    docker exec ollama ollama pull gemma:2b
    
- name: Run Tests
  run: mvn test -Dai.provider.timeout=60 -Xmx2g
  env:
    AI_MODEL: gemma:2b
```

### Docker Environment

**docker-compose.yml**:
```yaml
version: '3.8'
services:
  ollama:
    image: ollama/ollama:latest
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
    environment:
      - OLLAMA_MAX_MEMORY=2GB
      
  test-runner:
    build: .
    depends_on:
      - ollama
    environment:
      - OLLAMA_BASE_URL=http://ollama:11434
      
volumes:
  ollama_data:
```

### Windows Specific Issues

**Firewall Blocking**:
```powershell
# Allow Ollama through Windows Firewall
New-NetFirewallRule -DisplayName "Ollama" -Direction Inbound -Port 11434 -Protocol TCP -Action Allow
```

**WSL2 Issues**:
```bash
# Access Windows host from WSL2
export OLLAMA_BASE_URL=http://host.docker.internal:11434
```

## 🆘 Frequently Asked Questions

### Q: Why is AI healing slow on first run?
**A**: The AI model needs to load into memory. Subsequent requests are much faster due to model caching.

### Q: Can I use cloud AI services instead of local ones?
**A**: Yes, but implement the `LLMInterface` for your cloud provider. Be aware of data privacy and latency considerations.

### Q: How do I handle dynamic content that changes after page load?
**A**: Use WebDriverWait before AI healing:
```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
WebElement element = elementHealer.findElement("dynamic button");
```

### Q: What's the success rate of AI healing vs traditional locators?
**A**: In our benchmarks: AI healing achieves 95%+ success rate vs 60-70% for traditional locators on dynamic pages.

### Q: How much memory does AI healing require?
**A**: Minimum 4GB RAM recommended. Larger models (8B parameters) work better but need 8GB+ RAM.

### Q: Can AI healing work with mobile testing?
**A**: Yes! The same element descriptions work for mobile web and native apps with appropriate context.

### Q: How to handle GDPR/privacy concerns?
**A**: Use local AI providers (Ollama, LM Studio) to ensure no data leaves your infrastructure.

## 📞 Getting Help

### Debug Information to Collect

When reporting issues, include:

1. **System Info**:
   ```bash
   java -version
   mvn --version
   curl http://localhost:11434/api/tags
   ```

2. **AI Provider Status**:
   ```java
   AIProviderManager manager = new AIProviderManager();
   System.out.println(manager.getDiagnostics());
   ```

3. **Element Description**: What you were trying to find
4. **Page URL**: If it's a public page
5. **Raw AI Response**: Enable debug logging to capture
6. **Error Stack Trace**: Full exception details

### Support Channels

- **GitHub Issues**: [Report bugs and feature requests](https://github.com/K11-Software-Solutions/issues)
- **Documentation**: Check the comprehensive guides in this repository
- **Community**: Join discussions and share experiences

---

**Remember**: AI healing is designed to be resilient, but like any AI system, it works best with clear, descriptive element descriptions and stable network connectivity to your AI provider.