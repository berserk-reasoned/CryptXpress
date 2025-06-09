package com.encryptor.ui;

import com.encryptor.dao.OperationDAO;
import com.encryptor.model.OperationRecord;
import com.encryptor.service.EncryptionService;
import com.encryptor.service.EncryptionService.CryptoException;
import com.encryptor.service.EncryptionService.EncryptionMethod;
import com.encryptor.service.EncryptionService.EncryptionResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

/**
 * Minimal GUI frame for CryptXpress application.
 * Inspired by clean, artistic design principles.
 */
public class MainFrame extends JFrame {
    private final EncryptionService cryptoService = new EncryptionService();
    private final OperationDAO operationDAO = new OperationDAO();
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Random random = new Random();

    // State management
    private Path currentFile;
    private String currentView = "HOME";

    // Colors matching the aesthetic
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color TEXT_COLOR = new Color(40, 40, 40);
    private static final Color ACCENT_COLOR = new Color(220, 20, 20);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color DOODLE_COLOR = new Color(180, 180, 180, 100);

    // Main panels
    private JPanel mainPanel;
    private JPanel contentPanel;
    private JLabel statusLabel;

    public MainFrame() {
        initializeUI();
        showHomeView();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
    }

    private void initializeUI() {
        setTitle("CryptXpress");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 500));
        setBackground(BACKGROUND_COLOR);

        // Main container
        mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawDoodles(g);
            }
        };
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawDoodles(g);
            }
        };
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Status bar at bottom
        statusLabel = new JLabel("Ready") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawDoodles(g);
            }
        };
        statusLabel.setFont(new Font("Courier New", Font.PLAIN, 11));
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void drawDoodles(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(DOODLE_COLOR);

        int width = getWidth();
        int height = getHeight();

        // Define pixel art patterns
        int[][] pandaPattern = {
                {0,0,1,1,1,1,0,0},
                {0,1,1,1,1,1,1,0},
                {1,1,1,0,0,1,1,1},
                {1,1,0,1,1,0,1,1},
                {1,1,0,1,1,0,1,1},
                {1,1,1,0,0,1,1,1},
                {0,1,1,1,1,1,1,0},
                {0,0,1,1,1,1,0,0}
        };

        int[][] pandasWithBambooPattern = {
                {0,0,0,1,1,0,0,0,1,1,0,0,0},
                {0,1,1,1,1,1,1,0,1,1,1,1,0},
                {1,1,1,0,0,1,1,1,1,0,0,1,1},
                {1,1,0,1,1,0,1,1,0,1,1,0,1},
                {1,1,0,1,1,0,1,1,0,1,1,0,1},
                {1,1,1,0,0,1,1,1,1,0,0,1,1},
                {0,1,1,1,1,1,1,0,1,1,1,1,0},
                {0,0,0,1,1,0,0,0,1,1,0,0,0},
                {0,0,0,0,0,0,1,0,0,0,0,0,0},
                {0,0,0,0,0,0,1,0,0,0,0,0,0},
                {0,0,0,0,0,0,1,0,0,0,0,0,0},
                {0,0,0,0,0,0,1,0,0,0,0,0,0}
        };

        int[][] cloudPattern = {
                {0,0,0,1,1,1,1,0,0,0},
                {0,0,1,1,1,1,1,1,0,0},
                {0,1,1,1,1,1,1,1,1,0},
                {1,1,1,1,1,1,1,1,1,1},
                {1,1,1,1,1,1,1,1,1,1},
                {1,1,1,1,1,1,1,1,1,1},
                {0,1,1,1,1,1,1,1,1,0},
                {0,0,1,1,1,1,1,1,0,0},
                {0,0,0,1,1,1,1,0,0,0}
        };

        // Draw 3-5 figures (cubes or pixel art)
        for (int i = 0; i < 3 + random.nextInt(3); i++) {
            int choice = random.nextInt(4); // 0-2: pixel art, 3: cube
            int size = 25 + random.nextInt(30); // Make doodles bigger (25-55px)
            int x = random.nextInt(width - size*2);
            int y = random.nextInt(height - size*2);

            switch (choice) {
                case 0: // Single panda
                    drawPixelArt(g2d, pandaPattern, x, y, size/8);
                    break;
                case 1: // Pandas with bamboo
                    drawPixelArt(g2d, pandasWithBambooPattern, x, y, size/12);
                    break;
                case 2: // Cloud
                    drawPixelArt(g2d, cloudPattern, x, y, size/10);
                    break;
                default: // Cube (original)
                    if (random.nextBoolean()) {
                        g2d.fillRect(x, y, size, size);
                    } else {
                        g2d.drawRect(x, y, size, size);
                        g2d.drawLine(x, y, x + size/2, y - size/4);
                        g2d.drawLine(x + size, y, x + size + size/2, y - size/4);
                        g2d.drawLine(x + size, y + size, x + size + size/2, y + size - size/4);
                        g2d.drawLine(x + size/2, y - size/4, x + size + size/2, y - size/4);
                        g2d.drawLine(x + size + size/2, y - size/4, x + size + size/2, y + size - size/4);
                    }
                    break;
            }
        }

        // Draw small decorative lines
        for (int i = 0; i < 8; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = x1 + 20 + random.nextInt(60);
            int y2 = y1 + (random.nextBoolean() ? 1 : -1) * (10 + random.nextInt(30));
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.dispose();
    }

    private void drawPixelArt(Graphics2D g2d, int[][] pattern, int x, int y, int pixelSize) {
        if (pixelSize < 1) pixelSize = 1; // Ensure minimum size

        for (int row = 0; row < pattern.length; row++) {
            for (int col = 0; col < pattern[row].length; col++) {
                if (pattern[row][col] == 1) {
                    int px = x + col * pixelSize;
                    int py = y + row * pixelSize;
                    g2d.fillRect(px, py, pixelSize, pixelSize);
                }
            }
        }
    }

    private void showHomeView() {
        currentView = "HOME";
        contentPanel.removeAll();

        // App title - artistic typography like in the screenshot
        JLabel titleLabel = new JLabel("CRYPTXPRESS");
        titleLabel.setFont(new Font("Courier New", Font.BOLD, 48));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("File Encryption Suite");
        subtitleLabel.setFont(new Font("Courier New", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Main action buttons - minimal design
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(new EmptyBorder(60, 0, 0, 0));

        JButton encryptBtn = createMinimalButton("ENCRYPT");
        JButton decryptBtn = createMinimalButton("DECRYPT");
        JButton historyBtn = createMinimalButton("HISTORY");

        encryptBtn.addActionListener(e -> showEncryptView());
        decryptBtn.addActionListener(e -> showDecryptView());
        historyBtn.addActionListener(e -> showHistoryView());

        buttonPanel.add(encryptBtn);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(decryptBtn);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(historyBtn);

        // Add decorative line
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setForeground(BORDER_COLOR);
        separator.setMaximumSize(new Dimension(300, 1));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(separator);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalGlue());

        updateStatus("Ready");
        refreshUI();
    }

    private JButton createMinimalButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Courier New", Font.PLAIN, 16));
        button.setForeground(TEXT_COLOR);
        button.setBackground(BACKGROUND_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(12, 30, 12, 30)
        ));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(250, 250, 250));
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                        new EmptyBorder(12, 30, 12, 30)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BACKGROUND_COLOR);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        new EmptyBorder(12, 30, 12, 30)
                ));
            }
        });

        return button;
    }

    private void showEncryptView() {
        currentView = "ENCRYPT";
        contentPanel.removeAll();

        // Back button
        JButton backBtn = createBackButton();
        backBtn.addActionListener(e -> showHomeView());

        // Title
        JLabel titleLabel = new JLabel("ENCRYPT FILE");
        titleLabel.setFont(new Font("Courier New", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // File selection area
        JPanel filePanel = createFileSelectionPanel("Select file to encrypt");

        // Encryption method (shown only after file selection)
        JPanel methodPanel = createMethodSelectionPanel();
        methodPanel.setVisible(currentFile != null);

        // Action button
        JButton actionBtn = createActionButton("ENCRYPT FILE", this::performEncryption);
        actionBtn.setVisible(currentFile != null);

        contentPanel.add(backBtn);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(40));
        contentPanel.add(filePanel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(methodPanel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(actionBtn);
        contentPanel.add(Box.createVerticalGlue());

        refreshUI();
    }

    private void showDecryptView() {
        currentView = "DECRYPT";
        contentPanel.removeAll();

        // Back button
        JButton backBtn = createBackButton();
        backBtn.addActionListener(e -> showHomeView());

        // Title
        JLabel titleLabel = new JLabel("DECRYPT FILE");
        titleLabel.setFont(new Font("Courier New", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // File selection
        JPanel filePanel = createFileSelectionPanel("Select file to decrypt");

        // Key input (shown after file selection)
        JPanel keyPanel = createKeyInputPanel();
        keyPanel.setVisible(currentFile != null);

        // Method selection (shown after key input)
        JPanel methodPanel = createMethodSelectionPanel();
        methodPanel.setVisible(currentFile != null);

        // Action button
        JButton actionBtn = createActionButton("DECRYPT FILE", this::performDecryption);
        actionBtn.setVisible(currentFile != null);

        contentPanel.add(backBtn);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(40));
        contentPanel.add(filePanel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(keyPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(methodPanel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(actionBtn);
        contentPanel.add(Box.createVerticalGlue());

        refreshUI();
    }

    private void showHistoryView() {
        currentView = "HISTORY";
        contentPanel.removeAll();

        // Back button
        JButton backBtn = createBackButton();
        backBtn.addActionListener(e -> showHomeView());

        // Title
        JLabel titleLabel = new JLabel("OPERATION HISTORY");
        titleLabel.setFont(new Font("Courier New", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // History table
        JPanel historyPanel = createHistoryPanel();

        contentPanel.add(backBtn);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(40));
        contentPanel.add(historyPanel);

        loadOperationHistory();
        refreshUI();
    }

    private JButton createBackButton() {
        JButton backBtn = new JButton("← Back");
        backBtn.setFont(new Font("Courier New", Font.PLAIN, 12));
        backBtn.setForeground(Color.GRAY);
        backBtn.setBackground(BACKGROUND_COLOR);
        backBtn.setBorder(new EmptyBorder(5, 0, 5, 0));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return backBtn;
    }

    private JPanel createFileSelectionPanel(String prompt) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setMaximumSize(new Dimension(400, 100));

        JLabel promptLabel = new JLabel(prompt);
        promptLabel.setFont(new Font("Courier New", Font.PLAIN, 12));
        promptLabel.setForeground(Color.GRAY);
        promptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton selectBtn = new JButton(currentFile == null ? "Choose File" : currentFile.getFileName().toString());
        selectBtn.setFont(new Font("Courier New", Font.PLAIN, 14));
        selectBtn.setForeground(TEXT_COLOR);
        selectBtn.setBackground(BACKGROUND_COLOR);
        selectBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(15, 25, 15, 25)
        ));
        selectBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectBtn.setFocusPainted(false);
        selectBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        selectBtn.addActionListener(e -> {
            selectFile();
            if (currentView.equals("ENCRYPT")) {
                showEncryptView();
            } else if (currentView.equals("DECRYPT")) {
                showDecryptView();
            }
        });

        panel.add(promptLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(selectBtn);

        return panel;
    }

    private JPanel createMethodSelectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);

        JLabel label = new JLabel("Encryption Method");
        label.setFont(new Font("Courier New", Font.PLAIN, 12));
        label.setForeground(Color.GRAY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JComboBox<EncryptionMethod> methodCombo = new JComboBox<>(EncryptionMethod.values());
        methodCombo.setFont(new Font("Courier New", Font.PLAIN, 14));
        methodCombo.setMaximumSize(new Dimension(200, 30));
        methodCombo.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        panel.add(methodCombo);

        return panel;
    }

    private JPanel createKeyInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);

        JLabel label = new JLabel("Decryption Key");
        label.setFont(new Font("Courier New", Font.PLAIN, 12));
        label.setForeground(Color.GRAY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea keyArea = new JTextArea(3, 40);
        keyArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        keyArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        keyArea.setLineWrap(true);
        keyArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(keyArea);
        scrollPane.setMaximumSize(new Dimension(400, 80));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        panel.add(scrollPane);

        return panel;
    }

    private JButton createActionButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Courier New", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT_COLOR);
        button.setBorder(new EmptyBorder(15, 30, 15, 30));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> action.run());

        return button;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setMaximumSize(new Dimension(600, 300));

        // Simple text area for history
        JTextArea historyArea = new JTextArea(15, 50);
        historyArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        historyArea.setEditable(false);
        historyArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // Original methods adapted for minimal UI
    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        Preferences prefs = Preferences.userNodeForPackage(MainFrame.class);
        String lastDir = prefs.get("last_directory", System.getProperty("user.home"));
        fileChooser.setCurrentDirectory(new File(lastDir));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile().toPath();
            prefs.put("last_directory", currentFile.getParent().toString());
            updateStatus("File selected: " + currentFile.getFileName());
        }
    }

    private void performEncryption() {
        if (!validateFileSelection()) return;

        updateStatus("Encrypting file...");

        executorService.submit(() -> {
            try {
                EncryptionMethod method = EncryptionMethod.AES_256; // Default for now
                EncryptionResult result = cryptoService.encryptFile(currentFile, method);

                SwingUtilities.invokeLater(() -> {
                    showKeyDisplay(result.getBase64Key(), "Encryption completed successfully!");
                    currentFile = null; // Clear selection
                });

            } catch (CryptoException | IOException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Encryption failed: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    updateStatus("Encryption failed");
                });
            }
        });
    }

    private void performDecryption() {
        if (!validateFileSelection()) return;

        updateStatus("Decrypting file...");

        executorService.submit(() -> {
            try {
                String key = "dummy_key"; // Get from UI component
                EncryptionMethod method = EncryptionMethod.AES_256;
                Path decryptedFile = cryptoService.decryptFile(currentFile, key, method);

                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Decryption completed successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    updateStatus("Decryption completed");
                    currentFile = null; // Clear selection
                    showHomeView();
                });

            } catch (CryptoException | IOException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Decryption failed: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    updateStatus("Decryption failed");
                });
            }
        });
    }

    private void showKeyDisplay(String key, String message) {
        contentPanel.removeAll();

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Courier New", Font.BOLD, 16));
        messageLabel.setForeground(TEXT_COLOR);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel keyLabel = new JLabel("Generated Key:");
        keyLabel.setFont(new Font("Courier New", Font.PLAIN, 12));
        keyLabel.setForeground(Color.GRAY);
        keyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea keyArea = new JTextArea(key, 4, 50);
        keyArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        keyArea.setEditable(false);
        keyArea.setLineWrap(true);
        keyArea.setWrapStyleWord(true);
        keyArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scrollPane = new JScrollPane(keyArea);
        scrollPane.setMaximumSize(new Dimension(500, 100));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton copyBtn = createMinimalButton("Copy Key");
        copyBtn.addActionListener(e -> {
            java.awt.datatransfer.StringSelection selection =
                    new java.awt.datatransfer.StringSelection(key);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            updateStatus("Key copied to clipboard");
        });

        JButton homeBtn = createMinimalButton("Back to Home");
        homeBtn.addActionListener(e -> showHomeView());

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(keyLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(scrollPane);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(copyBtn);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(homeBtn);
        contentPanel.add(Box.createVerticalGlue());

        refreshUI();
    }

    private boolean validateFileSelection() {
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a file first.",
                    "No File Selected", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void loadOperationHistory() {
        // Simplified for minimal UI
        updateStatus("History loaded");
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void refreshUI() {
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
