package com.encryptor.service;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;

/**
 * Utility class to manage cipher-based file encryption and decryption.
 */
public final class FileEncryptor {

    private FileEncryptor() {
        // Prevent instantiation
    }

    /**
     * Encrypts a file using a provided cipher and writes output to a new file.
     * @param inputPath the original file to encrypt
     * @param cipher initialized encryption cipher
     * @return path to the new encrypted file
     * @throws IOException if I/O fails
     */
    public static Path encrypt(Path inputPath, Cipher cipher) throws IOException {
        Path outputPath = getOutputPath(inputPath, ".cryptx");

        try (InputStream in = Files.newInputStream(inputPath);
             OutputStream out = new CipherOutputStream(Files.newOutputStream(outputPath), cipher)) {
            in.transferTo(out);
        }

        return outputPath;
    }

    /**
     * Decrypts a file using a provided cipher and writes output to a new file.
     * @param inputPath the encrypted file to decrypt
     * @param cipher initialized decryption cipher
     * @return path to the new decrypted file
     * @throws IOException if I/O fails
     */
    public static Path decrypt(Path inputPath, Cipher cipher) throws IOException {
        Path outputPath = getOutputPath(inputPath, ".decrypted");

        try (InputStream in = new CipherInputStream(Files.newInputStream(inputPath), cipher);
             OutputStream out = Files.newOutputStream(outputPath)) {
            in.transferTo(out);
        }

        return outputPath;
    }

    /**
     * Generates an output path with the given suffix.
     */
    private static Path getOutputPath(Path original, String suffix) {
        return original.resolveSibling(original.getFileName().toString() + suffix);
    }
}
