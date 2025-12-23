package org.k11techlab.framework.ai.mcp;

import com.sun.net.httpserver.HttpServer;
import org.k11techlab.framework.ai.mcp.handlers.*;
import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.ai.openai.OpenAIClient;
import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;
import org.k11techlab.framework.ai.mcp.store.MongoContextStore;
import java.io.IOException;
import java.net.InetSocketAddress;

public class MCPServer {
    private HttpServer server;

    private final MongoContextStore contextStore =
            new MongoContextStore("mongodb://localhost:27017", "mcpdb", "context");

    private final RAGEnhancedAIClient aiClient =
            new RAGEnhancedAIClient(new OpenAIClient());

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/mcp/health", new HealthHandler());
        server.createContext("/mcp/completion", new CompletionHandler(aiClient));
        server.createContext("/mcp/context", new ContextHandler(contextStore));
        server.createContext("/mcp/context/list", new ContextListHandler(contextStore));
        server.createContext("/mcp/workflow", new WorkflowHandler(aiClient, contextStore));
        server.createContext("/mcp/correct-code", new CorrectCodeHandler(new OpenAIClient()));
        server.createContext("/mcp/generate-and-run-selenium-test", new GenerateAndRunTestHandler());
        server.createContext("/mcp/generate-and-run-playwright-test", new GenerateAndRunTestHandler());
        server.createContext("/mcp/generate-page-object", new GeneratePageObjectHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("MCP Server started on port " + port);
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    public static void main(String[] args) throws IOException {
        int port = 8091;
        String configPath = System.getProperty("user.dir") + java.io.File.separator + "config" + java.io.File.separator + "mcp-config.properties";
        System.out.println("[DEBUG] MCP config path: " + configPath);
        try {
            String portStr = ConfigurationManager.getBundle().getPropertyValue("mcp.port");
            System.out.println("[DEBUG] Loaded mcp.port value: " + portStr);
            if (portStr != null && !portStr.isEmpty()) port = Integer.parseInt(portStr.trim());
        } catch (Exception e) {
            System.err.println("Could not read MCP port from ConfigurationManager, using default 8091");
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
