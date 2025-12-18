package org.k11techlab.framework.ai.mcp.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility for communicating with Playwright MCP server via JSON-RPC over stdio.
 */
public class PlaywrightMCPClient implements Closeable {
    private final Process process;
    private final BufferedWriter writer;
    private final BufferedReader reader;
    private final AtomicInteger idGen = new AtomicInteger(1);

    public PlaywrightMCPClient() throws IOException {
        this.process = new ProcessBuilder(
                "npx",
                "@playwright/mcp@latest",
                "--headless",
                "--isolated",
                "--output-dir=./artifacts/mcp"
        ).redirectErrorStream(true).start();
        this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
    }

    public int nextId() {
        return idGen.getAndIncrement();
    }

    public void sendJsonRpc(String json) throws IOException {
        writer.write(json);
        writer.write("\n");
        writer.flush();
    }

    public String readResponse() throws IOException {
        // Reads a single line (one JSON-RPC response)
        return reader.readLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
        reader.close();
        process.destroy();
    }
}
