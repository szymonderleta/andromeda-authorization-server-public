package pl.derleta.authorization.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    /**
     * Tests for the PasswordGenerator class.
     * <p>
     * The PasswordGenerator class provides methods to generate strong passwords,
     * ensuring that each generated password contains a mix of uppercase letters,
     * lowercase letters, digits, and special characters.
     */
    @Test
    void generateStrongPassword_withDefaultLength_shouldReturnPasswordWithExpectedLength() {
        // Arrange
        int expectedLength = 12;

        // Act
        String password = PasswordGenerator.generateStrongPassword();

        // Assert
        assertAll(
                () -> assertNotNull(password),
                () -> assertEquals(expectedLength, password.length())
        );
    }

    @Test
    void generateStrongPassword_withCustomLength_shouldReturnPasswordWithGivenLength() {
        // Arrange
        int customLength = 16;

        // Act
        String password = PasswordGenerator.generateStrongPassword(customLength);

        // Assert
        assertAll(
                () -> assertNotNull(password),
                () -> assertEquals(customLength, password.length())
        );
    }

    @Test
    void generateStrongPassword_whenCalled_shouldContainAtLeastOneUppercaseLetter() {
        // Arrange & Act
        String password = PasswordGenerator.generateStrongPassword();

        // Assert
        assertAll(
                () -> assertNotNull(password),
                () -> assertTrue(password.chars().anyMatch(Character::isUpperCase))
        );
    }

    @Test
    void generateStrongPassword_whenCalled_shouldContainAtLeastOneLowercaseLetter() {
        // Arrange & Act
        String password = PasswordGenerator.generateStrongPassword();

        // Assert
        assertAll(
                () -> assertNotNull(password),
                () -> assertTrue(password.chars().anyMatch(Character::isLowerCase))
        );
    }

    @Test
    void generateStrongPassword_whenCalled_shouldContainAtLeastOneDigit() {
        // Arrange & Act
        String password = PasswordGenerator.generateStrongPassword();

        // Assert
        assertAll(
                () -> assertNotNull(password),
                () -> assertTrue(password.chars().anyMatch(Character::isDigit))
        );
    }

    @Test
    void generateStrongPassword_whenCalled_shouldContainAtLeastOneSpecialCharacter() {
        // Arrange
        String specialCharacters = "!@#$%^&*()-_=+";

        // Act
        String password = PasswordGenerator.generateStrongPassword();

        // Assert
        assertAll(
                () -> assertNotNull(password),
                () -> assertTrue(password.chars().anyMatch(ch -> specialCharacters.indexOf(ch) >= 0))
        );
    }

    @Test
    void generateStrongPassword_whenCalledTwice_shouldReturnDifferentPasswords() {
        // Act
        String password1 = PasswordGenerator.generateStrongPassword();
        String password2 = PasswordGenerator.generateStrongPassword();

        // Assert
        assertAll(
                () -> assertNotNull(password1),
                () -> assertNotNull(password2),
                () -> assertNotEquals(password1, password2)
        );
    }

}
