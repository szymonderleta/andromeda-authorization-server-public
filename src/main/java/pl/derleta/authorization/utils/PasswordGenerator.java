package pl.derleta.authorization.utils;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Utility class for generating strong passwords.
 * This class provides methods to create passwords with a mix of
 * uppercase letters, lowercase letters, digits, and special characters.
 * The generated passwords are designed to meet common security requirements.
 * This class cannot be instantiated.
 */
public final class PasswordGenerator {

    private static final int DEFAULT_PASSWORD_LENGTH = 12;
    private static final String UPPERCASE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+";

    /**
     * Generates a strong password using the default length.
     * The password includes at least one uppercase letter,
     * one lowercase letter, one digit, and one special character.
     * The remaining characters are randomly selected
     * from a mix of uppercase, lowercase, digits, and special characters.
     *
     * @return a randomly generated strong password using the default length
     */
    public static String generateStrongPassword() {
        return generateStrongPassword(DEFAULT_PASSWORD_LENGTH);
    }

    /**
     * Generates a strong password of the specified length.
     * The password includes at least one uppercase letter,
     * one lowercase letter, one digit, and one special character.
     * The remaining characters are randomly selected
     * from a mix of uppercase, lowercase, digits, and special characters.
     *
     * @param length the desired length of the password
     * @return a randomly generated strong password of the specified length
     */
    public static String generateStrongPassword(int length) {
        String allCharacters = UPPERCASE_CHARACTERS + LOWERCASE_CHARACTERS + DIGITS + SPECIAL_CHARACTERS;
        Random random = new SecureRandom();

        StringBuilder password = new StringBuilder();
        password.append(UPPERCASE_CHARACTERS.charAt(random.nextInt(UPPERCASE_CHARACTERS.length())));
        password.append(LOWERCASE_CHARACTERS.charAt(random.nextInt(LOWERCASE_CHARACTERS.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));

        for (int i = 4; i < length; i++) {
            password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
        }

        return shuffleString(password.toString());
    }

    /**
     * Shuffles the characters in a given string randomly.
     * The method randomly rearranges the characters of the input string
     * to create a new shuffled version of the string.
     *
     * @param input the string to be shuffled
     * @return a new string with the characters shuffled randomly
     */
    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int randomIndex = (int) (Math.random() * (i + 1));
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }
        return new String(characters);
    }

}
