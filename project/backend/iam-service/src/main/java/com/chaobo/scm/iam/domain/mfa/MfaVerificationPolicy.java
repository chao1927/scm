package com.chaobo.scm.iam.domain.mfa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * Cryptographic policy and ports for MFA.
 *
 * @author chaobo
 */
public final class MfaVerificationPolicy {

    private MfaVerificationPolicy() {
    }

    public static String recoveryCodeHash(String recoveryCode) {
        if (recoveryCode == null || recoveryCode.isBlank()) {
            return "";
        }
        try {
            return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256")
                    .digest(recoveryCode.trim().getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("MFA recovery code hashing failed", exception);
        }
    }

    public interface SecretProtector {
        /**
         * Encrypts an MFA secret with authenticated encryption.
         *
         * @param plaintext plaintext secret
         * @return authenticated ciphertext
         */
        String encrypt(String plaintext);
        /**
         * Decrypts and authenticates an MFA secret.
         *
         * @param ciphertext authenticated ciphertext
         * @return plaintext secret
         */
        String decrypt(String ciphertext);
    }

    public interface TotpVerifier {
        /**
         * Verifies a TOTP in the accepted clock window.
         *
         * @param base32Secret Base32 seed
         * @param code six-digit one-time code
         * @param now verification time
         * @return whether the code is valid
         */
        boolean verify(String base32Secret, String code, Instant now);
    }

    @Component
    public static final class AesGcmSecretProtector implements SecretProtector {
        private static final int IV_BYTES = 12;
        private static final int AES_KEY_BYTES = 32;
        private static final int GCM_TAG_BITS = 128;
        private final byte[] key;
        private final SecureRandom random = new SecureRandom();

        public AesGcmSecretProtector(@Value("${scm.iam.mfa.master-key:${IAM_MFA_MASTER_KEY:}}") String masterKey) {
            byte[] raw = masterKey == null ? new byte[0] : masterKey.getBytes(StandardCharsets.UTF_8);
            if (raw.length != AES_KEY_BYTES) {
                throw new IllegalStateException("IAM MFA master key must contain exactly 32 bytes");
            }
            this.key = raw.clone();
        }

        @Override
        public String encrypt(String plaintext) {
            if (plaintext == null || plaintext.isBlank()) {
                throw new IllegalArgumentException("MFA secret is required");
            }
            try {
                byte[] iv = new byte[IV_BYTES];
                random.nextBytes(iv);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"),
                        new GCMParameterSpec(GCM_TAG_BITS, iv));
                byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + encrypted.length)
                        .put(iv).put(encrypted).array());
            } catch (Exception exception) {
                throw new IllegalStateException("MFA secret encryption failed", exception);
            }
        }

        @Override
        public String decrypt(String ciphertext) {
            try {
                byte[] value = Base64.getDecoder().decode(ciphertext);
                byte[] iv = java.util.Arrays.copyOfRange(value, 0, IV_BYTES);
                byte[] encrypted = java.util.Arrays.copyOfRange(value, IV_BYTES, value.length);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                        new GCMParameterSpec(GCM_TAG_BITS, iv));
                return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
            } catch (Exception exception) {
                throw new IllegalStateException("MFA secret decryption failed", exception);
            }
        }
    }

    @Component
    public static final class StandardTotpVerifier implements TotpVerifier {
        private static final long STEP_SECONDS = 30;
        private static final int BASE32_RADIX_BITS = 5;
        private static final int BYTE_BITS = 8;
        private static final String SIX_DIGIT_PATTERN = "\\d{6}";
        private static final String BASE32_PADDING = "=";
        private static final String SPACE = " ";

        @Override
        public boolean verify(String base32Secret, String code, Instant now) {
            if (base32Secret == null || code == null || !code.matches(SIX_DIGIT_PATTERN)) {
                return false;
            }
            byte[] secret = decodeBase32(base32Secret);
            long counter = now.getEpochSecond() / STEP_SECONDS;
            for (long offset = -1; offset <= 1; offset++) {
                if (code.equals(generate(secret, counter + offset))) {
                    return true;
                }
            }
            return false;
        }

        private static String generate(byte[] secret, long counter) {
            try {
                Mac mac = Mac.getInstance("HmacSHA1");
                mac.init(new SecretKeySpec(secret, "HmacSHA1"));
                byte[] hash = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(counter).array());
                int offset = hash[hash.length - 1] & 0x0f;
                int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16)
                        | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
                return String.format("%06d", binary % 1_000_000);
            } catch (Exception exception) {
                throw new IllegalStateException("TOTP verification failed", exception);
            }
        }

        private static byte[] decodeBase32(String input) {
            String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
            int buffer = 0;
            int bitsLeft = 0;
            java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
            for (char character : input.replace(BASE32_PADDING, "").replace(SPACE, "")
                    .toUpperCase().toCharArray()) {
                int value = alphabet.indexOf(character);
                if (value < 0) { throw new IllegalArgumentException("invalid Base32 secret"); }
                buffer = (buffer << BASE32_RADIX_BITS) | value;
                bitsLeft += BASE32_RADIX_BITS;
                if (bitsLeft >= BYTE_BITS) {
                    output.write((buffer >> (bitsLeft - BYTE_BITS)) & 0xff);
                    bitsLeft -= BYTE_BITS;
                }
            }
            return output.toByteArray();
        }
    }
}
