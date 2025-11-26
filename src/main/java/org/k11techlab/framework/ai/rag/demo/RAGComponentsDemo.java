
package org.k11techlab.framework.ai.rag.demo;
import org.k11techlab.framework.ai.rag.components.SimpleAnswerSynthesizer;

import org.k11techlab.framework.ai.rag.components.CodebaseIndexer;
import org.k11techlab.framework.ai.rag.components.VectorStore;
import org.k11techlab.framework.ai.rag.components.DocumentRetriever;
import org.k11techlab.framework.ai.rag.components.EmbeddingCache;

import java.nio.file.Paths;
import java.util.List;
import org.k11techlab.framework.ai.rag.util.DotenvLoader;
import org.k11techlab.framework.ai.rag.components.HuggingFaceEmbedder;
import org.k11techlab.framework.ai.rag.components.OpenAIEmbedder;

public class RAGComponentsDemo {



    public static void main(String[] args) {

        DotenvLoader.loadEnv(".env");

        CodebaseIndexer indexer = new CodebaseIndexer();
        // Index both the main docs and the rag subfolder
        List<CodebaseIndexer.IndexedChunk> chunks = new java.util.ArrayList<>();
        chunks.addAll(indexer.indexDirectory(Paths.get("testartifacts", "docs"), ".md", ".txt"));
        chunks.addAll(indexer.indexDirectory(Paths.get("testartifacts", "docs", "rag"), ".md", ".txt"));


        // Choose embedding provider: "huggingface" or "openai"
        String embedProvider = System.getenv("EMBED_PROVIDER");
        if (embedProvider == null) embedProvider = System.getProperty("EMBED_PROVIDER", "huggingface");

        DocumentRetriever.EmbeddingFunction embedFn;
        if (embedProvider.equalsIgnoreCase("openai")) {
            String openaiApiKey = System.getenv("OPENAI_API_KEY");
            if (openaiApiKey == null || openaiApiKey.isEmpty()) {
                openaiApiKey = System.getProperty("OPENAI_API_KEY");
            }
            if (openaiApiKey == null || openaiApiKey.isEmpty()) {
                System.err.println("Please set OPENAI_API_KEY in environment or .env file.");
                return;
            }
            // You can change the model to "text-embedding-ada-002" or another supported model
            String openaiModel = System.getenv("OPENAI_EMBED_MODEL");
            if (openaiModel == null || openaiModel.isEmpty()) {
                openaiModel = System.getProperty("OPENAI_EMBED_MODEL", "text-embedding-ada-002");
            }
            embedFn = new OpenAIEmbedder(openaiApiKey, openaiModel);
            System.out.println("Using OpenAI embeddings (model: " + openaiModel + ")");
        } else {
            String hfApiKey = System.getenv("HF_API_KEY");
            if (hfApiKey == null || hfApiKey.isEmpty()) {
                hfApiKey = System.getProperty("HF_API_KEY");
            }
            if (hfApiKey == null || hfApiKey.isEmpty()) {
                System.err.println("Please set HF_API_KEY in environment or .env file.");
                return;
            }
            embedFn = new HuggingFaceEmbedder(hfApiKey);
            System.out.println("Using HuggingFace embeddings");
        }


        // Use persistent embedding cache
        EmbeddingCache embeddingCache = new EmbeddingCache("embedding_cache.json");
        VectorStore vectorStore = new VectorStore();
        boolean updated = false;
        for (CodebaseIndexer.IndexedChunk chunk : chunks) {
            double[] vec = embeddingCache.get(chunk.id);
            if (vec == null) {
                System.out.println("[EMBED] Cache MISS for chunk: " + chunk.metadata.get("path"));
                vec = embedFn.embed(chunk.content);
                embeddingCache.put(chunk.id, vec);
                updated = true;
            } else {
                System.out.println("[EMBED] Cache HIT for chunk: " + chunk.metadata.get("path"));
            }
            vectorStore.add(chunk.id, vec, chunk);
        }
        if (updated) embeddingCache.save();

        DocumentRetriever retriever = new DocumentRetriever(vectorStore, embedFn);
        String query = "How do I handle NoSuchElementException?";
        List<VectorStore.VectorEntry> results = retriever.retrieve(query, 3);

        System.out.println("Top relevant docs for: " + query);
        List<String> topChunks = new java.util.ArrayList<>();
        for (VectorStore.VectorEntry entry : results) {
            CodebaseIndexer.IndexedChunk chunk = (CodebaseIndexer.IndexedChunk) entry.payload;
            System.out.println("- " + chunk.metadata.get("path"));
            String snippet = chunk.content.substring(0, Math.min(200, chunk.content.length())) + "...\n";
            System.out.println(snippet);
            topChunks.add(chunk.content);
        }

        // Synthesize answer from retrieved chunks
        String synthesized = SimpleAnswerSynthesizer.synthesizeAnswer(query, topChunks);
        System.out.println("\n===== Synthesized Answer =====\n" + synthesized + "\n=============================");
    }
}
