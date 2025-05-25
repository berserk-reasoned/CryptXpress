package com.encryptor.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * CryptXpress Operation Record Model
 * Represents a single encryption/decryption operation for history tracking
 */
public class OperationRecord {
    private Long id;
    private String fileName;
    private String operationType;
    private String method;
    private String status;
    private LocalDateTime timestamp;

    // Constructors
    public OperationRecord() {
        this.timestamp = LocalDateTime.now();
    }

    public OperationRecord(String fileName, String operationType, String method, String status) {
        this();
        this.fileName = fileName;
        this.operationType = operationType;
        this.method = method;
        this.status = status;
    }

    public OperationRecord(Long id, String fileName, String operationType, String method, 
                          String status, LocalDateTime timestamp) {
        this.id = id;
        this.fileName = fileName;
        this.operationType = operationType;
        this.method = method;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Utility methods
    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public boolean isSuccessful() {
        return "SUCCESS".equalsIgnoreCase(status);
    }

    public boolean isEncryption() {
        return "ENCRYPT".equalsIgnoreCase(operationType);
    }

    public boolean isDecryption() {
        return "DECRYPT".equalsIgnoreCase(operationType);
    }

    @Override
    public String toString() {
        return String.format("OperationRecord{id=%d, fileName='%s', operation='%s', method='%s', status='%s', timestamp=%s}",
                id, fileName, operationType, method, status, getFormattedTimestamp());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        OperationRecord that = (OperationRecord) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
