package org.k11techlab.framework.ai.rag.components;

import java.nio.file.*;
import java.util.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * CodebaseIndexer
 * Scans and indexes documentation/code files for RAG retrieval.
 */
public class CodebaseIndexer {
    public static class IndexedChunk {
        public final String id;
        public final String content;
        public final Map<String, String> metadata;
        public IndexedChunk(String id, String content, Map<String, String> metadata) {
            this.id = id;
            this.content = content;
            this.metadata = metadata;
        }
    }

    public List<IndexedChunk> indexDirectory(Path root, String... extensions) {
        List<IndexedChunk> chunks = new ArrayList<>();
        try {
            Files.walk(root)
                .filter(Files::isRegularFile)
                .filter(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    for (String ext : extensions) if (name.endsWith(ext)) return true;
                    return false;
                })
                .forEach(p -> {
                    try {
                        String content = new String(Files.readAllBytes(p));
                        Map<String, String> meta = new HashMap<>();
                        meta.put("path", p.toString());
                        String id = CodebaseIndexer.sha256(p.toString() + "::" + content);
                        chunks.add(new IndexedChunk(id, content, meta));
                    } catch (Exception ignored) {}
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chunks;
    }

    // Deterministic SHA-256 hash for chunk ID
    public static String sha256(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
