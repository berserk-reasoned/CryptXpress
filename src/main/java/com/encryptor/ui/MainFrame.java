package com.encryptor.ui;

import com.encryptor.dao.OperationDAO;
import com.encryptor.model.OperationRecord;
import com.encryptor.service.EncryptionService;
import com.encryptor.service.EncryptionService.CryptoException;
import com.encryptor.service.EncryptionService.EncryptionMethod;
import com.encryptor.service.EncryptionService.EncryptionResult;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

/**
 * Main GUI frame for CryptXpress application.
 * Provides file encryption/decryption interface with operation history.
 */
public class MainFrame extends JFrame {
    private final EncryptionService cryptoService = new EncryptionService();
    private final OperationDAO operationDAO = new OperationDAO();
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    // UI Components
    private Path currentFile;
    private JLabel fileLabel;
    private JLabel fileSizeLabel;
    private JComboBox<EncryptionMethod> methodCombo;
    private JTextArea keyDisplayArea;
    private JTextField keyInputField;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTable historyTable;
    private DefaultTableModel historyModel;
    private JButton encryptButton;
    private JButton decryptButton;
    private JButton clearHistoryButton;

    public MainFrame() {
        initializeUI();
        loadOperationHistory();
        
        // Add shutdown hook for proper cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
    }

    private static void run() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            // Fallback to system look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Failed to set system look and feel: " + ex.getMessage());
            }

        }

        new MainFrame().setVisible(true);
    }

    private void initializeUI() {
        setTitle("CryptXpress - File Encryption Suite");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));

        // Create main layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Add components
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createTabbedPane(), BorderLayout.CENTER);
        mainPanel.add(createStatusPanel(), BorderLayout.SOUTH);

        add(mainPanel);
        setupKeyboardShortcuts();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        // Title and subtitle
        JLabel titleLabel = new JLabel("CryptXpress");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 150, 255));
        
        JLabel subtitleLabel = new JLabel("Secure File Encryption & Decryption");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        return headerPanel;
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Operations", createOperationsPanel());
        tabbedPane.addTab("History", createHistoryPanel());
        return tabbedPane;
    }

    private JPanel createOperationsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        panel.add(createFileSelectionPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createEncryptionSettingsPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createActionButtonsPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createKeyManagementPanel());
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }

    private JPanel createFileSelectionPanel() {
        JPanel panel = createTitledPanel("File Selection");
        
        fileLabel = new JLabel("No file selected");
        fileSizeLabel = new JLabel("");
        fileSizeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fileSizeLabel.setForeground(Color.GRAY);
        
        JPanel fileInfoPanel = new JPanel();
        fileInfoPanel.setLayout(new BoxLayout(fileInfoPanel, BoxLayout.Y_AXIS));
        fileInfoPanel.add(fileLabel);
        fileInfoPanel.add(fileSizeLabel);
        
        JButton browseButton = new JButton("Browse Files");
        browseButton.addActionListener(e -> selectFile());
        
        panel.add(fileInfoPanel, BorderLayout.CENTER);
        panel.add(browseButton, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createEncryptionSettingsPanel() {
        JPanel panel = createTitledPanel("Encryption Settings");
        
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Method selection
        gbc.gridx = 0; gbc.gridy = 0;
        settingsPanel.add(new JLabel("Encryption Method:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        methodCombo = new JComboBox<>(EncryptionMethod.values());
        methodCombo.setToolTipText("Select encryption algorithm");
        settingsPanel.add(methodCombo, gbc);
        
        // Key input for decryption
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        settingsPanel.add(new JLabel("Decryption Key:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        keyInputField = new JTextField();
        keyInputField.setToolTipText("Enter decryption key for encrypted files");
        settingsPanel.add(keyInputField, gbc);
        
        panel.add(settingsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createActionButtonsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        encryptButton = new JButton("Encrypt File");
        encryptButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        encryptButton.addActionListener(e -> performEncryption());
        
        decryptButton = new JButton("Decrypt File");
        decryptButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        decryptButton.addActionListener(e -> performDecryption());
        
        panel.add(encryptButton);
        panel.add(decryptButton);
        
        return panel;
    }

    private JPanel createKeyManagementPanel() {
        JPanel panel = createTitledPanel("Generated Keys");
        
        keyDisplayArea = new JTextArea(4, 50);
        keyDisplayArea.setEditable(false);
        keyDisplayArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        keyDisplayArea.setLineWrap(true);
        keyDisplayArea.setWrapStyleWord(true);
        keyDisplayArea.setToolTipText("Generated encryption keys will appear here");
        
        JScrollPane scrollPane = new JScrollPane(keyDisplayArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton copyButton = new JButton("Copy Key");
        copyButton.addActionListener(e -> copyKeyToClipboard());
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> keyDisplayArea.setText(""));
        
        buttonPanel.add(copyButton);
        buttonPanel.add(clearButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table model and table
        String[] columnNames = {"Timestamp", "Operation", "File Name", "Method", "Status"};
        historyModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        historyTable = new JTable(historyModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadOperationHistory());
        
        clearHistoryButton = new JButton("Clear History");
        clearHistoryButton.addActionListener(e -> clearHistory());
        
        controlPanel.add(refreshButton);
        controlPanel.add(clearHistoryButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("");
        progressBar.setVisible(false);
        
        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(progressBar, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), title));
        return panel;
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        
        // Set last used directory
        Preferences prefs = Preferences.userNodeForPackage(MainFrame.class);
        String lastDir = prefs.get("last_directory", System.getProperty("user.home"));
        fileChooser.setCurrentDirectory(new File(lastDir));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile().toPath();
            
            // Save last used directory
            prefs.put("last_directory", currentFile.getParent().toString());
            
            // Update UI
            fileLabel.setText(currentFile.getFileName().toString());
            
            try {
                long fileSize = java.nio.file.Files.size(currentFile);
                fileSizeLabel.setText(String.format("Size: %,d bytes", fileSize));
            } catch (IOException e) {
                fileSizeLabel.setText("Size: Unknown");
            }
            
            updateStatus("File selected: " + currentFile.getFileName());
        }
    }

    private void performEncryption() {
        if (!validateFileSelection()) return;
        
        setUIEnabled(false);
        showProgress("Encrypting file...");
        
        executorService.submit(() -> {
            try {
                EncryptionMethod method = (EncryptionMethod) methodCombo.getSelectedItem();
                EncryptionResult result = cryptoService.encryptFile(currentFile, method);
                
                SwingUtilities.invokeLater(() -> {
                    keyDisplayArea.setText("Encryption Key:\n" + result.getBase64Key());
                    updateStatus("File encrypted successfully: " + result.getEncryptedFile().getFileName());
                    
                    // Log successful operation
                    OperationRecord record = OperationRecord.encrypt(
                        currentFile, method.toString(), "SUCCESS", null);
                    operationDAO.logOperation(record);
                    loadOperationHistory();
                    
                    showSuccess("Encryption completed successfully!\nEncrypted file: " + 
                              result.getEncryptedFile().getFileName());
                });
                
            } catch (CryptoException | IOException e) {
                SwingUtilities.invokeLater(() -> {
                    // Log failed operation
                    OperationRecord record = OperationRecord.encrypt(
                        currentFile, methodCombo.getSelectedItem().toString(), "FAILED", e.getMessage());
                    operationDAO.logOperation(record);
                    loadOperationHistory();
                    
                    showError("Encryption Failed", "Failed to encrypt file: " + e.getMessage());
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    hideProgress();
                    setUIEnabled(true);
                });
            }
        });
    }

    private void performDecryption() {
        if (!validateFileSelection() || !validateDecryptionKey()) return;
        
        setUIEnabled(false);
        showProgress("Decrypting file...");
        
        executorService.submit(() -> {
            try {
                EncryptionMethod method = (EncryptionMethod) methodCombo.getSelectedItem();
                String key = keyInputField.getText().trim();
                
                Path decryptedFile = cryptoService.decryptFile(currentFile, key, method);
                
                SwingUtilities.invokeLater(() -> {
                    updateStatus("File decrypted successfully: " + decryptedFile.getFileName());
                    
                    // Log successful operation
                    OperationRecord record = OperationRecord.decrypt(
                        currentFile, method.toString(), "SUCCESS", null);
                    operationDAO.logOperation(record);
                    loadOperationHistory();
                    
                    showSuccess("Decryption completed successfully!\nDecrypted file: " + 
                              decryptedFile.getFileName());
                });
                
            } catch (CryptoException | IOException e) {
                SwingUtilities.invokeLater(() -> {
                    // Log failed operation
                    OperationRecord record = OperationRecord.decrypt(
                        currentFile, methodCombo.getSelectedItem().toString(), "FAILED", e.getMessage());
                    operationDAO.logOperation(record);
                    loadOperationHistory();
                    
                    showError("Decryption Failed", "Failed to decrypt file: " + e.getMessage());
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    hideProgress();
                    setUIEnabled(true);
                });
            }
        });
    }

    private boolean validateFileSelection() {
        if (currentFile == null) {
            showError("No File Selected", "Please select a file to process.");
            return false;
        }
        
        if (!java.nio.file.Files.exists(currentFile)) {
            showError("File Not Found", "The selected file does not exist.");
            return false;
        }
        
        return true;
    }

    private boolean validateDecryptionKey() {
        String key = keyInputField.getText().trim();
        if (key.isEmpty()) {
            showError("No Decryption Key", "Please enter the decryption key.");
            return false;
        }
        
        // Basic validation for Base64 format
        try {
            java.util.Base64.getDecoder().decode(key);
        } catch (IllegalArgumentException e) {
            showError("Invalid Key Format", "The decryption key must be in Base64 format.");
            return false;
        }
        
        return true;
    }

    private void loadOperationHistory() {
        executorService.submit(() -> {
            try {
                var operations = operationDAO.getRecentOperations(100);
                
                SwingUtilities.invokeLater(() -> {
                    historyModel.setRowCount(0);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    
                    for (OperationRecord record : operations) {
                        Object[] row = {
                            record.timestamp().format(formatter),
                            record.operationType(),
                            record.fileName(),
                            record.encryptionMethod(),
                            record.status()
                        };
                        historyModel.addRow(row);
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> 
                    showError("History Load Error", "Failed to load operation history: " + e.getMessage()));
            }
        });
    }

    private void clearHistory() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to clear all operation history?",
            "Confirm Clear History",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            executorService.submit(() -> {
                boolean success = operationDAO.clearHistory();
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        historyModel.setRowCount(0);
                        updateStatus("Operation history cleared");
                    } else {
                        showError("Clear Failed", "Failed to clear operation history");
                    }
                });
            });
        }
    }

    private void copyKeyToClipboard() {
        String key = keyDisplayArea.getText();
        if (!key.isEmpty()) {
            java.awt.datatransfer.StringSelection selection = 
                new java.awt.datatransfer.StringSelection(key);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            updateStatus("Key copied to clipboard");
        }
    }

    private void setUIEnabled(boolean enabled) {
        encryptButton.setEnabled(enabled);
        decryptButton.setEnabled(enabled);
        methodCombo.setEnabled(enabled);
        keyInputField.setEnabled(enabled);
        clearHistoryButton.setEnabled(enabled);
    }

    private void showProgress(String message) {
        statusLabel.setText(message);
        progressBar.setIndeterminate(true);
        progressBar.setString(message);
        progressBar.setVisible(true);
    }

    private void hideProgress() {
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
        statusLabel.setText("Ready");
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupKeyboardShortcuts() {
        // Ctrl+O for file selection
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("control O"), "openFile");
        getRootPane().getActionMap().put("openFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectFile();
            }
        });
        
        // Ctrl+E for encryption
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("control E"), "encrypt");
        getRootPane().getActionMap().put("encrypt", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (encryptButton.isEnabled()) performEncryption();
            }
        });
        
        // Ctrl+D for decryption
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("control D"), "decrypt");
        getRootPane().getActionMap().put("decrypt", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (decryptButton.isEnabled()) performDecryption();
            }
        });
    }

    private void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        // Set system properties for better rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(MainFrame::run);
    }
}
