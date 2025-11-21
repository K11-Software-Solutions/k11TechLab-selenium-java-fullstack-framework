# 🚀 Getting Started with AI-Enhanced Testing

## Quick Setup Guide

This guide helps you quickly set up and start using AI features in your existing K11 TechLab framework.

## 📦 Step 1: Add AI Dependencies to pom.xml

Add these dependencies to your existing `pom.xml`:

```xml
<!-- AI/ML Dependencies -->
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>5.2.1</version>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>

<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-core</artifactId>
    <version>9.8.0</version>
</dependency>

<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-analyzers-common</artifactId>
    <version>9.8.0</version>
</dependency>
```

## 🛠️ Step 2: Install Ollama (Local LLM)

### Option A: Windows Installation
1. Download Ollama from https://ollama.ai/download/windows
2. Install and run Ollama
3. Open PowerShell and run:
```powershell
ollama pull llama3:8b
ollama pull codellama:7b
```

### Option B: Alternative - LM Studio (GUI-based)
1. Download from https://lmstudio.ai/
2. Install and open LM Studio
3. Download a suitable model (Llama 3 8B or CodeLlama 7B)
4. Start the local server

## 🧪 Step 3: Test AI Integration

Run the AI demo test to verify everything works:

```bash
mvn clean test -Dtest=AIEnhancedTestDemo
```

## 📝 Step 4: Start Using AI Features

### Basic Usage Example

```java
public class YourTest extends AIEnhancedBaseTest {
    
    @Test
    public void testWithAI() {
        getDriver().get("https://example.com");
        
        // Get AI-suggested locators
        List<String> locators = getSmartLocators("login button");
        
        // Get wait strategy suggestions
        String waitAdvice = getWaitSuggestion("button", "click");
        
        // Generate test data
        String testData = generateTestData("user", "login test");
        
        // Your test logic here...
    }
}
```

### Voice Commands (Windows)

1. Enable Windows Voice Recognition
2. Use voice commands like:
   - "Generate test for login page"
   - "Create page object for checkout"
   - "Suggest wait strategy for button click"

## 🔧 Step 5: Configuration

Create `ai-config.properties` in your `src/main/resources/`:

```properties
# AI Configuration
ai.enabled=true
ai.provider=ollama
ai.model=llama3:8b
ai.url=http://localhost:11434
ai.temperature=0.3
ai.max.tokens=1500

# RAG Configuration
rag.index.enabled=true
rag.index.paths=src/main/java,src/test/java
rag.refresh.interval=3600
```

## 📊 Features Available

### 🎯 Smart Locator Generation
- Analyzes page structure
- Suggests stable locators
- Validates locator quality
- Provides alternatives

### 🤖 Test Case Generation
- Creates tests from descriptions
- Follows existing patterns
- Includes proper assertions
- Handles error cases

### ⏱️ Intelligent Wait Strategies  
- Context-aware suggestions
- Framework-specific recommendations
- Performance optimized
- Reduces flaky tests

### 📈 Failure Analysis
- Root cause identification
- Fix suggestions
- Pattern recognition
- Prevention advice

### 🗣️ Voice Integration
- Natural language commands
- Hands-free test creation
- Voice-to-code generation
- Scenario dictation

## 🚨 Troubleshooting

### AI Not Available
```
Check if Ollama is running: ollama list
Test connectivity: curl http://localhost:11434/api/tags
```

### Performance Issues
- Use smaller models (7B instead of 13B)
- Increase timeout values
- Reduce context size

### Memory Issues
- Close other applications
- Use GPU acceleration if available
- Consider cloud deployment

## 📚 Next Steps

1. **Explore Examples**: Look at `AIEnhancedTestDemo.java`
2. **Customize Prompts**: Modify generation templates
3. **Add More Models**: Try different LLMs for comparison
4. **Integrate CI/CD**: Add AI analysis to your pipeline
5. **Train Custom Models**: Fine-tune on your codebase

## 🎮 Quick Commands

```bash
# Start Ollama service
ollama serve

# Test AI health
java -cp target/test-classes org.k11techlab.framework.ai.HealthChecker

# Generate page object
java -cp target/test-classes org.k11techlab.framework.ai.generators.PageObjectGenerator "https://example.com" "LoginPage"

# Analyze test failures
java -cp target/test-classes org.k11techlab.framework.ai.FailureAnalyzer "target/surefire-reports/"
```

## 🔐 Security Notes

- All processing happens locally (no data leaves your machine)
- No API keys or external services required
- Complete offline capability
- Full control over data and models

## 💡 Tips for Success

1. **Start Small**: Begin with locator suggestions and test data
2. **Iterate**: Refine prompts based on results
3. **Combine**: Use AI with manual expertise
4. **Monitor**: Track AI suggestions accuracy
5. **Customize**: Adapt to your specific domain and patterns

Happy AI-Enhanced Testing! 🚀