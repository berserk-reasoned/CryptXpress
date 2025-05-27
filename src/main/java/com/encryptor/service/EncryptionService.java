package com.encryptor.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

public class EncryptionService {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public enum EncryptionMethod {
        AES_256("AES", 256, 16, "AES/CBC/PKCS7Padding"),
        BLOWFISH_128("Blowfish", 128, 8, "Blowfish/CBC/PKCS7Padding"),
        CHACHA20("ChaCha20", 256, 12, "ChaCha20-Poly1305");

        public final String algorithm;
        public final int keySize;
        public final int ivSize;
        public final String transformation;

        EncryptionMethod(String algorithm, int keySize, int ivSize, String transformation) {
            this.algorithm = algorithm;
            this.keySize = keySize;
            this.ivSize = ivSize;
            this.transformation = transformation;
        }
    }

    public static class CryptoResult {
        private final String key;
        private final byte[] iv;

        public CryptoResult(String key, byte[] iv) {
            this.key = key;
            this.iv = iv;
        }

        public String getCombinedKey() {
            return Base64.getEncoder().encodeToString(iv) + ":" + key;
        }
    }

    public CryptoResult encryptFile(Path input, EncryptionMethod method) throws CryptoException {
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            byte[] iv = new byte[method.ivSize];
            random.nextBytes(iv);

            KeyGenerator keyGen = KeyGenerator.getInstance(method.algorithm);
            keyGen.init(method.keySize, random);
            SecretKey key = keyGen.generateKey();

            Cipher cipher = Cipher.getInstance(method.transformation, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

            try (InputStream in = Files.newInputStream(input);
                 CipherOutputStream out = new CipherOutputStream(
                     Files.newOutputStream(getOutputPath(input, true)), cipher)) {
                in.transferTo(out);
            }

            return new CryptoResult(
                Base64.getEncoder().encodeToString(key.getEncoded()),
                iv
            );
        } catch (Exception e) {
            throw new CryptoException("Encryption failed: " + e.getMessage(), e);
        }
    }

    public Path decryptFile(Path input, String combinedKey, EncryptionMethod method) 
            throws CryptoException {
        try {
            String[] parts = combinedKey.split(":");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid key format");
            
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] keyBytes = Base64.getDecoder().decode(parts[1]);
            
            SecretKey key = new SecretKeySpec(keyBytes, method.algorithm);
            Cipher cipher = Cipher.getInstance(method.transformation, "BC");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

            try (CipherInputStream in = new CipherInputStream(
                     Files.newInputStream(input), cipher);
                 OutputStream out = Files.newOutputStream(getOutputPath(input, false))) {
                in.transferTo(out);
            }

            return getOutputPath(input, false);
        } catch (Exception e) {
            throw new CryptoException("Decryption failed: " + e.getMessage(), e);
        }
    }

    private Path getOutputPath(Path original, boolean encrypt) {
        String suffix = encrypt ? ".cryptx" : ".decrypted";
        return original.resolveSibling(
            original.getFileName() + suffix);
    }

    public static class CryptoException extends Exception {
        public CryptoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
