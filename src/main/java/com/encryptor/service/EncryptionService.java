package com.encryptor.service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.crypto.CipherInputStream;

/**
 * Main encryption service providing file encryption/decryption capabilities.
 * Supports AES-256 and Blowfish-128 algorithms.
 */
public class EncryptionService {

    private static final int AES_KEY_SIZE = 256;
    private static final int BLOWFISH_KEY_SIZE = 128;
    private static final int AES_IV_SIZE = 16;
    private static final int BLOWFISH_IV_SIZE = 8;

    public enum EncryptionMethod {
        AES_256("AES-256"),
        BLOWFISH_128("BLOWFISH-128");

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

        SecretKey key = null;
        byte[] iv = null;

        try {
            key = generateKey(method);
            iv = generateIV(method);
            Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, method, key, iv);

            Path encryptedFile = FileEncryptor.encrypt(inputFile, cipher);
            String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

            return new EncryptionResult(encryptedFile, base64Key, iv);

        } catch (Exception e) {
            throw new CryptoException("Encryption failed: " + e.getMessage(), e);
        } finally {
            // Securely wipe key from memory
            if (key != null) {
                CryptoUtils.wipe(key.getEncoded());
            }
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

        byte[] keyBytes = null;

        try {
            SecretKey key = decodeKey(base64Key, method);
            keyBytes = key.getEncoded();

            // Extract IV and encrypted data from file
            byte[] fileData = Files.readAllBytes(inputFile);
            int ivSize = getIVSize(method);

            if (fileData.length < ivSize) {
                throw new CryptoException("Invalid encrypted file format - file too small");
            }

            byte[] iv = new byte[ivSize];
            System.arraycopy(fileData, 0, iv, 0, ivSize);

            // Get the encrypted data (without IV)
            byte[] encryptedData = new byte[fileData.length - ivSize];
            System.arraycopy(fileData, ivSize, encryptedData, 0, encryptedData.length);

            // Initialize cipher for decryption
            Cipher cipher = initCipher(Cipher.DECRYPT_MODE, method, key, iv);

            // Generate output path based on input file name (not temp file)
            Path decryptedFilePath = generateDecryptedOutputPath(inputFile);

            // Decrypt directly to the final output file
            try (InputStream encryptedIn = new java.io.ByteArrayInputStream(encryptedData);
                 javax.crypto.CipherInputStream cipherIn = new javax.crypto.CipherInputStream(encryptedIn, cipher);
                 OutputStream out = Files.newOutputStream(decryptedFilePath)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = cipherIn.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            return decryptedFilePath;

        } catch (Exception e) {
            throw new CryptoException("Decryption failed: " + e.getMessage(), e);
        } finally {
            if (keyBytes != null) {
                CryptoUtils.wipe(keyBytes);
            }
        }
    }

    /**
     * Generates output path for decrypted files with Cryptxpress naming convention.
     * This method should be added to EncryptionService class.
     */
    private Path generateDecryptedOutputPath(Path encryptedFile) {
        String fileName = encryptedFile.getFileName().toString();

        // Check if this file follows our encrypted naming convention
        if (fileName.contains("_Cryptxpress_Encrypted")) {
            // Extract the original name and replace with decrypted suffix
            String decryptedFileName = fileName.replace("_Cryptxpress_Encrypted", "_Cryptxpress_Decrypted");
            Path outputPath = encryptedFile.getParent().resolve(decryptedFileName);

            // Handle conflicts
            int counter = 1;
            while (Files.exists(outputPath)) {
                String baseDecryptedName = fileName.replace("_Cryptxpress_Encrypted", "_Cryptxpress_Decrypted");
                String nameWithoutExt = baseDecryptedName.contains(".") ?
                        baseDecryptedName.substring(0, baseDecryptedName.lastIndexOf('.')) : baseDecryptedName;
                String extension = baseDecryptedName.contains(".") ?
                        baseDecryptedName.substring(baseDecryptedName.lastIndexOf('.')) : "";

                String conflictFileName = nameWithoutExt + "(" + counter + ")" + extension;
                outputPath = encryptedFile.getParent().resolve(conflictFileName);
                counter++;
            }

            return outputPath;
        } else {
            // For files that don't follow our convention, add the decrypted suffix
            String nameWithoutExtension = fileName.contains(".") ?
                    fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
            String extension = fileName.contains(".") ?
                    fileName.substring(fileName.lastIndexOf('.')) : "";

            String decryptedFileName = nameWithoutExtension + "_Cryptxpress_Decrypted" + extension;
            Path outputPath = encryptedFile.getParent().resolve(decryptedFileName);

            // Handle conflicts
            int counter = 1;
            while (Files.exists(outputPath)) {
                String conflictFileName = nameWithoutExtension + "_Cryptxpress_Decrypted(" + counter + ")" + extension;
                outputPath = encryptedFile.getParent().resolve(conflictFileName);
                counter++;
            }

            return outputPath;
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
        };
    }

    private byte[] generateIV(EncryptionMethod method) {
        int size = getIVSize(method);
        return CryptoUtils.generateRandomBytes(size);
    }

    private int getIVSize(EncryptionMethod method) {
        return switch (method) {
            case AES_256 -> AES_IV_SIZE;
            case BLOWFISH_128 -> BLOWFISH_IV_SIZE;
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
        };
    }

    private SecretKey decodeKey(String base64Key, EncryptionMethod method) throws CryptoException {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            String algorithm = switch (method) {
                case AES_256 -> "AES";
                case BLOWFISH_128 -> "Blowfish";
            };

            // Validate key size
            int expectedSize = switch (method) {
                case AES_256 -> AES_KEY_SIZE / 8; // Convert bits to bytes
                case BLOWFISH_128 -> BLOWFISH_KEY_SIZE / 8;
            };

            if (keyBytes.length != expectedSize) {
                throw new CryptoException("Invalid key size for " + method +
                        ". Expected " + expectedSize + " bytes, got " + keyBytes.length);
            }

            return new SecretKeySpec(keyBytes, algorithm);
        } catch (IllegalArgumentException e) {
            throw new CryptoException("Invalid Base64 key format", e);
        }
    }
}
