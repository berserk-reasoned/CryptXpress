-- File Encryptor Pro Database Setup
-- Run this script in MySQL to set up the database

CREATE DATABASE IF NOT EXISTS file_encryptor_db;
USE file_encryptor_db;

CREATE TABLE IF NOT EXISTS operation_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    original_path VARCHAR(500) NOT NULL,
    operation_type ENUM('ENCRYPT', 'DECRYPT') NOT NULL,
    encryption_method VARCHAR(50) NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    file_size BIGINT,
    status ENUM('SUCCESS', 'FAILED') DEFAULT 'SUCCESS',
    INDEX idx_timestamp (timestamp),
    INDEX idx_operation_type (operation_type),
    INDEX idx_status (status)
);

-- Sample data for testing
INSERT INTO operation_history (file_name, original_path, operation_type, encryption_method, file_size, status) 
VALUES 
    ('test_document.txt', '/sample/path/test_document.txt', 'ENCRYPT', 'AES', 1024, 'SUCCESS'),
    ('image.jpg', '/sample/path/image.jpg', 'ENCRYPT', 'BLOWFISH', 2048576, 'SUCCESS'),
    ('data.xlsx', '/sample/path/data.xlsx', 'DECRYPT', 'AES', 5120, 'SUCCESS');
