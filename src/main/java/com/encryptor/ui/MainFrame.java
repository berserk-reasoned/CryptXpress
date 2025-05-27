package com.encryptor.ui;

import com.encryptor.service.EncryptionService;
import com.encryptor.service.EncryptionService.CryptoException;
import com.encryptor.service.EncryptionService.EncryptionMethod;
import com.encryptor.dao.OperationDAO;
import com.encryptor.model.OperationRecord;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;
package com.encryptor.ui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class MainFrame extends JFrame {
    // ... existing fields
    
    private void logOperation(OperationRecord record) {
        operationDAO.logOperation(record);
        SwingUtilities.invokeLater(() -> {
            historyModel.addRow(new Object[]{
                record.timestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                record.operationType(),
                record.fileName(),
                record.encryptionMethod(),
                record.status()
            });
        });
    }

    // ... rest of the implementation matches previous answer
    // Ensure all references to OperationRecord use the new fields
}
public class MainFrame extends JFrame {
    private final EncryptionService cryptoService = new EncryptionService();
    private final OperationDAO operationDAO = new OperationDAO();
    private final ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
    
    private Path currentFile;
    private JLabel fileLabel;
    private JComboBox<EncryptionMethod> methodCombo;
    private JTextArea keyArea;
    private JTextField keyField;
    private JProgressBar progressBar;
    private JTable historyTable;
    private DefaultTableModel historyModel;

    public MainFrame() {
        initUI();
        loadHistory();
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        setTitle("CryptXpress v2.0");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        mainPanel.add(createHeader(), BorderLayout.NORTH);

        // Content tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Operations", createOperationsPanel());
        tabs.add("History", createHistoryPanel());
        mainPanel.add(tabs, BorderLayout.CENTER);

        add(mainPanel);
        setupKeyBindings();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel title = new JLabel("CryptXpress");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(0, 150, 255));

        JLabel subtitle = new JLabel("Secure File Encryption Suite");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createHorizontalStrut(15));
        titlePanel.add(subtitle);
        
        header.add(titlePanel, BorderLayout.WEST);
        return header;
    }

    private JPanel createOperationsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(createFileSelectionCard());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createEncryptionSettingsCard());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createActionButtons());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createKeyManagementCard());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createStatusBar());

        return panel;
    }

    private JPanel createFileSelectionCard() {
        JPanel card = new JPanel(new BorderLayout());
        styleCard(card, "📁 File Selection");

        fileLabel = new JLabel("No file selected");
        fileLabel.setForeground(Color.WHITE);

        JButton browseBtn = new JButton("Choose File");
        browseBtn.addActionListener(e -> selectFile());
        styleButton(browseBtn, new Color(0, 150, 255));

        JPanel content = new JPanel(new BorderLayout(10, 0));
        content.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        content.add(fileLabel, BorderLayout.CENTER);
        content.add(browseBtn, BorderLayout.EAST);
        card.add(content);

        return card;
    }

    private JPanel createEncryptionSettingsCard() {
        JPanel card = new JPanel(new BorderLayout());
        styleCard(card, "⚙️ Encryption Settings");

        methodCombo = new JComboBox<>(EncryptionMethod.values());
        styleComboBox(methodCombo);

        keyField = new JTextField();
        styleTextField(keyField);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        content.add(createLabel("Encryption Method:"));
        content.add(Box.createVerticalStrut(5));
        content.add(methodCombo);
        content.add(Box.createVerticalStrut(15));
        content.add(createLabel("Decryption Key:"));
        content.add(Box.createVerticalStrut(5));
        content.add(keyField);

        card.add(content);
        return card;
    }

    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setOpaque(false);

        JButton encryptBtn = new JButton("🔐 Encrypt");
        styleButton(encryptBtn, new Color(76, 175, 80));
        encryptBtn.addActionListener(e -> exec.execute(this::performEncryption));

        JButton decryptBtn = new JButton("🔓 Decrypt");
        styleButton(decryptBtn, new Color(244, 67, 54));
        decryptBtn.addActionListener(e -> exec.execute(this::performDecryption));

        panel.add(encryptBtn);
        panel.add(decryptBtn);
        return panel;
    }

    private JPanel createKeyManagementCard() {
        JPanel card = new JPanel(new BorderLayout());
        styleCard(card, "🔑 Key Management");

        keyArea = new JTextArea(3, 20);
        keyArea.setEditable(false);
        keyArea.setLineWrap(true);
        keyArea.setWrapStyleWord(true);
        keyArea.setForeground(new Color(0, 255, 127));
        keyArea.setBackground(new Color(35, 35, 40));
        keyArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton copyBtn = new JButton("Copy");
        styleButton(copyBtn, new Color(255, 193, 7));
        copyBtn.addActionListener(e -> copyToClipboard(keyArea.getText()));

        JButton saveBtn = new JButton("Save");
        styleButton(saveBtn, new Color(156, 39, 176));
        saveBtn.addActionListener(e -> saveKeyToFile());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnPanel.setOpaque(false);
        btnPanel.add(copyBtn);
        btnPanel.add(saveBtn);

        card.add(new JScrollPane(keyArea), BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0, 150, 255));
        progressBar.setBackground(new Color(45, 45, 50));

        JPanel statusPanel = new JPanel(new BorderLayout(10, 0));
        statusPanel.setOpaque(false);
        statusPanel.add(progressBar, BorderLayout.CENTER);

        panel.add(statusPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        String[] columns = {"Timestamp", "Operation", "File", "Method", "Status"};
        historyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(historyModel);
        styleTable(historyTable);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 65)));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void selectFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileHidingEnabled(false);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter(
            "All Files", "*"));
        
        Preferences prefs = Preferences.userRoot();
        chooser.setCurrentDirectory(new File(prefs.get("LAST_DIR", System.getProperty("user.home")));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile().toPath();
            prefs.put("LAST_DIR", currentFile.getParent().toString());
            fileLabel.setText(currentFile.getFileName().toString());
            updateStatus("Selected: " + currentFile.getFileName());
        }
    }

    private void performEncryption() {
        if (currentFile == null) {
            showError("No File Selected", "Please select a file first");
            return;
        }

        startProgress("Encrypting...");
        try {
            EncryptionMethod method = (EncryptionMethod) methodCombo.getSelectedItem();
            var result = cryptoService.encryptFile(currentFile, method);
            
            SwingUtilities.invokeLater(() -> {
                keyArea.setText(result.getBase64Key());
                logOperation(new OperationRecord(
                    currentFile.getFileName().toString(),
                    currentFile.toString(),
                    "ENCRYPT",
                    method.name(),
                    Files.size(currentFile),
                    "SUCCESS",
                    null
                ));
                updateStatus("Encryption successful!");
            });
        } catch (CryptoException | IOException e) {
            logOperation(new OperationRecord(
                currentFile.getFileName().toString(),
                currentFile.toString(),
                "ENCRYPT",
                ((EncryptionMethod) methodCombo.getSelectedItem()).name(),
                Files.size(currentFile),
                "FAILED",
                e.getMessage()
            ));
            showError("Encryption Failed", e.getMessage());
        } finally {
            stopProgress();
        }
    }

    private void performDecryption() {
        if (currentFile == null) {
            showError("No File Selected", "Please select an encrypted file");
            return;
        }

        if (keyField.getText().isBlank()) {
            showError("Missing Key", "Decryption key required");
            return;
        }

        startProgress("Decrypting...");
        try {
            EncryptionMethod method = (EncryptionMethod) methodCombo.getSelectedItem();
            Path decryptedPath = cryptoService.decryptFile(currentFile, keyField.getText(), method);
            
            SwingUtilities.invokeLater(() -> {
                logOperation(new OperationRecord(
                    currentFile.getFileName().toString(),
                    currentFile.toString(),
                    "DECRYPT",
                    method.name(),
                    Files.size(currentFile),
                    "SUCCESS",
                    null
                ));
                updateStatus("Decrypted to: " + decryptedPath.getFileName());
            });
        } catch (CryptoException | IOException e) {
            logOperation(new OperationRecord(
                currentFile.getFileName().toString(),
                currentFile.toString(),
                "DECRYPT",
                ((EncryptionMethod) methodCombo.getSelectedItem()).name(),
                Files.size(currentFile),
                "FAILED",
                e.getMessage()
            ));
            showError("Decryption Failed", e.getMessage());
        } finally {
            stopProgress();
        }
    }

    private void logOperation(OperationRecord record) {
        operationDAO.logOperation(record);
        SwingUtilities.invokeLater(this::loadHistory);
    }

    private void loadHistory() {
        historyModel.setRowCount(0);
        operationDAO.getRecentOperations(50).forEach(record ->
            historyModel.addRow(new Object[]{
                record.timestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                record.operationType(),
                record.fileName(),
                record.encryptionMethod(),
                record.status()
            })
        );
    }

    // UI Utilities
    private void styleCard(JPanel card, String title) {
        card.setBackground(new Color(45, 45, 50));
        card.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 65)),
            title,
            0, 0,
            new Font("Segoe UI", Font.BOLD, 12),
            Color.WHITE
        ));
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setBackground(new Color(60, 60, 65));
        combo.setForeground(Color.WHITE);
        combo.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? new Color(0, 150, 255) : new Color(60, 60, 65));
                return this;
            }
        });
    }

    private void styleTextField(JTextField field) {
        field.setBackground(new Color(60, 60, 65));
        field.setForeground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 85)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    private void styleTable(JTable table) {
        table.setBackground(new Color(45, 45, 50));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 60, 65));
        table.setSelectionBackground(new Color(0, 150, 255));
        table.getTableHeader().setBackground(new Color(60, 60, 65));
        table.getTableHeader().setForeground(Color.WHITE);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }

    private void startProgress(String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(true);
            progressBar.setString(message);
        });
    }

    private void stopProgress() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setString("");
        });
    }

    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message));
    }

    private void showError(String title, String message) {
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE));
    }

    private void setupKeyBindings() {
        // CTRL+O for open file
        getRootPane().getInputMap().put(KeyStroke.getKeyStroke("control O"), "openFile");
        getRootPane().getActionMap().put("openFile", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { selectFile(); }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
