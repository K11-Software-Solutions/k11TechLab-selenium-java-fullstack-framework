package org.k11techlab.framework_unittests.aiTests;

import org.k11techlab.framework.ai.manager.AIProviderManager;
import org.k11techlab.framework.ai.llm.LLMInterface;

import javax.swing.*;
import java.awt.*;

public class ChatbotGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private LLMInterface aiProvider;

    public ChatbotGUI() {
        setTitle("K11 Tech Lab AI Chatbot");
        setSize(650, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        // Header panel with logo and title
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));


        // SDET Insights logo (image)
        JLabel logoLabel;
        try {
            java.net.URL logoUrl = getClass().getClassLoader().getResource("sdetlogo.png");
            if (logoUrl != null) {
                ImageIcon icon = new ImageIcon(logoUrl);
                // Scale image for header (height ~36px)
                Image scaled = icon.getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH);
                logoLabel = new JLabel(new ImageIcon(scaled));
            } else {
                logoLabel = new JLabel("SDET Insights");
                logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
                logoLabel.setForeground(new Color(0x00BFFF));
            }
        } catch (Exception ex) {
            logoLabel = new JLabel("SDET Insights");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            logoLabel.setForeground(new Color(0x00BFFF));
        }
        headerPanel.add(logoLabel, BorderLayout.WEST);

        // Title
        JLabel titleLabel = new JLabel("K11 Tech Lab AI Chatbot", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(255, 255, 255));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // Chat area styling
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        chatArea.setBackground(new Color(245, 247, 250));
        chatArea.setForeground(new Color(30, 41, 59));
        chatArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1)));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        add(scrollPane, BorderLayout.CENTER);

        // Input panel styling
        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sendButton.setBackground(new Color(30, 41, 59));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Button and input actions
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        // Initialize AI provider (uses config-driven selection)
        AIProviderManager manager = new AIProviderManager();
        aiProvider = manager.getBestProvider();
        if (aiProvider == null) {
            chatArea.append("[ERROR] No AI provider available.\n");
        } else {
            chatArea.append("[AI Provider: " + manager.getCurrentProviderInfo() + "]\n");
        }
    }

    private void sendMessage() {
        String userText = inputField.getText().trim();
        if (!userText.isEmpty()) {
            chatArea.append("You: " + userText + "\n");
            inputField.setText("");
            String aiResponse = getAIResponse(userText);
            chatArea.append("AI: " + aiResponse + "\n");
        }
    }

    private String getAIResponse(String prompt) {
        if (aiProvider == null) {
            return "[No AI provider available]";
        }
        try {
            return aiProvider.generateResponse(prompt);
        } catch (Exception e) {
            return "[AI error: " + e.getMessage() + "]";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatbotGUI().setVisible(true);
        });
    }
}
