package org.k11techlab.framework.ai.agent;

import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;

/**
 * ConversationalAssistantAgent: An AI agent for natural language Q&A and guidance within the framework.
 * Usage: Instantiate and call ask(String userMessage) to get an AI-powered response.
 */
public class ConversationalAssistantAgent {
    private final RAGEnhancedAIClient ragClient;

    public ConversationalAssistantAgent(RAGEnhancedAIClient ragClient) {
        this.ragClient = ragClient;
    }

    /**
     * Ask a question or request guidance from the AI assistant.
     * @param userMessage The user's question or request.
     * @return The AI's response as a String.
     */
    public String ask(String userMessage) {
        // You can enhance this prompt with context, user profile, or project info
        String prompt = "You are a helpful AI assistant for a Java Selenium automation framework. " +
                        "Answer the following user query in a concise, actionable way.\n" +
                        userMessage;
        return ragClient.generateResponse(prompt);
    }
}
