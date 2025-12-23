
package org.k11techlab.framework.ai.openai;

import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;
import org.k11techlab.framework.ai.llm.LLMInterface;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.json.JSONObject;
import java.io.File;
import java.util.Properties;
import java.io.FileInputStream;

public class OpenAIClient implements LLMInterface {
    private final String apiKey;
    private final String model;
    private final String endpoint;

    public OpenAIClient() {
        // Try to load from environment first
        String key = System.getenv("OPENAI_API_KEY");
        String modelEnv = System.getenv("OPENAI_MODEL");
        // If not set, try to load from .env file in project root
        if ((key == null || key.isEmpty()) || (modelEnv == null || modelEnv.isEmpty())) {
            try {
                File envFile = new File(".env");
                if (envFile.exists()) {
                    Properties props = new Properties();
                    try (FileInputStream fis = new FileInputStream(envFile)) {
                        props.load(fis);
                    }
                    if ((key == null || key.isEmpty()) && props.getProperty("OPENAI_API_KEY") != null) {
                        key = props.getProperty("OPENAI_API_KEY");
                        // Set for this process
                        System.setProperty("OPENAI_API_KEY", key);
                    }
                    if ((modelEnv == null || modelEnv.isEmpty()) && props.getProperty("OPENAI_MODEL") != null) {
                        modelEnv = props.getProperty("OPENAI_MODEL");
                        System.setProperty("OPENAI_MODEL", modelEnv);
                    }
                }
            } catch (Exception e) {
                Log.info("[OpenAIClient] Could not load .env file: " + e.getMessage());
            }
        }
        this.apiKey = key;
        this.model = (modelEnv != null && !modelEnv.isEmpty()) ? modelEnv : "gpt-3.5-turbo";
        this.endpoint = "https://api.openai.com/v1/chat/completions";
    }

    @Override
    public String generateResponse(String prompt) {
        int maxTokens = 2048;
        try {
            String configMaxTokens = ConfigurationManager.getBundle().getPropertyValue("openai.maxTokens");
            if (configMaxTokens != null && !configMaxTokens.isEmpty()) {
                maxTokens = Integer.parseInt(configMaxTokens);
            }
        } catch (Exception e) {
            // fallback to default if config not found or parse error
        }
        return generateResponse(prompt, 0.7f, maxTokens);
    }

    @Override
    public String generateResponse(String prompt, float temperature, int maxTokens) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "[OpenAI API key not set]";
        }
        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("messages", new org.json.JSONArray().put(message));
            body.put("temperature", temperature);
            body.put("max_tokens", maxTokens);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes());
            }

            int status = conn.getResponseCode();
            if (status == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                JSONObject json = new JSONObject(response.toString());
                return json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            } else {
                return "[OpenAI error: HTTP " + status + "]";
            }
        } catch (IOException e) {
            return "[OpenAI error: " + e.getMessage() + "]";
        }
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public String getModelInfo() {
        return "OpenAI - Model: " + model;
    }

    @Override
    public void close() {
        // No resources to close
        Log.info("OpenAI client closed");
    }
}
