package com.mycompany.carrentalforms;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class PasswordUtil {
     private static final char[] HEX = "0123456789abcdef".toCharArray();
    private static final String SALT_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private PasswordUtil() { }

    public static String hash(String salt, char[] password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            md.update(new String(password).getBytes(StandardCharsets.UTF_8));
            byte[] d = md.digest();
            char[] out = new char[d.length * 2];
            for (int i = 0; i < d.length; i++) {
                out[i * 2]     = HEX[(d[i] >> 4) & 0xF];
                out[i * 2 + 1] = HEX[d[i] & 0xF];
            }
            return new String(out);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** 8-character random salt for new staff accounts. */
    public static String newSalt() {
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) sb.append(SALT_CHARS.charAt(r.nextInt(SALT_CHARS.length())));
        return sb.toString();
    }

    /** Constant-time comparison. */
    public static boolean matches(String expectedHex, String actualHex) {
        return MessageDigest.isEqual(
                expectedHex.getBytes(StandardCharsets.UTF_8),
                actualHex.getBytes(StandardCharsets.UTF_8));
    }
    
}
