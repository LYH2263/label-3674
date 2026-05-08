package com.label.community.security;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private PasswordUtil() {
    }

    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(10));
    }

    public static boolean matches(String plain, String stored) {
        if (stored == null || stored.isBlank()) {
            return false;
        }
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return BCrypt.checkpw(plain, stored);
        }
        return stored.equals(plain);
    }
}
