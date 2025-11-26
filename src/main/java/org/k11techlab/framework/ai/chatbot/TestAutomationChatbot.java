package org.k11techlab.framework.ai.chatbot;

import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.ai.nlp.NLTestGenerator;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RAG-Powered Test Automation Chatbot
 * Provides interactive conversational interface for test automation with memory and context awareness
 */
public class TestAutomationChatbot {
    
    private final RAGEnhancedAIClient ragAI;
    private final NLTestGenerator testGenerator;
    private final ConversationManager conversationManager;
    private final CommandProcessor commandProcessor;
    private final ResponseFormatter formatter;
    
    public enum ChatbotMode {
        GENERAL_ASSISTANCE("General test automation help and guidance"),
        TEST_GENERATION("Focus on creating and generating test code"),
        DEBUGGING_HELP("Help with debugging test failures and issues"),
        BEST_PRACTICES("Provide best practices and recommendations"),
        CODE_REVIEW("Review and improve existing test code"),
        FRAMEWORK_SETUP("Help with framework configuration and setup");
        
        private final String description;
        
        ChatbotMode(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public static class ChatSession {
        private final String sessionId;
        private final LocalDateTime createdAt;
        private LocalDateTime lastActivity;
        private ChatbotMode mode;
        private List<ChatMessage> conversationHistory;
        private Map<String, Object> sessionContext;
        private String currentProject;
        private boolean isActive;
        
        public ChatSession(String sessionId) {
            this.sessionId = sessionId;
            this.createdAt = LocalDateTime.now();
            this.lastActivity = LocalDateTime.now();
            this.mode = ChatbotMode.GENERAL_ASSISTANCE;
            this.conversationHistory = new ArrayList<>();
            this.sessionContext = new HashMap<>();
            this.isActive = true;
        }
        
        // Getters and setters
        public String getSessionId() { return sessionId; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getLastActivity() { return lastActivity; }
        public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
        public ChatbotMode getMode() { return mode; }
        public void setMode(ChatbotMode mode) { this.mode = mode; }
        public List<ChatMessage> getConversationHistory() { return conversationHistory; }
        public Map<String, Object> getSessionContext() { return sessionContext; }
        public String getCurrentProject() { return currentProject; }
        public void setCurrentProject(String currentProject) { this.currentProject = currentProject; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        
        public void addMessage(ChatMessage message) {
            conversationHistory.add(message);
            setLastActivity(LocalDateTime.now());
        }
    }
    
    public static class ChatMessage {
        private final String id;
        private final String content;
        private final MessageType type;
        private final LocalDateTime timestamp;
        private final String sender;
        private Map<String, Object> metadata;
        private List<String> attachments;
        
        public enum MessageType {
            USER_MESSAGE,
            BOT_RESPONSE,
            SYSTEM_MESSAGE,
            CODE_SNIPPET,
            TEST_GENERATED,
            ERROR_REPORT,
            RECOMMENDATION
        }
        
        public ChatMessage(String content, MessageType type, String sender) {
            this.id = UUID.randomUUID().toString();
            this.content = content;
            this.type = type;
            this.timestamp = LocalDateTime.now();
            this.sender = sender;
            this.metadata = new HashMap<>();
            this.attachments = new ArrayList<>();
        }
        
        // Getters
        public String getId() { return id; }
        public String getContent() { return content; }
        public MessageType getType() { return type; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getSender() { return sender; }
        public Map<String, Object> getMetadata() { return metadata; }
        public List<String> getAttachments() { return attachments; }
        
        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
        }
        
        public void addAttachment(String attachment) {
            attachments.add(attachment);
        }
    }
    
    public static class ChatResponse {
        private final String content;
        private final ChatMessage.MessageType type;
        private final Map<String, Object> actions;
        private final List<String> suggestions;
        private final boolean requiresFollowUp;
        
        public ChatResponse(String content, ChatMessage.MessageType type) {
            this.content = content;
            this.type = type;
            this.actions = new HashMap<>();
            this.suggestions = new ArrayList<>();
            this.requiresFollowUp = false;
        }
        
        public ChatResponse(String content, ChatMessage.MessageType type, boolean requiresFollowUp) {
            this.content = content;
            this.type = type;
            this.actions = new HashMap<>();
            this.suggestions = new ArrayList<>();
            this.requiresFollowUp = requiresFollowUp;
        }
        
        // Getters
        public String getContent() { return content; }
        public ChatMessage.MessageType getType() { return type; }
        public Map<String, Object> getActions() { return actions; }
        public List<String> getSuggestions() { return suggestions; }
        public boolean isRequiresFollowUp() { return requiresFollowUp; }
    }
    
    /**
     * Initialize the Test Automation Chatbot
     */
    public TestAutomationChatbot(RAGEnhancedAIClient ragAI, NLTestGenerator testGenerator) {
        this.ragAI = ragAI;
        this.testGenerator = testGenerator;
        this.conversationManager = new ConversationManager();
        this.commandProcessor = new CommandProcessor(ragAI, testGenerator);
        this.formatter = new ResponseFormatter();
        
        Log.info("🤖 Test Automation Chatbot initialized and ready for conversations!");
    }
    
    /**
     * Start a new chat session
     */
    public String startSession() {
        return startSession(null);
    }
    
    /**
     * Start a new chat session with specific project context
     */
    public String startSession(String projectName) {
        String sessionId = UUID.randomUUID().toString();
        ChatSession session = conversationManager.createSession(sessionId);
        
        if (projectName != null) {
            session.setCurrentProject(projectName);
        }
        
        // Add welcome message
        String welcomeMessage = generateWelcomeMessage(session);
        session.addMessage(new ChatMessage(welcomeMessage, ChatMessage.MessageType.SYSTEM_MESSAGE, "System"));
        
        Log.info("🚀 New chat session started: " + sessionId);
        return sessionId;
    }
    
    /**
     * Process user message and generate response
     */
    public ChatResponse chat(String sessionId, String userMessage) {
        return chat(sessionId, userMessage, null);
    }
    
    /**
     * Process user message with additional context
     */
    public ChatResponse chat(String sessionId, String userMessage, Map<String, Object> context) {
        try {
            ChatSession session = conversationManager.getSession(sessionId);
            if (session == null) {
                return new ChatResponse("❌ Session not found. Please start a new session.", 
                                      ChatMessage.MessageType.ERROR_REPORT);
            }
            
            // Add user message to history
            ChatMessage userMsg = new ChatMessage(userMessage, ChatMessage.MessageType.USER_MESSAGE, "User");
            if (context != null) {
                context.forEach(userMsg::addMetadata);
            }
            session.addMessage(userMsg);
            
            // Process the message
            ChatResponse response = processMessage(session, userMessage, context);
            
            // Add bot response to history
            ChatMessage botMsg = new ChatMessage(response.getContent(), response.getType(), "Bot");
            session.addMessage(botMsg);
            
            Log.info("💬 Processed message in session: " + sessionId);
            return response;
            
        } catch (Exception e) {
            Log.error("❌ Error processing chat message: " + e.getMessage());
            return new ChatResponse("❌ Sorry, I encountered an error processing your request. Please try again.", 
                                  ChatMessage.MessageType.ERROR_REPORT);
        }
    }
    
    /**
     * Process message and determine appropriate response
     */
    private ChatResponse processMessage(ChatSession session, String userMessage, Map<String, Object> context) {
        // Check for commands first
        if (userMessage.startsWith("/")) {
            return commandProcessor.processCommand(session, userMessage, context);
        }
        
        // Detect intent and route accordingly
        ChatIntent intent = detectIntent(userMessage);
        
        switch (intent) {
            case GENERATE_TEST:
                return handleTestGeneration(session, userMessage, context);
            
            case DEBUG_HELP:
                return handleDebuggingHelp(session, userMessage, context);
            
            case CODE_REVIEW:
                return handleCodeReview(session, userMessage, context);
            
            case FRAMEWORK_HELP:
                return handleFrameworkHelp(session, userMessage, context);
            
            case BEST_PRACTICES:
                return handleBestPractices(session, userMessage, context);
            
            case GENERAL_QUESTION:
            default:
                return handleGeneralQuestion(session, userMessage, context);
        }
    }
    
    /**
     * Handle test generation requests
     */
    private ChatResponse handleTestGeneration(ChatSession session, String userMessage, Map<String, Object> context) {
        try {
            // Extract test requirements from the message
            String baseUrl = extractBaseUrl(userMessage, context);
            List<String> conversationHistory = extractConversationContext(session);
            
            // Generate the test
            NLTestGenerator.GeneratedTest generatedTest;
            if (baseUrl != null) {
                generatedTest = testGenerator.generateTestWithContext(userMessage, baseUrl, conversationHistory);
            } else {
                // Ask for base URL if not provided
                return new ChatResponse(
                    "I'd be happy to generate a test for you! 🎯\n\n" +
                    "To create the most accurate test, could you please provide:\n" +
                    "1. The base URL of the application\n" +
                    "2. Any specific test data or credentials needed\n\n" +
                    "For example: 'Generate a login test for https://example.com using username 'testuser' and password 'testpass''",
                    ChatMessage.MessageType.BOT_RESPONSE,
                    true
                );
            }
            
            // Format the response
            StringBuilder response = new StringBuilder();
            response.append("✅ **Test Generated Successfully!**\n\n");
            response.append("**Test Class:** `").append(generatedTest.getTestClassName()).append("`\n");
            response.append("**Confidence Score:** ").append(String.format("%.1f%%", generatedTest.getConfidenceScore() * 100)).append("\n\n");
            
            response.append("**Generated Steps:**\n");
            for (int i = 0; i < generatedTest.getGeneratedSteps().size(); i++) {
                response.append((i + 1)).append(". ").append(generatedTest.getGeneratedSteps().get(i)).append("\n");
            }
            
            response.append("\n**💡 Recommendations:**\n");
            generatedTest.getRecommendations().forEach((key, value) -> 
                response.append("- **").append(key.toUpperCase()).append(":** ").append(value).append("\n"));
            
            response.append("\n📝 Would you like me to:\n");
            response.append("- Show the complete test code?\n");
            response.append("- Generate the Page Object Model class?\n");
            response.append("- Explain any part of the generated test?\n");
            response.append("- Make modifications to the test?");
            
            ChatResponse chatResponse = new ChatResponse(response.toString(), ChatMessage.MessageType.TEST_GENERATED, true);
            chatResponse.getActions().put("generatedTest", generatedTest);
            chatResponse.getSuggestions().add("Show me the complete test code");
            chatResponse.getSuggestions().add("Generate the Page Object class");
            chatResponse.getSuggestions().add("Explain the test steps");
            
            return chatResponse;
            
        } catch (Exception e) {
            return new ChatResponse(
                "❌ I encountered an issue while generating the test: " + e.getMessage() + "\n\n" +
                "Could you please provide more details about the test you'd like to create?",
                ChatMessage.MessageType.ERROR_REPORT
            );
        }
    }
    
    /**
     * Handle debugging help requests
     */
    private ChatResponse handleDebuggingHelp(ChatSession session, String userMessage, Map<String, Object> context) {
        String contextualPrompt = createDebuggingPrompt(userMessage, session);
        String aiResponse = ragAI.generateResponse(contextualPrompt);
        
        String formattedResponse = "🔍 **Debugging Analysis:**\n\n" + aiResponse + 
                                 "\n\n💡 **Quick Debugging Tips:**\n" +
                                 "- Check browser console for JavaScript errors\n" +
                                 "- Verify element locators with AI healing\n" +
                                 "- Add explicit waits for dynamic elements\n" +
                                 "- Capture screenshots on failure points";
        
        ChatResponse response = new ChatResponse(formattedResponse, ChatMessage.MessageType.BOT_RESPONSE);
        response.getSuggestions().add("Show me the error logs");
        response.getSuggestions().add("Help me fix this locator");
        response.getSuggestions().add("Explain the test failure");
        
        return response;
    }
    
    /**
     * Handle code review requests
     */
    private ChatResponse handleCodeReview(ChatSession session, String userMessage, Map<String, Object> context) {
        String contextualPrompt = createCodeReviewPrompt(userMessage, session);
        String aiResponse = ragAI.generateResponse(contextualPrompt);
        
        String formattedResponse = "📋 **Code Review Results:**\n\n" + aiResponse +
                                 "\n\n🎯 **Focus Areas:**\n" +
                                 "- Test maintainability and readability\n" +
                                 "- Proper use of AI element healing\n" +
                                 "- Error handling and assertions\n" +
                                 "- Performance and reliability";
        
        return new ChatResponse(formattedResponse, ChatMessage.MessageType.BOT_RESPONSE);
    }
    
    /**
     * Handle framework help requests
     */
    private ChatResponse handleFrameworkHelp(ChatSession session, String userMessage, Map<String, Object> context) {
        String contextualPrompt = createFrameworkPrompt(userMessage, session);
        String aiResponse = ragAI.generateResponse(contextualPrompt);
        
        String formattedResponse = "⚙️ **Framework Guidance:**\n\n" + aiResponse +
                                 "\n\n📚 **Available Features:**\n" +
                                 "- AI-Enhanced Element Healing\n" +
                                 "- Multi-Provider AI Support (Ollama, LM Studio)\n" +
                                 "- RAG-Enhanced Knowledge Base\n" +
                                 "- Natural Language Test Generation\n" +
                                 "- Comprehensive Reporting";
        
        ChatResponse response = new ChatResponse(formattedResponse, ChatMessage.MessageType.BOT_RESPONSE);
        response.getSuggestions().add("Show me framework setup guide");
        response.getSuggestions().add("How to configure AI providers?");
        response.getSuggestions().add("Best practices for this framework");
        
        return response;
    }
    
    /**
     * Handle best practices requests
     */
    private ChatResponse handleBestPractices(ChatSession session, String userMessage, Map<String, Object> context) {
        String contextualPrompt = createBestPracticesPrompt(userMessage, session);
        String aiResponse = ragAI.generateResponse(contextualPrompt);
        
        String formattedResponse = "🌟 **Best Practices Guide:**\n\n" + aiResponse +
                                 "\n\n🏆 **Top Recommendations:**\n" +
                                 "- Use AI element descriptions instead of brittle XPaths\n" +
                                 "- Implement proper waiting strategies\n" +
                                 "- Structure tests with Page Object Model\n" +
                                 "- Include meaningful assertions and logging";
        
        return new ChatResponse(formattedResponse, ChatMessage.MessageType.RECOMMENDATION);
    }
    
    /**
     * Handle general questions
     */
    private ChatResponse handleGeneralQuestion(ChatSession session, String userMessage, Map<String, Object> context) {
        String contextualPrompt = createGeneralPrompt(userMessage, session);
        String aiResponse = ragAI.generateResponse(contextualPrompt);
        
        ChatResponse response = new ChatResponse("💬 " + aiResponse, ChatMessage.MessageType.BOT_RESPONSE);
        response.getSuggestions().add("Generate a test for me");
        response.getSuggestions().add("Help me debug an issue");
        response.getSuggestions().add("Review my test code");
        
        return response;
    }
    
    /**
     * Get chat session information
     */
    public ChatSession getSession(String sessionId) {
        return conversationManager.getSession(sessionId);
    }
    
    /**
     * Get conversation history
     */
    public List<ChatMessage> getConversationHistory(String sessionId) {
        ChatSession session = conversationManager.getSession(sessionId);
        return session != null ? session.getConversationHistory() : new ArrayList<>();
    }
    
    /**
     * Set chatbot mode for session
     */
    public void setMode(String sessionId, ChatbotMode mode) {
        ChatSession session = conversationManager.getSession(sessionId);
        if (session != null) {
            session.setMode(mode);
            Log.info("🔧 Session " + sessionId + " mode changed to: " + mode);
        }
    }
    
    /**
     * End a chat session
     */
    public void endSession(String sessionId) {
        conversationManager.endSession(sessionId);
        Log.info("👋 Chat session ended: " + sessionId);
    }
    
    // Helper methods for intent detection and prompt creation...
    
    private enum ChatIntent {
        GENERATE_TEST,
        DEBUG_HELP,
        CODE_REVIEW,
        FRAMEWORK_HELP,
        BEST_PRACTICES,
        GENERAL_QUESTION
    }
    
    private ChatIntent detectIntent(String message) {
        String msg = message.toLowerCase();
        
        if (msg.contains("generate") && (msg.contains("test") || msg.contains("code"))) {
            return ChatIntent.GENERATE_TEST;
        } else if (msg.contains("debug") || msg.contains("error") || msg.contains("fail")) {
            return ChatIntent.DEBUG_HELP;
        } else if (msg.contains("review") || msg.contains("improve") || msg.contains("check")) {
            return ChatIntent.CODE_REVIEW;
        } else if (msg.contains("framework") || msg.contains("setup") || msg.contains("config")) {
            return ChatIntent.FRAMEWORK_HELP;
        } else if (msg.contains("best practice") || msg.contains("recommend") || msg.contains("should")) {
            return ChatIntent.BEST_PRACTICES;
        }
        
        return ChatIntent.GENERAL_QUESTION;
    }
    
    private String generateWelcomeMessage(ChatSession session) {
        return "🤖 **Welcome to K11 TechLab AI Test Assistant!**\n\n" +
               "I'm here to help you with test automation using our advanced AI-enhanced framework. " +
               "I can:\n\n" +
               "🎯 **Generate tests** from natural language descriptions\n" +
               "🔍 **Debug issues** and provide solutions\n" +
               "📋 **Review code** and suggest improvements\n" +
               "⚙️ **Help with framework** setup and configuration\n" +
               "🌟 **Share best practices** for reliable test automation\n\n" +
               "💡 **Try saying:**\n" +
               "- \"Generate a login test for https://example.com\"\n" +
               "- \"Help me debug this failing test\"\n" +
               "- \"Review my test code for improvements\"\n" +
               "- \"Show me best practices for element finding\"\n\n" +
               "**Session ID:** `" + session.getSessionId() + "`\n" +
               "**Mode:** " + session.getMode().getDescription() + "\n\n" +
               "How can I help you today? 😊";
    }
    
    private String extractBaseUrl(String message, Map<String, Object> context) {
        // Extract URL from message using regex
        String urlPattern = "https?://[\\w\\.-]+(?:\\.[a-zA-Z]{2,})+(?:[/?#]\\S*)?";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(urlPattern);
        java.util.regex.Matcher matcher = pattern.matcher(message);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        // Check context for URL
        if (context != null && context.containsKey("baseUrl")) {
            return (String) context.get("baseUrl");
        }
        
        return null;
    }
    
    private List<String> extractConversationContext(ChatSession session) {
        return session.getConversationHistory().stream()
                .filter(msg -> msg.getType() == ChatMessage.MessageType.USER_MESSAGE)
                .map(ChatMessage::getContent)
                .collect(ArrayList::new, 
                        (list, content) -> {
                            if (list.size() < 5) list.add(content);
                        }, 
                        ArrayList::addAll);
    }
    
    private String createDebuggingPrompt(String userMessage, ChatSession session) {
        return "Help debug this test automation issue:\n\n" + userMessage + 
               "\n\nProvide specific debugging steps and potential solutions.";
    }
    
    private String createCodeReviewPrompt(String userMessage, ChatSession session) {
        return "Review this test automation code and provide improvement suggestions:\n\n" + userMessage +
               "\n\nFocus on maintainability, reliability, and best practices.";
    }
    
    private String createFrameworkPrompt(String userMessage, ChatSession session) {
        return "Provide guidance about the K11 TechLab test automation framework:\n\n" + userMessage +
               "\n\nInclude specific configuration and usage examples.";
    }
    
    private String createBestPracticesPrompt(String userMessage, ChatSession session) {
        return "Provide test automation best practices guidance for:\n\n" + userMessage +
               "\n\nInclude specific examples and recommendations.";
    }
    
    private String createGeneralPrompt(String userMessage, ChatSession session) {
        return "Answer this test automation question:\n\n" + userMessage +
               "\n\nProvide helpful and actionable advice.";
    }
    
    /**
     * Conversation Manager - handles session lifecycle
     */
    private static class ConversationManager {
        private final Map<String, ChatSession> activeSessions = new ConcurrentHashMap<>();
        
        public ChatSession createSession(String sessionId) {
            ChatSession session = new ChatSession(sessionId);
            activeSessions.put(sessionId, session);
            return session;
        }
        
        public ChatSession getSession(String sessionId) {
            return activeSessions.get(sessionId);
        }
        
        public void endSession(String sessionId) {
            ChatSession session = activeSessions.get(sessionId);
            if (session != null) {
                session.setActive(false);
                activeSessions.remove(sessionId);
            }
        }
        
        public Map<String, ChatSession> getActiveSessions() {
            return new HashMap<>(activeSessions);
        }
    }
    
    /**
     * Command Processor - handles special commands
     */
    private static class CommandProcessor {
        private final RAGEnhancedAIClient ragAI;
        private final NLTestGenerator testGenerator;
        
        public CommandProcessor(RAGEnhancedAIClient ragAI, NLTestGenerator testGenerator) {
            this.ragAI = ragAI;
            this.testGenerator = testGenerator;
        }
        
        public ChatResponse processCommand(ChatSession session, String command, Map<String, Object> context) {
            String[] parts = command.substring(1).split("\\s+", 2);
            String cmd = parts[0].toLowerCase();
            String args = parts.length > 1 ? parts[1] : "";
            
            switch (cmd) {
                case "help":
                    return new ChatResponse(getHelpMessage(), ChatMessage.MessageType.SYSTEM_MESSAGE);
                case "mode":
                    return handleModeCommand(session, args);
                case "history":
                    return handleHistoryCommand(session);
                case "clear":
                    return handleClearCommand(session);
                case "generate":
                    return handleGenerateCommand(session, args, context);
                default:
                    return new ChatResponse("❌ Unknown command: " + cmd + "\nType /help for available commands.", 
                                          ChatMessage.MessageType.ERROR_REPORT);
            }
        }
        
        private String getHelpMessage() {
            return "📋 **Available Commands:**\n\n" +
                   "`/help` - Show this help message\n" +
                   "`/mode <mode>` - Switch chatbot mode\n" +
                   "`/history` - Show conversation history\n" +
                   "`/clear` - Clear conversation history\n" +
                   "`/generate <description>` - Quick test generation\n\n" +
                   "**Available Modes:**\n" +
                   "- `general` - General assistance\n" +
                   "- `testing` - Test generation focus\n" +
                   "- `debugging` - Debugging help\n" +
                   "- `practices` - Best practices\n" +
                   "- `review` - Code review\n" +
                   "- `setup` - Framework setup";
        }
        
        private ChatResponse handleModeCommand(ChatSession session, String mode) {
            // Implementation for mode switching
            return new ChatResponse("🔧 Mode switched to: " + mode, ChatMessage.MessageType.SYSTEM_MESSAGE);
        }
        
        private ChatResponse handleHistoryCommand(ChatSession session) {
            StringBuilder history = new StringBuilder("📜 **Conversation History:**\n\n");
            session.getConversationHistory().forEach(msg -> 
                history.append("**").append(msg.getSender()).append(":** ").append(msg.getContent()).append("\n\n"));
            
            return new ChatResponse(history.toString(), ChatMessage.MessageType.SYSTEM_MESSAGE);
        }
        
        private ChatResponse handleClearCommand(ChatSession session) {
            session.getConversationHistory().clear();
            return new ChatResponse("🧹 Conversation history cleared!", ChatMessage.MessageType.SYSTEM_MESSAGE);
        }
        
        private ChatResponse handleGenerateCommand(ChatSession session, String description, Map<String, Object> context) {
            // Quick test generation command
            return new ChatResponse("🎯 Quick test generation for: " + description, ChatMessage.MessageType.TEST_GENERATED);
        }
    }
    
    /**
     * Response Formatter - formats responses for different outputs
     */
    private static class ResponseFormatter {
        public String formatForConsole(ChatResponse response) {
            return response.getContent().replaceAll("\\*\\*(.*?)\\*\\*", "$1"); // Remove markdown bold
        }
        
        public String formatForWeb(ChatResponse response) {
            return response.getContent(); // Keep markdown for web rendering
        }
        
        public String formatForJson(ChatResponse response) {
            return response.getContent().replaceAll("\\n", "\\\\n"); // Escape newlines for JSON
        }
    }
}