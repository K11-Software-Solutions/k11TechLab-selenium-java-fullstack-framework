#!/bin/bash

# GitHub Actions Simulation Script
# This script simulates the GitHub Actions environment for testing

echo "🧪 GitHub Actions AI Testing Simulation"
echo "========================================"

# Check if we're on Ubuntu-like system
if ! command -v apt-get &> /dev/null; then
    echo "❌ This script is designed for Ubuntu/Debian systems (like GitHub Actions)"
    echo "   For Windows, use the PowerShell scripts instead"
    exit 1
fi

# Set up environment variables
export GITHUB_ACTIONS=true
export RUNNER_OS=Linux
export OLLAMA_HOST=http://localhost:11434

echo "🔧 Step 1: Installing dependencies..."

# Install Java 11 (if not present)
if ! command -v java &> /dev/null; then
    echo "  Installing OpenJDK 11..."
    sudo apt-get update
    sudo apt-get install -y openjdk-11-jdk
fi

# Install Chrome (if not present)
if ! command -v google-chrome &> /dev/null; then
    echo "  Installing Chrome..."
    wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | sudo apt-key add -
    sudo sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list'
    sudo apt-get update
    sudo apt-get install -y google-chrome-stable
fi

# Install ChromeDriver
echo "  Setting up ChromeDriver..."
CHROME_VERSION=$(google-chrome --version | cut -d ' ' -f3 | cut -d '.' -f1-3)
echo "  Chrome version: $CHROME_VERSION"

CHROMEDRIVER_VERSION=$(curl -s "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_${CHROME_VERSION}" 2>/dev/null || echo "114.0.5735.90")
echo "  ChromeDriver version: $CHROMEDRIVER_VERSION"

if ! wget -O chromedriver.zip "https://chromedriver.storage.googleapis.com/${CHROMEDRIVER_VERSION}/chromedriver_linux64.zip" 2>/dev/null; then
    echo "  Fallback: using package manager for ChromeDriver"
    sudo apt-get install -y chromium-chromedriver
    sudo ln -sf /usr/lib/chromium-browser/chromedriver /usr/local/bin/chromedriver
else
    unzip -q chromedriver.zip
    sudo mv chromedriver /usr/local/bin/
    sudo chmod +x /usr/local/bin/chromedriver
    rm chromedriver.zip
fi

echo "🤖 Step 2: Installing Ollama..."

# Install Ollama
if ! command -v ollama &> /dev/null; then
    curl -fsSL https://ollama.ai/install.sh | sh
fi

# Start Ollama service
echo "  Starting Ollama service..."
nohup ollama serve > ollama.log 2>&1 &
OLLAMA_PID=$!
echo "  Ollama PID: $OLLAMA_PID"

# Wait for Ollama to be ready
echo "  Waiting for Ollama to be ready..."
for i in {1..30}; do
    if curl -f http://localhost:11434/api/tags >/dev/null 2>&1; then
        echo "  ✅ Ollama is ready!"
        break
    fi
    echo "  ⏳ Waiting for Ollama... ($i/30)"
    sleep 2
done

# Verify Ollama is running
if ! curl -f http://localhost:11434/api/tags >/dev/null 2>&1; then
    echo "  ❌ Ollama failed to start"
    cat ollama.log
    exit 1
fi

echo "📦 Step 3: Pulling AI model..."

# Pull tinyllama model
echo "  📥 Pulling tinyllama model..."
if timeout 300 ollama pull tinyllama; then
    echo "  ✅ tinyllama model ready"
else
    echo "  ⚠️ Trying phi3-mini as fallback..."
    if timeout 300 ollama pull phi3-mini; then
        echo "  ✅ phi3-mini model ready"
        export AI_MODEL=phi3-mini
    else
        echo "  ❌ Model pull failed"
        ollama list
        exit 1
    fi
fi

echo "📋 Available models:"
ollama list

echo "🧪 Step 4: Setting up test environment..."

# Create test resources directory
mkdir -p src/test/resources

# Create AI test configuration
cat > src/test/resources/ai-test.properties << EOF
ai.model=${AI_MODEL:-tinyllama}
ai.timeout=120000
ai.enabled=true
ollama.host=http://localhost:11434
EOF

echo "📁 Created AI test configuration:"
cat src/test/resources/ai-test.properties

echo "🚀 Step 5: Running tests..."

# Verify environment
echo "🔍 Pre-test verification:"
echo "  Java: $(java -version 2>&1 | head -n1)"
echo "  Chrome: $(google-chrome --version)"
echo "  ChromeDriver: $(chromedriver --version)"
echo "  Ollama: $(curl -f http://localhost:11434/api/tags >/dev/null 2>&1 && echo 'Running' || echo 'Stopped')"

# Run AI tests
echo "  Running AI fallback tests (SimpleAIDemo)..."
if mvn test -Dtest="SimpleAIDemo" \
    -Dwebdriver.chrome.driver=/usr/local/bin/chromedriver \
    -Dheadless=true \
    -Djava.awt.headless=true; then
    echo "  ✅ AI fallback tests passed"
else
    echo "  ❌ AI fallback tests failed"
    exit 1
fi

echo "  Running full AI tests with Ollama..."
if mvn test -Dtest="QuickAITest" \
    -Dwebdriver.chrome.driver=/usr/local/bin/chromedriver \
    -Dheadless=true \
    -Dai.model=${AI_MODEL:-tinyllama} \
    -Dollama.host=http://localhost:11434 \
    -Djava.awt.headless=true; then
    echo "  ✅ Full AI tests passed"
else
    echo "  ❌ Full AI tests failed"
    exit 1
fi

echo "🎉 GitHub Actions simulation completed successfully!"
echo ""
echo "📊 Summary:"
echo "  ✅ Environment setup complete"
echo "  ✅ Ollama + ${AI_MODEL:-tinyllama} model working"
echo "  ✅ AI tests passing"
echo "  ✅ Ready for GitHub Actions deployment"

# Cleanup
kill $OLLAMA_PID 2>/dev/null
rm -f ollama.log nohup.out