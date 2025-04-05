package pl.derleta.authorization.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationTokenGeneratorTest {

    /**
     * Tests for ConfigurationTokenGenerator class:
     * <p>
     * - getToken() method: Verifies whether the default token length is correct and the token is valid.
     * - getToken(int length) method: Validates the token generation with specific lengths.
     */
    @Test
    void getToken_withDefaultLength_shouldGenerateValidToken() {
        // Act
        String token = ConfigurationTokenGenerator.getToken();

        // Assert
        assertNotNull(token, "Token should not be null");
        assertEquals(100, token.length(), "Token length should match the default size");
        assertTrue(token.matches("[A-Za-z0-9]+"), "Token should contain only alphanumeric characters");
    }

    @Test
    void getToken_withCustomSmallLength_shouldGenerateValidToken() {
        // Arrange
        int customLength = 10;

        // Act
        String token = ConfigurationTokenGenerator.getToken(customLength);

        // Assert
        assertNotNull(token, "Token should not be null");
        assertEquals(customLength, token.length(), "Token length should match the custom size");
        assertTrue(token.matches("[A-Za-z0-9]+"), "Token should contain only alphanumeric characters");
    }

    @Test
    void getToken_withCustomLargeLength_shouldGenerateValidToken() {
        // Arrange
        int customLength = 200;

        // Act
        String token = ConfigurationTokenGenerator.getToken(customLength);

        // Assert
        assertNotNull(token, "Token should not be null");
        assertEquals(customLength, token.length(), "Token length should match the custom size");
        assertTrue(token.matches("[A-Za-z0-9]+"), "Token should contain only alphanumeric characters");
    }

    @Test
    void getToken_withCustomLengthZero_shouldThrowIllegalArgumentException() {
        // Arrange
        int customLength = 0;

        // Act and Assert
        try {
            ConfigurationTokenGenerator.getToken(customLength);
        } catch (IllegalArgumentException e) {
            assertEquals("Token length must be greater than 0", e.getMessage());
        }
    }

    @Test
    void getToken_withCustomNegativeLength_shouldThrowIllegalArgumentException() {
        // Arrange
        int customLength = -5;

        // Act and Assert
        try {
            ConfigurationTokenGenerator.getToken(customLength);
        } catch (IllegalArgumentException e) {
            assertEquals("Token length must be greater than 0", e.getMessage());
        }
    }

}
