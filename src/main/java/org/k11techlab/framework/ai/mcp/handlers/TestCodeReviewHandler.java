package org.k11techlab.framework.ai.mcp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.ai.util.HttpUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;

public class TestCodeReviewHandler implements HttpHandler {

    private final RAGEnhancedAIClient aiClient;

    public TestCodeReviewHandler(RAGEnhancedAIClient aiClient) {
        this.aiClient = aiClient;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = HttpUtils.readBody(exchange);

            String prompt = "You are a senior SDET. Review the following automated test code for readability, flakiness, reliability issues, and improvement suggestions. Provide a concise list of issues and suggested fixes.\n\n" + body;

            String review = aiClient.generateResponse(prompt);

            String json = "{\"review\":\"" + HttpUtils.escapeJson(review) + "\"}";
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (IOException ioException) {
                // ignore
            }
        }
    }
}
