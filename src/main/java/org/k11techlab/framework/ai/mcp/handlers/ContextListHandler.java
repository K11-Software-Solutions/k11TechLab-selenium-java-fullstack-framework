package org.k11techlab.framework.ai.mcp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bson.Document;
import org.k11techlab.framework.ai.mcp.store.ContextStore;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.k11techlab.framework.ai.util.HttpUtils.escapeJson;

public class ContextListHandler implements HttpHandler {

    private final ContextStore store;

    public ContextListHandler(ContextStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            List<Document> all = store.listAll();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < all.size(); i++) {
                Document d = all.get(i);
                sb.append("{\"key\":\"")
                  .append(escapeJson(d.getString("key")))
                  .append("\",\"value\":\"")
                  .append(escapeJson(d.getString("value")))
                  .append("\"}");
                if (i < all.size() - 1) sb.append(",");
            }
            sb.append("]");

            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
