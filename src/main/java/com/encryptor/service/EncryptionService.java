package com.encryptor.service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionService {
    
    public enum EncryptionMethod {
        AES("AES"),
        DES("DES"),
        BLOWFISH("Blowfish");
        
        private final String algorithm;
        
        EncryptionMethod(String algorithm) {
            this.algorithm = algorithm;
        }
        
        public String getAlgorithm() {
            return algorithm;
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
    
    public EncryptionResult encryptFile(File inputFile, EncryptionMethod method) throws Exception {
        byte[] fileData = Files.readAllBytes(inputFile.toPath());
        
        KeyGenerator keyGen = KeyGenerator.getInstance(method.getAlgorithm());
        keyGen.init(getKeySize(method));
        SecretKey secretKey = keyGen.generateKey();
        
        Cipher cipher = Cipher.getInstance(method.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        byte[] encryptedData = cipher.doFinal(fileData);
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        
        return new EncryptionResult(encryptedData, encodedKey);
    }
    
    public byte[] decryptFile(byte[] encryptedData, String encodedKey, EncryptionMethod method) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKey secretKey = new SecretKeySpec(decodedKey, method.getAlgorithm());
        
        Cipher cipher = Cipher.getInstance(method.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        
        return cipher.doFinal(encryptedData);
    }
    
    private int getKeySize(EncryptionMethod method) {
        switch (method) {
            case AES: return 256;
            case DES: return 56;
            case BLOWFISH: return 128;
            default: return 128;
        }
    }
}
