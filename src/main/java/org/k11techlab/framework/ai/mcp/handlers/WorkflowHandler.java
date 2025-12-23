package org.k11techlab.framework.ai.mcp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.k11techlab.framework.ai.mcp.store.ContextStore;
import org.k11techlab.framework.ai.llm.LLMInterface;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.k11techlab.framework.ai.util.HttpUtils.*;

public class WorkflowHandler implements HttpHandler {

    private final LLMInterface aiClient;
    private final ContextStore contextStore;

    public WorkflowHandler(LLMInterface aiClient, ContextStore contextStore) {
        this.aiClient = aiClient;
        this.contextStore = contextStore;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = readBody(exchange);
            String result = aiClient.generateResponse(body);

            String key = "wf-" + UUID.randomUUID();
            contextStore.put(key, result);

            String response =
                    "{\"workflow\":\"executed\",\"key\":\"" +
                    escapeJson(key) + "\",\"result\":\"" +
                    escapeJson(result) + "\"}";

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
