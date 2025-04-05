package pl.derleta.authorization.repository.impl.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.derleta.authorization.domain.entity.token.TokenEntity;
import pl.derleta.authorization.repository.TokenRepository;
import pl.derleta.authorization.repository.sort.SortParameters;
import pl.derleta.authorization.utils.ValidatorUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;


/**
 * The AccessTokenRepository class provides data access functionalities for managing JWT access tokens
 * in the database. It is responsible for storing, retrieving, and managing access tokens along with their
 * associated user details. This repository utilizes {@link JdbcTemplate} for interacting with the database.
 * Extends the {@link TokenRepository} class.
 */
@Repository
public class AccessTokenRepository extends TokenRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Initializes the AccessTokenRepository with the specified DataSource.
     * This constructor creates a new instance of JdbcTemplate using the provided DataSource,
     * which is used for database operations.
     *
     * @param dataSource the DataSource used to configure the JdbcTemplate instance for database access.
     */
    @Autowired
    public AccessTokenRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Retrieves the total count of JWT access tokens stored in the database.
     *
     * @return the total number of records in the "access_tokens" table as an Integer.
     */
    public Integer getSize() {
        String sql = """
                SELECT COUNT(*) FROM access_tokens;
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    /**
     * Retrieves a paginated list of access tokens along with their associated user details.
     * The query limits the results based on the provided offset and size parameters.
     *
     * @param offset the starting index for retrieving records.
     * @param size   the number of records to retrieve.
     * @return a list of {@link TokenEntity} objects representing access tokens and their user details.
     */
    public List<TokenEntity> getPage(final int offset, final int size) {
        String sql = """ 
                SELECT u.*, t.*
                FROM users u
                JOIN access_tokens t ON u.user_id = t.user_id
                LIMIT ?, ?;
                """;
        return jdbcTemplate.query(sql, new TokenMapper(), offset, size);
    }

    /**
     * Retrieves a paginated and sorted list of valid access tokens.
     * A token is considered valid if its expiration date is greater than the current time.
     *
     * @param offset          the starting index for retrieving records.
     * @param size            the number of records to retrieve.
     * @param sortByParam     the column name to sort the results by.
     * @param sortOrderParam  the sorting order, either "ASC" (ascending) or "DESC" (descending).
     * @return a list of {@link TokenEntity} objects representing valid access tokens,
     *         sorted and paginated based on the provided parameters.
     */
    public List<TokenEntity> findValid(final int offset, final int size, final String sortByParam, final String sortOrderParam) {
        String sql = buildQueryForValidTokensPage(sortByParam, sortOrderParam);
        return jdbcTemplate.query(sql, new TokenMapper(),
                offset, size);
    }

    /**
     * Builds a SQL query string to retrieve a paginated and sorted list of valid tokens and their associated user data.
     * A token is considered valid if its expiration date is greater than the current time.
     * The query includes placeholders for pagination parameters (offset and limit).
     * Additionally, it validates the sorting parameters before constructing the query.
     *
     * @param sortBy the column name to sort the results by. It must be one of the allowed sort columns.
     * @param sortOrder the sorting order, either "ASC" (ascending) or "DESC" (descending). It must be one of the allowed sort orders.
     * @return a SQL query string that sorts and paginates valid token data based on the provided parameters.
     */
    private static String buildQueryForValidTokensPage(final String sortBy, final String sortOrder) {
        ValidatorUtils.validateSortParameters(sortBy, sortOrder, ALLOWED_SORT_COLUMNS, ALLOWED_SORT_ORDERS);
        String normalizedSortOrder = sortOrder.toUpperCase(Locale.ROOT);
        SortParameters sortParameters = new SortParameters(sortBy, normalizedSortOrder);
        return String.format("""
                SELECT u.*, t.*
                FROM users u
                JOIN access_tokens t ON u.user_id = t.user_id
                WHERE expiration_date > NOW()
                ORDER BY %s %s
                LIMIT ?, ?;
            """.formatted(sortParameters.sortBy(), sortParameters.sortOrder()));
    }

    /**
     * Retrieves an access token and its associated user information by the specified token ID.
     * If no matching token ID is found, an empty {@link Optional} is returned.
     *
     * @param tokenId the unique identifier of the token to retrieve.
     * @return an {@link Optional} containing the {@link TokenEntity} if found; otherwise, an empty {@link Optional}.
     */
    public Optional<TokenEntity> findById(final long tokenId) {
        String sql = """
                SELECT u.*, t.*
                FROM users u
                JOIN access_tokens t ON u.user_id = t.user_id
                WHERE token_id = ?;
                """;
        List<TokenEntity> results = jdbcTemplate.query(sql, new TokenMapper(), tokenId);
        if (results.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(results.getFirst());
        }
    }

    /**
     * Saves a new access token into the database. The token is associated with a specific
     * token ID and user ID and is given an expiration date of 1 day from the current time.
     * If a token with the same ID already exists in the database, the operation will fail
     * and return 0.
     *
     * @param tokenId the unique identifier for the access token.
     * @param userId the unique identifier of the user associated with the token.
     * @param token the access token to be saved.
     * @return the number of rows affected by the save operation. Returns 0 if a duplicate
     *         key is detected.
     */
    public int save(final long tokenId, final long userId, final String token) {
        String sql = """ 
                INSERT INTO access_tokens (token_id, user_id, token, expiration_date)
                VALUES (?, ?, ?, NOW() + INTERVAL 1 DAY);
                """;
        try {
            return jdbcTemplate.update(sql,
                    tokenId,
                    userId,
                    token);
        } catch (DuplicateKeyException e) {
            return 0;
        }
    }

    /**
     * Deletes a record from the "access_tokens" table based on the provided token ID and user ID.
     *
     * @param tokenId the unique identifier of the token to be deleted.
     * @param userId the unique identifier of the user associated with the token.
     * @return the number of rows affected by the delete operation.
     */
    public int deleteById(final long tokenId, final long userId) {
        String sql = """ 
                DELETE FROM access_tokens
                WHERE token_id = ? AND user_id = ?;
                """;
        return jdbcTemplate.update(sql, tokenId, userId);
    }

    /**
     * Retrieves the next available unique identifier for a new token by querying the database.
     * The method calculates the next identifier by finding the maximum value of the existing token IDs
     * in the "access_tokens" table and adding one to it.
     *
     * @return the next available token ID as a Long.
     */
    public Long getNextId() {
        String idSql = """
                SELECT MAX(token_id) + 1
                FROM access_tokens
                """;
        return jdbcTemplate.queryForObject(idSql, Long.class);
    }

    /**
     * Retrieves a paginated and sorted list of access tokens along with their associated user details,
     * filtered by username and email patterns. The results are sorted and paginated based on the provided parameters.
     *
     * @param offset         the starting index for pagination.
     * @param size           the number of records to retrieve.
     * @param sortByParam    the column name to sort the results by.
     * @param sortOrderParam the sorting order, either "ASC" (ascending) or "DESC" (descending).
     * @param username       the username filter to search for, specified as a substring.
     * @param email          the email filter to search for, specified as a substring.
     * @return a list of {@link TokenEntity} objects representing access tokens
     *         and their associated user details, filtered by the specified parameters.
     */
    public List<TokenEntity> getSortedPageWithFilters(final int offset, final int size, final String sortByParam, final String sortOrderParam, final String username, final String email) {
        String sql = buildQueryForSortedPage(sortByParam, sortOrderParam);
        return jdbcTemplate.query(sql, new TokenMapper(),
                getSqlLikeParam(username), getSqlLikeParam(email),
                offset, size);
    }

    /**
     * Builds a SQL query string for retrieving a paginated and sorted list of user and access token data
     * filtered by username and email patterns. The results are ordered by the specified column and sort order.
     *
     * @param sortBy the column name to sort the results by. It must be one of the allowed sort columns.
     * @param sortOrder the sorting order, either "ASC" (ascending) or "DESC" (descending). It must be one of the
     *                  allowed sort orders.
     * @return a SQL query string that includes placeholders for filtering, sorting, and pagination parameters.
     */
    private static String buildQueryForSortedPage(final String sortBy, final String sortOrder) {
        ValidatorUtils.validateSortParameters(sortBy, sortOrder, ALLOWED_SORT_COLUMNS, ALLOWED_SORT_ORDERS);
        String normalizedSortOrder = sortOrder.toUpperCase(Locale.ROOT);
        SortParameters sortParameters = new SortParameters(sortBy, normalizedSortOrder);
        return String.format("""
                SELECT u.*, t.*
                FROM users u
                JOIN access_tokens t ON u.user_id = t.user_id
                WHERE u.username LIKE ?
                AND u.email LIKE ?
                ORDER BY %s %s
                LIMIT ?, ?;
            """.formatted(sortParameters.sortBy(), sortParameters.sortOrder()));
    }

    /**
     * Retrieves the count of users whose username and email match the provided filters.
     * The matching is performed using SQL `LIKE` query with wildcard characters.
     *
     * @param username the username filter to search for, specified as a substring.
     * @param email    the email filter to search for, specified as a substring.
     * @return the count of users matching the given filters as a Long.
     */
    public Long getFiltersCount(final String username, final String email) {
        String sql = """ 
                SELECT COUNT(*)
                FROM users u
                JOIN access_tokens t ON u.user_id = t.user_id
                WHERE u.username LIKE ?
                AND u.email LIKE ?;
                """;
        return jdbcTemplate.queryForObject(sql, Long.class, getSqlLikeParam(username), getSqlLikeParam(email));
    }

    /**
     * Retrieves the count of valid JWT access tokens from the database table "access_tokens".
     * A token is considered valid if its expiration date is greater than the current time.
     *
     * @return the total count of valid JWT access tokens as a Long.
     */
    public Long getValidCount() {
        String sql = """
                SELECT COUNT(*)
                FROM access_tokens t
                WHERE expiration_date > NOW();
                """;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

}
