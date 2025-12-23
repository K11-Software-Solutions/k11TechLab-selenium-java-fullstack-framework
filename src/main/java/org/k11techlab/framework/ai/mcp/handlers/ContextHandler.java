package org.k11techlab.framework.ai.mcp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.k11techlab.framework.ai.mcp.store.ContextStore;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.k11techlab.framework.ai.util.HttpUtils.*;

public class ContextHandler implements HttpHandler {

    private final ContextStore store;

    public ContextHandler(ContextStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> qp = parseQuery(exchange.getRequestURI().getRawQuery());
                String key = qp.get("key");

                if (key == null) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }

                String value = store.get(key);
                String response =
                        "{\"key\":\"" + escapeJson(key) + "\",\"value\":\"" + escapeJson(value) + "\"}";

                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readBody(exchange);
                Map<String, String> form = parseQuery(body);

                String key = form.get("key");
                String value = form.get("value");

                if (key == null || value == null) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }

                store.put(key, value);
                exchange.sendResponseHeaders(200, 0);
                return;
            }

            exchange.sendResponseHeaders(405, -1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
