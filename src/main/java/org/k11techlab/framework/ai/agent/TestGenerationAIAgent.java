package org.k11techlab.framework.ai.agent;

import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.ai.nlp.NLTestGenerator;
import org.k11techlab.framework.ai.nlp.NLTestGenerator.GeneratedTest;
import org.k11techlab.framework.ai.nlp.NLTestGenerator.TestGenerationRequest;

/**
 * TestGenerationAIAgent: An AI agent for generating Selenium test code from natural language descriptions.
 * Usage: Instantiate and call generateTest(String description, String baseUrl) to get a GeneratedTest.
 */
public class TestGenerationAIAgent {
    private final NLTestGenerator testGenerator;

    public TestGenerationAIAgent(RAGEnhancedAIClient ragClient) {
        this.testGenerator = new NLTestGenerator(ragClient);
    }

    /**
     * Generate a Selenium test from a natural language description.
     * @param description The test scenario in plain English.
     * @param baseUrl The base URL for the test (optional).
     * @return The generated test code and metadata.
     */
    public GeneratedTest generateTest(String description, String baseUrl) {
        TestGenerationRequest request = new TestGenerationRequest(description);
        request.setBaseUrl(baseUrl);
        return testGenerator.generateTest(request);
    }
}
