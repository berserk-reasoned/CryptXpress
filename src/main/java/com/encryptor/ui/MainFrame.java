package com.encryptor.ui;

import com.encryptor.dao.OperationDAO;
import com.encryptor.model.OperationRecord;
import com.encryptor.service.EncryptionService;
import com.encryptor.service.EncryptionService.EncryptionMethod;
import com.encryptor.service.EncryptionService.EncryptionResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainFrame extends JFrame {
    private static final Color BACKGROUND = new Color(28, 28, 30);
    private static final Color CARD_BACKGROUND = new Color(45, 45, 50);
    private static final Color PRIMARY = new Color(0, 150, 255);
    
    private File selectedFile;
    private EncryptionService encryptionService;
    private OperationDAO operationDAO;
    
    private JLabel fileLabel;
    private JComboBox<EncryptionMethod> methodCombo;
    private JTextArea keyArea;
    private JTextField keyField;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private DefaultTableModel historyModel;

    public MainFrame() {
        initServices();
        initUI();
        loadHistory();
    }

    private void initServices() {
        encryptionService = new EncryptionService();
        operationDAO = new OperationDAO();
    }

    private void initUI() {
        setTitle("CryptXpress");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Set system look and feel as fallback
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception ex) {
            System.err.println("Could not set look and feel: " + ex.getMessage());
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        // Content Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BACKGROUND);
        tabs.setForeground(Color.WHITE);
        tabs.add("Operations", createOperationsPanel());
        tabs.add("History", createHistoryPanel());
        mainPanel.add(tabs, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("CryptXpress");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(PRIMARY);

        JLabel subtitle = new JLabel("Advanced File Encryption Solution");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BACKGROUND);
        titlePanel.add(title);
        titlePanel.add(subtitle);

        panel.add(titlePanel, BorderLayout.WEST);
        return panel;
    }

    private JPanel createOperationsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND);

        // File Selection Card
        panel.add(createFileCard());
        panel.add(Box.createVerticalStrut(20));

        // Encryption Card
        panel.add(createEncryptionCard());
        panel.add(Box.createVerticalStrut(20));

        // Actions Card
        panel.add(createActionCard());
        panel.add(Box.createVerticalStrut(20));

        // Key Card
        panel.add(createKeyCard());
        panel.add(Box.createVerticalStrut(20));

        // Status Bar
        panel.add(createStatusBar());
        
        return panel;
    }

    private JPanel createFileCard() {
        JPanel card = new JPanel(new BorderLayout());
        styleCard(card, "📁 File Selection");

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(CARD_BACKGROUND);

        fileLabel = new JLabel("No file selected");
        fileLabel.setForeground(Color.WHITE);
        fileLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        OvalButton browseBtn = new OvalButton("Choose File");
        browseBtn.setBackground(PRIMARY);
        browseBtn.addActionListener(this::selectFile);

        content.add(fileLabel, BorderLayout.CENTER);
        content.add(browseBtn, BorderLayout.EAST);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createEncryptionCard() {
        JPanel card = new JPanel(new BorderLayout());
        styleCard(card, "🔒 Encryption Settings");

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CARD_BACKGROUND);
        content.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Method Selection
        methodCombo = new JComboBox<>(EncryptionMethod.values());
        styleComboBox(methodCombo);

        // Key Input
        keyField = new JTextField();
        keyField.setForeground(Color.WHITE);
        keyField.setBackground(new Color(60, 60, 65));
        keyField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY),
            new EmptyBorder(8, 12, 8, 12)
        ));

        content.add(createLabel("Encryption Method:"));
        content.add(Box.createVerticalStrut(5));
        content.add(methodCombo);
        content.add(Box.createVerticalStrut(15));
        content.add(createLabel("Decryption Key (for decryption only):"));
        content.add(Box.createVerticalStrut(5));
        content.add(keyField);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionCard() {
        JPanel card = new JPanel(new GridLayout(1, 2, 20, 0));
        card.setBackground(BACKGROUND);

        OvalButton encryptBtn = new OvalButton("🔐 Encrypt File");
        encryptBtn.setBackground(new Color(76, 175, 80));
        encryptBtn.addActionListener(this::encryptFile);

        OvalButton decryptBtn = new OvalButton("🔓 Decrypt File");
        decryptBtn.setBackground(new Color(244, 67, 54));
        decryptBtn.addActionListener(this::decryptFile);

        card.add(encryptBtn);
        card.add(decryptBtn);
        return card;
    }

    private JPanel createKeyCard() {
        JPanel card = new JPanel(new BorderLayout());
        styleCard(card, "🔑 Generated Encryption Key");

        keyArea = new JTextArea(4, 20);
        keyArea.setEditable(false);
        keyArea.setLineWrap(true);
        keyArea.setWrapStyleWord(true);
        keyArea.setForeground(new Color(0, 255, 127));
        keyArea.setBackground(new Color(35, 35, 40));
        keyArea.setBorder(new EmptyBorder(15, 15, 15, 15));
        keyArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        keyArea.setText("Encryption key will appear here after encrypting a file...");

        JScrollPane scrollPane = new JScrollPane(keyArea);
        scrollPane.setBorder(null);
        scrollPane.setBackground(CARD_BACKGROUND);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setForeground(PRIMARY);
        progressBar.setBackground(CARD_BACKGROUND);
        progressBar.setBorder(new EmptyBorder(5, 5, 5, 5));

        statusLabel = new JLabel("Ready - Choose a file to begin");
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setBorder(new EmptyBorder(5, 0, 0, 0));

        panel.add(progressBar, BorderLayout.NORTH);
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel historyTitle = new JLabel("Operation History");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        historyTitle.setForeground(Color.WHITE);
        historyTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        String[] columns = {"Date & Time", "Operation", "File Name", "Method", "Status"};
        historyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        JTable table = new JTable(historyModel);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);

        panel.add(historyTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Helper Methods
    private void styleCard(JPanel card, String title) {
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                title,
                0,
                0,
                new Font("Segoe UI", Font.BOLD, 12),
                Color.WHITE
            ),
            new EmptyBorder(15, 15, 15, 15)
        ));
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setBackground(new Color(60, 60, 65));
        combo.setForeground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? PRIMARY : new Color(60, 60, 65));
                setForeground(Color.WHITE);
                setBorder(new EmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }

    private void styleTable(JTable table) {
        table.setBackground(CARD_BACKGROUND);
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(70, 70, 75));
        table.setSelectionBackground(PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(25);
        table.getTableHeader().setBackground(BACKGROUND);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    }

    // Event Handlers
    private void selectFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            fileLabel.setText("Selected: " + selectedFile.getName());
            statusLabel.setText("File selected: " + selectedFile.getName());
        }
    }

    private void encryptFile(ActionEvent e) {
        if (selectedFile == null) {
            showError("No File Selected", "Please select a file to encrypt first.");
            return;
        }

        if (!selectedFile.exists()) {
            showError("File Not Found", "The selected file no longer exists.");
            return;
        }

        new SwingWorker<EncryptionResult, Void>() {
            @Override
            protected EncryptionResult doInBackground() throws Exception {
                setProgressUI(true, "Reading file...");
                
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                EncryptionMethod method = (EncryptionMethod) methodCombo.getSelectedItem();
                
                setProgressUI(true, "Encrypting file...");
                return encryptionService.encryptFile(fileData, method);
            }
            
            @Override
            protected void done() {
                try {
                    EncryptionResult result = get();
                    
                    // Display the key
                    keyArea.setText("SAVE THIS KEY - YOU NEED IT TO DECRYPT:\n\n" + result.getKey());
                    
                    // Save encrypted file
                    EncryptionMethod method = (EncryptionMethod) methodCombo.getSelectedItem();
                    saveResult(result.getEncryptedData(), ".cryptx", "ENCRYPT", method);
                    
                    setProgressUI(false, "Encryption completed successfully!");
                    
                } catch (Exception ex) {
                    logOperation("ENCRYPT", (EncryptionMethod) methodCombo.getSelectedItem(), "FAILED");
                    showError("Encryption Failed", "Error: " + ex.getMessage());
                    setProgressUI(false, "Encryption failed");
                } finally {
                    loadHistory();
                }
            }
        }.execute();
    }

    private void decryptFile(ActionEvent e) {
        if (selectedFile == null) {
            showError("No File Selected", "Please select an encrypted file to decrypt.");
            return;
        }

        if (!selectedFile.getName().endsWith(".cryptx")) {
            showError("Invalid File Type", "Please select a .cryptx encrypted file.");
            return;
        }

        String key = keyField.getText().trim();
        if (key.isEmpty()) {
            showError("No Decryption Key", "Please enter the decryption key.");
            return;
        }

        new SwingWorker<byte[], Void>() {
            @Override
            protected byte[] doInBackground() throws Exception {
                setProgressUI(true, "Reading encrypted file...");
                
                byte[] encryptedData = Files.readAllBytes(selectedFile.toPath());
                EncryptionMethod method = (EncryptionMethod) methodCombo.getSelectedItem();
                
                setProgressUI(true, "Decrypting file...");
                return encryptionService.decryptFile(encryptedData, key, method);
            }
            
            @Override
            protected void done() {
                try {
                    byte[] decryptedData = get();
                    
                    String originalName = selectedFile.getName().replace(".cryptx", "");
                    EncryptionMethod method = (EncryptionMethod) methodCombo.getSelectedItem();
                    saveResult(decryptedData, "", "DECRYPT", method, originalName);
                    
                    setProgressUI(false, "Decryption completed successfully!");
                    
                } catch (Exception ex) {
                    logOperation("DECRYPT", (EncryptionMethod) methodCombo.getSelectedItem(), "FAILED");
                    showError("Decryption Failed", "Error: " + ex.getMessage());
                    setProgressUI(false, "Decryption failed");
                } finally {
                    loadHistory();
                }
            }
        }.execute();
    }

    private void saveResult(byte[] data, String suffix, String operation, EncryptionMethod method) {
        saveResult(data, suffix, operation, method, selectedFile.getName());
    }

    private void saveResult(byte[] data, String suffix, String operation, 
                          EncryptionMethod method, String fileName) {
        JFileChooser saver = new JFileChooser();
        saver.setSelectedFile(new File(fileName + suffix));
        
        if (saver.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.write(saver.getSelectedFile().toPath(), data);
                logOperation(operation, method, "SUCCESS");
                showSuccess("Operation Complete", 
                    operation + " completed successfully!\nFile saved: " + 
                    saver.getSelectedFile().getName());
            } catch (Exception ex) {
                logOperation(operation, method, "FAILED");
                showError("Save Failed", "Could not save file: " + ex.getMessage());
            }
        }
    }

    // Utility Methods
    private void setProgressUI(boolean working, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(working);
            statusLabel.setText(message);
        });
    }

    private void resetUI() {
        selectedFile = null;
        fileLabel.setText("No file selected");
        keyField.setText("");
        statusLabel.setText("Ready - Choose a file to begin");
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void logOperation(String type, EncryptionMethod method, String status) {
        if (selectedFile != null) {
            operationDAO.insertOperation(new OperationRecord(
                selectedFile.getName(),
                type,
                method.name(),
                status
            ));
        }
    }

    private void loadHistory() {
        SwingUtilities.invokeLater(() -> {
            historyModel.setRowCount(0);
            List<OperationRecord> records = operationDAO.getAllOperations();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            records.forEach(record -> historyModel.addRow(new Object[]{
                record.getTimestamp().format(dtf),
                record.getOperationType(),
                record.getFileName(),
                record.getMethod(),
                record.getStatus()
            }));
        });
    }

    // Custom Components
    class OvalButton extends JButton {
        OvalButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setBorder(new EmptyBorder(12, 30, 12, 30));
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Color bgColor = getBackground();
            if (getModel().isPressed()) {
                g2.setColor(bgColor.darker());
            } else if (getModel().isRollover()) {
                g2.setColor(bgColor.brighter());
            } else {
                g2.setColor(bgColor);
            }
            
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        // Set system properties for better UI rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Failed to start CryptXpress: " + e.getMessage(), 
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
