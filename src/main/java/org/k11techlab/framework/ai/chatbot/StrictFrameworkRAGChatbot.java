package org.k11techlab.framework.ai.chatbot;

import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.ai.llm.LLMInterface;

/**
 * StrictFrameworkRAGChatbot: Answers questions strictly using the framework's knowledge base (RAG).
 * If no relevant knowledge is found, returns a fallback message instead of a generic LLM answer.
 */
import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;

public class StrictFrameworkRAGChatbot {
    private final RAGKnowledgeChatbot baseChatbot;
    private final int maxContextLength;
    private final int maxTokens;
    private final int maxRetrievedDocs;

    public StrictFrameworkRAGChatbot(LLMInterface baseProvider) {
        this.baseChatbot = new RAGKnowledgeChatbot(baseProvider);
        // Read from chatbot.ai.properties or use defaults
        this.maxContextLength = parseIntOrDefault(ConfigurationManager.getString("chatbot.rag.maxContextLength"), 3000);
        this.maxTokens = parseIntOrDefault(ConfigurationManager.getString("chatbot.rag.maxTokens"), 1200);
        this.maxRetrievedDocs = parseIntOrDefault(ConfigurationManager.getString("chatbot.rag.maxRetrievedDocs"), 3);
    }

    /**
     * Answer a framework-specific question, only if relevant knowledge is found.
     * If no relevant knowledge is found, returns a fallback message.
     */
    public String answerFrameworkQuestionOnly(String question) {
        // Use a high relevance threshold to require strong knowledge match
        RAGEnhancedAIClient.RAGConfiguration config = new RAGEnhancedAIClient.RAGConfiguration();
        config.setRelevanceThreshold(1.0); // Only use highly relevant knowledge
        config.setMaxContextLength(maxContextLength);
        config.setMaxRetrievedDocs(maxRetrievedDocs);
        baseChatbot.getRagClient().updateConfiguration(config);

        // Try to answer the question with increased maxTokens
        String answer = baseChatbot.getRagClient().generateResponse(question, 0.7f, maxTokens);

        // If the answer does not include knowledge sources, assume no relevant knowledge was found
        if (!answer.contains("Knowledge Sources Used")) {
            return "Sorry, I could not find any relevant information in the framework knowledge base.";
        }
        return answer;
    }

    public RAGKnowledgeChatbot getBaseChatbot() {
        return baseChatbot;
    }

    private int parseIntOrDefault(String value, int defaultVal) {
        try {
            if (value != null) return Integer.parseInt(value.trim());
        } catch (Exception ignored) {}
        return defaultVal;
    }
}
