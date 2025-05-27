package com.encryptor.model;

import java.time.LocalDateTime;

public record OperationRecord(
    String fileName,
    String originalPath,
    String operationType,
    String encryptionMethod,
    long fileSize,
    String status,
    String errorMessage,
    LocalDateTime timestamp
) {
    public OperationRecord(String fileName, String originalPath, String operationType, 
                          String encryptionMethod, long fileSize, String status, 
                          String errorMessage) {
        this(fileName, originalPath, operationType, encryptionMethod, fileSize, 
            status, errorMessage, LocalDateTime.now());
    }
}
