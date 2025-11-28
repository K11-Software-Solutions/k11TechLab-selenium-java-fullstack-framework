// ...existing code...
package org.k11techlab.framework.ai.chatbot;

import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.ai.llm.LLMInterface;

/**
 * RAGKnowledgeChatbot: Answers questions using Retrieval-Augmented Generation (RAG)
 * and the framework's knowledge base.
 */
public class RAGKnowledgeChatbot {
    private final RAGEnhancedAIClient ragClient;

    /**
     * Initialize with a base LLM provider and RAG knowledge base.
     * @param baseProvider The LLM provider to use (e.g., OpenAI, Ollama, etc.)
     */
    public RAGKnowledgeChatbot(LLMInterface baseProvider) {
        this.ragClient = new RAGEnhancedAIClient(baseProvider);
    }

    /**
     * Answer a question using the framework's knowledge base (RAG).
     * @param question The user's question
     * @return The answer generated using RAG
     */
    public String answerQuestion(String question) {
        return ragClient.generateResponse(question);
    }

    /**
     * Optionally, expose the underlying RAG client for advanced use.
     */
    public RAGEnhancedAIClient getRagClient() {
        return ragClient;
    }
}
