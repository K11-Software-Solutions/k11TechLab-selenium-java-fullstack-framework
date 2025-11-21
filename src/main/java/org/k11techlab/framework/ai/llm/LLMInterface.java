package org.k11techlab.framework.ai.llm;

/**
 * Interface for Local Language Model integrations.
 * Supports various LLM providers like Ollama, LM Studio, GPT4All, etc.
 */
public interface LLMInterface extends AutoCloseable {
    
    /**
     * Generate response from LLM using default parameters
     * @param prompt The input prompt for the LLM
     * @return Generated response from the model
     */
    String generateResponse(String prompt);
    
    /**
     * Generate response with custom parameters
     * @param prompt The input prompt
     * @param temperature Randomness (0.0 = deterministic, 1.0 = very random)
     * @param maxTokens Maximum tokens in response
     * @return Generated response
     */
    String generateResponse(String prompt, float temperature, int maxTokens);
    
    /**
     * Check if the LLM service is available and responsive
     * @return true if service is available
     */
    boolean isAvailable();
    
    /**
     * Get information about the current model
     * @return Model information string
     */
    String getModelInfo();
    
    /**
     * Close any resources used by the LLM client
     */
    @Override
    void close();
}