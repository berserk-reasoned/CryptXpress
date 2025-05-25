package com.encryptor.ui;

import com.encryptor.dao.OperationDAO;
import com.encryptor.model.OperationRecord;
import com.encryptor.service.EncryptionService;
import com.encryptor.service.EncryptionService.EncryptionMethod;
import com.encryptor.service.EncryptionService.EncryptionResult;

import javax.crypto.spec.IvParameterSpec;
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
    private OvalButton selectFileBtn;
    private JComboBox<EncryptionMethod> encryptionCombo;
    private OvalButton encryptBtn;
    private OvalButton decryptBtn;
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
        setSize(1000, 800);
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
        mainPanel.setBackground(new Color(28, 28, 30));
        
        // Create header
        JPanel headerPanel = createHeaderPanel();
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(40, 40, 45));
        tabbedPane.setForeground(Color.WHITE);
        
        // Create operation panel
        operationPanel = createOperationPanel();
        tabbedPane.addTab("🔐 Operations", operationPanel);
        
        // Create history panel
        historyPanel = createHistoryPanel();
        tabbedPane.addTab("📜 History", historyPanel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel();
        header.setBackground(new Color(23, 23, 25));
        header.setBorder(new EmptyBorder(25, 40, 25, 40));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("SecureCrypt");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(0, 200, 255));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Military-Grade File Encryption");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(160, 160, 160));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitleLabel);
        
        return header;
    }
    
    private JPanel createOperationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(35, 35, 40));
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
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(35, 35, 40));
        
        contentPanel.add(fileSection);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(optionsSection);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(actionsSection);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(keySection);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(statusSection, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createFileSelectionSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.X_AXIS));
        section.setBackground(new Color(45, 45, 50));
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 65)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel iconLabel = new JLabel("📁");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        iconLabel.setForeground(new Color(180, 180, 180));
        
        fileLabel = new JLabel("No file selected");
        fileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fileLabel.setForeground(new Color(200, 200, 200));
        
        selectFileBtn = new OvalButton("Browse Files");
        selectFileBtn.setBackground(new Color(0, 120, 215));
        selectFileBtn.setForeground(Color.WHITE);
        selectFileBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(new Color(45, 45, 50));
        textPanel.add(fileLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(selectFileBtn);
        
        section.add(iconLabel);
        section.add(Box.createHorizontalStrut(20));
        section.add(textPanel);
        
        return section;
    }
    
    private JPanel createEncryptionOptionsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(45, 45, 50));
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 65)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel("🔒 Encryption Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        encryptionCombo = new JComboBox<>(EncryptionMethod.values());
        encryptionCombo.setBackground(new Color(55, 55, 60));
        encryptionCombo.setForeground(Color.WHITE);
        encryptionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        encryptionCombo.setRenderer(new ModernComboBoxRenderer());
        
        JLabel keyInputLabel = new JLabel("Decryption Key:");
        keyInputLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        keyInputLabel.setForeground(new Color(180, 180, 180));
        
        keyInputField = new JTextField();
        keyInputField.setBackground(new Color(55, 55, 60));
        keyInputField.setForeground(Color.WHITE);
        keyInputField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        keyInputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 85)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        section.add(titleLabel);
        section.add(Box.createVerticalStrut(15));
        section.add(encryptionCombo);
        section.add(Box.createVerticalStrut(15));
        section.add(keyInputLabel);
        section.add(Box.createVerticalStrut(5));
        section.add(keyInputField);
        
        return section;
    }
    
    private JPanel createActionsSection() {
        JPanel section = new JPanel();
        section.setLayout(new GridLayout(1, 2, 20, 0));
        section.setBackground(new Color(35, 35, 40));
        
        encryptBtn = new OvalButton("🔐 Encrypt");
        encryptBtn.setBackground(new Color(0, 200, 150));
        encryptBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        decryptBtn = new OvalButton("🔓 Decrypt");
        decryptBtn.setBackground(new Color(255, 95, 90));
        decryptBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        section.add(encryptBtn);
        section.add(decryptBtn);
        
        return section;
    }
    
    private JPanel createKeySection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(new Color(45, 45, 50));
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 65)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel("🔑 Encryption Key");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        keyArea = new JTextArea(3, 50);
        keyArea.setBackground(new Color(30, 30, 35));
        keyArea.setForeground(new Color(0, 255, 150));
        keyArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        keyArea.setEditable(false);
        keyArea.setLineWrap(true);
        keyArea.setWrapStyleWord(true);
        keyArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(keyArea);
        scrollPane.setBorder(null);
        
        section.add(titleLabel, BorderLayout.NORTH);
        section.add(Box.createVerticalStrut(10));
        section.add(scrollPane, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createStatusSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(new Color(35, 35, 40));
        section.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setBackground(new Color(50, 50, 55));
        progressBar.setForeground(new Color(0, 200, 255));
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
        panel.setBackground(new Color(35, 35, 40));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Table
        String[] columns = {"ID", "File Name", "Operation", "Method", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        historyTable = new JTable(tableModel);
        historyTable.setBackground(new Color(45, 45, 50));
        historyTable.setForeground(Color.WHITE);
        historyTable.setGridColor(new Color(60, 60, 65));
        historyTable.setSelectionBackground(new Color(0, 150, 255));
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.getTableHeader().setBackground(new Color(30, 30, 35));
        historyTable.getTableHeader().setForeground(Color.WHITE);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(null);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void styleComponents() {
        // Add hover effects
        addHoverEffect(selectFileBtn, new Color(0, 120, 215), new Color(0, 140, 235));
        addHoverEffect(encryptBtn, new Color(0, 200, 150), new Color(0, 220, 170));
        addHoverEffect(decryptBtn, new Color(255, 95, 90), new Color(255, 115, 110));
        
        // Add component shadows
        addComponentShadow(operationPanel);
        addComponentShadow(historyPanel);
    }
    
    private void addHoverEffect(OvalButton button, Color normal, Color hover) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hover);
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(normal);
            }
        });
    }
    
    private void addComponentShadow(JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0, 0, 0, 30)),
            component.getBorder()
        ));
    }
    
    private void setupEventHandlers() {
        selectFileBtn.addActionListener(this::selectFile);
        encryptBtn.addActionListener(this::encryptFile);
        decryptBtn.addActionListener(this::decryptFile);
    }
    
    // ... Rest of the methods (selectFile, encryptFile, decryptFile, loadHistory) remain same as previous
    // with the following changes:
    
    private void resetUI() {
        selectedFile = null;
        fileLabel.setText("No file selected");
        keyInputField.setText("");
        keyArea.setText("");
        statusLabel.setText("Ready");
        progressBar.setValue(0);
    }
    
    // Update file naming in decryptFile method:
    String originalName = selectedFile.getName();
    if (originalName.endsWith(".encrypted")) {
        originalName = originalName.substring(0, originalName.lastIndexOf(".encrypted"));
    }
}

class OvalButton extends JButton {
    public OvalButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
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

class ModernComboBoxRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
            boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(
            list, value, index, isSelected, cellHasFocus);
        
        label.setBackground(new Color(55, 55, 60));
        label.setForeground(Color.WHITE);
        label.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        if (isSelected) {
            label.setBackground(new Color(0, 120, 215));
        }
        
        return label;
    }
}
