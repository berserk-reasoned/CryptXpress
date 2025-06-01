package com.encryptor.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

/**
 * Provides cryptographic key generation and cipher configuration for various encryption algorithms.
 * Supports AES-256, Blowfish-128, and ChaCha20.
 */
public class SecurityServices {

    private static final int AES_KEY_SIZE = 256;
    private static final int BLOWFISH_KEY_SIZE = 128;
    private static final int NONCE_SIZE = 12; // for ChaCha20

    /**
     * Generates a secret key for the specified encryption algorithm.
     *
     * @param method The encryption method (AES-256, Blowfish-128, ChaCha20)
     * @return The generated secret key
     * @throws Exception If key generation fails
     */
    public static SecretKey generateKey(String method) throws Exception {
        KeyGenerator keyGen;
        switch (method.toUpperCase()) {
            case "AES-256" -> {
                keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(AES_KEY_SIZE);
                return keyGen.generateKey();
            }
            case "BLOWFISH-128" -> {
                keyGen = KeyGenerator.getInstance("Blowfish");
                keyGen.init(BLOWFISH_KEY_SIZE);
                return keyGen.generateKey();
            }
            case "CHACHA20" -> {
                byte[] key = new byte[32];
                new SecureRandom().nextBytes(key);
                return new SecretKeySpec(key, "ChaCha20");
            }
            default -> throw new IllegalArgumentException("Unsupported encryption method: " + method);
        }
    }

    /**
     * Initializes a Cipher instance for encryption or decryption.
     *
     * @param mode        Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     * @param method      The encryption method
     * @param key         The SecretKey used for encryption/decryption
     * @param parameters  Additional parameters such as IV or nonce if required
     * @return Configured Cipher instance
     * @throws Exception If cipher initialization fails
     */
    public static Cipher initCipher(int mode, String method, SecretKey key, Map<String, byte[]> parameters) throws Exception {
        Cipher cipher;
        switch (method.toUpperCase()) {
            case "AES-256" -> {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                IvParameterSpec ivSpec = new IvParameterSpec(parameters.get("iv"));
                cipher.init(mode, key, ivSpec);
            }
            case "BLOWFISH-128" -> {
                cipher = Cipher.getInstance("Blowfish/CBC/PKCS5Padding");
                IvParameterSpec ivSpec = new IvParameterSpec(parameters.get("iv"));
                cipher.init(mode, key, ivSpec);
            }
            case "CHACHA20" -> {
                cipher = Cipher.getInstance("ChaCha20");
                ChaCha20ParameterSpec spec = new ChaCha20ParameterSpec(parameters.get("nonce"), 1);
                cipher.init(mode, key, spec);
            }
            default -> throw new IllegalArgumentException("Unsupported encryption method: " + method);
        }
        return cipher;
    }

    /**
     * Encodes a secret key to Base64 format.
     *
     * @param key The secret key
     * @return Base64 encoded string
     */
    public static String encodeKey(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Decodes a Base64 encoded key string to a SecretKey.
     *
     * @param encodedKey The Base64 encoded key string
     * @param algorithm  The algorithm name (AES, Blowfish, ChaCha20)
     * @return SecretKey object
     */
    public static SecretKey decodeKey(String encodedKey, String algorithm) {
        byte[] decoded = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decoded, algorithm);
    }

    /**
     * Generates a random IV or nonce for encryption use.
     *
     * @param length The byte length of the random data
     * @return Byte array of secure random data
     */
    public static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
