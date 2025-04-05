package pl.derleta.authorization.utils;

import java.security.SecureRandom;

/**
 * Utility class for generating random alphanumeric tokens.
 * The tokens can be generated with a default length or a specified length.
 * This class provides methods for secure random token generation to ensure high entropy.
 * This class is designed to be non-instantiable.
 */
public final class ConfigurationTokenGenerator {

    private static final int DEFAULT_SIZE = 100;
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generates a random alphanumeric token using the default length.
     *
     * @return a randomly generated alphanumeric token using the default length
     */
    public static String getToken() {
        return getToken(DEFAULT_SIZE);
    }

    /**
     * Generates a random alphanumeric token of the specified length.
     *
     * @param length the length of the token to generate
     * @return a randomly generated alphanumeric token of the specified length
     */
    public static String getToken(int length) {
        if(length < 1) {
            throw new IllegalArgumentException("Token length must be greater than 0");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALPHANUMERIC.length());
            char randomChar = ALPHANUMERIC.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }

}
