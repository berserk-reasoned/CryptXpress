package com.encryptor.ui;

import com.encryptor.dao.OperationDAO;
import com.encryptor.model.OperationRecord;
import com.encryptor.service.EncryptionService;
import com.encryptor.service.EncryptionService.CryptoException;
import com.encryptor.service.EncryptionService.EncryptionMethod;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

public class MainFrame extends JFrame {
    private final EncryptionService cryptoService = new EncryptionService();
    private final OperationDAO operationDAO = new OperationDAO();
    private final ExecutorService exec = Executors.newFixedThreadPool(5);

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
            showError("Look & Feel Error", e.getMessage());
        }

        setTitle("CryptXpress v2.0");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.add(createHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Operations", createOperationsPanel());
        tabs.add("History", createHistoryPanel());
        mainPanel.add(tabs, BorderLayout.CENTER);

        add(mainPanel);
        setupKeyBindings();
    }

    private JPanel createHeader() {
        JLabel title = new JLabel("CryptXpress", JLabel.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(0, 150, 255));

        JLabel subtitle = new JLabel("Secure File Encryption Suite", JLabel.LEFT);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);

        JPanel header = new JPanel(new BorderLayout());
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.add(title);
        titlePanel.add(subtitle);
        titlePanel.setOpaque(false);

        header.add(titlePanel, BorderLayout.WEST);
        return header;
    }

    private JPanel createOperationsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(createFileSelectionCard());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createEncryptionSettingsCard());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createActionButtons());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createKeyManagementCard());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createStatusBar());

        return panel;
    }

    private JPanel createFileSelectionCard() {
        JPanel card = createCard("File Selection");
        fileLabel = new JLabel("No file selected");
        fileLabel.setForeground(Color.WHITE);

        JButton browseBtn = new JButton("Choose File");
        browseBtn.addActionListener(e -> selectFile());

        card.add(fileLabel, BorderLayout.CENTER);
        card.add(browseBtn, BorderLayout.EAST);
        return card;
    }

    private JPanel createEncryptionSettingsCard() {
        JPanel card = createCard("Encryption Settings");

        methodCombo = new JComboBox<>(EncryptionMethod.values());
        keyField = new JTextField();

        JPanel inner = new JPanel(new GridLayout(4, 1, 5, 5));
        inner.setOpaque(false);
        inner.add(new JLabel("Encryption Method:"));
        inner.add(methodCombo);
        inner.add(new JLabel("Decryption Key:"));
        inner.add(keyField);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));

        JButton encryptBtn = new JButton("Encrypt");
        encryptBtn.addActionListener(e -> exec.execute(this::performEncryption));

        JButton decryptBtn = new JButton("Decrypt");
        decryptBtn.addActionListener(e -> exec.execute(this::performDecryption));

        panel.add(encryptBtn);
        panel.add(decryptBtn);
        return panel;
    }

    private JPanel createKeyManagementCard() {
        JPanel card = createCard("Key Management");
        keyArea = new JTextArea(3, 20);
        keyArea.setEditable(false);
        card.add(new JScrollPane(keyArea), BorderLayout.CENTER);
        return card;
    }

    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar();
        panel.add(progressBar, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"Timestamp", "Operation", "File", "Method", "Status"};
        historyModel = new DefaultTableModel(columns, 0);
        historyTable = new JTable(historyModel);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        return panel;
    }

    private void selectFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("All Files", "*"));

        Preferences prefs = Preferences.userRoot();
        chooser.setCurrentDirectory(new File(prefs.get("LAST_DIR", System.getProperty("user.home"))));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile().toPath();
            prefs.put("LAST_DIR", currentFile.getParent().toString());
            fileLabel.setText(currentFile.getFileName().toString());
        }
    }

    private void performEncryption() {
        if (currentFile == null) {
            showError("File Missing", "Select a file to encrypt");
            return;
        }
        updateProgress("Encrypting...");
        try {
            EncryptionMethod method = (EncryptionMethod) methodCombo.getSelectedItem();
            var result = cryptoService.encryptFile(currentFile, method);
            keyArea.setText(result.getBase64Key());
            logOperation(OperationRecord.encrypt(currentFile, method.name(), "SUCCESS", null));
            updateStatus("Encryption complete");
        } catch (CryptoException | IOException e) {
            logOperation(OperationRecord.encrypt(currentFile, ((EncryptionMethod) methodCombo.getSelectedItem()).name(), "FAILED", e.getMessage()));
            showError("Encryption Error", e.getMessage());
        } finally {
            progressBar.setIndeterminate(false);
        }
    }

    private void performDecryption() {
        if (currentFile == null || keyField.getText().isBlank()) {
            showError("Input Error", "File or key missing");
            return;
        }
        updateProgress("Decrypting...");
        try {
            EncryptionMethod method = (EncryptionMethod) methodCombo.getSelectedItem();
            cryptoService.decryptFile(currentFile, keyField.getText(), method);
            logOperation(OperationRecord.decrypt(currentFile, method.name(), "SUCCESS", null));
            updateStatus("Decryption complete");
        } catch (CryptoException | IOException e) {
            logOperation(OperationRecord.decrypt(currentFile, ((EncryptionMethod) methodCombo.getSelectedItem()).name(), "FAILED", e.getMessage()));
            showError("Decryption Error", e.getMessage());
        } finally {
            progressBar.setIndeterminate(false);
        }
    }

    private void logOperation(OperationRecord record) {
        operationDAO.logOperation(record);
        SwingUtilities.invokeLater(this::loadHistory);
    }

    private void loadHistory() {
        historyModel.setRowCount(0);
        for (OperationRecord r : operationDAO.getRecentOperations(50)) {
            historyModel.addRow(new Object[]{
                r.timestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                r.operationType(), r.fileName(), r.encryptionMethod(), r.status()
            });
        }
    }

    private JPanel createCard(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setBackground(new Color(50, 50, 60));
        return panel;
    }

    private void updateProgress(String message) {
        progressBar.setIndeterminate(true);
        progressBar.setString(message);
    }

    private void updateStatus(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    private void showError(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    private void setupKeyBindings() {
        getRootPane().getInputMap().put(KeyStroke.getKeyStroke("control O"), "openFile");
        getRootPane().getActionMap().put("openFile", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectFile();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
