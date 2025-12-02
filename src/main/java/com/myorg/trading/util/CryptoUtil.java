package com.myorg.trading.util;

import org.jboss.aerogear.security.otp.Totp;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility for generating Time-based One-Time Passwords (TOTP).
 * Uses org.jboss.aerogear:aerogear-otp-java for reliability.
 */
@Slf4j
public class CryptoUtil {

    /**
     * Generates a standard 6-digit TOTP based on the provided secret.
     * Compatible with Google Authenticator / Microsoft Authenticator logic.
     *
     * @param secretKey The Base32 secret key provided by the broker (e.g., Angel One, Zerodha).
     * @return The 6-digit TOTP code.
     */
    public static String generateTotp(String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("TOTP Secret Key cannot be null or empty");
        }

        try {
            // Remove any whitespace that might have been pasted accidentally
            String cleanSecret = secretKey.replaceAll("\\s+", "");

            // Aerogear handles Base32 decoding and time-step calculation automatically
            Totp totp = new Totp(cleanSecret);
            return totp.now();
        } catch (Exception e) {
            log.error("Failed to generate TOTP for secret: {}", maskSecret(secretKey), e);
            throw new RuntimeException("Error generating TOTP: " + e.getMessage(), e);
        }
    }

    /**
     * Helper to mask secret in logs so we don't leak credentials.
     */
    private static String maskSecret(String secret) {
        if (secret == null || secret.length() < 4) return "****";
        return secret.substring(0, 2) + "****" + secret.substring(secret.length() - 2);
    }
}