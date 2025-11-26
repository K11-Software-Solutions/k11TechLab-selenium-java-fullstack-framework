package org.k11techlab.framework.ai.rag;

import org.k11techlab.framework.ai.llm.LLMInterface;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG-Enhanced AI Client
 * Combines Retrieval-Augmented Generation with existing AI providers
 * to provide context-aware, knowledge-enhanced responses for test automation.
 * 
 * Features:
 * - Context-aware locator generation
 * - Knowledge-enhanced troubleshooting
 * - Best practices integration
 * - Code pattern suggestions
 * - Error solution recommendations
 * 
 * @author K11 TechLab
 * @version 1.0
 */
public class RAGEnhancedAIClient implements LLMInterface {
    
    private final LLMInterface baseAIProvider;
    private final KnowledgeBase knowledgeBase;
    private final RAGConfiguration config;
    
    /**
     * RAG Configuration settings
     */
    public static class RAGConfiguration {
        private int maxRetrievedDocs = 5;
        private int maxContextLength = 2000;
        private boolean includeSourceReferences = true;
        private boolean enableCategoryFiltering = true;
        private double relevanceThreshold = 1.0;
        
        // Getters and setters
        public int getMaxRetrievedDocs() { return maxRetrievedDocs; }
        public void setMaxRetrievedDocs(int maxRetrievedDocs) { this.maxRetrievedDocs = maxRetrievedDocs; }
        
        public int getMaxContextLength() { return maxContextLength; }
        public void setMaxContextLength(int maxContextLength) { this.maxContextLength = maxContextLength; }
        
        public boolean isIncludeSourceReferences() { return includeSourceReferences; }
        public void setIncludeSourceReferences(boolean includeSourceReferences) { this.includeSourceReferences = includeSourceReferences; }
        
        public boolean isEnableCategoryFiltering() { return enableCategoryFiltering; }
        public void setEnableCategoryFiltering(boolean enableCategoryFiltering) { this.enableCategoryFiltering = enableCategoryFiltering; }
        
        public double getRelevanceThreshold() { return relevanceThreshold; }
        public void setRelevanceThreshold(double relevanceThreshold) { this.relevanceThreshold = relevanceThreshold; }
    }
    
    /**
     * Initialize RAG-enhanced AI client
     */
    public RAGEnhancedAIClient(LLMInterface baseAIProvider) {
        this(baseAIProvider, new RAGConfiguration());
    }
    
    /**
     * Initialize RAG-enhanced AI client with custom configuration
     */
    public RAGEnhancedAIClient(LLMInterface baseAIProvider, RAGConfiguration config) {
        this.baseAIProvider = baseAIProvider;
        this.knowledgeBase = new KnowledgeBase();
        this.config = config;
        
        Log.info("🧠 RAG-Enhanced AI Client initialized");
        Log.info("📊 Knowledge base statistics: " + knowledgeBase.getStatistics());
    }
    
    @Override
    public String generateResponse(String prompt) {
        return generateResponse(prompt, 0.7f, 500);
    }
    
    @Override
    public String generateResponse(String prompt, float temperature, int maxTokens) {
        try {
            // Step 1: Retrieve relevant knowledge
            List<KnowledgeBase.DocumentChunk> relevantKnowledge = retrieveRelevantContext(prompt);
            
            // Step 2: Enhance prompt with retrieved knowledge
            String enhancedPrompt = enhancePromptWithKnowledge(prompt, relevantKnowledge);
            
            // Step 3: Generate response using enhanced prompt
            String response = baseAIProvider.generateResponse(enhancedPrompt, temperature, maxTokens);
            
            // Step 4: Post-process response
            String finalResponse = postProcessResponse(response, relevantKnowledge);
            
            Log.info("🧠 RAG-enhanced response generated with " + relevantKnowledge.size() + " knowledge sources");
            return finalResponse;
            
        } catch (Exception e) {
            Log.error("RAG enhancement failed, falling back to base provider: " + e.getMessage());
            return baseAIProvider.generateResponse(prompt, temperature, maxTokens);
        }
    }
    
    /**
     * Retrieve relevant context from knowledge base
     */
    private List<KnowledgeBase.DocumentChunk> retrieveRelevantContext(String prompt) {
        // Determine the most appropriate category based on prompt content
        KnowledgeBase.DocumentCategory category = null;
        if (config.isEnableCategoryFiltering()) {
            category = determineQueryCategory(prompt);
        }
        
        // Retrieve relevant documents
        List<KnowledgeBase.DocumentChunk> relevantDocs = knowledgeBase.retrieveRelevantKnowledge(
            prompt, config.getMaxRetrievedDocs(), category
        );
        
        // Filter by relevance threshold
        return relevantDocs.stream()
            .filter(doc -> doc.getRelevanceScore() >= config.getRelevanceThreshold())
            .collect(Collectors.toList());
    }
    
    /**
     * Determine the most appropriate category for the query
     */
    private KnowledgeBase.DocumentCategory determineQueryCategory(String prompt) {
        String promptLower = prompt.toLowerCase();
        
        // Check for locator-related queries
        if (promptLower.contains("locator") || promptLower.contains("find element") || 
            promptLower.contains("xpath") || promptLower.contains("css selector") ||
            promptLower.contains("by.id") || promptLower.contains("by.name")) {
            return KnowledgeBase.DocumentCategory.LOCATOR_PATTERNS;
        }
        
        // Check for error-related queries
        if (promptLower.contains("error") || promptLower.contains("exception") || 
            promptLower.contains("failed") || promptLower.contains("not found") ||
            promptLower.contains("timeout")) {
            return KnowledgeBase.DocumentCategory.ERROR_SOLUTIONS;
        }
        
        // Check for troubleshooting queries
        if (promptLower.contains("troubleshoot") || promptLower.contains("debug") ||
            promptLower.contains("problem") || promptLower.contains("issue") ||
            promptLower.contains("fix")) {
            return KnowledgeBase.DocumentCategory.TROUBLESHOOTING;
        }
        
        // Check for test example queries
        if (promptLower.contains("example") || promptLower.contains("how to") ||
            promptLower.contains("sample") || promptLower.contains("tutorial") ||
            promptLower.contains("test")) {
            return KnowledgeBase.DocumentCategory.TEST_EXAMPLES;
        }
        
        // Check for best practices queries
        if (promptLower.contains("best practice") || promptLower.contains("pattern") ||
            promptLower.contains("recommend") || promptLower.contains("should")) {
            return KnowledgeBase.DocumentCategory.BEST_PRACTICES;
        }
        
        // Default: no category filter (search all)
        return null;
    }
    
    /**
     * Enhance the original prompt with retrieved knowledge
     */
    private String enhancePromptWithKnowledge(String originalPrompt, List<KnowledgeBase.DocumentChunk> relevantKnowledge) {
        if (relevantKnowledge.isEmpty()) {
            return originalPrompt;
        }
        
        StringBuilder enhancedPrompt = new StringBuilder();
        
        // Add context header
        enhancedPrompt.append("You are an expert Selenium test automation assistant with access to the following relevant knowledge:\n\n");
        
        // Add retrieved knowledge as context
        int totalContextLength = 0;
        for (int i = 0; i < relevantKnowledge.size() && totalContextLength < config.getMaxContextLength(); i++) {
            KnowledgeBase.DocumentChunk chunk = relevantKnowledge.get(i);
            String contextEntry = String.format("KNOWLEDGE SOURCE %d (Category: %s, Relevance: %.1f):\n%s\n\n",
                i + 1, chunk.getCategory().name(), chunk.getRelevanceScore(), chunk.getContent());
            
            if (totalContextLength + contextEntry.length() <= config.getMaxContextLength()) {
                enhancedPrompt.append(contextEntry);
                totalContextLength += contextEntry.length();
            } else {
                // Truncate if needed
                int remainingLength = config.getMaxContextLength() - totalContextLength;
                if (remainingLength > 100) { // Only add if meaningful amount of content fits
                    enhancedPrompt.append(contextEntry.substring(0, remainingLength)).append("...\n\n");
                }
                break;
            }
        }
        
        // Add instruction to use the knowledge
        enhancedPrompt.append("Based on the above knowledge sources, please answer the following question. ");
        enhancedPrompt.append("Use the provided knowledge to give accurate, context-aware responses. ");
        enhancedPrompt.append("If the knowledge sources contain relevant examples or patterns, incorporate them into your response.\n\n");
        
        // Add original prompt
        enhancedPrompt.append("QUESTION: ").append(originalPrompt);
        
        return enhancedPrompt.toString();
    }
    
    /**
     * Post-process the AI response to add source references and improvements
     */
    private String postProcessResponse(String response, List<KnowledgeBase.DocumentChunk> usedKnowledge) {
        if (!config.isIncludeSourceReferences() || usedKnowledge.isEmpty()) {
            return response;
        }
        
        StringBuilder enhancedResponse = new StringBuilder(response);
        
        // Add knowledge sources section
        enhancedResponse.append("\n\n📚 **Knowledge Sources Used:**\n");
        for (int i = 0; i < usedKnowledge.size(); i++) {
            KnowledgeBase.DocumentChunk chunk = usedKnowledge.get(i);
            enhancedResponse.append(String.format("   %d. %s (Category: %s, Relevance: %.1f)\n", 
                i + 1, chunk.getSource(), chunk.getCategory().name(), chunk.getRelevanceScore()));
        }
        
        // Add helpful footer
        enhancedResponse.append("\n💡 **Tip:** This response was enhanced with context-aware knowledge retrieval for better accuracy.\n");
        
        return enhancedResponse.toString();
    }
    
    /**
     * Generate locator suggestions with RAG enhancement
     */
    public String generateLocatorSuggestions(String elementDescription, String pageContext) {
        String enhancedPrompt = String.format(
            "Generate 5 different Selenium locator strategies to find the element '%s' on a %s page. " +
            "Provide locators in the exact format: By.id(\"...\"), By.xpath(\"...\"), etc. " +
            "Consider the page context and prioritize stable, maintainable locators.",
            elementDescription, pageContext != null ? pageContext : "web"
        );
        
        return generateResponse(enhancedPrompt);
    }
    
    /**
     * Generate troubleshooting suggestions with RAG enhancement
     */
    public String generateTroubleshootingSuggestions(String errorMessage, String testContext) {
        String enhancedPrompt = String.format(
            "Help troubleshoot this Selenium test automation issue:\n" +
            "Error: %s\n" +
            "Context: %s\n\n" +
            "Provide specific, actionable solutions with code examples where appropriate.",
            errorMessage, testContext != null ? testContext : "general test execution"
        );
        
        return generateResponse(enhancedPrompt);
    }
    
    /**
     * Generate test code suggestions with RAG enhancement
     */
    public String generateTestCodeSuggestions(String testScenario, String testType) {
        String enhancedPrompt = String.format(
            "Generate Selenium test code for the following scenario: %s\n" +
            "Test Type: %s\n\n" +
            "Use AI-enhanced element finding with elementHealer.findElement() method. " +
            "Include proper error handling and best practices.",
            testScenario, testType != null ? testType : "functional test"
        );
        
        return generateResponse(enhancedPrompt);
    }
    
    @Override
    public boolean isAvailable() {
        return baseAIProvider.isAvailable();
    }
    
    @Override
    public String getModelInfo() {
        return "RAG-Enhanced " + baseAIProvider.getModelInfo() + 
               " (" + knowledgeBase.getStatistics() + ")";
    }
    
    @Override
    public void close() {
        // Close base provider
        baseAIProvider.close();
    }
    
    /**
     * Add custom knowledge to the RAG system
     */
    public void addCustomKnowledge(String id, String content, String source, 
                                  KnowledgeBase.DocumentCategory category, String... keywords) {
        knowledgeBase.addCustomKnowledge(id, content, source, category, keywords);
    }
    
    /**
     * Get knowledge base statistics
     */
    public String getKnowledgeBaseInfo() {
        return knowledgeBase.getStatistics().toString();
    }
    
    /**
     * Update RAG configuration
     */
    public void updateConfiguration(RAGConfiguration newConfig) {
        this.config.setMaxRetrievedDocs(newConfig.getMaxRetrievedDocs());
        this.config.setMaxContextLength(newConfig.getMaxContextLength());
        this.config.setIncludeSourceReferences(newConfig.isIncludeSourceReferences());
        this.config.setEnableCategoryFiltering(newConfig.isEnableCategoryFiltering());
        this.config.setRelevanceThreshold(newConfig.getRelevanceThreshold());
        
        Log.info("🔧 RAG configuration updated");
    }
}