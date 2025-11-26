package org.k11techlab.framework.ai.rag.demo;

import java.util.*;

/**
 * RAGLocalAIDemo - Simulates RAG with a local embedding model (no cloud API)
 * Illustrates how a local AI can power semantic search and answer synthesis.
 */
public class RAGLocalAIDemo {
    public static void main(String[] args) {
        System.out.println("🧠 RAG (Local AI) DEMO");
        System.out.println("==============================================\n");

        // Simulate user queries
        demoQuery1_LocalSemanticSearch();
        System.out.println("\n" + "=".repeat(80) + "\n");

        demoQuery2_LocalAnswerSynthesis();
        System.out.println("\n" + "=".repeat(80) + "\n");
    }

    /**
     * Demo 1: Local Semantic Search
     */
    private static void demoQuery1_LocalSemanticSearch() {
        System.out.println("🔍 DEMO 1: Local Semantic Search");
        System.out.println("Query: 'How do I handle NoSuchElementException?'\n");

        // Simulated local embedding vectors (pretend these are from a local model)
        Map<String, double[]> docEmbeddings = new HashMap<>();
        docEmbeddings.put("doc1", new double[]{0.1, 0.2, 0.3});
        docEmbeddings.put("doc2", new double[]{0.2, 0.1, 0.4});
        docEmbeddings.put("faq_no_such_element", new double[]{0.9, 0.8, 0.7}); // Closest

        double[] queryEmbedding = new double[]{0.88, 0.79, 0.69}; // Simulated query embedding

        // Find the most similar doc (cosine similarity)
        String bestDoc = null;
        double bestScore = -1;
        for (Map.Entry<String, double[]> entry : docEmbeddings.entrySet()) {
            double score = cosineSimilarity(queryEmbedding, entry.getValue());
            if (score > bestScore) {
                bestScore = score;
                bestDoc = entry.getKey();
            }
        }
        System.out.println("Most relevant doc: " + bestDoc + " (similarity: " + bestScore + ")");
        System.out.println("✅ Retrieved answer: 'NoSuchElementException is thrown when... (see FAQ)'\n");
    }

    /**
     * Demo 2: Local Answer Synthesis
     */
    private static void demoQuery2_LocalAnswerSynthesis() {
        System.out.println("📝 DEMO 2: Local Answer Synthesis");
        System.out.println("Query: 'How do I handle NoSuchElementException?'\n");

        // Simulated retrieved context
        String context = "NoSuchElementException is thrown when an element is not found. " +
                "To handle it, use try-catch or check for element existence. " +
                "Best practice: use hasNext() for iterators, explicit waits for Selenium.'";

        // Simulated local LLM answer synthesis
        String synthesized = "To handle NoSuchElementException, always check if the element exists " +
                "before accessing it. In Selenium, use explicit waits. For iterators, use hasNext(). " +
                "Wrap risky code in try-catch to gracefully handle the exception.";

        System.out.println("Context: " + context + "\n");
        System.out.println("🚀 Synthesized Answer:\n" + synthesized);
    }

    // Simple cosine similarity for demo
    private static double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
