package org.k11techlab.framework.ai.cli;

import org.k11techlab.framework.ai.chatbot.TestAutomationChatbot;
import org.k11techlab.framework.ai.nlp.NLTestGenerator;
import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Command Line Interface for AI Test Assistant
 * Provides interactive terminal-based access to Natural Language Test Generation and Chatbot
 */
public class AITestAssistantCLI {
    
    private final TestAutomationChatbot chatbot;
    private final NLTestGenerator testGenerator;
    private final Scanner scanner;
    private String currentSessionId;
    private boolean isRunning;
    
    private static final String WELCOME_BANNER = 
        "🤖 ════════════════════════════════════════════════════════════════════════════════════\n" +
        "   K11 TECHLAB AI TEST ASSISTANT - COMMAND LINE INTERFACE\n" +
        "   Advanced Natural Language Test Generation & RAG-Powered Chatbot\n" +
        "════════════════════════════════════════════════════════════════════════════════════\n";
    
    private static final String HELP_TEXT =
        "\n💡 Available Commands:\n" +
        "   🎯 GENERATION COMMANDS:\n" +
        "      /generate <description>     - Generate test from description\n" +
        "      /quick <url> <description>  - Quick test generation with URL\n" +
        "      /save <filename>           - Save last generated test to file\n" +
        "\n   🤖 CHATBOT COMMANDS:\n" +
        "      /chat                      - Switch to interactive chat mode\n" +
        "      /mode <mode>              - Change chatbot mode (general, testing, debugging, etc.)\n" +
        "      /history                  - Show conversation history\n" +
        "      /clear                    - Clear conversation history\n" +
        "\n   ⚙️ UTILITY COMMANDS:\n" +
        "      /session                  - Show current session info\n" +
        "      /new                      - Start new session\n" +
        "      /help                     - Show this help message\n" +
        "      /exit                     - Exit the application\n" +
        "\n   📝 EXAMPLES:\n" +
        "      /generate login test for e-commerce site with user validation\n" +
        "      /quick https://example.com search and filter products test\n" +
        "      What are best practices for element locators?\n" +
        "      Help me debug a timeout error in my test\n";
    
    public AITestAssistantCLI() {
        System.out.println("🚀 Initializing AI Test Assistant CLI...");
        
        // Initialize AI components
        org.k11techlab.framework.ai.manager.AIProviderManager aiProviderManager = 
            new org.k11techlab.framework.ai.manager.AIProviderManager();
        RAGEnhancedAIClient ragAI = new RAGEnhancedAIClient(aiProviderManager.getBestProvider());
        this.testGenerator = new NLTestGenerator(ragAI);
        this.chatbot = new TestAutomationChatbot(ragAI, testGenerator);
        
        this.scanner = new Scanner(System.in);
        this.isRunning = true;
        
        // Start initial session
        this.currentSessionId = chatbot.startSession("CLI Session");
        
        System.out.println("✅ AI Test Assistant CLI ready!");
    }
    
    public static void main(String[] args) {
        AITestAssistantCLI cli = new AITestAssistantCLI();
        cli.run();
    }
    
    public void run() {
        System.out.println(WELCOME_BANNER);
        System.out.println("🎯 Ready to help with your test automation needs!");
        System.out.println("💬 Type your message or use /help for commands\n");
        
        while (isRunning) {
            try {
                System.out.print("🤖 AI Assistant > ");
                String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    continue;
                }
                
                processInput(input);
                
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                System.out.println("💡 Type /help for available commands\n");
            }
        }
        
        cleanup();
    }
    
    private void processInput(String input) {
        if (input.startsWith("/")) {
            processCommand(input);
        } else {
            processChatMessage(input);
        }
    }
    
    private void processCommand(String command) {
        String[] parts = command.substring(1).split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";
        
        switch (cmd) {
            case "help":
                System.out.println(HELP_TEXT);
                break;
                
            case "generate":
                handleGenerateCommand(args);
                break;
                
            case "quick":
                handleQuickGenerateCommand(args);
                break;
                
            case "save":
                handleSaveCommand(args);
                break;
                
            case "chat":
                handleChatMode();
                break;
                
            case "mode":
                handleModeCommand(args);
                break;
                
            case "history":
                handleHistoryCommand();
                break;
                
            case "clear":
                handleClearCommand();
                break;
                
            case "session":
                handleSessionCommand();
                break;
                
            case "new":
                handleNewSessionCommand();
                break;
                
            case "exit":
                handleExitCommand();
                break;
                
            default:
                System.out.println("❌ Unknown command: /" + cmd);
                System.out.println("💡 Type /help for available commands\n");
        }
    }
    
    private void processChatMessage(String message) {
        System.out.println("🤖 Processing your request...\n");
        
        TestAutomationChatbot.ChatResponse response = chatbot.chat(currentSessionId, message);
        
        System.out.println("🤖 AI Assistant:");
        System.out.println(formatResponseForCLI(response.getContent()));
        
        if (!response.getSuggestions().isEmpty()) {
            System.out.println("\n💡 Suggestions:");
            for (int i = 0; i < response.getSuggestions().size(); i++) {
                System.out.println("   " + (i + 1) + ". " + response.getSuggestions().get(i));
            }
        }
        
        System.out.println();
    }
    
    private void handleGenerateCommand(String description) {
        if (description.isEmpty()) {
            System.out.println("❌ Please provide a test description.");
            System.out.println("💡 Example: /generate login test for e-commerce site\n");
            return;
        }
        
        System.out.println("🎯 Generating test from description: " + description);
        System.out.println("⏳ This may take a moment...\n");
        
        try {
            // Extract URL if present
            String baseUrl = extractUrl(description);
            if (baseUrl == null) {
                System.out.print("🌐 Enter base URL (or press Enter to skip): ");
                baseUrl = scanner.nextLine().trim();
                if (baseUrl.isEmpty()) {
                    baseUrl = "https://example.com";
                }
            }
            
            NLTestGenerator.GeneratedTest generatedTest = testGenerator.generateQuickTest(description, baseUrl);
            
            System.out.println("✅ Test Generated Successfully!");
            System.out.println("════════════════════════════════════════");
            System.out.println("📝 Test Class: " + generatedTest.getTestClassName());
            System.out.println("📊 Confidence Score: " + String.format("%.1f%%", generatedTest.getConfidenceScore() * 100));
            System.out.println("🔧 Steps Generated: " + generatedTest.getGeneratedSteps().size());
            
            System.out.println("\n📋 Test Steps:");
            for (int i = 0; i < generatedTest.getGeneratedSteps().size(); i++) {
                System.out.println("   " + (i + 1) + ". " + generatedTest.getGeneratedSteps().get(i));
            }
            
            System.out.println("\n💡 Recommendations:");
            generatedTest.getRecommendations().forEach((category, recommendation) ->
                System.out.println("   • " + category.toUpperCase() + ": " + recommendation));
            
            System.out.println("\n🔧 Actions Available:");
            System.out.println("   • /save <filename> - Save the generated test code");
            System.out.println("   • Ask me to explain any part of the test");
            System.out.println("   • Request modifications or improvements\n");
            
            // Store for potential saving
            storeLastGenerated(generatedTest);
            
        } catch (Exception e) {
            System.err.println("❌ Test generation failed: " + e.getMessage());
            System.out.println("💡 Try rephrasing your description or check your AI provider settings\n");
        }
    }
    
    private void handleQuickGenerateCommand(String args) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            System.out.println("❌ Usage: /quick <url> <description>");
            System.out.println("💡 Example: /quick https://example.com login test with validation\n");
            return;
        }
        
        String url = parts[0];
        String description = parts[1];
        
        System.out.println("🚀 Quick generating test for: " + url);
        System.out.println("📝 Description: " + description);
        System.out.println("⏳ Processing...\n");
        
        try {
            NLTestGenerator.GeneratedTest generatedTest = testGenerator.generateQuickTest(description, url);
            
            System.out.println("✅ Quick Test Generated!");
            System.out.println("═══════════════════════════");
            System.out.println("📝 " + generatedTest.getTestClassName());
            System.out.println("📊 Confidence: " + String.format("%.1f%%", generatedTest.getConfidenceScore() * 100));
            System.out.println("🔧 Steps: " + generatedTest.getGeneratedSteps().size());
            
            System.out.println("\n💾 Use /save <filename> to save the generated test\n");
            storeLastGenerated(generatedTest);
            
        } catch (Exception e) {
            System.err.println("❌ Quick generation failed: " + e.getMessage() + "\n");
        }
    }
    
    private void handleSaveCommand(String filename) {
        if (lastGeneratedTest == null) {
            System.out.println("❌ No test has been generated yet.");
            System.out.println("💡 Generate a test first using /generate or /quick\n");
            return;
        }
        
        if (filename.isEmpty()) {
            filename = lastGeneratedTest.getTestClassName() + ".java";
        } else if (!filename.endsWith(".java")) {
            filename += ".java";
        }
        
        try {
            File file = new File(filename);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(lastGeneratedTest.getTestCode());
            }
            
            System.out.println("✅ Test saved to: " + file.getAbsolutePath());
            System.out.println("📁 File size: " + file.length() + " bytes\n");
            
        } catch (IOException e) {
            System.err.println("❌ Failed to save file: " + e.getMessage() + "\n");
        }
    }
    
    private void handleChatMode() {
        System.out.println("💬 Entering Interactive Chat Mode");
        System.out.println("══════════════════════════════════");
        System.out.println("🤖 I'm here to help! Ask me anything about test automation.");
        System.out.println("💡 Type 'exit' to return to command mode\n");
        
        while (true) {
            System.out.print("You: ");
            String message = scanner.nextLine().trim();
            
            if (message.equalsIgnoreCase("exit") || message.equalsIgnoreCase("/exit")) {
                System.out.println("👋 Exiting chat mode\n");
                break;
            }
            
            if (message.isEmpty()) {
                continue;
            }
            
            TestAutomationChatbot.ChatResponse response = chatbot.chat(currentSessionId, message);
            System.out.println("🤖: " + formatResponseForCLI(response.getContent()) + "\n");
        }
    }
    
    private void handleModeCommand(String mode) {
        if (mode.isEmpty()) {
            System.out.println("💡 Available modes: general, testing, debugging, practices, review, setup");
            System.out.println("📝 Current mode: " + chatbot.getSession(currentSessionId).getMode() + "\n");
            return;
        }
        
        try {
            TestAutomationChatbot.ChatbotMode newMode = parseMode(mode);
            chatbot.setMode(currentSessionId, newMode);
            System.out.println("🔧 Mode changed to: " + newMode.getDescription() + "\n");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Invalid mode: " + mode);
            System.out.println("💡 Available modes: general, testing, debugging, practices, review, setup\n");
        }
    }
    
    private void handleHistoryCommand() {
        var history = chatbot.getConversationHistory(currentSessionId);
        
        if (history.isEmpty()) {
            System.out.println("📜 No conversation history in this session.\n");
            return;
        }
        
        System.out.println("📜 Conversation History");
        System.out.println("═══════════════════════");
        
        for (int i = 0; i < history.size(); i++) {
            var message = history.get(i);
            String sender = message.getSender().equals("User") ? "You" : "🤖";
            String time = message.getTimestamp().toLocalTime().toString();
            
            System.out.println(String.format("[%s] %s: %s", 
                time, sender, truncateMessage(message.getContent(), 100)));
        }
        System.out.println();
    }
    
    private void handleClearCommand() {
        chatbot.getSession(currentSessionId).getConversationHistory().clear();
        System.out.println("🧹 Conversation history cleared!\n");
    }
    
    private void handleSessionCommand() {
        var session = chatbot.getSession(currentSessionId);
        
        System.out.println("📊 Current Session Information");
        System.out.println("═════════════════════════════");
        System.out.println("🆔 Session ID: " + session.getSessionId());
        System.out.println("📅 Created: " + session.getCreatedAt().toLocalTime());
        System.out.println("⏰ Last Activity: " + session.getLastActivity().toLocalTime());
        System.out.println("🔧 Mode: " + session.getMode().getDescription());
        System.out.println("💬 Messages: " + session.getConversationHistory().size());
        System.out.println("✅ Active: " + session.isActive());
        if (session.getCurrentProject() != null) {
            System.out.println("📁 Project: " + session.getCurrentProject());
        }
        System.out.println();
    }
    
    private void handleNewSessionCommand() {
        // End current session
        chatbot.endSession(currentSessionId);
        
        // Start new session
        System.out.print("📁 Enter project name (optional): ");
        String projectName = scanner.nextLine().trim();
        
        if (projectName.isEmpty()) {
            currentSessionId = chatbot.startSession();
        } else {
            currentSessionId = chatbot.startSession(projectName);
        }
        
        System.out.println("🆔 New session started: " + currentSessionId);
        System.out.println("🎯 Ready for a fresh conversation!\n");
    }
    
    private void handleExitCommand() {
        System.out.println("\n👋 Thank you for using K11 TechLab AI Test Assistant!");
        System.out.println("🚀 Your AI-enhanced test automation journey continues...");
        isRunning = false;
    }
    
    // Helper methods
    private NLTestGenerator.GeneratedTest lastGeneratedTest;
    
    private void storeLastGenerated(NLTestGenerator.GeneratedTest test) {
        this.lastGeneratedTest = test;
    }
    
    private String extractUrl(String text) {
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (word.startsWith("http://") || word.startsWith("https://")) {
                return word;
            }
        }
        return null;
    }
    
    private String formatResponseForCLI(String response) {
        // Remove markdown formatting for CLI display
        return response
            .replaceAll("\\*\\*(.*?)\\*\\*", "$1")  // Remove bold
            .replaceAll("`([^`]+)`", "$1")          // Remove code formatting
            .replaceAll("^#+\\s", "")               // Remove headers
            .replaceAll("\n+", "\n");              // Normalize line breaks
    }
    
    private String truncateMessage(String message, int maxLength) {
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength - 3) + "...";
    }
    
    private TestAutomationChatbot.ChatbotMode parseMode(String mode) {
        switch (mode.toLowerCase()) {
            case "general": return TestAutomationChatbot.ChatbotMode.GENERAL_ASSISTANCE;
            case "testing": return TestAutomationChatbot.ChatbotMode.TEST_GENERATION;
            case "debugging": return TestAutomationChatbot.ChatbotMode.DEBUGGING_HELP;
            case "practices": return TestAutomationChatbot.ChatbotMode.BEST_PRACTICES;
            case "review": return TestAutomationChatbot.ChatbotMode.CODE_REVIEW;
            case "setup": return TestAutomationChatbot.ChatbotMode.FRAMEWORK_SETUP;
            default: throw new IllegalArgumentException("Invalid mode: " + mode);
        }
    }
    
    private void cleanup() {
        if (currentSessionId != null) {
            chatbot.endSession(currentSessionId);
        }
        scanner.close();
        System.out.println("🧹 Cleanup completed. Goodbye! 👋\n");
    }
}