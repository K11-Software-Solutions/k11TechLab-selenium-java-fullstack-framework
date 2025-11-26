package org.k11techlab.framework_unittests.aiTests;

import org.k11techlab.framework.ai.chatbot.TestAutomationChatbot;
import org.k11techlab.framework.ai.nlp.NLTestGenerator;
import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.ai.llm.LLMInterface;
import java.util.Scanner;

public class ChatbotInteractiveDemo {
    public static void main(String[] args) {
        // Initialize AI components with a mock LLM provider
        LLMInterface mockLLM = new LLMInterface() {
            @Override
            public String generateResponse(String prompt) {
                return "[MOCK LLM] You said: " + prompt;
            }
            @Override
            public String generateResponse(String prompt, float temperature, int maxTokens) {
                return "[MOCK LLM] You said: " + prompt + " (temp=" + temperature + ", maxTokens=" + maxTokens + ")";
            }
            @Override
            public boolean isAvailable() {
                return true;
            }
            @Override
            public String getModelInfo() {
                return "Mock LLM v1.0";
            }
            @Override
            public void close() {}
        };
        RAGEnhancedAIClient ragAI = new RAGEnhancedAIClient(mockLLM);
        NLTestGenerator testGenerator = new NLTestGenerator(ragAI);
        TestAutomationChatbot chatbot = new TestAutomationChatbot(ragAI, testGenerator);

        // Start a chat session
        String sessionId = chatbot.startSession("Interactive CLI Demo");
        System.out.println("\n🤖 Welcome to the Interactive AI Test Automation Chatbot!");
        System.out.println("Type your query below. Type '/help' for commands, 'exit' to quit.\n");

        Scanner scanner = new Scanner(System.in);
        String lastTestCode = null;
        while (true) {
            System.out.print("You: ");
            String userInput = scanner.nextLine();
            if (userInput.equalsIgnoreCase("exit")) {
                break;
            }

            // Command handling
            if (userInput.equalsIgnoreCase("/help")) {
                System.out.println("\nAvailable commands:\n" +
                        "/help - Show this help message\n" +
                        "/history - Show conversation history\n" +
                        "/mode <mode> - Switch chatbot mode (general, testing, debugging, practices, review, setup)\n" +
                        "/save - Save conversation history to file\n" +
                        "/savecode - Save last generated test code to file\n" +
                        "exit - End the session\n");
                continue;
            }
            if (userInput.equalsIgnoreCase("/history")) {
                System.out.println("\n--- Conversation History ---");
                for (TestAutomationChatbot.ChatMessage msg : chatbot.getConversationHistory(sessionId)) {
                    System.out.println(msg.getSender() + ": " + msg.getContent());
                }
                System.out.println("---------------------------\n");
                continue;
            }
            if (userInput.startsWith("/mode ")) {
                String mode = userInput.substring(6).trim().toUpperCase();
                try {
                    TestAutomationChatbot.ChatbotMode chatbotMode = TestAutomationChatbot.ChatbotMode.valueOf(mode);
                    chatbot.setMode(sessionId, chatbotMode);
                    System.out.println("Mode switched to: " + chatbotMode);
                } catch (Exception e) {
                    System.out.println("Invalid mode. Available: GENERAL_ASSISTANCE, TEST_GENERATION, DEBUGGING_HELP, BEST_PRACTICES, CODE_REVIEW, FRAMEWORK_SETUP");
                }
                continue;
            }
            if (userInput.equalsIgnoreCase("/save")) {
                try {
                    StringBuilder history = new StringBuilder();
                    for (TestAutomationChatbot.ChatMessage msg : chatbot.getConversationHistory(sessionId)) {
                        history.append(msg.getSender()).append(": ").append(msg.getContent()).append("\n");
                    }
                    java.nio.file.Files.write(java.nio.file.Paths.get("chatbot_conversation.txt"), history.toString().getBytes());
                    System.out.println("Conversation saved to chatbot_conversation.txt\n");
                } catch (Exception e) {
                    System.out.println("Failed to save conversation: " + e.getMessage());
                }
                continue;
            }
            if (userInput.equalsIgnoreCase("/savecode")) {
                if (lastTestCode != null) {
                    try {
                        java.nio.file.Files.write(java.nio.file.Paths.get("last_generated_test.java"), lastTestCode.getBytes());
                        System.out.println("Last generated test code saved to last_generated_test.java\n");
                    } catch (Exception e) {
                        System.out.println("Failed to save test code: " + e.getMessage());
                    }
                } else {
                    System.out.println("No test code generated yet in this session.\n");
                }
                continue;
            }

            // Send user input to chatbot
            TestAutomationChatbot.ChatResponse response = chatbot.chat(sessionId, userInput);
            String aiContent = response.getContent();

            // Code/markdown formatting for console
            if (aiContent.contains("```")) {
                System.out.println("AI (code):\n" + aiContent.replaceAll("```[a-zA-Z]*", "==== CODE ====").replaceAll("```", "=============="));
            } else {
                System.out.println("AI: " + aiContent);
            }

            // Save last generated test code if present in actions
            if (response.getActions().containsKey("generatedTest")) {
                Object genTest = response.getActions().get("generatedTest");
                try {
                    // Reflection to get getTestCode()
                    java.lang.reflect.Method getTestCode = genTest.getClass().getMethod("getTestCode");
                    lastTestCode = (String) getTestCode.invoke(genTest);
                } catch (Exception e) {
                    lastTestCode = null;
                }
            }
        }
        chatbot.endSession(sessionId);
        System.out.println("\n👋 Chat session ended. Goodbye!");
    }
}
