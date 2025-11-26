# LM Studio Setup Guide for K11 Tech Lab Test Automation Framework

## Overview
This guide helps you integrate LM Studio with the K11 Tech Lab Selenium Java Test Automation Framework for AI-powered testing capabilities.

## Prerequisites

### 1. Download and Install LM Studio
- Visit: https://lmstudio.ai/
- Download the appropriate version for your OS (Windows, macOS, Linux)
- Install LM Studio following the standard installation process

### 2. Download a Compatible Model
Popular options for code generation and testing:
- **Code Llama 7B Instruct** (Recommended for testing)
- **Llama 2 7B Chat** (Good general purpose)
- **Mistral 7B Instruct** (Fast and efficient)
- **DeepSeek Coder 6.7B** (Excellent for code tasks)

### 3. Start LM Studio Server
1. Open LM Studio
2. Go to the "Local Server" tab
3. Select your downloaded model
4. Click "Start Server"
5. Note the server URL (usually http://localhost:1234)

## Configuration

### 1. Update LM Studio Config
Edit `config/llmstudio-config.properties`:
```properties
# Update these based on your setup
llmstudio.base.url=http://localhost:1234
llmstudio.model.name=your-exact-model-name
llmstudio.timeout.seconds=90
```

### 2. Find Your Model Name
In LM Studio:
1. Go to "Local Server" tab
2. The model name is shown when you select a model
3. Use the exact name (case-sensitive)

Common model names:
- `llama-2-7b-chat`
- `codellama-7b-instruct` 
- `mistral-7b-instruct-v0.1`
- `local-model` (LM Studio default)

## Usage Examples

### 1. Basic AI Provider Manager Usage
```java
AIProviderManager aiManager = new AIProviderManager();

// Get best available provider (will try LM Studio if available)
LLMInterface provider = aiManager.getBestProvider();

// Request specific LM Studio provider
LLMInterface lmStudio = aiManager.getProvider(AIProviderManager.Provider.LLMSTUDIO);
```

### 2. Generate Test Locators
```java
String locators = provider.generateResponse(
    "Generate 3 Selenium locators for a login button: By.id, By.cssSelector, By.xpath",
    0.3f, 150
);
```

### 3. Generate Test Cases
```java
String testCase = provider.generateResponse(
    "Create a complete Selenium test for user registration with email validation",
    0.4f, 300
);
```

## Running Tests

### 1. Individual LM Studio Tests
```bash
mvn test -Dtest=LMStudioAITest
```

### 2. Full AI Demo with Multiple Providers
```bash
mvn test -Dtest=FullAIDemoUpdated
```

### 3. Quick AI Test
```bash
mvn test -Dtest=QuickAITest
```

## Troubleshooting

### Problem: "LM Studio not available"
**Solutions:**
1. Verify LM Studio server is running
2. Check the server URL in config (http://localhost:1234)
3. Ensure the model name matches exactly
4. Try increasing timeout in config

### Problem: "Model not found"
**Solutions:**
1. Use exact model name from LM Studio
2. Try `local-model` as a generic name
3. Check model is loaded in LM Studio server tab

### Problem: Slow responses
**Solutions:**
1. Use smaller models (7B instead of 13B+)
2. Increase timeout settings
3. Reduce max_tokens parameter
4. Use lower temperature for faster generation

### Problem: Server connection failed
**Solutions:**
1. Check Windows Firewall/Antivirus
2. Try 127.0.0.1 instead of localhost
3. Verify port 1234 is not blocked
4. Restart LM Studio

## Model Recommendations

### For Testing/QA Tasks:
1. **Code Llama 7B Instruct** - Best for code generation and test creation
2. **DeepSeek Coder 6.7B** - Excellent code understanding
3. **Llama 2 7B Chat** - Good general purpose with decent coding skills

### For Performance:
- Use 7B models for faster responses
- 13B+ models for higher quality but slower responses
- Quantized models (4-bit) for lower memory usage

## Advanced Configuration

### Custom Model Parameters
```java
LLMStudioClient customClient = new LLMStudioClient(
    "http://localhost:1234",     // Server URL
    "your-custom-model",         // Model name
    120                          // Timeout seconds
);
```

### Multiple Model Support
You can run multiple LM Studio instances on different ports:
- Port 1234: Code generation model
- Port 1235: General chat model
- Configure different clients for each

## Integration with CI/CD

### GitHub Actions Example
```yaml
- name: Setup LM Studio (Optional)
  run: |
    # Only run if LM Studio available
    if command -v lmstudio &> /dev/null; then
      lmstudio start-server --model codellama-7b &
      sleep 30
    fi
    
- name: Run AI Tests with Fallback
  run: mvn test -Dtest=*AITest*
```

The framework automatically falls back to Ollama or Simple AI if LM Studio is not available, making it CI/CD friendly.

## Performance Tips

1. **Model Selection**: Use 7B models for development, 13B+ for production
2. **Temperature Settings**: 0.1 for precise code, 0.7 for creative suggestions
3. **Token Limits**: 50-100 for locators, 200-400 for test cases
4. **Caching**: Consider caching frequent AI responses for better performance
5. **Batch Requests**: Group similar requests together when possible

## Security Considerations

1. LM Studio runs locally - no data sent to external servers
2. All AI processing happens on your machine
3. Perfect for confidential/proprietary test scenarios
4. No API keys or external dependencies required

## Support

For issues specific to:
- **LM Studio**: Check LM Studio documentation and community
- **Framework Integration**: Refer to framework logs and diagnostics
- **Model Issues**: Try different models or parameters

The framework provides comprehensive diagnostics via:
```java
System.out.println(aiManager.getAllProviderDiagnostics());
```