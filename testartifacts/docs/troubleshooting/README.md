# 🔧 Troubleshooting Documentation

This folder contains troubleshooting guides and diagnostic information for the AI-powered test automation framework.

## 📁 Contents

- **[AI_TROUBLESHOOTING.md](AI_TROUBLESHOOTING.md)** - Complete troubleshooting guide for AI framework issues

## 🎯 Quick Access

### Common Issues
- **AI Provider Not Available** - Check if Ollama/LM Studio is running
- **Slow Response Times** - Optimize model selection and caching
- **Element Not Found** - Improve element descriptions and page state checking
- **Memory Issues** - Increase JVM memory and use lighter models
- **Connection Errors** - Verify ports and network configuration

### Quick Diagnostics Command
```bash
mvn test -Dtest=AIProviderDiagnosticsTest
```

### Emergency Fallback
If AI healing is completely unavailable, tests automatically fall back to traditional locators when properly implemented with the hybrid pattern.

## 📞 Need Help?

1. **Check the comprehensive guide**: [AI_TROUBLESHOOTING.md](AI_TROUBLESHOOTING.md)
2. **Run diagnostics**: Use the command above to get system status
3. **Gather debug info**: Follow the debug information collection guide
4. **Report issues**: Include all diagnostic output when reporting problems

---

*For the complete documentation suite, see [AI_DOCUMENTATION_INDEX.md](../../AI_DOCUMENTATION_INDEX.md)*