package com.encryptor.model;

import java.time.LocalDateTime;

public class OperationRecord {
    private int id;
    private String fileName;
    private String originalPath;
    private String operationType;
    private String encryptionMethod;
    private LocalDateTime timestamp;
    private long fileSize;
    private String status;
    
    // Constructors
    public OperationRecord() {}
    
    public OperationRecord(String fileName, String originalPath, String operationType, 
                          String encryptionMethod, long fileSize, String status) {
        this.fileName = fileName;
        this.originalPath = originalPath;
        this.operationType = operationType;
        this.encryptionMethod = encryptionMethod;
        this.fileSize = fileSize;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getOriginalPath() { return originalPath; }
    public void setOriginalPath(String originalPath) { this.originalPath = originalPath; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    public String getEncryptionMethod() { return encryptionMethod; }
    public void setEncryptionMethod(String encryptionMethod) { this.encryptionMethod = encryptionMethod; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
