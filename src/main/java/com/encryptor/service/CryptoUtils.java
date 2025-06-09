package com.encryptor.service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for key generation and Base64 encoding/decoding.
 * Keeps cryptographic utilities centralized and reusable.
 */
public final class CryptoUtils {

    private static final SecureRandom secureRandom = new SecureRandom();

    private CryptoUtils() {
        // Prevent instantiation
    }

    /**
     * Generates a secure random byte array.
     * @param length desired byte length
     * @return byte array with random data
     */
    public static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    /**
     * Encodes bytes to a Base64 string.
     * @param data byte array
     * @return Base64-encoded string
     */
    public static String toBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Decodes a Base64 string to bytes.
     * @param base64 Base64 string
     * @return decoded byte array
     */
    public static byte[] fromBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    /**
     * Securely wipes a byte array from memory.
     * @param data byte array to zero out
     */
    public static void wipe(byte[] data) {
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                data[i] = 0;
            }
        }
    }
}