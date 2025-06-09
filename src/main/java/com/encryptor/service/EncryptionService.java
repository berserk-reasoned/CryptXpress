package com.encryptor.service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Main encryption service providing file encryption/decryption capabilities.
 * Supports AES-256, Blowfish-128, and ChaCha20 algorithms.
 */
public class EncryptionService {

    private static final int AES_KEY_SIZE = 256;
    private static final int BLOWFISH_KEY_SIZE = 128;
    private static final int AES_IV_SIZE = 16;
    private static final int BLOWFISH_IV_SIZE = 8;
    private static final int CHACHA20_NONCE_SIZE = 12;
    private static final int CHACHA20_KEY_SIZE = 32;

    public enum EncryptionMethod {
        AES_256("AES-256"),
        BLOWFISH_128("BLOWFISH-128"),
        CHACHA20("CHACHA20");

        private final String displayName;

        EncryptionMethod(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static class EncryptionResult {
        private final Path encryptedFile;
        private final String base64Key;
        private final byte[] iv;

        public EncryptionResult(Path encryptedFile, String base64Key, byte[] iv) {
            this.encryptedFile = encryptedFile;
            this.base64Key = base64Key;
            this.iv = iv;
        }

        public Path getEncryptedFile() { return encryptedFile; }
        public String getBase64Key() { return base64Key; }
        public byte[] getIv() { return iv; }
    }

    public static class CryptoException extends Exception {
        public CryptoException(String message, Throwable cause) {
            super(message, cause);
        }
        public CryptoException(String message) {
            super(message);
        }
    }

    /**
     * Encrypts a file using the specified encryption method.
     */
    public EncryptionResult encryptFile(Path inputFile, EncryptionMethod method)
            throws CryptoException, IOException {

        if (!Files.exists(inputFile)) {
            throw new CryptoException("Input file does not exist: " + inputFile);
        }

        try {
            SecretKey key = generateKey(method);
            byte[] iv = generateIV(method);
            Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, method, key, iv);

            Path encryptedFile = FileEncryptor.encrypt(inputFile, cipher);
            String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

            return new EncryptionResult(encryptedFile, base64Key, iv);

        } catch (Exception e) {
            throw new CryptoException("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts a file using the provided key and method.
     */
    public Path decryptFile(Path inputFile, String base64Key, EncryptionMethod method)
            throws CryptoException, IOException {

        if (!Files.exists(inputFile)) {
            throw new CryptoException("Input file does not exist: " + inputFile);
        }

        if (base64Key == null || base64Key.trim().isEmpty()) {
            throw new CryptoException("Decryption key cannot be empty");
        }

        try {
            SecretKey key = decodeKey(base64Key, method);

            // For decryption, we need to extract the IV from the encrypted file
            byte[] fileData = Files.readAllBytes(inputFile);
            int ivSize = getIVSize(method);

            if (fileData.length < ivSize) {
                throw new CryptoException("Invalid encrypted file format");
            }

            byte[] iv = new byte[ivSize];
            System.arraycopy(fileData, 0, iv, 0, ivSize);

            // Create a temporary file with just the encrypted data (without IV)
            byte[] encryptedData = new byte[fileData.length - ivSize];
            System.arraycopy(fileData, ivSize, encryptedData, 0, encryptedData.length);

            Path tempEncryptedFile = Files.createTempFile("temp_encrypted", ".tmp");
            Files.write(tempEncryptedFile, encryptedData);

            Cipher cipher = initCipher(Cipher.DECRYPT_MODE, method, key, iv);
            Path decryptedFile = FileEncryptor.decrypt(tempEncryptedFile, cipher);

            // Clean up temp file
            Files.deleteIfExists(tempEncryptedFile);

            return decryptedFile;

        } catch (Exception e) {
            throw new CryptoException("Decryption failed: " + e.getMessage(), e);
        }
    }

    private SecretKey generateKey(EncryptionMethod method) throws Exception {
        return switch (method) {
            case AES_256 -> {
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(AES_KEY_SIZE);
                yield keyGen.generateKey();
            }
            case BLOWFISH_128 -> {
                KeyGenerator keyGen = KeyGenerator.getInstance("Blowfish");
                keyGen.init(BLOWFISH_KEY_SIZE);
                yield keyGen.generateKey();
            }
            case CHACHA20 -> {
                byte[] keyBytes = new byte[CHACHA20_KEY_SIZE];
                new SecureRandom().nextBytes(keyBytes);
                yield new SecretKeySpec(keyBytes, "ChaCha20");
            }
        };
    }

    private byte[] generateIV(EncryptionMethod method) {
        int size = getIVSize(method);
        byte[] iv = new byte[size];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private int getIVSize(EncryptionMethod method) {
        return switch (method) {
            case AES_256 -> AES_IV_SIZE;
            case BLOWFISH_128 -> BLOWFISH_IV_SIZE;
            case CHACHA20 -> CHACHA20_NONCE_SIZE;
        };
    }

    private Cipher initCipher(int mode, EncryptionMethod method, SecretKey key, byte[] iv) throws Exception {
        return switch (method) {
            case AES_256 -> {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(mode, key, ivSpec);
                yield cipher;
            }
            case BLOWFISH_128 -> {
                Cipher cipher = Cipher.getInstance("Blowfish/CBC/PKCS5Padding");
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(mode, key, ivSpec);
                yield cipher;
            }
            case CHACHA20 -> {
                Cipher cipher = Cipher.getInstance("ChaCha20");
                ChaCha20ParameterSpec spec = new ChaCha20ParameterSpec(iv, 1);
                cipher.init(mode, key, spec);
                yield cipher;
            }
        };
    }

    private SecretKey decodeKey(String base64Key, EncryptionMethod method) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        String algorithm = switch (method) {
            case AES_256 -> "AES";
            case BLOWFISH_128 -> "Blowfish";
            case CHACHA20 -> "ChaCha20";
        };
        return new SecretKeySpec(keyBytes, algorithm);
    }
}
