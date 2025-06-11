package com.encryptor.ui;

import com.encryptor.dao.OperationDAO;
import com.encryptor.model.OperationRecord;
import com.encryptor.service.EncryptionService;
import com.encryptor.service.EncryptionService.CryptoException;
import com.encryptor.service.EncryptionService.EncryptionMethod;
import com.encryptor.service.EncryptionService.EncryptionResult;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

/**
 * Controller class handling all crypto operations and business logic.
 * Separated from UI to follow MVC pattern.
 */
public class CryptoController {

    public interface CryptoControllerListener {
        void onOperationStarted(String message);
        void onOperationCompleted(String message);
        void onOperationFailed(String error);
        void onKeyGenerated(String key, String message);
        void onFileSelected(Path file);
        void onHistoryLoaded(List<OperationRecord> history);
    }

    private final EncryptionService cryptoService;
    private final OperationDAO operationDAO;
    private final ExecutorService executorService;
    private final Preferences preferences;
    private CryptoControllerListener listener;

    // Current operation state
    private Path currentFile;

    public CryptoController() {
        this.cryptoService = new EncryptionService();
        this.operationDAO = new OperationDAO();
        this.executorService = Executors.newFixedThreadPool(2);
        this.preferences = Preferences.userNodeForPackage(CryptoController.class);
    }

    public void setListener(CryptoControllerListener listener) {
        this.listener = listener;
    }

    /**
     * Handles file selection with file chooser dialog
     */
    public void selectFile(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Restore last used directory
        String lastDir = preferences.get("last_directory", System.getProperty("user.home"));
        fileChooser.setCurrentDirectory(new File(lastDir));

        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile().toPath();

            // Save last used directory
            preferences.put("last_directory", currentFile.getParent().toString());

            if (listener != null) {
                listener.onFileSelected(currentFile);
            }
        }
    }

    /**
     * Performs file encryption with the specified method
     */
    public void encryptFile(EncryptionMethod method) {
        if (!validateFileSelection()) return;

        if (listener != null) {
            listener.onOperationStarted("Encrypting file...");
        }

        executorService.submit(() -> {
            try {
                // Generate proper encrypted file name
                EncryptionResult result = cryptoService.encryptFile(currentFile, method);

               // EncryptionResult result = cryptoService.encryptFile(currentFile, method, encryptedFilePath);

                // Save operation to history
                saveOperationRecord("ENCRYPT", method.toString(), currentFile,
                        result.getEncryptedFile(), true, null);

                SwingUtilities.invokeLater(() -> {
                    if (listener != null) {
                        listener.onKeyGenerated(result.getBase64Key(),
                                "File encrypted successfully!\nSaved to: " + result.getEncryptedFile().getFileName() +
                                        "\nSave this key to decrypt the file later.");
                    }
                });

            } catch (CryptoException | IOException e) {
                // Save failed operation to history
                saveOperationRecord("ENCRYPT", method.toString(), currentFile,
                        null, false, e.getMessage());

                SwingUtilities.invokeLater(() -> {
                    if (listener != null) {
                        listener.onOperationFailed("Encryption failed: " + e.getMessage());
                    }
                });
            }
        });
    }


    /**
     * Performs file decryption with the provided key and method
     */
    public void decryptFile(String base64Key, EncryptionMethod method) {
        if (!validateFileSelection()) return;

        if (base64Key == null || base64Key.trim().isEmpty()) {
            if (listener != null) {
                listener.onOperationFailed("Please enter the decryption key");
            }
            return;
        }

        if (listener != null) {
            listener.onOperationStarted("Decrypting file...");
        }

        executorService.submit(() -> {
            try {
                // Generate proper decrypted file name
              //  Path decryptedFilePath = generateDecryptedFileName(currentFile);

                Path decryptedFile = cryptoService.decryptFile(currentFile, base64Key.trim(), method);

                // Save operation to history
                saveOperationRecord("DECRYPT", method.toString(), currentFile,
                        decryptedFile, true, null);

                SwingUtilities.invokeLater(() -> {
                    if (listener != null) {
                        listener.onOperationCompleted("File decrypted successfully!\nSaved to: " +
                                decryptedFile.getFileName());
                    }
                });

            } catch (CryptoException | IOException e) {
                // Save failed operation to history
                saveOperationRecord("DECRYPT", method.toString(), currentFile,
                        null, false, e.getMessage());

                SwingUtilities.invokeLater(() -> {
                    if (listener != null) {
                        listener.onOperationFailed("Decryption failed: " + e.getMessage());
                    }
                });
            }
        });
    }
    private Path generateEncryptedFileName(Path originalFile) {
        String fileName = originalFile.getFileName().toString();
        String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));

        String encryptedFileName = nameWithoutExtension + "_Cryptxpress_Encrypted" + extension;
        return originalFile.getParent().resolve(encryptedFileName);
    }

    /**
     * Generates the decrypted file name with proper naming convention
     * Format: originalName_Cryptxpress_Decrypted.extension
     */
    private Path generateDecryptedFileName(Path originalFile) {
        String fileName = originalFile.getFileName().toString();

        // If it's already an encrypted file, remove the encrypted suffix
        if (fileName.contains("_Cryptxpress_Encrypted")) {
            fileName = fileName.replace("_Cryptxpress_Encrypted", "_Cryptxpress_Decrypted");
        } else {
            // For files that don't follow our naming convention
            String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            fileName = nameWithoutExtension + "_Cryptxpress_Decrypted" + extension;
        }

        return originalFile.getParent().resolve(fileName);
    }

    /**
     * Loads operation history from database
     */
    public void loadOperationHistory() {
        executorService.submit(() -> {
            try {
                List<OperationRecord> history = operationDAO.getAllOperations();
                SwingUtilities.invokeLater(() -> {
                    if (listener != null) {
                        listener.onHistoryLoaded(history);
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    if (listener != null) {
                        listener.onOperationFailed("Failed to load history: " + e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * Clears the currently selected file
     */
    public void clearFileSelection() {
        currentFile = null;
    }

    /**
     * Gets the currently selected file
     */
    public Path getCurrentFile() {
        return currentFile;
    }

    /**
     * Validates that a file is selected
     */
    private boolean validateFileSelection() {
        if (currentFile == null) {
            if (listener != null) {
                listener.onOperationFailed("Please select a file first");
            }
            return false;
        }
        return true;
    }

    /**
     * Saves operation record to database
     */
    /**
     * Saves operation record to database
     */
    private void saveOperationRecord(String operation, String method, Path inputFile,
                                     Path outputFile, boolean success, String errorMessage) {
        try {
            // Get file size
            long fileSize = 0;
            try {
                if (inputFile != null && Files.exists(inputFile)) {
                    fileSize = Files.size(inputFile);
                }
            } catch (Exception e) {
                // Use 0 if file size cannot be determined
            }

            // Create the record properly using the constructor
            OperationRecord record = new OperationRecord(
                    inputFile != null ? inputFile.getFileName().toString() : "unknown",
                    inputFile != null ? inputFile.toString() : "unknown",
                    operation,
                    method,
                    fileSize,
                    success ? "SUCCESS" : "FAILED",
                    errorMessage,
                    LocalDateTime.now(),
                    outputFile != null ? outputFile.toString() : null
            );

            operationDAO.saveOperation(record);
        } catch (Exception e) {
            // Log error but don't propagate - history saving shouldn't fail the main operation
            System.err.println("Failed to save operation record: " + e.getMessage());
        }
    }
    /**
     * Formats operation history for display
     */
    public String formatOperationHistory(List<OperationRecord> history) {
        if (history.isEmpty()) {
            return "No operations recorded yet.";
        }

        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (OperationRecord record : history) {
            sb.append("=".repeat(60)).append("\n");
            sb.append("Operation: ").append(record.getOperation()).append("\n");
            sb.append("Method: ").append(record.getMethod()).append("\n");
            sb.append("Input File: ").append(record.getInputFile()).append("\n");
            if (record.getOutputFile() != null) {
                sb.append("Output File: ").append(record.getOutputFile()).append("\n");
            }
            sb.append("Date: ").append(record.getTimestamp().format(formatter)).append("\n");
            sb.append("Status: ").append(record.isSuccess() ? "SUCCESS" : "FAILED").append("\n");
            if (!record.isSuccess() && record.getErrorMessage() != null) {
                sb.append("Error: ").append(record.getErrorMessage()).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Shuts down the controller and cleans up resources
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
