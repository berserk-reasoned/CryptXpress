-- setup.sql

CREATE DATABASE IF NOT EXISTS file_encryptor_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE file_encryptor_db;

CREATE TABLE IF NOT EXISTS operation_history (
                                                 id INT AUTO_INCREMENT PRIMARY KEY,
                                                 file_name VARCHAR(255) NOT NULL,
    original_path VARCHAR(500) NOT NULL,
    operation_type ENUM('ENCRYPT', 'DECRYPT') NOT NULL,
    encryption_method ENUM('AES-256', 'BLOWFISH-128', 'CHACHA20') NOT NULL,
    timestamp DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    file_size BIGINT UNSIGNED,
    status ENUM('SUCCESS', 'FAILED') DEFAULT 'SUCCESS',
    error_message VARCHAR(1000),
    INDEX idx_timestamp (timestamp)
    );

CREATE USER IF NOT EXISTS 'cryptxpress'@'localhost'
    IDENTIFIED WITH caching_sha2_password BY 'SecurePass123!';

GRANT INSERT, SELECT ON file_encryptor_db.operation_history TO 'cryptxpress'@'localhost';
