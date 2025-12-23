package org.k11techlab.framework.ai.mcp;

import com.sun.net.httpserver.HttpServer;
import org.k11techlab.framework.ai.mcp.handlers.*;
import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.ai.openai.OpenAIClient;
import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;
import org.k11techlab.framework.ai.mcp.store.ContextStore;
import org.k11techlab.framework.ai.mcp.store.MongoContextStore;
import org.k11techlab.framework.ai.mcp.store.InMemoryContextStore;
import java.io.IOException;
import java.net.InetSocketAddress;

public class MCPServer {
    private HttpServer server;

    private final ContextStore contextStore;

    public MCPServer() {
        // Try to connect to MongoDB, fall back to in-memory if unavailable
        String mongoUri = "mongodb://localhost:27017";
        String mongoDb = "mcpdb";
        String mongoCollection = "context";
        
        try {
            mongoUri = ConfigurationManager.getBundle().getPropertyValue("mongo.uri");
            mongoDb = ConfigurationManager.getBundle().getPropertyValue("mongo.db");
            mongoCollection = ConfigurationManager.getBundle().getPropertyValue("mongo.collection");
        } catch (Exception e) {
            System.out.println("ℹ️  Using default MongoDB configuration (failed to load: mongo.uri, mongo.db, mongo.collection)");
        }
        
        MongoContextStore mongoStore = new MongoContextStore(mongoUri, mongoDb, mongoCollection);
        
        if (mongoStore.isConnected()) {
            this.contextStore = mongoStore;
        } else {
            System.out.println("ℹ️  Using in-memory context store as fallback");
            this.contextStore = new InMemoryContextStore();
        }
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/mcp/health", new HealthHandler());
        // Use OpenAIClient directly instead of RAGEnhancedAIClient to avoid Ollama dependency
        server.createContext("/mcp/completion", new CompletionHandler(new OpenAIClient()));
        server.createContext("/mcp/context", new ContextHandler(contextStore));
        server.createContext("/mcp/context/list", new ContextListHandler(contextStore));
        // Use OpenAIClient directly instead of RAGEnhancedAIClient to avoid Ollama dependency
        server.createContext("/mcp/workflow", new WorkflowHandler(new OpenAIClient(), contextStore));
        server.createContext("/mcp/correct-code", new CorrectCodeHandler(new OpenAIClient()));
        server.createContext("/mcp/generate-and-run-selenium-test", new GenerateAndRunTestHandler());
        server.createContext("/mcp/generate-and-run-playwright-test", new GenerateAndRunTestHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("✅ MCP Server started on port " + port);
        System.out.println("ℹ️  Note: Using OpenAI client directly. RAG features require Ollama to be running.");
    }

    public void stop() {
        if (server != null) server.stop(0);
        if (contextStore != null) contextStore.close();
    }

    public static void main(String[] args) throws IOException {
        int port = 8090; // Default to config file value
        String configPath = System.getProperty("user.dir") + java.io.File.separator + "config" + java.io.File.separator + "mcp-config.properties";
        System.out.println("[DEBUG] MCP config path: " + configPath);
        try {
            String portStr = ConfigurationManager.getBundle().getPropertyValue("mcp.port");
            System.out.println("[DEBUG] Loaded mcp.port value: " + portStr);
            if (portStr != null && !portStr.isEmpty()) port = Integer.parseInt(portStr.trim());
        } catch (Exception e) {
            System.err.println("Could not read MCP port from ConfigurationManager, using default 8090");
        }

        int maxTries = 10;
        MCPServer server = null;

        for (int i = 0; i < maxTries; i++) {
            try {
                server = new MCPServer();
                server.start(port);
                break;
            } catch (java.net.BindException e) {
                System.err.println("Port " + port + " is in use, trying next port...");
                port++;
            }
        }

        if (server == null) System.err.println("Failed to start MCP Server: No available ports.");
    }
}
