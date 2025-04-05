package pl.derleta.authorization.utils;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidatorUtilsTest {

    /**
     * Tests for validateSortParameters method in ValidatorUtils.
     * This method validates the sortBy and sortOrder parameters against allowed values,
     * throwing an IllegalArgumentException for invalid values.
     */

    @Test
    void validateSortParameters_withValidInput_shouldNotThrowException() {
        // Arrange
        String sortBy = "name";
        String sortOrder = "ASC";
        Set<String> allowedSortColumns = Set.of("name", "date", "id");
        Set<String> allowedSortOrders = Set.of("ASC", "DESC");

        // Act & Assert
        ValidatorUtils.validateSortParameters(sortBy, sortOrder, allowedSortColumns, allowedSortOrders);
    }

    @Test
    void validateSortParameters_withInvalidSortOrder_shouldThrowException() {
        // Arrange
        String sortBy = "name";
        String sortOrder = "UP";
        Set<String> allowedSortColumns = Set.of("name", "date", "id");
        Set<String> allowedSortOrders = Set.of("ASC", "DESC");

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidatorUtils.validateSortParameters(sortBy, sortOrder, allowedSortColumns, allowedSortOrders),
                "Invalid sortOrder parameter: " + sortOrder
        );
    }

    @Test
    void validateSortParameters_withInvalidSortBy_shouldThrowException() {
        // Arrange
        String sortBy = "age";
        String sortOrder = "ASC";
        Set<String> allowedSortColumns = Set.of("name", "date", "id");
        Set<String> allowedSortOrders = Set.of("ASC", "DESC");

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidatorUtils.validateSortParameters(sortBy, sortOrder, allowedSortColumns, allowedSortOrders),
                "Invalid sortBy parameter: " + sortBy
        );
    }

    @Test
    void validateSortParameters_withInvalidSortByAndSortOrder_shouldThrowException() {
        // Arrange
        String sortBy = "age";
        String sortOrder = "UP";
        Set<String> allowedSortColumns = Set.of("name", "date", "id");
        Set<String> allowedSortOrders = Set.of("ASC", "DESC");

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> ValidatorUtils.validateSortParameters(sortBy, sortOrder, allowedSortColumns, allowedSortOrders)
        );
    }

}
