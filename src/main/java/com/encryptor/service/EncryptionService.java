package com.encryptor.service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionService {
    private static final String CBC_PADDING = "/CBC/PKCS5Padding";
    private static final SecureRandom secureRandom = new SecureRandom();

    public enum EncryptionMethod {
        AES("AES", 256),
        DES("DES", 56),
        BLOWFISH("Blowfish", 128);

        private final String algorithm;
        private final int keySize;

        EncryptionMethod(String algorithm, int keySize) {
            this.algorithm = algorithm;
            this.keySize = keySize;
        }

        public String getTransformation() {
            return algorithm + CBC_PADDING;
        }
    }

    public static class EncryptionResult {
        private final byte[] encryptedData;
        private final String key;

        public EncryptionResult(byte[] encryptedData, String key) {
            this.encryptedData = encryptedData;
            this.key = key;
        }

        public byte[] getEncryptedData() { return encryptedData; }
        public String getKey() { return key; }
    }

    public EncryptionResult encryptFile(byte[] fileData, EncryptionMethod method) throws Exception {
        try {
            // Generate secure key
            KeyGenerator keyGen = KeyGenerator.getInstance(method.algorithm);
            keyGen.init(method.keySize, secureRandom);
            SecretKey secretKey = keyGen.generateKey();

            // Initialize cipher with IV
            Cipher cipher = Cipher.getInstance(method.getTransformation());
            byte[] iv = new byte[cipher.getBlockSize()];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            // Encrypt data with prepended IV
            byte[] encrypted = cipher.doFinal(fileData);
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return new EncryptionResult(
                combined,
                Base64.getEncoder().encodeToString(secretKey.getEncoded())
            );
        } catch (Exception e) {
            throw new Exception("Encryption failed: " + e.getMessage());
        }
    }

    public byte[] decryptFile(byte[] encryptedData, String base64Key, EncryptionMethod method) throws Exception {
        try {
            // Decode key from Base64
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            SecretKeySpec key = new SecretKeySpec(keyBytes, method.algorithm);

            // Extract IV from encrypted data
            Cipher cipher = Cipher.getInstance(method.getTransformation());
            int blockSize = cipher.getBlockSize();
            
            if (encryptedData.length < blockSize) {
                throw new IllegalArgumentException("Invalid encrypted data format");
            }

            byte[] iv = new byte[blockSize];
            System.arraycopy(encryptedData, 0, iv, 0, iv.length);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Decrypt remaining data
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            return cipher.doFinal(encryptedData, blockSize, encryptedData.length - blockSize);
        } catch (Exception e) {
            throw new Exception("Decryption failed: " + e.getMessage());
        }
    }

    // Additional helper method for key validation
    public static boolean isValidKey(String base64Key, EncryptionMethod method) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            return keyBytes.length * 8 == method.keySize; // Convert bytes to bits
        } catch (Exception e) {
            return false;
        }
    }
}
