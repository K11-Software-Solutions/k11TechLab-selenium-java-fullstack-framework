package org.k11techlab.framework.ai.rag.components;

import java.util.*;

/**
 * VectorStore
 * Stores vector embeddings for document chunks and supports similarity search.
 */
public class VectorStore {
    public static class VectorEntry {
        public final String id;
        public final double[] vector;
        public final Object payload;
        public VectorEntry(String id, double[] vector, Object payload) {
            this.id = id;
            this.vector = vector;
            this.payload = payload;
        }
    }

    private final List<VectorEntry> entries = new ArrayList<>();

    public void add(String id, double[] vector, Object payload) {
        entries.add(new VectorEntry(id, vector, payload));
    }

    public List<VectorEntry> search(double[] queryVector, int topK) {
        if (queryVector == null || queryVector.length == 0) {
            System.err.println("Warning: Query vector is empty or null. Skipping search.");
            return Collections.emptyList();
        }
        PriorityQueue<VectorEntry> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> {
            if (e.vector == null || e.vector.length != queryVector.length) {
                return Double.NEGATIVE_INFINITY;
            }
            return -cosineSim(queryVector, e.vector);
        }));
        for (VectorEntry e : entries) pq.add(e);
        List<VectorEntry> result = new ArrayList<>();
        for (int i = 0; i < topK && !pq.isEmpty(); i++) result.add(pq.poll());
        return result;
    }

    private double cosineSim(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-8);
    }
}
