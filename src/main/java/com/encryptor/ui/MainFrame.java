package com.encryptor.ui;

import com.encryptor.dao.OperationDAO;
import com.encryptor.model.OperationRecord;
import com.encryptor.service.EncryptionService;
import com.encryptor.service.EncryptionService.EncryptionMethod;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
        setTitle("SecureCrypt Pro");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        // Content Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Operations", createOperationsPanel());
        tabs.add("History", createHistoryPanel());
        mainPanel.add(tabs, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("SecureCrypt");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(PRIMARY);

        JLabel subtitle = new JLabel("Military-Grade File Encryption");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);

        panel.add(title, BorderLayout.WEST);
        panel.add(subtitle, BorderLayout.SOUTH);
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

        // Method Selection
        methodCombo = new JComboBox<>(EncryptionMethod.values());
        styleComboBox(methodCombo);

        // Key Input
        keyField = new JTextField();
        keyField.setForeground(Color.WHITE);
        keyField.setBackground(CARD_BACKGROUND);
        keyField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY),
            new EmptyBorder(8, 12, 8, 12)
        ));

        content.add(createLabel("Encryption Method:"));
        content.add(Box.createVerticalStrut(5));
        content.add(methodCombo);
        content.add(Box.createVerticalStrut(15));
        content.add(createLabel("Decryption Key:"));
        content.add(Box.createVerticalStrut(5));
        content.add(keyField);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionCard() {
        JPanel card = new JPanel(new GridLayout(1, 2, 20, 0));
        card.setBackground(BACKGROUND);

        OvalButton encryptBtn = new OvalButton("🔐 Encrypt");
        encryptBtn.setBackground(new Color(76, 175, 80));
        encryptBtn.addActionListener(this::encryptFile);

        OvalButton decryptBtn = new OvalButton("🔓 Decrypt");
        decryptBtn.setBackground(new Color(244, 67, 54));
        decryptBtn.addActionListener(this::decryptFile);

        card.add(encryptBtn);
        card.add(decryptBtn);
        return card;
    }

    private JPanel createKeyCard() {
        JPanel card = new JPanel(new BorderLayout());
        styleCard(card, "🔑 Encryption Key");

        keyArea = new JTextArea(3, 20);
        keyArea.setEditable(false);
        keyArea.setLineWrap(true);
        keyArea.setForeground(new Color(0, 255, 127));
        keyArea.setBackground(CARD_BACKGROUND);
        keyArea.setBorder(new EmptyBorder(10, 15, 10, 15));

        card.add(new JScrollPane(keyArea), BorderLayout.CENTER);
        return card;
    }

    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setForeground(PRIMARY);
        progressBar.setBackground(CARD_BACKGROUND);

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.GRAY);

        panel.add(progressBar, BorderLayout.NORTH);
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);

        String[] columns = {"Date", "Operation", "File", "Status"};
        historyModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(historyModel);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Helper Methods
    private void styleCard(JPanel card, String title) {
        card.setBackground(BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(title),
            new EmptyBorder(15, 15, 15, 15)
        ));
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setBackground(CARD_BACKGROUND);
        combo.setForeground(Color.WHITE);
        combo.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? PRIMARY : CARD_BACKGROUND);
                setForeground(Color.WHITE);
                return this;
            }
        });
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.GRAY);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }

    private void styleTable(JTable table) {
        table.setBackground(CARD_BACKGROUND);
        table.setForeground(Color.WHITE);
        table.setGridColor(Color.DARK_GRAY);
        table.setSelectionBackground(PRIMARY);
        table.getTableHeader().setBackground(BACKGROUND);
        table.getTableHeader().setForeground(Color.WHITE);
    }

    // Event Handlers
    private void selectFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            fileLabel.setText(selectedFile.getName());
        }
    }

    private void encryptFile(ActionEvent e) {
        if (selectedFile == null) {
            showError("No file selected!", "Select a file first");
            return;
        }

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                setProgressUI(true, "Encrypting...");
                
                EncryptionMethod method = (EncryptionMethod) methodCombo.getSelectedItem();
                byte[] encrypted = encryptionService.encryptFile(selectedFile, method);
                saveResult(encrypted, ".encrypted", "ENCRYPT", method);
                
                return null;
            }
            
            protected void done() {
                setProgressUI(false, "Encryption complete");
                resetUI();
                loadHistory();
            }
        }.execute();
    }

    private void decryptFile(ActionEvent e) {
        if (selectedFile == null || !selectedFile.getName().endsWith(".encrypted")) {
            showError("Invalid file!", "Select an encrypted file");
            return;
        }

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                setProgressUI(true, "Decrypting...");
                
                EncryptionMethod method = (EncryptionMethod) methodCombo.getSelectedItem();
                byte[] decrypted = encryptionService.decryptFile(
                    Files.readAllBytes(selectedFile.toPath()),
                    keyField.getText(),
                    method
                );
                
                String originalName = selectedFile.getName().replace(".encrypted", "");
                saveResult(decrypted, "", "DECRYPT", method, originalName);
                
                return null;
            }
            
            protected void done() {
                setProgressUI(false, "Decryption complete");
                resetUI();
                loadHistory();
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
            } catch (Exception ex) {
                logOperation(operation, method, "FAILED");
                showError("Save failed", ex.getMessage());
            }
        }
    }

    // Utility Methods
    private void setProgressUI(boolean working, String message) {
        progressBar.setIndeterminate(working);
        statusLabel.setText(message);
    }

    private void resetUI() {
        selectedFile = null;
        fileLabel.setText("No file selected");
        keyField.setText("");
        keyArea.setText("");
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void logOperation(String type, EncryptionMethod method, String status) {
        operationDAO.insertOperation(new OperationRecord(
            selectedFile.getName(),
            type,
            method.name(),
            status
        ));
    }

    private void loadHistory() {
        historyModel.setRowCount(0);
        List<OperationRecord> records = operationDAO.getAllOperations();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        records.forEach(record -> historyModel.addRow(new Object[]{
            record.getTimestamp().format(dtf),
            record.getOperationType(),
            record.getFileName(),
            record.getStatus()
        }));
    }

    // Custom Components
    class OvalButton extends JButton {
        OvalButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setBorder(new EmptyBorder(12, 30, 12, 30));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (getModel().isPressed()) {
                g2.setColor(getBackground().darker());
            } else if (getModel().isRollover()) {
                g2.setColor(getBackground().brighter());
            } else {
                g2.setColor(getBackground());
            }
            
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
