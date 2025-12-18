package org.k11techlab.framework.ai.agent.demo;

import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.ai.openai.OpenAIClient;
import org.k11techlab.framework.ai.nlp.NLTestGenerator.GeneratedTest;
import org.k11techlab.framework.ai.agent.TestGenerationAIAgent;

public class TestGenerationAIAgentDemo {
    public static void main(String[] args) {
        // Use OpenAIClient as the LLMInterface implementation for the demo
        OpenAIClient openAI = new OpenAIClient();
        RAGEnhancedAIClient ragClient = new RAGEnhancedAIClient(openAI);
        TestGenerationAIAgent agent = new TestGenerationAIAgent(ragClient);

        String description = "Test the login functionality for a user with valid credentials.";
        String baseUrl = "https://demo.testsite.com";
        GeneratedTest generated = agent.generateTest(description, baseUrl);

        System.out.println("Generated Test Class Name: " + generated.getTestClassName());
        System.out.println("Generated Test Code:\n" + generated.getTestCode());
    }
}
