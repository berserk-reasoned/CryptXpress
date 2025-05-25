package com.encryptor.ui;

import com.encryptor.dao.OperationDAO;
import com.encryptor.model.OperationRecord;
import com.encryptor.service.EncryptionService;
import com.encryptor.service.EncryptionService.EncryptionMethod;
import com.encryptor.service.EncryptionService.EncryptionResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainFrame extends JFrame {
    private JPanel mainPanel;
    private JPanel operationPanel;
    private JPanel historyPanel;
    private JTabbedPane tabbedPane;
    
    // Operation components
    private JLabel fileLabel;
    private JButton selectFileBtn;
    private JComboBox<EncryptionMethod> encryptionCombo;
    private JButton encryptBtn;
    private JButton decryptBtn;
    private JTextArea keyArea;
    private JTextField keyInputField;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    // History components
    private JTable historyTable;
    private DefaultTableModel tableModel;
    
    // Service and DAO
    private EncryptionService encryptionService;
    private OperationDAO operationDAO;
    
    // Selected file
    private File selectedFile;
    
    public MainFrame() {
        initializeServices();
        initializeUI();
        setupEventHandlers();
        loadHistory();
    }
    
    private void initializeServices() {
        encryptionService = new EncryptionService();
        operationDAO = new OperationDAO();
    }
    
    private void initializeUI() {
        setTitle("File Encryptor Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Modern UI styling
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        createMainLayout();
        styleComponents();
    }
    
    private void createMainLayout() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 30));
        
        // Create header
        JPanel headerPanel = createHeaderPanel();
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(40, 40, 40));
        tabbedPane.setForeground(Color.WHITE);
        
        // Create operation panel
        operationPanel = createOperationPanel();
        tabbedPane.addTab("🔐 Encrypt/Decrypt", operationPanel);
        
        // Create history panel
        historyPanel = createHistoryPanel();
        tabbedPane.addTab("📊 History", historyPanel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 20, 20));
        header.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel titleLabel = new JLabel("File Encryptor Pro");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 150, 255));
        
        JLabel subtitleLabel = new JLabel("Secure file encryption with modern algorithms");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(180, 180, 180));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(20, 20, 20));
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        header.add(titlePanel, BorderLayout.WEST);
        
        return header;
    }
    
    private JPanel createOperationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(35, 35, 35));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        // File selection section
        JPanel fileSection = createFileSelectionSection();
        
        // Encryption options section
        JPanel optionsSection = createEncryptionOptionsSection();
        
        // Action buttons section
        JPanel actionsSection = createActionsSection();
        
        // Key display section
        JPanel keySection = createKeySection();
        
        // Status section
        JPanel statusSection = createStatusSection();
        
        // Layout
        JPanel topSection = new JPanel(new GridLayout(2, 1, 0, 20));
        topSection.setBackground(new Color(35, 35, 35));
        topSection.add(fileSection);
        topSection.add(optionsSection);
        
        JPanel middleSection = new JPanel(new GridLayout(2, 1, 0, 20));
        middleSection.setBackground(new Color(35, 35, 35));
        middleSection.add(actionsSection);
        middleSection.add(keySection);
        
        panel.add(topSection, BorderLayout.NORTH);
        panel.add(middleSection, BorderLayout.CENTER);
        panel.add(statusSection, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createFileSelectionSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(new Color(45, 45, 45));
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("📁 Select File");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        fileLabel = new JLabel("No file selected");
        fileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fileLabel.setForeground(new Color(180, 180, 180));
        
        selectFileBtn = new JButton("Browse Files");
        selectFileBtn.setBackground(new Color(0, 120, 215));
        selectFileBtn.setForeground(Color.WHITE);
        selectFileBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        selectFileBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        selectFileBtn.setFocusPainted(false);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(45, 45, 45));
        contentPanel.add(fileLabel, BorderLayout.CENTER);
        contentPanel.add(selectFileBtn, BorderLayout.EAST);
        
        section.add(titleLabel, BorderLayout.NORTH);
        section.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        section.add(contentPanel, BorderLayout.SOUTH);
        
        return section;
    }
    
    private JPanel createEncryptionOptionsSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(new Color(45, 45, 45));
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("🔒 Encryption Method");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        encryptionCombo = new JComboBox<>(EncryptionMethod.values());
        encryptionCombo.setBackground(new Color(55, 55, 55));
        encryptionCombo.setForeground(Color.WHITE);
        encryptionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        encryptionCombo.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        // Key input for decryption
        JLabel keyInputLabel = new JLabel("Decryption Key (for decrypt only):");
        keyInputLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        keyInputLabel.setForeground(new Color(180, 180, 180));
        
        keyInputField = new JTextField();
        keyInputField.setBackground(new Color(55, 55, 55));
        keyInputField.setForeground(Color.WHITE);
        keyInputField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        keyInputField.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        inputPanel.setBackground(new Color(45, 45, 45));
        inputPanel.add(encryptionCombo);
        
        JPanel keyPanel = new JPanel(new BorderLayout());
        keyPanel.setBackground(new Color(45, 45, 45));
        keyPanel.add(keyInputLabel, BorderLayout.NORTH);
        keyPanel.add(keyInputField, BorderLayout.SOUTH);
        inputPanel.add(keyPanel);
        
        section.add(titleLabel, BorderLayout.NORTH);
        section.add(Box.createVerticalStrut(10));
        section.add(inputPanel, BorderLayout.SOUTH);
        
        return section;
    }
    
    private JPanel createActionsSection() {
        JPanel section = new JPanel(new GridLayout(1, 2, 20, 0));
        section.setBackground(new Color(35, 35, 35));
        
        encryptBtn = new JButton("🔐 Encrypt File");
        encryptBtn.setBackground(new Color(16, 124, 16));
        encryptBtn.setForeground(Color.WHITE);
        encryptBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        encryptBtn.setBorder(new EmptyBorder(15, 25, 15, 25));
        encryptBtn.setFocusPainted(false);
        
        decryptBtn = new JButton("🔓 Decrypt File");
        decryptBtn.setBackground(new Color(196, 43, 28));
        decryptBtn.setForeground(Color.WHITE);
        decryptBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        decryptBtn.setBorder(new EmptyBorder(15, 25, 15, 25));
        decryptBtn.setFocusPainted(false);
        
        section.add(encryptBtn);
        section.add(decryptBtn);
        
        return section;
    }
    
    private JPanel createKeySection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(new Color(45, 45, 45));
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("🔑 Generated Key");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        keyArea = new JTextArea(4, 50);
        keyArea.setBackground(new Color(30, 30, 30));
        keyArea.setForeground(new Color(0, 255, 127));
        keyArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        keyArea.setEditable(false);
        keyArea.setLineWrap(true);
        keyArea.setWrapStyleWord(true);
        keyArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(keyArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        
        section.add(titleLabel, BorderLayout.NORTH);
        section.add(Box.createVerticalStrut(10));
        section.add(scrollPane, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createStatusSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(new Color(35, 35, 35));
        section.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setBackground(new Color(50, 50, 50));
        progressBar.setForeground(new Color(0, 150, 255));
        progressBar.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(180, 180, 180));
        
        section.add(progressBar, BorderLayout.NORTH);
        section.add(statusLabel, BorderLayout.SOUTH);
        
        return section;
    }
    
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(35, 35, 35));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel titleLabel = new JLabel("📊 Operation History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        // Table
        String[] columns = {"ID", "File Name", "Operation", "Method", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        historyTable = new JTable(tableModel);
        historyTable.setBackground(new Color(45, 45, 45));
        historyTable.setForeground(Color.WHITE);
        historyTable.setGridColor(new Color(60, 60, 60));
        historyTable.setSelectionBackground(new Color(0, 120, 215));
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.getTableHeader().setBackground(new Color(30, 30, 30));
        historyTable.getTableHeader().setForeground(Color.WHITE);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBackground(new Color(45, 45, 45));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        
        // Refresh button
        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setBackground(new Color(0, 120, 215));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadHistory());
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(35, 35, 35));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(20));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void styleComponents() {
        // Add hover effects and modern styling
        addHoverEffect(selectFileBtn, new Color(0, 120, 215), new Color(0, 140, 235));
        addHoverEffect(encryptBtn, new Color(16, 124, 16), new Color(36, 144, 36));
        addHoverEffect(decryptBtn, new Color(196, 43, 28), new Color(216, 63, 48));
    }
    
    private void addHoverEffect(JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(normalColor);
            }
        });
    }
    
    private void setupEventHandlers() {
        selectFileBtn.addActionListener(this::selectFile);
        encryptBtn.addActionListener(this::encryptFile);
        decryptBtn.addActionListener(this::decryptFile);
    }
    
    private void selectFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            fileLabel.setText(selectedFile.getName());
            statusLabel.setText("File selected: " + selectedFile.getName());
        }
    }
    
    private void encryptFile(ActionEvent e) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a file first!", "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    statusLabel.setText("Encrypting file...");
                    progressBar.setIndeterminate(true);
                    
                    EncryptionMethod method = (EncryptionMethod) encryptionCombo.getSelectedItem();
                    EncryptionResult result = encryptionService.encryptFile(selectedFile, method);
                    
                    // Save encrypted file
                    JFileChooser saveChooser = new JFileChooser();
                    saveChooser.setSelectedFile(new File(selectedFile.getName() + ".encrypted"));
                    
                    SwingUtilities.invokeAndWait(() -> {
                        int saveResult = saveChooser.showSaveDialog(MainFrame.this);
                        if (saveResult == JFileChooser.APPROVE_OPTION) {
                            try {
                                File outputFile = saveChooser.getSelectedFile();
                                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                                    fos.write(result.getEncryptedData());
                                }
                                
                                // Display key
                                keyArea.setText(result.getKey());
                                
                                // Save to database
                                OperationRecord record = new OperationRecord(
                                    selectedFile.getName(),
                                    selectedFile.getAbsolutePath(),
                                    "ENCRYPT",
                                    method.name(),
                                    selectedFile.length(),
                                    "SUCCESS"
                                );
                                operationDAO.insertOperation(record);
                                
                                statusLabel.setText("File encrypted successfully!");
                                JOptionPane.showMessageDialog(MainFrame.this, 
                                    "File encrypted successfully!\nSave the key safely - you'll need it for decryption.",
                                    "Encryption Complete", JOptionPane.INFORMATION_MESSAGE);
                                
                            } catch (IOException ex) {
                                statusLabel.setText("Error saving encrypted file");
                                JOptionPane.showMessageDialog(MainFrame.this, 
                                    "Error saving encrypted file: " + ex.getMessage(),
                                    "Save Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                    
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Encryption failed");
                        JOptionPane.showMessageDialog(MainFrame.this, 
                            "Encryption failed: " + ex.getMessage(),
                            "Encryption Error", JOptionPane.ERROR_MESSAGE);
                        
                        // Save failed operation to database
                        OperationRecord record = new OperationRecord(
                            selectedFile.getName(),
                            selectedFile.getAbsolutePath(),
                            "ENCRYPT",
                            ((EncryptionMethod) encryptionCombo.getSelectedItem()).name(),
                            selectedFile.length(),
                            "FAILED"
                        );
                        operationDAO.insertOperation(record);
                    });
                }
                return null;
            }
            
            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setValue(0);
                loadHistory(); // Refresh history
            }
        };
        
        worker.execute();
    }
    
    private void decryptFile(ActionEvent e) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select an encrypted file first!", "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String key = keyInputField.getText().trim();
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the decryption key!", "No Key Provided", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    statusLabel.setText("Decrypting file...");
                    progressBar.setIndeterminate(true);
                    
                    EncryptionMethod method = (EncryptionMethod) encryptionCombo.getSelectedItem();
                    
                    // Read encrypted file
                    byte[] encryptedData = Files.readAllBytes(selectedFile.toPath());
                    
                    // Decrypt
                    byte[] decryptedData = encryptionService.decryptFile(encryptedData, key, method);
                    
                    // Save decrypted file
                    SwingUtilities.invokeAndWait(() -> {
                        JFileChooser saveChooser = new JFileChooser();
                        String originalName = selectedFile.getName();
                        if (originalName.endsWith(".encrypted")) {
                            originalName = originalName.substring(0, originalName.length() - 10);
                        }
                        saveChooser.setSelectedFile(new File(originalName + "_decrypted"));
                        
                        int saveResult = saveChooser.showSaveDialog(MainFrame.this);
                        if (saveResult == JFileChooser.APPROVE_OPTION) {
                            try {
                                File outputFile = saveChooser.getSelectedFile();
                                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                                    fos.write(decryptedData);
                                }
                                
                                // Save to database
                                OperationRecord record = new OperationRecord(
                                    selectedFile.getName(),
                                    selectedFile.getAbsolutePath(),
                                    "DECRYPT",
                                    method.name(),
                                    selectedFile.length(),
                                    "SUCCESS"
                                );
                                operationDAO.insertOperation(record);
                                
                                statusLabel.setText("File decrypted successfully!");
                                JOptionPane.showMessageDialog(MainFrame.this, 
                                    "File decrypted successfully!",
                                    "Decryption Complete", JOptionPane.INFORMATION_MESSAGE);
                                
                            } catch (IOException ex) {
                                statusLabel.setText("Error saving decrypted file");
                                JOptionPane.showMessageDialog(MainFrame.this, 
                                    "Error saving decrypted file: " + ex.getMessage(),
                                    "Save Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                    
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Decryption failed");
                        JOptionPane.showMessageDialog(MainFrame.this, 
                            "Decryption failed: " + ex.getMessage() + "\nPlease check your key and encryption method.",
                            "Decryption Error", JOptionPane.ERROR_MESSAGE);
                        
                        // Save failed operation to database
                        OperationRecord record = new OperationRecord(
                            selectedFile.getName(),
                            selectedFile.getAbsolutePath(),
                            "DECRYPT",
                            ((EncryptionMethod) encryptionCombo.getSelectedItem()).name(),
                            selectedFile.length(),
                            "FAILED"
                        );
                        operationDAO.insertOperation(record);
                    });
                }
                return null;
            }
            
            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setValue(0);
                loadHistory(); // Refresh history
            }
        };
        
        worker.execute();
    }
    
    private void loadHistory() {
        SwingWorker<List<OperationRecord>, Void> worker = new SwingWorker<List<OperationRecord>, Void>() {
            @Override
            protected List<OperationRecord> doInBackground() throws Exception {
                return operationDAO.getAllOperations();
            }
            
            @Override
            protected void done() {
                try {
                    List<OperationRecord> operations = get();
                    tableModel.setRowCount(0); // Clear existing rows
                    
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    
                    for (OperationRecord record : operations) {
                        Object[] row = {
                            record.getId(),
                            record.getFileName(),
                            record.getOperationType(),
                            record.getEncryptionMethod(),
                            record.getTimestamp().format(formatter),
                            record.getStatus()
                        };
                        tableModel.addRow(row);
                    }
                    
                } catch (Exception e) {
                    statusLabel.setText("Failed to load history");
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
}
