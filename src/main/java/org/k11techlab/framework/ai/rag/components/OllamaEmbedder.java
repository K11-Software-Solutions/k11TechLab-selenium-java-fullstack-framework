package org.k11techlab.framework.ai.rag.components;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * OllamaEmbedder - Uses a local Ollama server to generate embeddings for RAG.
 */
public class OllamaEmbedder implements DocumentRetriever.EmbeddingFunction {
    private final String ollamaUrl;
    private final String model;
    private final ObjectMapper mapper = new ObjectMapper();

    public OllamaEmbedder(String ollamaUrl, String model) {
        this.ollamaUrl = ollamaUrl;
        this.model = model;
    }

    @Override
    public double[] embed(String text) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            // Use Jackson to build the JSON payload safely
            var payloadNode = mapper.createObjectNode();
            payloadNode.put("model", model);
            payloadNode.put("prompt", text); // Jackson will handle escaping
            String payload = mapper.writeValueAsString(payloadNode);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl + "/api/embeddings"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("Ollama error: " + response.statusCode() + "\n" + response.body());
                return new double[0];
            }
            // Ollama returns: {"embedding": [ ... ]}
            var node = mapper.readTree(response.body());
            if (!node.has("embedding")) return new double[0];
            List<Double> vec = mapper.readerForListOf(Double.class).readValue(node.get("embedding"));
            double[] arr = new double[vec.size()];
            for (int i = 0; i < vec.size(); i++) arr[i] = vec.get(i);
            return arr;
        } catch (Exception e) {
            e.printStackTrace();
            return new double[0];
        }
    }
}
