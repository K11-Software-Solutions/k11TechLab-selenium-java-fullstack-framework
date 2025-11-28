package org.k11techlab.framework.ai.nlp;

import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Natural Language Test Generator - Converts plain English descriptions into complete test code
 * Uses RAG-enhanced AI to generate production-ready Selenium tests with best practices
 */
public class NLTestGenerator {
    
    private final RAGEnhancedAIClient ragAI;
    private final TestPatternAnalyzer patternAnalyzer;
    private final CodeTemplateEngine templateEngine;
    
    public enum TestType {
        LOGIN("User authentication and login flow"),
        FORM_SUBMISSION("Form filling and submission"),
        NAVIGATION("Page navigation and menu interaction"),
        SEARCH("Search functionality testing"),
        DATA_VALIDATION("Input validation and error handling"),
        E_COMMERCE("Shopping cart and checkout flow"),
        CRUD_OPERATIONS("Create, Read, Update, Delete operations"),
        FILE_UPLOAD("File upload and download testing"),
        RESPONSIVE("Responsive design and mobile testing"),
        API_INTEGRATION("API integration and data verification");
        
        private final String description;
        
        TestType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public static class TestGenerationRequest {
        private String naturalLanguageDescription;
        private String testName;
        private String baseUrl;
        private TestType testType;
        private List<String> testSteps;
        private Map<String, String> testData;
        private boolean includeErrorHandling = true;
        private boolean includeScreenshots = true;
        private boolean usePageObjectModel = true;
        
        // Constructors
        public TestGenerationRequest(String description) {
            this.naturalLanguageDescription = description;
            this.testSteps = new ArrayList<>();
            this.testData = new HashMap<>();
        }
        
        // Getters and setters
        public String getNaturalLanguageDescription() { return naturalLanguageDescription; }
        public void setNaturalLanguageDescription(String naturalLanguageDescription) { this.naturalLanguageDescription = naturalLanguageDescription; }
        
        public String getTestName() { return testName; }
        public void setTestName(String testName) { this.testName = testName; }
        
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        
        public TestType getTestType() { return testType; }
        public void setTestType(TestType testType) { this.testType = testType; }
        
        public List<String> getTestSteps() { return testSteps; }
        public void setTestSteps(List<String> testSteps) { this.testSteps = testSteps; }
        
        public Map<String, String> getTestData() { return testData; }
        public void setTestData(Map<String, String> testData) { this.testData = testData; }
        
        public boolean isIncludeErrorHandling() { return includeErrorHandling; }
        public void setIncludeErrorHandling(boolean includeErrorHandling) { this.includeErrorHandling = includeErrorHandling; }
        
        public boolean isIncludeScreenshots() { return includeScreenshots; }
        public void setIncludeScreenshots(boolean includeScreenshots) { this.includeScreenshots = includeScreenshots; }
        
        public boolean isUsePageObjectModel() { return usePageObjectModel; }
        public void setUsePageObjectModel(boolean usePageObjectModel) { this.usePageObjectModel = usePageObjectModel; }
    }
    
    public static class GeneratedTest {
        private String testCode;
        private String testClassName;
        private String pageObjectCode;
        private List<String> generatedSteps;
        private Map<String, String> recommendations;
        private double confidenceScore;
        
        public GeneratedTest(String testCode, String testClassName) {
            this.testCode = testCode;
            this.testClassName = testClassName;
            this.generatedSteps = new ArrayList<>();
            this.recommendations = new HashMap<>();
        }
        
        // Getters and setters
        public String getTestCode() { return testCode; }
        public void setTestCode(String testCode) { this.testCode = testCode; }
        
        public String getTestClassName() { return testClassName; }
        public void setTestClassName(String testClassName) { this.testClassName = testClassName; }
        
        public String getPageObjectCode() { return pageObjectCode; }
        public void setPageObjectCode(String pageObjectCode) { this.pageObjectCode = pageObjectCode; }
        
        public List<String> getGeneratedSteps() { return generatedSteps; }
        public void setGeneratedSteps(List<String> generatedSteps) { this.generatedSteps = generatedSteps; }
        
        public Map<String, String> getRecommendations() { return recommendations; }
        public void setRecommendations(Map<String, String> recommendations) { this.recommendations = recommendations; }
        
        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    }
    
    /**
     * Initialize Natural Language Test Generator
     */
    public NLTestGenerator(RAGEnhancedAIClient ragAI) {
        this.ragAI = ragAI;
        this.patternAnalyzer = new TestPatternAnalyzer();
        this.templateEngine = new CodeTemplateEngine();
        
        Log.info("🎨 Natural Language Test Generator initialized");
    }
    
    /**
     * Generate test from natural language description
     */
    public GeneratedTest generateTest(TestGenerationRequest request) {
        Log.info("🎯 Generating test from description: " + request.getNaturalLanguageDescription());
        
        try {
            // Step 1: Analyze the natural language description
            TestAnalysis analysis = analyzeDescription(request);
            
            // Step 2: Generate test structure
            TestStructure structure = createTestStructure(analysis, request);
            
            // Step 3: Generate test code using RAG
            String testCode = generateTestCode(structure, request);
            
            // Step 4: Generate page object if requested
            String pageObjectCode = null;
            if (request.isUsePageObjectModel()) {
                pageObjectCode = generatePageObject(structure, request);
            }
            
            // Step 5: Create result
            GeneratedTest result = new GeneratedTest(testCode, structure.getClassName());
            result.setPageObjectCode(pageObjectCode);
            result.setGeneratedSteps(structure.getSteps());
            result.setRecommendations(generateRecommendations(analysis, structure));
            result.setConfidenceScore(calculateConfidenceScore(analysis, structure));
            
            Log.info("✅ Test generated successfully with confidence: " + result.getConfidenceScore());
            return result;
            
        } catch (Exception e) {
            Log.error("❌ Test generation failed: " + e.getMessage());
            throw new RuntimeException("Failed to generate test: " + e.getMessage(), e);
        }
    }
    
    /**
     * Quick test generation from simple description
     */
    public GeneratedTest generateQuickTest(String description, String baseUrl) {
        TestGenerationRequest request = new TestGenerationRequest(description);
        request.setBaseUrl(baseUrl);
        request.setTestName(generateTestName(description));
        request.setTestType(detectTestType(description));
        
        return generateTest(request);
    }
    
    /**
     * Generate test with conversation context
     */
    public GeneratedTest generateTestWithContext(String description, String baseUrl, 
                                               List<String> conversationHistory) {
        // Enhance description with conversation context
        String enhancedDescription = enhanceDescriptionWithContext(description, conversationHistory);
        
        TestGenerationRequest request = new TestGenerationRequest(enhancedDescription);
        request.setBaseUrl(baseUrl);
        request.setTestName(generateTestName(description));
        request.setTestType(detectTestType(enhancedDescription));
        
        return generateTest(request);
    }
    
    /**
     * Analyze natural language description
     */
    private TestAnalysis analyzeDescription(TestGenerationRequest request) {
        String prompt = createAnalysisPrompt(request);
        String analysis = ragAI.generateResponse(prompt);
        
        return patternAnalyzer.parseAnalysis(analysis, request);
    }
    
    /**
     * Create analysis prompt for RAG
     */
    private String createAnalysisPrompt(TestGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this test requirement and provide a structured analysis:\n\n");
        prompt.append("Description: ").append(request.getNaturalLanguageDescription()).append("\n");
        
        if (request.getBaseUrl() != null) {
            prompt.append("Base URL: ").append(request.getBaseUrl()).append("\n");
        }
        
        if (request.getTestType() != null) {
            prompt.append("Expected Type: ").append(request.getTestType().getDescription()).append("\n");
        }
        
        prompt.append("\nPlease analyze and provide:\n");
        prompt.append("1. Test steps breakdown\n");
        prompt.append("2. Required elements and their descriptions\n");
        prompt.append("3. Test data requirements\n");
        prompt.append("4. Validation points\n");
        prompt.append("5. Potential edge cases\n");
        prompt.append("6. Best practices to apply\n");
        
        return prompt.toString();
    }
    
    /**
     * Create test structure from analysis
     */
    private TestStructure createTestStructure(TestAnalysis analysis, TestGenerationRequest request) {
        TestStructure structure = new TestStructure();
        
        // Set basic info
        structure.setClassName(request.getTestName() != null ? request.getTestName() : 
                              generateTestName(request.getNaturalLanguageDescription()));
        structure.setPackageName("org.k11techlab.framework.generated.tests");
        structure.setBaseUrl(request.getBaseUrl());
        
        // Extract steps from analysis
        structure.setSteps(analysis.getTestSteps());
        structure.setElements(analysis.getRequiredElements());
        structure.setValidations(analysis.getValidationPoints());
        structure.setTestData(analysis.getTestData());
        
        return structure;
    }
    
    /**
     * Generate test code using RAG
     */
    private String generateTestCode(TestStructure structure, TestGenerationRequest request) {
        String prompt = createCodeGenerationPrompt(structure, request);
        String generatedCode = ragAI.generateResponse(prompt);
        
        // Post-process and clean up the generated code
        return templateEngine.processGeneratedCode(generatedCode, structure);
    }
    
    /**
     * Create code generation prompt
     */
    private String createCodeGenerationPrompt(TestStructure structure, TestGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a complete Selenium test class with the following requirements:\n\n");
        
        prompt.append("Class Name: ").append(structure.getClassName()).append("\n");
        prompt.append("Package: ").append(structure.getPackageName()).append("\n");
        prompt.append("Base URL: ").append(structure.getBaseUrl()).append("\n\n");
        
        prompt.append("Test Steps:\n");
        for (int i = 0; i < structure.getSteps().size(); i++) {
            prompt.append((i + 1)).append(". ").append(structure.getSteps().get(i)).append("\n");
        }
        
        prompt.append("\nRequired Elements:\n");
        structure.getElements().forEach((name, description) -> 
            prompt.append("- ").append(name).append(": ").append(description).append("\n"));
        
        prompt.append("\nValidations:\n");
        structure.getValidations().forEach(validation -> 
            prompt.append("- ").append(validation).append("\n"));
        
        prompt.append("\nRequirements:\n");
        prompt.append("- Use AI-enhanced element finding: elementHealer.findElement(description)\n");
        prompt.append("- Include proper imports and annotations\n");
        prompt.append("- Add TestNG annotations (@Test, @BeforeMethod, @AfterMethod)\n");
        prompt.append("- Extend BaseSeleniumTest class\n");
        
        if (request.isIncludeErrorHandling()) {
            prompt.append("- Include comprehensive error handling with try-catch blocks\n");
        }
        
        if (request.isIncludeScreenshots()) {
            prompt.append("- Capture screenshots on failures\n");
        }
        
        prompt.append("- Use proper waiting strategies with WebDriverWait\n");
        prompt.append("- Follow Java naming conventions\n");
        prompt.append("- Add meaningful assertions and log messages\n");
        
        return prompt.toString();
    }
    
    /**
     * Generate page object code
     */
    private String generatePageObject(TestStructure structure, TestGenerationRequest request) {
        String prompt = createPageObjectPrompt(structure, request);
        return ragAI.generateResponse(prompt);
    }
    
    /**
     * Create page object generation prompt
     */
    private String createPageObjectPrompt(TestStructure structure, TestGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a Page Object Model class for:\n\n");
        
        String pageClassName = structure.getClassName().replace("Test", "Page");
        prompt.append("Class Name: ").append(pageClassName).append("\n");
        prompt.append("Package: ").append(structure.getPackageName().replace("tests", "pages")).append("\n\n");
        
        prompt.append("Required Elements:\n");
        structure.getElements().forEach((name, description) -> 
            prompt.append("- ").append(name).append(": ").append(description).append("\n"));
        
        prompt.append("\nRequirements:\n");
        prompt.append("- Use AI-enhanced element finding in methods\n");
        prompt.append("- Create action methods for each user interaction\n");
        prompt.append("- Include validation methods\n");
        prompt.append("- Use method chaining pattern where appropriate\n");
        prompt.append("- Add WebDriver constructor parameter\n");
        prompt.append("- Include proper imports\n");
        
        return prompt.toString();
    }
    
    /**
     * Detect test type from description
     */
    private TestType detectTestType(String description) {
        String desc = description.toLowerCase();
        
        if (desc.contains("login") || desc.contains("sign in") || desc.contains("authenticate")) {
            return TestType.LOGIN;
        } else if (desc.contains("form") || desc.contains("submit") || desc.contains("input")) {
            return TestType.FORM_SUBMISSION;
        } else if (desc.contains("navigate") || desc.contains("menu") || desc.contains("click")) {
            return TestType.NAVIGATION;
        } else if (desc.contains("search") || desc.contains("find")) {
            return TestType.SEARCH;
        } else if (desc.contains("cart") || desc.contains("buy") || desc.contains("checkout")) {
            return TestType.E_COMMERCE;
        } else if (desc.contains("upload") || desc.contains("file")) {
            return TestType.FILE_UPLOAD;
        } else if (desc.contains("create") || desc.contains("edit") || desc.contains("delete")) {
            return TestType.CRUD_OPERATIONS;
        }
        
        return TestType.NAVIGATION; // Default
    }
    
    /**
     * Generate test name from description
     */
    private String generateTestName(String description) {
        // Always use a concise, valid class name for generated tests
        // If the description contains 'login', use 'GeneratedLoginTest', etc.
        String desc = description.toLowerCase();
        if (desc.contains("login")) {
            return "GeneratedLoginTest";
        } else if (desc.contains("e-commerce") || desc.contains("shopping") || desc.contains("cart") || desc.contains("checkout")) {
            return "GeneratedEcommerceTest";
        } else if (desc.contains("search")) {
            return "GeneratedSearchTest";
        } else if (desc.contains("form")) {
            return "GeneratedFormTest";
        } else if (desc.contains("api")) {
            return "GeneratedApiTest";
        }
        // Fallback: use 'GeneratedTest'
        return "GeneratedTest";
    }
    
    /**
     * Enhance description with conversation context
     */
    private String enhanceDescriptionWithContext(String description, List<String> context) {
        if (context == null || context.isEmpty()) {
            return description;
        }
        
        StringBuilder enhanced = new StringBuilder(description);
        enhanced.append("\n\nContext from previous conversation:\n");
        
        for (int i = Math.max(0, context.size() - 3); i < context.size(); i++) {
            enhanced.append("- ").append(context.get(i)).append("\n");
        }
        
        return enhanced.toString();
    }
    
    /**
     * Generate recommendations
     */
    private Map<String, String> generateRecommendations(TestAnalysis analysis, TestStructure structure) {
        Map<String, String> recommendations = new HashMap<>();
        
        recommendations.put("maintenance", "Use AI element descriptions instead of brittle locators");
        recommendations.put("reliability", "Add explicit waits for dynamic elements");
        recommendations.put("debugging", "Include screenshot capture on test failures");
        recommendations.put("data", "Consider parameterizing test data for different scenarios");
        
        if (structure.getSteps().size() > 10) {
            recommendations.put("complexity", "Consider breaking this into multiple smaller tests");
        }
        
        return recommendations;
    }
    
    /**
     * Calculate confidence score
     */
    private double calculateConfidenceScore(TestAnalysis analysis, TestStructure structure) {
        double score = 0.5; // Base score
        
        // Add points for clear steps
        if (structure.getSteps().size() > 2) score += 0.2;
        
        // Add points for identified elements
        if (!structure.getElements().isEmpty()) score += 0.2;
        
        // Add points for validations
        if (!structure.getValidations().isEmpty()) score += 0.1;
        
        return Math.min(1.0, score);
    }
    
    /**
     * Supporting classes
     */
    private static class TestAnalysis {
        private List<String> testSteps = new ArrayList<>();
        private Map<String, String> requiredElements = new HashMap<>();
        private List<String> validationPoints = new ArrayList<>();
        private Map<String, String> testData = new HashMap<>();
        
        // Getters and setters
        public List<String> getTestSteps() { return testSteps; }
        public void setTestSteps(List<String> testSteps) { this.testSteps = testSteps; }
        
        public Map<String, String> getRequiredElements() { return requiredElements; }
        public void setRequiredElements(Map<String, String> requiredElements) { this.requiredElements = requiredElements; }
        
        public List<String> getValidationPoints() { return validationPoints; }
        public void setValidationPoints(List<String> validationPoints) { this.validationPoints = validationPoints; }
        
        public Map<String, String> getTestData() { return testData; }
        public void setTestData(Map<String, String> testData) { this.testData = testData; }
    }
    
    private static class TestStructure {
        private String className;
        private String packageName;
        private String baseUrl;
        private List<String> steps = new ArrayList<>();
        private Map<String, String> elements = new HashMap<>();
        private List<String> validations = new ArrayList<>();
        private Map<String, String> testData = new HashMap<>();
        
        // Getters and setters
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        
        public List<String> getSteps() { return steps; }
        public void setSteps(List<String> steps) { this.steps = steps; }
        
        public Map<String, String> getElements() { return elements; }
        public void setElements(Map<String, String> elements) { this.elements = elements; }
        
        public List<String> getValidations() { return validations; }
        public void setValidations(List<String> validations) { this.validations = validations; }
        
        public Map<String, String> getTestData() { return testData; }
        public void setTestData(Map<String, String> testData) { this.testData = testData; }
    }
    
    /**
     * Analyze test patterns
     */
    private static class TestPatternAnalyzer {
        public TestAnalysis parseAnalysis(String analysisText, TestGenerationRequest request) {
            TestAnalysis analysis = new TestAnalysis();
            
            // Simple parsing logic - could be enhanced with more sophisticated NLP
            String[] lines = analysisText.split("\n");
            String currentSection = "";
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.toLowerCase().contains("steps")) {
                    currentSection = "steps";
                } else if (line.toLowerCase().contains("elements")) {
                    currentSection = "elements";
                } else if (line.toLowerCase().contains("validation")) {
                    currentSection = "validation";
                } else if (line.toLowerCase().contains("data")) {
                    currentSection = "data";
                } else {
                    switch (currentSection) {
                        case "steps":
                            if (line.matches("^\\d+\\..*")) {
                                analysis.getTestSteps().add(line.replaceFirst("^\\d+\\.\\s*", ""));
                            }
                            break;
                        case "elements":
                            if (line.contains(":")) {
                                String[] parts = line.split(":", 2);
                                if (parts.length == 2) {
                                    analysis.getRequiredElements().put(parts[0].trim(), parts[1].trim());
                                }
                            }
                            break;
                        case "validation":
                            if (line.startsWith("-") || line.matches("^\\d+\\..*")) {
                                analysis.getValidationPoints().add(line.replaceFirst("^[-\\d\\.\\s]*", ""));
                            }
                            break;
                    }
                }
            }
            
            // Fallback parsing from request if analysis is insufficient
            if (analysis.getTestSteps().isEmpty() && !request.getTestSteps().isEmpty()) {
                analysis.setTestSteps(request.getTestSteps());
            }
            
            return analysis;
        }
    }
    
    /**
     * Process and clean generated code
     */
    private static class CodeTemplateEngine {
        public String processGeneratedCode(String rawCode, TestStructure structure) {
            // Remove markdown code blocks if present
            String cleanCode = rawCode.replaceAll("```java\\n?", "").replaceAll("```\\n?", "");
            
            // Ensure proper formatting
            if (!cleanCode.contains("package " + structure.getPackageName())) {
                cleanCode = "package " + structure.getPackageName() + ";\n\n" + cleanCode;
            }
            
            // Add standard imports if missing
            if (!cleanCode.contains("import org.testng.annotations.Test")) {
                String imports = generateStandardImports();
                int packageEnd = cleanCode.indexOf(";") + 1;
                cleanCode = cleanCode.substring(0, packageEnd) + "\n\n" + imports + "\n" + 
                           cleanCode.substring(packageEnd);
            }
            
            return cleanCode;
        }
        
        private String generateStandardImports() {
            return "import org.testng.annotations.*;\n" +
                   "import org.openqa.selenium.WebElement;\n" +
                   "import org.openqa.selenium.support.ui.WebDriverWait;\n" +
                   "import org.openqa.selenium.support.ui.ExpectedConditions;\n" +
                   "import org.k11techlab.framework.selenium.webuitestbase.BaseSeleniumTest;\n" +
                   "import org.k11techlab.framework.ai.healing.AIElementHealer;\n" +
                   "import java.time.Duration;\n" +
                   "import static org.testng.Assert.*;";
        }
    }
}