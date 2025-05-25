package com.encryptor.service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CryptXpress Encryption Service
 * Provides secure file encryption and decryption using various algorithms
 */
public class EncryptionService {
    private static final String CBC_PADDING = "/CBC/PKCS5Padding";
    private static final SecureRandom secureRandom = new SecureRandom();

    public enum EncryptionMethod {
        AES("AES", 256, "Advanced Encryption Standard - Most Secure"),
        BLOWFISH("Blowfish", 128, "Blowfish Algorithm - Fast and Secure"),
        DES("DES", 56, "Data Encryption Standard - Legacy Support");

        private final String algorithm;
        private final int keySize;
        private final String description;

        EncryptionMethod(String algorithm, int keySize, String description) {
            this.algorithm = algorithm;
            this.keySize = keySize;
            this.description = description;
        }

        public String getAlgorithm() { 
            return algorithm; 
        }
        
        public int getKeySize() { 
            return keySize; 
        }
        
        public String getDescription() { 
            return description; 
        }

        public String getTransformation() {
            return algorithm + CBC_PADDING;
        }
        
        @Override
        public String toString() {
            return algorithm + " (" + keySize + "-bit)";
        }
    }

    public static class EncryptionResult {
        private final byte[] encryptedData;
        private final String key;
        private final EncryptionMethod method;

        public EncryptionResult(byte[] encryptedData, String key, EncryptionMethod method) {
            this.encryptedData = encryptedData;
            this.key = key;
            this.method = method;
        }

        public byte[] getEncryptedData() { 
            return encryptedData; 
        }
        
        public String getKey() { 
            return key; 
        }
        
        public EncryptionMethod getMethod() { 
            return method; 
        }
    }

    /**
     * Encrypts a file using the specified encryption method
     */
    public EncryptionResult encryptFile(File file, EncryptionMethod method) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist or is null");
        }
        
        if (!file.canRead()) {
            throw new IllegalArgumentException("Cannot read the specified file");
        }

        byte[] fileData = Files.readAllBytes(file.toPath());
        return encryptFile(fileData, method);
    }

    /**
     * Encrypts byte array data using the specified encryption method
     */
    public EncryptionResult encryptFile(byte[] fileData, EncryptionMethod method) throws Exception {
        if (fileData == null || fileData.length == 0) {
            throw new IllegalArgumentException("No data to encrypt");
        }

        try {
            // Generate secure key
            KeyGenerator keyGen = KeyGenerator.getInstance(method.getAlgorithm());
            keyGen.init(method.getKeySize(), secureRandom);
            SecretKey secretKey = keyGen.generateKey();

            // Initialize cipher with IV
            Cipher cipher = Cipher.getInstance(method.getTransformation());
            byte[] iv = new byte[cipher.getBlockSize()];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            // Encrypt data
            byte[] encrypted = cipher.doFinal(fileData);
            
            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            // Return result with Base64 encoded key
            return new EncryptionResult(
                combined,
                Base64.getEncoder().encodeToString(secretKey.getEncoded()),
                method
            );
            
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("Encryption algorithm not supported: " + method.getAlgorithm());
        } catch (NoSuchPaddingException e) {
            throw new Exception("Padding scheme not supported for: " + method.getAlgorithm());
        } catch (Exception e) {
            throw new Exception("Encryption failed: " + e.getMessage());
        }
    }

    /**
     * Decrypts encrypted data using the provided key and method
     */
    public byte[] decryptFile(byte[] encryptedData, String base64Key, EncryptionMethod method) throws Exception {
        if (encryptedData == null || encryptedData.length == 0) {
            throw new IllegalArgumentException("No encrypted data provided");
        }

        if (base64Key == null || base64Key.trim().isEmpty()) {
            throw new IllegalArgumentException("Decryption key is required");
        }

        try {
            // Validate and decode key from Base64
            if (!isValidKey(base64Key, method)) {
                throw new IllegalArgumentException("Invalid decryption key format or size for " + method.getAlgorithm());
            }

            byte[] keyBytes = Base64.getDecoder().decode(base64Key.trim());
            SecretKeySpec key = new SecretKeySpec(keyBytes, method.getAlgorithm());

            // Initialize cipher and extract IV
            Cipher cipher = Cipher.getInstance(method.getTransformation());
            int blockSize = cipher.getBlockSize();
            
            if (encryptedData.length < blockSize) {
                throw new IllegalArgumentException("Invalid encrypted data format - data too short");
            }

            // Extract IV from the beginning of encrypted data
            byte[] iv = new byte[blockSize];
            System.arraycopy(encryptedData, 0, iv, 0, iv.length);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Initialize cipher for decryption
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            
            // Decrypt the remaining data (after IV)
            return cipher.doFinal(encryptedData, blockSize, encryptedData.length - blockSize);
            
        } catch (IllegalArgumentException e) {
            throw e; // Re-throw argument exceptions as-is
        } catch (Exception e) {
            throw new Exception("Decryption failed - please check your key and file: " + e.getMessage());
        }
    }

    /**
     * Validates if a Base64 key is valid for the given encryption method
     */
    public static boolean isValidKey(String base64Key, EncryptionMethod method) {
        if (base64Key == null || base64Key.trim().isEmpty()) {
            return false;
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key.trim());
            int expectedKeySize = method.getKeySize() / 8; // Convert bits to bytes
            return keyBytes.length == expectedKeySize;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generates a random key for the specified encryption method
     */
    public static String generateKey(EncryptionMethod method) throws Exception {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(method.getAlgorithm());
            keyGen.init(method.getKeySize(), secureRandom);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new Exception("Failed to generate key for " + method.getAlgorithm() + ": " + e.getMessage());
        }
    }

    /**
     * Gets information about available encryption methods
     */
    public static String getMethodInfo(EncryptionMethod method) {
        return String.format("%s\nKey Size: %d bits\nDescription: %s", 
            method.getAlgorithm(), 
            method.getKeySize(), 
            method.getDescription());
    }

    /**
     * Estimates the strength of an encryption method
     */
    public static String getSecurityLevel(EncryptionMethod method) {
        switch (method) {
            case AES:
                return "Military Grade";
            case BLOWFISH:
                return "Commercial Grade";
            case DES:
                return "Legacy (Not Recommended)";
            default:
                return "Unknown";
        }
    }

    /**
     * Checks if the encryption service is properly initialized
     */
    public static boolean isServiceAvailable() {
        try {
            // Test if crypto services are available
            KeyGenerator.getInstance("AES");
            Cipher.getInstance("AES/CBC/PKCS5Padding");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the maximum file size that can be processed (in bytes)
     * Returns -1 for no limit
     */
    public static long getMaxFileSize() {
        // For most systems, we can handle files up to available memory
        // Set a reasonable limit of 100MB for safety
        return 100 * 1024 * 1024; // 100MB
    }

    /**
     * Validates if a file can be encrypted/decrypted
     */
    public static void validateFile(File file) throws Exception {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getName());
        }
        
        if (!file.isFile()) {
            throw new IllegalArgumentException("Path is not a file: " + file.getName());
        }
        
        if (!file.canRead()) {
            throw new IllegalArgumentException("Cannot read file: " + file.getName());
        }
        
        long maxSize = getMaxFileSize();
        if (maxSize > 0 && file.length() > maxSize) {
            throw new IllegalArgumentException("File too large. Maximum size: " + (maxSize / (1024 * 1024)) + "MB");
        }
        
        if (file.length() == 0) {
            throw new IllegalArgumentException("File is empty: " + file.getName());
        }
    }
}
