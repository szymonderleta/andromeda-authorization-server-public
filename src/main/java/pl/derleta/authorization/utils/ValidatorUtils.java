package pl.derleta.authorization.utils;

import java.util.Locale;
import java.util.Set;

/**
 * Utility class for validating parameters related to sorting functionality.
 * <p>
 * This class provides methods to validate the sort field and sort order
 * based on predefined allowed values. It ensures the parameters used in
 * sorting operations comply with the specified constraints.
 */
public class ValidatorUtils {

    /**
     * Validates the provided sorting parameters to ensure they conform to the allowed values.
     *
     * @param sortBy the column or field name used for sorting, which must be included in the set of allowed sort columns
     * @param sortOrder the sorting order (e.g., "ASC" or "DESC"), which must be included in the set of allowed sort orders
     * @param allowedSortColumns a set of valid column or field names that can be used for sorting
     * @param allowedSortOrders a set of valid sorting orders that are permitted
     * @throws IllegalArgumentException if the sortBy parameter is not in the allowedSortColumns set
     * @throws IllegalArgumentException if the sortOrder parameter is not in the allowedSortOrders set
     */
    public static void validateSortParameters(
            final String sortBy,
            final String sortOrder,
            final Set<String> allowedSortColumns,
            final Set<String> allowedSortOrders
    ) {

        String normalizedSortOrder = sortOrder.toUpperCase(Locale.ROOT);
        if (!allowedSortOrders.contains(normalizedSortOrder)) {
            throw new IllegalArgumentException("Invalid sortOrder parameter: " + sortOrder);
        }

        if (!allowedSortColumns.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sortBy parameter: " + sortBy);
        }
    }

}
