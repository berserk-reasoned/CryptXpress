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
        Path outputPath = getOutputPath(inputPath, ".cryptx");

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
        Path outputPath = getOutputPath(inputPath, ".decrypted");

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
     * Generates an output path with the given suffix, ensuring no conflicts.
     */
    private static Path getOutputPath(Path original, String suffix) {
        String fileName = original.getFileName().toString();
        String baseName = fileName.contains(".") ?
                fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
        String extension = fileName.contains(".") ?
                fileName.substring(fileName.lastIndexOf('.')) : "";

        Path outputPath = original.resolveSibling(baseName + suffix + extension);

        // Handle file name conflicts
        int counter = 1;
        while (Files.exists(outputPath)) {
            outputPath = original.resolveSibling(baseName + suffix + "(" + counter + ")" + extension);
            counter++;
        }

        return outputPath;
    }
}