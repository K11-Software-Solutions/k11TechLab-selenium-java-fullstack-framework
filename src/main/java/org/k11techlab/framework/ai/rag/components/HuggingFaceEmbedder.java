package org.k11techlab.framework.ai.rag.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.util.List;
import java.util.Map;

public class HuggingFaceEmbedder implements DocumentRetriever.EmbeddingFunction {
    private static final String MODEL_ID = "sentence-transformers/all-MiniLM-L6-v2";
    private static final String HF_URL =
            "https://router.huggingface.co/hf-inference/models/"
                    + MODEL_ID
                    + "/pipeline/feature-extraction";
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public HuggingFaceEmbedder(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public double[] embed(String text) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String payload = mapper.writeValueAsString(
                    Map.of("inputs", List.of(text))
            );
            HttpPost post = new HttpPost(HF_URL);
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(payload));
            System.out.println("📡 HF Request → " + HF_URL);
            System.out.println("Payload: " + payload);
            ClassicHttpResponse response = (ClassicHttpResponse) client.execute(post);
            int status = response.getCode();
            if (status != 200) {
                String errorBody = new String(response.getEntity().getContent().readAllBytes());
                System.err.println("❌ HF Error: " + status);
                System.err.println(errorBody);
                return new double[384];
            }
            List<List<Double>> result = mapper.readValue(
                    response.getEntity().getContent(),
                    mapper.getTypeFactory().constructCollectionType(
                            List.class,
                            mapper.getTypeFactory().constructCollectionType(List.class, Double.class)
                    )
            );
            List<Double> vec = result.get(0);
            double[] arr = new double[vec.size()];
            for (int i = 0; i < vec.size(); i++) arr[i] = vec.get(i);
            return arr;
        } catch (Exception e) {
            e.printStackTrace();
            return new double[384];
        }
    }
}
