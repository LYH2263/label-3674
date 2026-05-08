package com.label.community.security;

import com.label.community.common.BusinessException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public final class SensitiveCryptoUtil {
    private SensitiveCryptoUtil() {
    }

    private static SecretKeySpec resolveKey() {
        String rawKey = System.getenv("APP_CRYPTO_KEY");
        if (rawKey == null || rawKey.isBlank()) {
            throw new BusinessException(500, 50010, "未配置敏感信息加密密钥 APP_CRYPTO_KEY");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawKey.getBytes(StandardCharsets.UTF_8));
            byte[] key = new byte[16];
            System.arraycopy(digest, 0, key, 0, key.length);
            return new SecretKeySpec(key, "AES");
        } catch (Exception ex) {
            throw new BusinessException(500, 50013, "敏感信息加密密钥初始化失败");
        }
    }

    public static String encrypt(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, resolveKey());
            return Base64.getEncoder().encodeToString(cipher.doFinal(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new BusinessException(500, 50011, "敏感信息加密失败");
        }
    }

    public static String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) {
            return encrypted;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, resolveKey());
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new BusinessException(500, 50012, "敏感信息解密失败");
        }
    }
}
