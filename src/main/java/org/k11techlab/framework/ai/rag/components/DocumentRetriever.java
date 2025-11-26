package org.k11techlab.framework.ai.rag.components;

import java.util.*;

/**
 * DocumentRetriever
 * Retrieves the most relevant document chunks for a given query using a VectorStore.
 */
public class DocumentRetriever {
    private final VectorStore vectorStore;
    private final EmbeddingFunction embeddingFunction;

    public interface EmbeddingFunction {
        double[] embed(String text);
    }

    public DocumentRetriever(VectorStore vectorStore, EmbeddingFunction embeddingFunction) {
        this.vectorStore = vectorStore;
        this.embeddingFunction = embeddingFunction;
    }

    public List<VectorStore.VectorEntry> retrieve(String query, int topK) {
        double[] queryVec = embeddingFunction.embed(query);
        return vectorStore.search(queryVec, topK);
    }
}
