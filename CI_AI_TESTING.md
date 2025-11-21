# 🤖 AI Testing in GitHub Actions

This document explains how to run AI tests in CI/CD pipelines using GitHub Actions.

## 🚀 Available Workflows

### 1. **Fast AI Tests** (`fast-ai-test.yml`)
- **Trigger**: Push/PR to AI-related files
- **Duration**: ~5-10 minutes
- **Features**: 
  - ✅ Compiles AI framework
  - ✅ Runs SimpleAIClient tests (no Ollama needed)
  - ✅ Validates framework integrity
  - ❌ No real LLM integration

### 2. **Full AI Tests** (`ai-testing.yml`)
- **Trigger**: Push to main, manual dispatch
- **Duration**: ~15-25 minutes
- **Features**:
  - ✅ Installs Ollama + AI model
  - ✅ Runs complete AI test suite
  - ✅ Tests real LLM integration
  - ✅ Generates AI reports

## 🔧 Workflow Configuration

### Environment Variables
```yaml
env:
  OLLAMA_HOST: http://localhost:11434
  AI_MODEL: tinyllama  # or llama3 for full features
  AI_TIMEOUT: 120000
```

### System Properties
```bash
mvn test -Dai.model=tinyllama -Dollama.host=http://localhost:11434
```

## 📊 Test Categories

### Standard Tests (No AI)
```bash
mvn test -Dtest='!org.k11techlab.framework_unittests.aiTests.**'
```

### AI Tests with Ollama
```bash
mvn test -Dtest="org.k11techlab.framework_unittests.aiTests.**"
```

### AI Fallback Tests
```bash
mvn test -Dtest="SimpleAIDemo"
```

## 🎯 CI Models

### TinyLlama (Recommended for CI)
- **Size**: ~1.1GB
- **Speed**: Fast responses (15-30s)
- **Quality**: Good for basic suggestions
- **Install**: `ollama pull tinyllama`

### Llama3 (Full Features)
- **Size**: ~4.7GB
- **Speed**: Comprehensive responses (40-60s)
- **Quality**: Excellent, detailed suggestions
- **Install**: `ollama pull llama3`

### Phi-3 (Alternative)
- **Size**: ~2.3GB
- **Speed**: Balanced (20-40s)
- **Quality**: Good quality, Microsoft model
- **Install**: `ollama pull phi3`

## 🚦 Manual Workflow Triggers

### Run Full AI Tests
```bash
# Using GitHub CLI
gh workflow run "AI Testing Assistant CI"

# Or via GitHub web interface:
# Actions → AI Testing Assistant CI → Run workflow
```

### Run Quick Tests
```bash
# Triggered automatically on AI file changes
# Or manually:
gh workflow run "Fast AI Tests"
```

## 📋 Test Results

### Artifacts Generated
- `ai-test-results/` - Test reports and logs
- `target/surefire-reports/` - Maven test results
- GitHub Step Summary with AI insights

### Example Output
```
🤖 AI Test Results
AI tests completed with Ollama + tinyllama model
✅ AI locator generation: PASSED
✅ AI test case creation: PASSED  
✅ AI debugging assistance: PASSED
✅ AI page analysis: PASSED
```

## 🔍 Troubleshooting CI

### Common Issues

1. **Ollama Installation Timeout**
   ```yaml
   - name: Wait for Ollama
     run: timeout 60 bash -c 'until curl -f http://localhost:11434; do sleep 2; done'
   ```

2. **Model Pull Timeout**
   ```yaml
   - name: Pull model with timeout
     run: timeout 600 ollama pull tinyllama
   ```

3. **Chrome/ChromeDriver Issues**
   ```yaml
   - name: Debug Chrome setup
     run: |
       google-chrome --version
       chromedriver --version
   ```

### Performance Optimization

1. **Use Smaller Models**: `tinyllama` instead of `llama3`
2. **Cache Dependencies**: Maven and Ollama models
3. **Parallel Jobs**: Separate AI and non-AI tests
4. **Conditional Runs**: Only on AI file changes

## 📈 Monitoring

### Success Metrics
- ✅ Build time < 20 minutes
- ✅ AI response time < 60 seconds
- ✅ All tests passing
- ✅ No timeout errors

### Alerts Setup
```yaml
- name: Notify on AI Test Failure
  if: failure()
  run: |
    echo "🚨 AI tests failed - check Ollama integration"
    echo "Model: ${{ env.AI_MODEL }}"
    echo "Host: ${{ env.OLLAMA_HOST }}"
```

## 🎯 Best Practices

1. **Separate Workflows**: Keep AI tests separate from standard tests
2. **Timeout Management**: Set appropriate timeouts for LLM responses
3. **Model Selection**: Use appropriate models for CI vs local development
4. **Caching**: Cache Maven dependencies and potentially AI models
5. **Fallback Testing**: Always test SimpleAIClient fallback path

## 📝 Local Development vs CI

| Aspect | Local Development | CI Environment |
|--------|------------------|----------------|
| Model | llama3 (full features) | tinyllama (speed) |
| Timeout | 90s | 120s |
| Chrome | GUI | Headless |
| Ollama | Manual install | Auto-install |
| Tests | All features | Core functionality |

This setup ensures your AI Testing Assistant works reliably in both development and CI environments! 🚀