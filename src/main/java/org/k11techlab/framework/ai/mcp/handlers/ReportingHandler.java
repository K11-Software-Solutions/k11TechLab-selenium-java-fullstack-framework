package org.k11techlab.framework.ai.mcp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.k11techlab.framework.ai.util.HttpUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class ReportingHandler implements HttpHandler {

    private final File reportDir = new File("mcp_testreport");
    private final File logDir = new File("mcp_testlog");

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"reports\":");
            sb.append(listDirJson(reportDir));
            sb.append(",\"logs\":");
            sb.append(listDirJson(logDir));
            sb.append("}");

            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try { exchange.sendResponseHeaders(500, -1); } catch (IOException ignored) {}
        }
    }

    private String listDirJson(File dir) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                boolean first = true;
                for (File f : files) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{");
                    sb.append("\"name\":\"").append(HttpUtils.escapeJson(f.getName())).append("\"");
                    sb.append(",\"size\":").append(f.length());
                    sb.append(",\"modified\":\"").append(new Date(f.lastModified())).append("\"");
                    sb.append("}");
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
