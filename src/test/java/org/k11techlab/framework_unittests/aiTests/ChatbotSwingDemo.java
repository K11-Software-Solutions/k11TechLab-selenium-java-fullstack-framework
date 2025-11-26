package org.k11techlab.framework_unittests.aiTests;

import org.k11techlab.framework.ai.chatbot.TestAutomationChatbot;
import org.k11techlab.framework.ai.nlp.NLTestGenerator;
import org.k11techlab.framework.ai.rag.RAGEnhancedAIClient;
import org.k11techlab.framework.ai.llm.LLMInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatbotSwingDemo {
    public static void main(String[] args) {
        // Use the same mock LLM as in the CLI demo
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
        String sessionId = chatbot.startSession("Swing GUI Demo");

        JFrame frame = new JFrame("AI Test Automation Chatbot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
            private void sendMessage() {
                String userInput = inputField.getText();
                if (userInput.trim().isEmpty()) return;
                chatArea.append("You: " + userInput + "\n");
                TestAutomationChatbot.ChatResponse response = chatbot.chat(sessionId, userInput);
                chatArea.append("AI: " + response.getContent() + "\n");
                inputField.setText("");
            }
        });
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendButton.doClick();
            }
        });

        frame.setVisible(true);
    }
}
