package org.k11techlab.framework.ai.util;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.k11techlab.framework.ai.mcp.MCPServer;

public class MCPDemoJavaClient {
            // Utility to check MCP server health
            public static boolean isMCPServerHealthy() {
                try {
                    URL url = new URL("http://localhost:8080/mcp/health");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        try (java.util.Scanner scanner = new java.util.Scanner(conn.getInputStream(), "UTF-8")) {
                            String result = scanner.hasNext() ? scanner.next() : "";
                            return result.contains("MCP Server is running");
                        }
                    }
                } catch (Exception e) {
                    // Server not running
                }
                return false;
            }
        public static String correctJavaCode(String code) throws Exception {
            URL url = new URL("http://localhost:8080/mcp/correct-code");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(code.getBytes(StandardCharsets.UTF_8));
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (java.util.Scanner scanner = new java.util.Scanner(conn.getInputStream(), "UTF-8")) {
                    scanner.useDelimiter("\\A");
                    return scanner.hasNext() ? scanner.next() : "";
                }
            } else {
                throw new RuntimeException("Failed to correct code: HTTP " + responseCode);
            }
        }
    public static void main(String[] args) throws Exception {
        if (!isMCPServerHealthy()) {
            System.err.println("ERROR: MCP server is not running on port 8080. Please start it before running this client.");
            return;
        }
        String scenario = "Open https://example.com, enter username 'user' and password 'pass', click login, assert dashboard is displayed.";
        URL url = new URL("http://localhost:8080/mcp/generate-and-run-selenium-test");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(scenario.getBytes(StandardCharsets.UTF_8));
        }
        int status = conn.getResponseCode();
        StringBuilder response = new StringBuilder();
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(
                status >= 400 ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        }
        System.out.println("Response from MCP server:\n" + response);
    }
}
