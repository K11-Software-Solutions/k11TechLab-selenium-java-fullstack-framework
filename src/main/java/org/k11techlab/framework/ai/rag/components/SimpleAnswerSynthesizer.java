package org.k11techlab.framework.ai.rag.components;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleAnswerSynthesizer {
    /**
     * Concatenates the top retrieved chunks and optionally truncates for prompt size.
     * This is a placeholder for LLM-based answer synthesis.
     */
    public static String synthesizeAnswer(String question, List<String> retrievedChunks) {
        // Deduplicate and limit chunks
        java.util.LinkedHashSet<String> uniqueChunks = new java.util.LinkedHashSet<>();
        for (String chunk : retrievedChunks) {
            uniqueChunks.add(chunk);
            if (uniqueChunks.size() >= 2) break; // Limit to top 2 unique chunks for clarity
        }

        // Try to extract the most relevant Q&A section for the question
        StringBuilder answer = new StringBuilder();
        String lowerQ = question.toLowerCase();
        for (String chunk : uniqueChunks) {
            // Look for a section that matches the question
            String[] lines = chunk.split("\n");
            boolean found = false;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].toLowerCase().contains(lowerQ)) {
                    // Print this line and following lines until next Q or end
                    found = true;
                    answer.append(lines[i].trim()).append("\n");
                    for (int j = i + 1; j < lines.length; j++) {
                        if (lines[j].trim().startsWith("## ")) break;
                        answer.append(lines[j].trim()).append("\n");
                    }
                    break;
                }
            }
            if (found) break;
        }
        if (answer.length() == 0) {
            // Fallback: just show the first unique chunk, truncated (increased to 1200 chars)
            String chunk = uniqueChunks.iterator().next();
            answer.append(chunk.substring(0, Math.min(1200, chunk.length()))).append("...\n");
        }
        return answer.toString().trim();
    }
}
