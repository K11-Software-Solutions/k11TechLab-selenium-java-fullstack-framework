package org.k11techlab.framework.ai.mcp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.k11techlab.framework.ai.llm.LLMInterface;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.k11techlab.framework.ai.util.HttpUtils.readBody;
import static org.k11techlab.framework.ai.util.HttpUtils.escapeJson;

public class CompletionHandler implements HttpHandler {

    private final LLMInterface aiClient;

    public CompletionHandler(LLMInterface aiClient) {
        this.aiClient = aiClient;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String prompt = readBody(exchange);
            String result = aiClient.generateResponse(prompt);

            String response = "{\"result\":\"" + escapeJson(result) + "\"}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
