package org.k11techlab.framework.ai.mcp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.k11techlab.framework.ai.openai.OpenAIClient;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.k11techlab.framework.ai.util.HttpUtils.readBody;
import static org.k11techlab.framework.ai.util.HttpUtils.escapeJson;

public class CorrectCodeHandler implements HttpHandler {

    private final OpenAIClient openAI;

    public CorrectCodeHandler(OpenAIClient openAI) {
        this.openAI = openAI;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String code = readBody(exchange);

            String prompt =
                    "Fix this Java code so it compiles. " +
                    "Ensure imports, braces, and syntax are correct. " +
                    "Output ONLY Java code.\n\n" + code;

            String corrected = openAI.generateResponse(prompt);

            String response =
                    "{\"correctedCode\":\"" + escapeJson(corrected) + "\"}";

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
