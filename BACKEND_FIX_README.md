# Backend Connection Fix - Setup Guide

## Changes Made

This PR fixes the backend connection issues in the K11 TechLab Selenium Java Framework by addressing several critical problems:

### 1. Port Configuration Issues
- **Problem**: Port mismatch between server (8091/8080) and configuration (8090)
- **Solution**: 
  - MCPServer now consistently reads `mcp.port` from `config/mcp-config.properties` (default: 8090)
  - MCPDemoJavaClient updated to read port from configuration
  - OpenAISeleniumTestGenerator updated to use configured port
  - Server auto-increments port if configured port is in use

### 2. MongoDB Dependency
- **Problem**: Server failed to start if MongoDB was not running
- **Solution**:
  - Created `ContextStore` interface for abstraction
  - Created `InMemoryContextStore` as a fallback when MongoDB is unavailable
  - Updated `MongoContextStore` with proper connection timeouts (2 seconds)
  - Server gracefully falls back to in-memory store if MongoDB connection fails

### 3. Ollama/RAG Dependency
- **Problem**: Server failed to start without Ollama running (for RAG features)
- **Solution**:
  - MCPServer now uses `OpenAIClient` directly instead of `RAGEnhancedAIClient`
  - RAG features are optional and only initialized when explicitly needed
  - Server starts successfully without Ollama

### 4. Java Version Compatibility
- **Problem**: pom.xml configured for Java 21, but Java 17 available
- **Solution**: Updated pom.xml to target Java 17

## Running the MCP Server

### Prerequisites
- Java 17+
- Maven 3.6+
- (Optional) MongoDB for persistent context storage
- (Optional) Ollama for RAG features

### Starting the Server

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="org.k11techlab.framework.ai.mcp.MCPServer"

# Or compile and run directly
mvn clean compile
java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) \
  org.k11techlab.framework.ai.mcp.MCPServer
```

### Configuration

Edit `config/mcp-config.properties` to customize:

```properties
# MCP Server port
mcp.port=8090

# MongoDB configuration (optional)
mongo.uri=mongodb://localhost:27017
mongo.db=mcpdb
mongo.collection=context
```

### Testing the Server

```bash
# Check health endpoint
curl http://localhost:8090/mcp/health

# Expected response:
# MCP Server is running
```

### Using the Client

```bash
# Run the demo client
mvn exec:java -Dexec.mainClass="org.k11techlab.framework.ai.util.MCPDemoJavaClient"
```

## Architecture

```
MCPServer
├── ContextStore (interface)
│   ├── MongoContextStore (persistent, requires MongoDB)
│   └── InMemoryContextStore (fallback, no dependencies)
├── LLMInterface
│   ├── OpenAIClient (default, requires API key)
│   └── RAGEnhancedAIClient (optional, requires Ollama)
└── HTTP Endpoints
    ├── /mcp/health
    ├── /mcp/completion
    ├── /mcp/context
    ├── /mcp/context/list
    ├── /mcp/workflow
    ├── /mcp/correct-code
    ├── /mcp/generate-and-run-selenium-test
    └── /mcp/generate-and-run-playwright-test
```

## Server Startup Behavior

1. **Port Selection**:
   - Reads `mcp.port` from config (default: 8090)
   - If port is in use, automatically tries next port (8091, 8092, etc.)
   - Attempts up to 10 ports before failing

2. **MongoDB Connection**:
   - Attempts to connect with 2-second timeout
   - On success: ✅ Uses MongoContextStore
   - On failure: ⚠️  Falls back to InMemoryContextStore
   - Logs connection status

3. **AI Provider**:
   - Uses OpenAIClient directly (requires API key in environment)
   - RAG features available if Ollama is running (optional)

## Troubleshooting

### Server won't start
- **Check port availability**: `netstat -an | grep 8090`
- **Check logs**: Look for error messages in console output
- **Java version**: Ensure Java 17+ is installed: `java -version`

### MongoDB warnings
- ⚠️  "Failed to connect to MongoDB" - This is normal if MongoDB is not installed
- Server will automatically use in-memory storage
- To use MongoDB, install and start it: `mongod --dbpath /path/to/data`

### Ollama warnings  
- "RAG features require Ollama to be running" - This is informational
- Server works fine without Ollama for basic features
- To enable RAG: Install Ollama from https://ollama.ai/

### Client can't connect
- Verify server is running: `curl http://localhost:8090/mcp/health`
- Check if port matches configuration in `config/mcp-config.properties`
- Check server logs for actual port if configured port was in use

## Testing

```bash
# 1. Start the server
mvn exec:java -Dexec.mainClass="org.k11techlab.framework.ai.mcp.MCPServer" &

# 2. Wait for startup
sleep 3

# 3. Test health endpoint
curl http://localhost:8090/mcp/health

# 4. Run client
mvn exec:java -Dexec.mainClass="org.k11techlab.framework.ai.util.MCPDemoJavaClient"
```

## Summary

The backend is now fully functional with:
- ✅ Consistent port configuration
- ✅ Graceful MongoDB fallback
- ✅ No Ollama dependency for basic operation
- ✅ Java 17 compatibility
- ✅ Clear error messages and logging
- ✅ Auto port selection when port in use

All changes are minimal and surgical, focusing only on fixing the connection issues without modifying unrelated functionality.
