package com.encryptor.service;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;

/**
 * Enhanced file encryption/decryption utility with IV handling.
 * Prepends IV to encrypted files for proper decryption.
 */
public final class FileEncryptor {

    private FileEncryptor() {
        // Prevent instantiation
    }

    /**
     * Encrypts a file using a provided cipher and writes output to a new file.
     * The IV is prepended to the encrypted file for later decryption.
     *
     * @param inputPath the original file to encrypt
     * @param cipher initialized encryption cipher
     * @return path to the new encrypted file
     * @throws IOException if I/O fails
     */
    public static Path encrypt(Path inputPath, Cipher cipher) throws IOException {
        Path outputPath = getEncryptedOutputPath(inputPath);

        try (InputStream in = Files.newInputStream(inputPath);
             OutputStream out = Files.newOutputStream(outputPath)) {

            // First, write the IV to the output file
            byte[] iv = cipher.getIV();
            if (iv != null) {
                out.write(iv);
            }

            // Then encrypt and write the file content
            try (CipherOutputStream cipherOut = new CipherOutputStream(out, cipher)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    cipherOut.write(buffer, 0, bytesRead);
                }
            }
        }

        return outputPath;
    }

    /**
     * Decrypts a file using a provided cipher and writes output to a new file.
     *
     * @param inputPath the encrypted file to decrypt (without IV prefix)
     * @param cipher initialized decryption cipher
     * @return path to the new decrypted file
     * @throws IOException if I/O fails
     */
    public static Path decrypt(Path inputPath, Cipher cipher) throws IOException {
        Path outputPath = getDecryptedOutputPath(inputPath);

        try (InputStream in = new CipherInputStream(Files.newInputStream(inputPath), cipher);
             OutputStream out = Files.newOutputStream(outputPath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        return outputPath;
    }

    /**
     * Generates output path for encrypted files with Cryptxpress naming convention.
     * Format: originalName_Cryptxpress_Encrypted.extension
     */
    private static Path getEncryptedOutputPath(Path originalFile) {
        String fileName = originalFile.getFileName().toString();
        String nameWithoutExtension = fileName.contains(".") ?
                fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
        String extension = fileName.contains(".") ?
                fileName.substring(fileName.lastIndexOf('.')) : "";

        String encryptedFileName = nameWithoutExtension + "_Cryptxpress_Encrypted" + extension;
        Path outputPath = originalFile.getParent().resolve(encryptedFileName);

        // Handle file name conflicts
        int counter = 1;
        while (Files.exists(outputPath)) {
            String conflictFileName = nameWithoutExtension + "_Cryptxpress_Encrypted(" + counter + ")" + extension;
            outputPath = originalFile.getParent().resolve(conflictFileName);
            counter++;
        }

        return outputPath;
    }

    /**
     * Generates output path for decrypted files with Cryptxpress naming convention.
     * Format: originalName_Cryptxpress_Decrypted.extension
     * If the file was encrypted with our convention, it tries to restore the original name structure.
     */
    private static Path getDecryptedOutputPath(Path encryptedFile) {
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
}
