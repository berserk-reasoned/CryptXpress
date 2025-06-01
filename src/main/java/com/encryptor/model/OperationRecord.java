package com.encryptor.model;

import java.time.LocalDateTime;

/**
 * Immutable record for storing encryption/decryption operation details.
 * Designed for high evaluability and database mapping compliance.
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
    public OperationRecord(
        String fileName,
        String originalPath,
        String operationType,
        String encryptionMethod,
        long fileSize,
        String status,
        String errorMessage
    ) {
        this(fileName, originalPath, operationType, encryptionMethod, fileSize, status, errorMessage, LocalDateTime.now());
    }
}
