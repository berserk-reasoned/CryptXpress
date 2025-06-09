package com.encryptor.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Enhanced immutable record for storing encryption/decryption operation details.
 * Includes factory methods for easy creation and better error handling.
 */
public record OperationRecord(
        String fileName,
        String originalPath,
        String operationType, // ENCRYPT or DECRYPT
        String encryptionMethod, // AES-256, BLOWFISH-128, CHACHA20
        long fileSize,
        String status, // SUCCESS or FAILED
        String errorMessage,
        LocalDateTime timestamp
) {

    // Constructor with current timestamp
    public OperationRecord(
            String fileName,
            String originalPath,
            String operationType,
            String encryptionMethod,
            long fileSize,
            String status,
            String errorMessage
    ) {
        this(fileName, originalPath, operationType, encryptionMethod,
                fileSize, status, errorMessage, LocalDateTime.now());
    }

    // Factory method for successful encryption
    public static OperationRecord encrypt(Path filePath, String method, String status, String errorMessage) {
        long fileSize = 0;
        try {
            fileSize = Files.size(filePath);
        } catch (Exception e) {
            // Use 0 if file size cannot be determined
        }

        return new OperationRecord(
                filePath.getFileName().toString(),
                filePath.toString(),
                "ENCRYPT",
                method,
                fileSize,
                status,
                errorMessage
        );
    }

    // Factory method for successful decryption
    public static OperationRecord decrypt(Path filePath, String method, String status, String errorMessage) {
        long fileSize = 0;
        try {
            fileSize = Files.size(filePath);
        } catch (Exception e) {
            // Use 0 if file size cannot be determined
        }

        return new OperationRecord(
                filePath.getFileName().toString(),
                filePath.toString(),
                "DECRYPT",
                method,
                fileSize,
                status,
                errorMessage
        );
    }

    // Validation method
    public boolean isValid() {
        return fileName != null && !fileName.isBlank() &&
                originalPath != null && !originalPath.isBlank() &&
                operationType != null && (operationType.equals("ENCRYPT") || operationType.equals("DECRYPT")) &&
                encryptionMethod != null && !encryptionMethod.isBlank() &&
                status != null && (status.equals("SUCCESS") || status.equals("FAILED")) &&
                timestamp != null;
    }
}
