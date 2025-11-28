package com.myorg.trading.service.broker;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Simple AES-GCM encrypt/decrypt helper.
 * NOTE: In production use a managed KMS (AWS KMS / Vault). Keep this only for dev/small deployments.
 */
@Component
public class CredentialManager {

    @Value("${app.crypto.key:}")
    private String base64Key; // 32-byte key in base64, provided via env var APP_CRYPTO_KEY

    private SecretKeySpec keySpec;
    private static final String AES_ALGO = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH = 12; // 96 bits recommended for GCM

    @PostConstruct
    public void init() {
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalStateException("APP_CRYPTO_KEY must be set (base64-encoded 256-bit key)");
        }
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalStateException("APP_CRYPTO_KEY must decode to 32 bytes (256-bit)");
        }
        keySpec = new SecretKeySpec(keyBytes, AES_ALGO);
    }

    public String encrypt(String plain) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom sr = new SecureRandom();
            sr.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            byte[] cipherBytes = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            // store as base64(iv + cipher)
            byte[] out = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(cipherBytes, 0, out, iv.length, cipherBytes.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String base64) {
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(data, 0, iv, 0, iv.length);
            int cipherLen = data.length - iv.length;
            byte[] cipherBytes = new byte[cipherLen];
            System.arraycopy(data, iv.length, cipherBytes, 0, cipherLen);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
