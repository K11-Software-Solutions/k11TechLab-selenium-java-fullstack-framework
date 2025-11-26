package org.k11techlab.framework.ai.rag.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.util.List;
import java.util.Map;

public class OpenAIEmbedder implements DocumentRetriever.EmbeddingFunction {
    private static final String OPENAI_URL = "https://api.openai.com/v1/embeddings";
    private final String apiKey;
    private final String model;
    private final ObjectMapper mapper = new ObjectMapper();

    public OpenAIEmbedder(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public double[] embed(String text) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String payload = mapper.writeValueAsString(
                Map.of("input", List.of(text), "model", model)
            );
            HttpPost post = new HttpPost(OPENAI_URL);
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(payload));
            System.out.println("📡 OpenAI Request → " + OPENAI_URL);
            System.out.println("Payload: " + payload);
            ClassicHttpResponse response = (ClassicHttpResponse) client.execute(post);
            int status = response.getCode();
            String responseBody = new String(response.getEntity().getContent().readAllBytes());
            System.out.println("OpenAI API response (status " + status + "):\n" + responseBody);
            if (status != 200) {
                System.err.println("❌ OpenAI Error: " + status);
                return new double[1536]; // fallback for ada-002
            }
            Map<?,?> result = mapper.readValue(responseBody, Map.class);
            List<?> data = (List<?>) result.get("data");
            if (data == null || data.isEmpty()) return new double[1536];
            Map<?,?> embeddingObj = (Map<?,?>) data.get(0);
            List<Double> vec = (List<Double>) embeddingObj.get("embedding");
            double[] arr = new double[vec.size()];
            for (int i = 0; i < vec.size(); i++) arr[i] = vec.get(i);
            return arr;
        } catch (Exception e) {
            e.printStackTrace();
            return new double[1536];
        }
    }
}
