package org.k11techlab.framework.ai.mcp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HealthHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        try {
            byte[] response = "MCP Server is running".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
