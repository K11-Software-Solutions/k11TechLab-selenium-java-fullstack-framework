package org.k11techlab.framework.ai.chatbot;

import org.k11techlab.framework.ai.manager.AIProviderManager;
import org.k11techlab.framework.ai.llm.LLMInterface;

import javax.swing.*;
import java.awt.*;
import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatbotGUI extends JFrame {
    private JEditorPane chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private LLMInterface aiProvider;
    private String responderName;

    public ChatbotGUI() {
        setTitle("K11 Software Solutions AI Chatbot");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        // Load responder name from config (chatbot.ai.properties)
        responderName = ConfigurationManager.getString("chatbot.responder", "AI");

        // Header panel with logo, title, and model info
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));


        // K11 Software Solutions logo (image or fallback text)
        JLabel logoLabel;
        try {
            java.net.URL logoUrl = getClass().getClassLoader().getResource("sdetlogo.png");
            if (logoUrl != null) {
                ImageIcon icon = new ImageIcon(logoUrl);
                // Scale image for header (height ~36px)
                Image scaled = icon.getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH);
                logoLabel = new JLabel(new ImageIcon(scaled));
            } else {
                logoLabel = new JLabel("K11 Software Solutions");
                logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
                logoLabel.setForeground(new Color(0x00BFFF));
            }
        } catch (Exception ex) {
            logoLabel = new JLabel("K11 Software Solutions");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            logoLabel.setForeground(new Color(0x00BFFF));
        }
        headerPanel.add(logoLabel, BorderLayout.WEST);

        // Title and model info panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("K11 Tech Lab AI Chatbot", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(255, 255, 255));
        titlePanel.add(titleLabel, BorderLayout.NORTH);

        // Model info label
        JLabel modelLabel = new JLabel();
        modelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        modelLabel.setForeground(new Color(180, 220, 255));
        titlePanel.add(modelLabel, BorderLayout.SOUTH);
        headerPanel.add(titlePanel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // Chat area styling (JEditorPane for HTML headers)
        chatArea = new JEditorPane();
        chatArea.setContentType("text/html");
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        chatArea.setBackground(new Color(245, 247, 250));
        chatArea.setForeground(new Color(30, 41, 59));
        chatArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1)));
        chatArea.setText("<html><body style='background:#f5f7fa;margin:0;padding:0;'></body></html>");
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
            appendSystemMessage("[ERROR] No AI provider available.");
            modelLabel.setText("Model: N/A");
        } else {
            appendSystemMessage("[AI Provider: " + manager.getCurrentProviderInfo() + "]");
            String modelInfo = aiProvider.getModelInfo();
            modelLabel.setText("Model: " + (modelInfo != null ? modelInfo : "Unknown"));
        }
    }

    private void sendMessage() {
        String userText = inputField.getText().trim();
        if (!userText.isEmpty()) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            appendHeaderBar("You", timestamp, new Color(220, 235, 255), new Color(30, 41, 59));
            appendMessageBody(userText);
            inputField.setText("");
            String aiResponse = getAIResponse(userText);
            String responderTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            appendHeaderBar(responderName, responderTimestamp, new Color(230, 255, 230), new Color(30, 41, 59));
            appendMessageBody(aiResponse);
        }
    }

    /**
     * Append a styled header bar (name left, timestamp right, colored background) to the chat area.
     */
    private void appendHeaderBar(String name, String timestamp, Color bgColor, Color fgColor) {
        // Show timestamp in right corner, name on next line, both with background color
        String bgHex = String.format("#%02x%02x%02x", bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue());
        String fgHex = String.format("#%02x%02x%02x", fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue());
        String html = String.format(
            "<div style='background:%s;width:100%%;box-sizing:border-box;padding:0;'>" +
            "<div style='display:flex;justify-content:flex-end;align-items:center;" +
            "font-family:Segoe UI;font-size:13px;font-weight:bold;color:%s;padding:2px 8px 0 8px;'>%s</div>" +
            "<div style='font-family:Segoe UI;font-size:14px;font-weight:bold;color:%s;padding:0 8px 2px 8px;text-align:left;'>%s</div>" +
            "</div>",
            bgHex, fgHex, timestamp, fgHex, name
        );
        appendHtmlToChat(html);
    }

    /**
     * Append plain message body to the chat area.
     */
    private void appendMessageBody(String text) {
        // Escape HTML special characters
        String safe = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
        String html = String.format("<div style='font-family:Segoe UI;font-size:15px;color:#1e293b;background:#f5f7fa;padding:6px 8px 10px 8px;'>%s</div>", safe);
        appendHtmlToChat(html);
    }

    private void appendSystemMessage(String text) {
        String safe = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        String html = String.format("<div style='font-family:Segoe UI;font-size:13px;color:#444;background:#f5f7fa;padding:4px 8px 4px 8px;'>%s</div>", safe);
        appendHtmlToChat(html);
    }

    private void appendHtmlToChat(String html) {
        String old = chatArea.getText();
        int bodyIdx = old.indexOf("<body");
        if (bodyIdx < 0) return;
        int start = old.indexOf('>', bodyIdx);
        int end = old.lastIndexOf("</body>");
        if (start < 0 || end < 0) return;
        String before = old.substring(0, start + 1);
        String body = old.substring(start + 1, end);
        String after = old.substring(end);
        chatArea.setText(before + body + html + after);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
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
