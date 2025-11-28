package org.k11techlab.framework.ai.rag;
import java.nio.file.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Knowledge Base for RAG (Retrieval-Augmented Generation) System
 * Stores and retrieves domain-specific knowledge for AI-enhanced responses
 */

import org.k11techlab.framework.ai.rag.components.*;

public class KnowledgeBase {

    private final List<DocumentChunk> knowledgeChunks;
    private final Map<DocumentCategory, Integer> categoryCounts;

    // RAG components
    private final VectorStore vectorStore;
    private final DocumentRetriever documentRetriever;
    private final EmbeddingCache embeddingCache;
    private final DocumentRetriever.EmbeddingFunction embeddingFunction;

    public enum DocumentCategory {
        LOCATOR_PATTERNS,
        TEST_EXAMPLES,
        TROUBLESHOOTING,
        BEST_PRACTICES,
        ERROR_SOLUTIONS,
        FRAMEWORK_DOCS
    }

    /**
     * Document chunk containing knowledge content and metadata
     */
    public static class DocumentChunk {
        private String id;
        private String content;
        private String source;
        private DocumentCategory category;
        private Set<String> keywords;
        private double relevanceScore = 0.0;

        public DocumentChunk(String id, String content, String source, DocumentCategory category, String... keywords) {
            this.id = id;
            this.content = content;
            this.source = source;
            this.category = category;
            this.keywords = new HashSet<>(Arrays.asList(keywords));
        }

        // Getters and setters
        public String getId() { return id; }
        public String getContent() { return content; }
        public String getSource() { return source; }
        public DocumentCategory getCategory() { return category; }
        public Set<String> getKeywords() { return keywords; }
        public double getRelevanceScore() { return relevanceScore; }
        public void setRelevanceScore(double score) { this.relevanceScore = score; }

        public void addKeywords(String... newKeywords) {
            this.keywords.addAll(Arrays.asList(newKeywords));
        }
    }

    /**
     * Initialize knowledge base with built-in knowledge
     */

    /**
     * Initialize knowledge base with built-in knowledge and RAG components
     * Uses OllamaEmbedder by default (can be changed to OpenAI/HuggingFace as needed)
     */
    public KnowledgeBase() {
        this.knowledgeChunks = new ArrayList<>();
        this.categoryCounts = new HashMap<>();

        // --- RAG components setup ---
        // You can change these URLs/keys as needed for your environment
        String ollamaUrl = System.getProperty("ollama.url", "http://localhost:11434");
        String ollamaModel = System.getProperty("ollama.model", "llama3");
        this.embeddingCache = new EmbeddingCache("rag_embedding_cache.json");

        DocumentRetriever.EmbeddingFunction embedder = new OllamaEmbedder(ollamaUrl, ollamaModel);
        this.embeddingFunction = text -> {
            double[] cached = embeddingCache.get(text);
            if (cached != null) return cached;
            double[] vec = embedder.embed(text);
            if (vec != null && vec.length > 0) embeddingCache.put(text, vec);
            return vec;
        };
        this.vectorStore = new VectorStore();
        this.documentRetriever = new DocumentRetriever(vectorStore, embeddingFunction);

        Log.info("🧠 Initializing RAG Knowledge Base...");
        indexBuiltInKnowledge();
        indexExternalDocs();
        // Index all chunks into vector store for RAG
        indexChunksToVectorStore();
        embeddingCache.save();
        Log.info("📚 Knowledge Base initialized with " + knowledgeChunks.size() + " documents (RAG ready)");
    }

    /**
     * Index all knowledge chunks into the VectorStore for RAG retrieval
     */
    private void indexChunksToVectorStore() {
        for (DocumentChunk chunk : knowledgeChunks) {
            double[] vec = embeddingFunction.embed(chunk.getContent());
            if (vec != null && vec.length > 0) {
                vectorStore.add(chunk.getId(), vec, chunk);
            }
        }
    }
        /**
         * Retrieve relevant knowledge using RAG vector search (semantic)
         * Returns DocumentChunks ranked by vector similarity
         */
        public List<DocumentChunk> retrieveRelevantKnowledgeRAG(String query, int maxResults) {
            List<VectorStore.VectorEntry> entries = documentRetriever.retrieve(query, maxResults);
            List<DocumentChunk> result = new ArrayList<>();
            for (VectorStore.VectorEntry entry : entries) {
                if (entry.payload instanceof DocumentChunk) {
                    result.add((DocumentChunk) entry.payload);
                }
            }
            return result;
        }
    /**
     * Index external documentation files from testartifacts/docs
     */
    private void indexExternalDocs() {
        Path docsRoot = Paths.get("testartifacts", "docs");
        if (!Files.exists(docsRoot)) return;
        try {
            Files.walk(docsRoot)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    String fileName = path.getFileName().toString().toLowerCase();
                    try {
                        String content = null;
                        if (fileName.endsWith(".md") || fileName.endsWith(".txt")) {
                            content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                        } else if (fileName.endsWith(".pdf")) {
                            try (PDDocument pdf = PDDocument.load(path.toFile())) {
                                PDFTextStripper stripper = new PDFTextStripper();
                                content = stripper.getText(pdf);
                            }
                        }
                        if (content != null && !content.trim().isEmpty()) {
                            DocumentChunk chunk = new DocumentChunk(
                                path.getFileName().toString(),
                                content,
                                path.toString(),
                                DocumentCategory.FRAMEWORK_DOCS,
                                "doc", "external", fileName
                            );
                            knowledgeChunks.add(chunk);
                        }
                    } catch (Exception e) {
                        Log.info("Failed to index doc: " + path + " - " + e.getMessage());
                    }
                });
        } catch (IOException e) {
            Log.info("Error walking docs directory: " + e.getMessage());
        }
    }

    /**
     * Index all built-in knowledge
     */
    private void indexBuiltInKnowledge() {
        indexLocatorPatterns();
        indexTestExamples();
        indexTroubleshootingGuides();
        indexBestPractices();
        indexErrorSolutions();
        indexFrameworkDocs();
    }

    /**
     * Index locator patterns and strategies
     */
    private void indexLocatorPatterns() {
        Map<String, String> locatorPatterns = new HashMap<>();
        
        locatorPatterns.put("id-locator", "By.id('element-id') - Most reliable locator if stable ID exists. Performance: Excellent. Use when elements have unique IDs.");
        
        locatorPatterns.put("name-locator", "By.name('element-name') - Good for form elements. Semantic meaning preserved. Best for form inputs and buttons.");
        
        locatorPatterns.put("css-selector", "By.cssSelector('selector') - Flexible and fast. Use semantic selectors like button[type='submit'] or input[data-testid='username'].");
        
        locatorPatterns.put("xpath-best-practices", "XPath Guidelines: Avoid absolute paths like //div[1]/div[2]. Use relative paths with attributes. Prefer contains() for dynamic content.");
        
        locatorPatterns.put("data-testid", "By.cssSelector('[data-testid=\"value\"]') - Dedicated test attributes. Developer-friendly and stable across UI changes.");
        
        locatorPatterns.put("semantic-locators", "Use semantic HTML elements: By.tagName('nav'), By.cssSelector('main'), By.cssSelector('article'). More stable than class-based locators.");
        
        locatorPatterns.put("anti-patterns", "AVOID: Positional XPaths (//div[3]/button[1]), Style-dependent classes (.btn-primary), Generic tag names (By.tagName('button'))");

        for (Map.Entry<String, String> entry : locatorPatterns.entrySet()) {
            DocumentChunk chunk = new DocumentChunk(
                entry.getKey(),
                entry.getValue(),
                "built-in-patterns",
                DocumentCategory.LOCATOR_PATTERNS,
                "locator", "selenium", "element", "finding"
            );
            String[] keywordArray = entry.getKey().split("-");
            chunk.addKeywords(keywordArray);
            knowledgeChunks.add(chunk);
        }
        
        categoryCounts.put(DocumentCategory.LOCATOR_PATTERNS, locatorPatterns.size());
    }

    /**
     * Index test examples and templates
     */
    private void indexTestExamples() {
        Map<String, String> testExamples = new HashMap<>();
        
        testExamples.put("login-test", 
            "// AI-Enhanced Login Test Example\n" +
            "@Test\n" +
            "public void testLogin() {\n" +
            "    WebElement usernameField = elementHealer.findElement(\"username input field\");\n" +
            "    WebElement passwordField = elementHealer.findElement(\"password input field\");\n" +
            "    WebElement loginButton = elementHealer.findElement(\"login submit button\");\n" +
            "    \n" +
            "    usernameField.sendKeys(\"testuser\");\n" +
            "    passwordField.sendKeys(\"password\");\n" +
            "    loginButton.click();\n" +
            "    \n" +
            "    WebElement welcomeMessage = elementHealer.findElement(\"welcome message\");\n" +
            "    assert welcomeMessage.isDisplayed();\n" +
            "}"
        );
        
        testExamples.put("form-test", 
            "// AI-Enhanced Form Test with Error Handling\n" +
            "@Test\n" +
            "public void testFormSubmission() {\n" +
            "    try {\n" +
            "        WebElement form = elementHealer.findElement(\"contact form\");\n" +
            "        WebElement nameField = elementHealer.findElement(\"name input in contact form\");\n" +
            "        WebElement submitBtn = elementHealer.findElement(\"submit button in contact form\");\n" +
            "        \n" +
            "        nameField.clear();\n" +
            "        nameField.sendKeys(\"Test User\");\n" +
            "        submitBtn.click();\n" +
            "        \n" +
            "        WebElement successMsg = elementHealer.findElementWithWait(\"success message\", 10);\n" +
            "        assertTrue(successMsg.isDisplayed());\n" +
            "    } catch (ElementNotFoundException e) {\n" +
            "        captureScreenshot();\n" +
            "        throw e;\n" +
            "    }\n" +
            "}"
        );
        
        testExamples.put("dropdown-test", 
            "// AI-Enhanced Dropdown Selection Test\n" +
            "@Test\n" +
            "public void testDropdownSelection() {\n" +
            "    WebElement dropdown = elementHealer.findElement(\"country dropdown\");\n" +
            "    Select select = new Select(dropdown);\n" +
            "    \n" +
            "    // Wait for options to load\n" +
            "    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));\n" +
            "    wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(\n" +
            "        By.tagName(\"option\"), 1));\n" +
            "    \n" +
            "    select.selectByVisibleText(\"United States\");\n" +
            "    \n" +
            "    WebElement selectedOption = elementHealer.findElement(\"selected country option\");\n" +
            "    assertEquals(\"United States\", selectedOption.getText());\n" +
            "}"
        );

        for (Map.Entry<String, String> entry : testExamples.entrySet()) {
            DocumentChunk chunk = new DocumentChunk(
                entry.getKey(),
                entry.getValue(),
                "built-in-examples",
                DocumentCategory.TEST_EXAMPLES,
                "test", "example", "selenium", "automation"
            );
            String[] keywordArray = entry.getKey().split("-");
            chunk.addKeywords(keywordArray);
            knowledgeChunks.add(chunk);
        }
        
        categoryCounts.put(DocumentCategory.TEST_EXAMPLES, testExamples.size());
    }

    /**
     * Index troubleshooting guides
     */
    private void indexTroubleshootingGuides() {
        Map<String, String> troubleshootingGuides = new HashMap<>();
        
        troubleshootingGuides.put("element-not-found", 
            "When AI healing fails to find an element:\n" +
            "1. Check if the element description is clear and specific\n" +
            "2. Verify the element is visible and not in an iframe\n" +
            "3. Add wait conditions for dynamic content\n" +
            "4. Use browser developer tools to verify element existence\n" +
            "5. Try alternative descriptions: 'login button' vs 'sign in button'"
        );
        
        troubleshootingGuides.put("ai-provider-timeout", 
            "When AI provider requests timeout:\n" +
            "1. Check AI provider service is running (Ollama/LM Studio)\n" +
            "2. Increase timeout values in configuration\n" +
            "3. Verify network connectivity to AI service\n" +
            "4. Check AI service logs for errors\n" +
            "5. Use fallback providers as backup"
        );
        
        troubleshootingGuides.put("slow-ai-responses", 
            "To improve AI response performance:\n" +
            "1. Use local AI providers (Ollama/LM Studio) vs cloud APIs\n" +
            "2. Choose smaller, faster models (llama3.2:1b vs llama3.2:8b)\n" +
            "3. Reduce context length in prompts\n" +
            "4. Implement response caching\n" +
            "5. Use specific element descriptions to get faster matches"
        );

        for (Map.Entry<String, String> entry : troubleshootingGuides.entrySet()) {
            DocumentChunk chunk = new DocumentChunk(
                entry.getKey(),
                entry.getValue(),
                "built-in-troubleshooting",
                DocumentCategory.TROUBLESHOOTING,
                "troubleshoot", "error", "problem"
            );
            String[] keywordArray = entry.getKey().split("-");
            chunk.addKeywords(keywordArray);
            knowledgeChunks.add(chunk);
        }
        
        categoryCounts.put(DocumentCategory.TROUBLESHOOTING, troubleshootingGuides.size());
    }

    /**
     * Index best practices
     */
    private void indexBestPractices() {
        Map<String, String> bestPractices = new HashMap<>();
        
        bestPractices.put("element-descriptions", 
            "AI Element Description Best Practices:\n" +
            "- Be specific: 'submit button in login form' vs 'button'\n" +
            "- Include context: 'username field in registration form'\n" +
            "- Use semantic terms: 'navigation menu' vs 'div element'\n" +
            "- Describe function: 'save changes button' vs 'green button'\n" +
            "- Avoid technical details: 'search box' vs 'input[type=text]'"
        );
        
        bestPractices.put("wait-strategies", 
            "Selenium Wait Best Practices:\n" +
            "- Use explicit waits over implicit waits\n" +
            "- Wait for element conditions, not fixed times\n" +
            "- Combine AI healing with proper wait strategies\n" +
            "- Use ExpectedConditions for common scenarios\n" +
            "- Set reasonable timeout values (5-15 seconds)"
        );
        
        bestPractices.put("error-handling", 
            "Test Error Handling Best Practices:\n" +
            "- Always use try-catch blocks for AI element finding\n" +
            "- Capture screenshots on failures\n" +
            "- Log detailed error information\n" +
            "- Implement retry mechanisms for flaky elements\n" +
            "- Fail fast but provide meaningful error messages"
        );

        for (Map.Entry<String, String> entry : bestPractices.entrySet()) {
            DocumentChunk chunk = new DocumentChunk(
                entry.getKey(),
                entry.getValue(),
                "built-in-practices",
                DocumentCategory.BEST_PRACTICES,
                "best", "practice", "recommendation"
            );
            String[] keywordArray = entry.getKey().split("-");
            chunk.addKeywords(keywordArray);
            knowledgeChunks.add(chunk);
        }
        
        categoryCounts.put(DocumentCategory.BEST_PRACTICES, bestPractices.size());
    }

    /**
     * Index error solutions
     */
    private void indexErrorSolutions() {
        Map<String, String> errorSolutions = new HashMap<>();
        
        errorSolutions.put("NoSuchElementException", 
            "Solution for NoSuchElementException:\n" +
            "1. Element description may be too vague - be more specific\n" +
            "2. Page might still be loading - add explicit wait\n" +
            "3. Element might be in different iframe - switch context\n" +
            "4. Element might be dynamically created - wait for presence\n" +
            "5. Use AI healing with better descriptions\n" +
            "Code: WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));\n" +
            "wait.until(ExpectedConditions.presenceOfElementLocated(locator));"
        );
        
        errorSolutions.put("TimeoutException", 
            "Solution for TimeoutException:\n" +
            "1. Increase wait timeout: WebDriverWait(driver, Duration.ofSeconds(30))\n" +
            "2. Check if element actually appears on page\n" +
            "3. Verify network conditions and page load times\n" +
            "4. Use different wait conditions (visibility vs presence)\n" +
            "5. Implement retry logic with exponential backoff\n" +
            "Code: FluentWait<WebDriver> wait = new FluentWait<>(driver)\n" +
            "    .withTimeout(Duration.ofSeconds(30))\n" +
            "    .pollingEvery(Duration.ofSeconds(2));"
        );
        
        errorSolutions.put("StaleElementReferenceException", 
            "Solution for StaleElementReferenceException:\n" +
            "1. Re-find the element instead of storing reference\n" +
            "2. Use AI healing to find fresh element references\n" +
            "3. Avoid storing WebElement objects in class variables\n" +
            "4. Refresh page or navigate if DOM has changed significantly\n" +
            "5. Use Page Object Model with element re-finding\n" +
            "Code: // Instead of storing element, find it fresh each time\n" +
            "WebElement element = elementHealer.findElement('button description');"
        );

        for (Map.Entry<String, String> entry : errorSolutions.entrySet()) {
            DocumentChunk chunk = new DocumentChunk(
                entry.getKey(),
                entry.getValue(),
                "built-in-solutions",
                DocumentCategory.ERROR_SOLUTIONS,
                "error", "exception", "solution", "fix"
            );
            chunk.addKeywords(entry.getKey().toLowerCase().replace("exception", "").split("(?=[A-Z])"));
            knowledgeChunks.add(chunk);
        }
        
        categoryCounts.put(DocumentCategory.ERROR_SOLUTIONS, errorSolutions.size());
    }

    /**
     * Index framework documentation
     */
    private void indexFrameworkDocs() {
        Map<String, String> frameworkDocs = new HashMap<>();
        
        frameworkDocs.put("ai-healing-setup", 
            "AI Element Healing Setup:\n" +
            "1. Initialize AIProviderManager with desired provider\n" +
            "2. Create AIElementHealer instance with WebDriver\n" +
            "3. Use elementHealer.findElement() instead of driver.findElement()\n" +
            "4. Configure provider fallbacks for reliability\n" +
            "Code Example:\n" +
            "AIProviderManager manager = new AIProviderManager(true);\n" +
            "AIElementHealer healer = new AIElementHealer(manager.getProvider(), driver);\n" +
            "WebElement element = healer.findElement('submit button');"
        );
        
        frameworkDocs.put("configuration", 
            "Framework Configuration:\n" +
            "1. Set AI provider URLs in test-config.properties\n" +
            "2. Configure timeouts and retry attempts\n" +
            "3. Enable/disable AI features with system properties\n" +
            "4. Set up logging levels for debugging\n" +
            "Properties: ai.provider.url, ai.timeout.seconds, ai.retry.attempts"
        );

        for (Map.Entry<String, String> entry : frameworkDocs.entrySet()) {
            DocumentChunk chunk = new DocumentChunk(
                entry.getKey(),
                entry.getValue(),
                "framework-docs",
                DocumentCategory.FRAMEWORK_DOCS,
                "framework", "documentation", "setup", "configuration"
            );
            String[] keywordArray = entry.getKey().split("-");
            chunk.addKeywords(keywordArray);
            knowledgeChunks.add(chunk);
        }
        
        categoryCounts.put(DocumentCategory.FRAMEWORK_DOCS, frameworkDocs.size());
    }

    /**
     * Retrieve relevant knowledge based on query
     */
    public List<DocumentChunk> retrieveRelevantKnowledge(String query, int maxResults, DocumentCategory category) {
        List<DocumentChunk> candidates = new ArrayList<>();
        
        // Filter by category if specified
        for (DocumentChunk chunk : knowledgeChunks) {
            if (category == null || chunk.getCategory() == category) {
                double relevanceScore = calculateRelevanceScore(query, chunk);
                if (relevanceScore > 0) {
                    chunk.setRelevanceScore(relevanceScore);
                    candidates.add(chunk);
                }
            }
        }
        
        // Sort by relevance score (descending) and return top results
        return candidates.stream()
            .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
            .limit(maxResults)
            .collect(Collectors.toList());
    }

    /**
     * Calculate relevance score between query and document chunk
     */
    private double calculateRelevanceScore(String query, DocumentChunk chunk) {
        if (query == null || query.trim().isEmpty()) {
            return 0.0;
        }
        
        double score = 0.0;
        String queryLower = query.toLowerCase().trim();
        String contentLower = chunk.getContent().toLowerCase();
        String[] queryTerms = queryLower.split("\\s+");
        
        // Keyword matching
        for (String term : queryTerms) {
            if (term.length() < 3) continue; // Skip very short terms
            
            // Exact keyword match (highest weight)
            if (chunk.getKeywords().contains(term)) {
                score += 3.0;
            }
            
            // Content contains term
            if (contentLower.contains(term)) {
                score += 2.0;
            }
            
            // Partial keyword match
            for (String keyword : chunk.getKeywords()) {
                if (keyword.contains(term) || term.contains(keyword)) {
                    score += 1.5;
                }
            }
        }
        
        // Boost score for category relevance
        if (isQueryCategoryMatch(queryLower, chunk.getCategory())) {
            score *= 1.5;
        }
        
        // Boost for source quality
        if ("built-in-patterns".equals(chunk.getSource()) || "built-in-solutions".equals(chunk.getSource())) {
            score *= 1.2;
        }
        
        return score;
    }

    /**
     * Check if query matches document category
     */
    private boolean isQueryCategoryMatch(String query, DocumentCategory category) {
        switch (category) {
            case LOCATOR_PATTERNS:
                return query.contains("locator") || query.contains("find") || query.contains("element") || 
                       query.contains("xpath") || query.contains("css") || query.contains("selector");
            case ERROR_SOLUTIONS:
                return query.contains("error") || query.contains("exception") || query.contains("failed") ||
                       query.contains("not found") || query.contains("timeout") || query.contains("stale");
            case TROUBLESHOOTING:
                return query.contains("troubleshoot") || query.contains("debug") || query.contains("problem") ||
                       query.contains("issue") || query.contains("fix") || query.contains("help");
            case TEST_EXAMPLES:
                return query.contains("example") || query.contains("how to") || query.contains("sample") ||
                       query.contains("tutorial") || query.contains("test");
            case BEST_PRACTICES:
                return query.contains("best practice") || query.contains("pattern") || query.contains("recommend") ||
                       query.contains("should") || query.contains("guideline");
            case FRAMEWORK_DOCS:
                return query.contains("setup") || query.contains("configuration") || query.contains("framework") ||
                       query.contains("initialize") || query.contains("config");
            default:
                return false;
        }
    }

    /**
     * Add custom knowledge to the knowledge base
     */
    public void addCustomKnowledge(String id, String content, String source, DocumentCategory category, String... keywords) {
        DocumentChunk chunk = new DocumentChunk(id, content, source, category, keywords);
        knowledgeChunks.add(chunk);
        categoryCounts.merge(category, 1, Integer::sum);
        Log.info("📝 Added custom knowledge: " + id + " to category: " + category);
    }

    /**
     * Get knowledge base statistics
     */
    public String getStatistics() {
        int totalDocs = knowledgeChunks.size();
        int categories = categoryCounts.size();
        return String.format("Documents: %d, Categories: %d, Avg per category: %.1f", 
            totalDocs, categories, totalDocs / (double) categories);
    }

    /**
     * Get knowledge base info with detailed breakdown
     */
    public String getKnowledgeBaseInfo() {
        StringBuilder info = new StringBuilder();
        info.append("📚 RAG Knowledge Base Information:\n");
        info.append("Total Documents: ").append(knowledgeChunks.size()).append("\n");
        info.append("Categories: ").append(categoryCounts.size()).append("\n\n");
        
        for (Map.Entry<DocumentCategory, Integer> entry : categoryCounts.entrySet()) {
            info.append("• ").append(entry.getKey().name()).append(": ").append(entry.getValue()).append(" documents\n");
        }
        
        return info.toString();
    }

    /**
     * Get all knowledge chunks (for testing/debugging)
     */
    public List<DocumentChunk> getAllKnowledgeChunks() {
        return new ArrayList<>(knowledgeChunks);
    }
}