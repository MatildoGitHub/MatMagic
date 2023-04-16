package garcia.personal.MatMagic.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordEncoder {
    public static String encode(String password) {
        String salt = generateSalt();
        String hashedPassword = hash(password + salt);
        return salt + "$" + hashedPassword;
    }

    public static boolean matches(String password, String encodedPassword) {
        String[] parts = encodedPassword.split("\\$");
        String salt = parts[0];
        String hashedPassword = hash(password + salt);
        return hashedPassword.equals(parts[1]);
    }

    private static String generateSalt() {
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private static String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
