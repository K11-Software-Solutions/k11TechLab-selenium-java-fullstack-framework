package org.k11techlab.framework.ai.agent.demo;

import org.k11techlab.framework.ai.openai.OpenAIClient;
import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.ai.agent.ConversationalAssistantAgent;

public class ConversationalAssistantDemo {
    public static void main(String[] args) {
        // Use OpenAIClient as the LLMInterface implementation
        OpenAIClient openAI = new OpenAIClient();
        ConversationalAssistantAgent assistant = new ConversationalAssistantAgent(
            new RAGEnhancedAIClient(openAI)
        );

        String userMessage = "How do I write a Selenium test for login?";
        String response = assistant.ask(userMessage);
        System.out.println("AI Assistant: " + response);
    }
}
