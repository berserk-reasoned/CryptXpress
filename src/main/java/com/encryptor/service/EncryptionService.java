package com.encryptor.service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
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
        keyGen.init(getKeySize(method), new SecureRandom());
        SecretKey secretKey = keyGen.generateKey();
        
        String transformation = getTransformation(method);
        Cipher cipher = Cipher.getInstance(transformation);
        
        byte[] iv = null;
        if (transformation.contains("CBC")) {
            iv = new byte[cipher.getBlockSize()];
            new SecureRandom().nextBytes(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        }
        
        byte[] encryptedData = cipher.doFinal(fileData);
        
        // Prepend IV to encrypted data if using CBC
        if (iv != null) {
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            encryptedData = combined;
        }
        
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        return new EncryptionResult(encryptedData, encodedKey);
    }
    
    public byte[] decryptFile(byte[] encryptedData, String encodedKey, EncryptionMethod method) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKey secretKey = new SecretKeySpec(decodedKey, method.getAlgorithm());
        
        String transformation = getTransformation(method);
        Cipher cipher = Cipher.getInstance(transformation);
        
        if (transformation.contains("CBC")) {
            int blockSize = cipher.getBlockSize();
            byte[] iv = new byte[blockSize];
            System.arraycopy(encryptedData, 0, iv, 0, blockSize);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return cipher.doFinal(encryptedData, blockSize, encryptedData.length - blockSize);
        }
        
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedData);
    }
    
    private String getTransformation(EncryptionMethod method) {
        switch (method) {
            case AES: return "AES/CBC/PKCS5Padding";
            case DES: return "DES/ECB/PKCS5Padding";
            case BLOWFISH: return "Blowfish/CBC/PKCS5Padding";
            default: return method.getAlgorithm() + "/CBC/PKCS5Padding";
        }
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
