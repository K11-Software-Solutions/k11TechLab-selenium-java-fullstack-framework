package org.k11techlab.framework.ai.rag.demo;

import org.k11techlab.framework.ai.rag.components.OllamaEmbedder;
import org.k11techlab.framework.ai.rag.components.DocumentRetriever;
import org.k11techlab.framework.ai.rag.components.VectorStore;
import org.k11techlab.framework.ai.rag.components.CodebaseIndexer;
import org.k11techlab.framework.ai.rag.components.EmbeddingCache;

import java.nio.file.Paths;
import java.util.List;

/**
 * RAGLocalOllamaDemo - Shows RAG with local Ollama embeddings
 */
public class RAGLocalOllamaDemo {
    public static void main(String[] args) {
        System.out.println("🦙 RAG (Local Ollama) DEMO");
        System.out.println("==============================================\n");

        // Configure Ollama
        String ollamaUrl = "http://localhost:11434"; // Default Ollama server
        String model = "nomic-embed-text"; // Or another embedding-capable model

        // Index docs (recursive)
        CodebaseIndexer indexer = new CodebaseIndexer();
        List<CodebaseIndexer.IndexedChunk> chunks = indexer.indexDirectory(Paths.get("testartifacts", "docs"), ".md", ".txt");

        // Use Ollama for embeddings
        DocumentRetriever.EmbeddingFunction embedFn = new OllamaEmbedder(ollamaUrl, model);
        EmbeddingCache embeddingCache = new EmbeddingCache("embedding_cache_ollama.json");
        VectorStore vectorStore = new VectorStore();
        boolean updated = false;
        for (CodebaseIndexer.IndexedChunk chunk : chunks) {
            double[] vec = embeddingCache.get(chunk.id);
            if (vec == null) {
                vec = embedFn.embed(chunk.content);
                embeddingCache.put(chunk.id, vec);
                updated = true;
            }
            vectorStore.add(chunk.id, vec, chunk);
        }
        if (updated) embeddingCache.save();

        // Query
        String query = "How do I handle NoSuchElementException?";
        DocumentRetriever retriever = new DocumentRetriever(vectorStore, embedFn);
        List<VectorStore.VectorEntry> results = retriever.retrieve(query, 3);

        System.out.println("Top relevant docs for: " + query);
        for (VectorStore.VectorEntry entry : results) {
            CodebaseIndexer.IndexedChunk chunk = (CodebaseIndexer.IndexedChunk) entry.payload;
            System.out.println("- " + chunk.metadata.get("path"));
            System.out.println(chunk.content.substring(0, Math.min(200, chunk.content.length())) + "...\n");
        }
    }
}
