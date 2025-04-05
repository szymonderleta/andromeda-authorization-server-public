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


/**
 * Repository class responsible for managing JWT refresh tokens in the underlying
 * database. It provides functionality to interact with and manipulate refresh token
 * records, including creation, retrieval, updating, and deletion.
 * <p>
 * This class includes methods for:
 * - Counting all refresh tokens in the database.
 * - Retrieving paginated, sorted, and filtered lists of refresh tokens.
 * - Fetching specific tokens based on unique identifiers.
 * - Checking and managing the validity of tokens through expiration dates.
 * - Saving new tokens and managing token lifecycle operations.
 * <p>
 * The class leverages {@code JdbcTemplate} for database interactions
 * and custom row mapping through the {@code TokenMapper} to convert query
 * results into application-level domain objects.
 * <p>
 * All operations are tailored to work with the "refresh_tokens" database table.
 */
@Repository
public class RefreshTokenRepository extends TokenRepository {

    private final JdbcTemplate jdbcTemplate;


    /**
     * Constructs a new instance of RefreshTokenRepository and initializes the JDBC template
     * with the provided data source.
     *
     * @param dataSource the data source used to configure the JDBC template for database operations
     */
    @Autowired
    public RefreshTokenRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Retrieves the total count of JWT refresh tokens stored in the database.
     *
     * @return the total count of JWT refresh tokens as an Integer
     */
    public Integer getSize() {
        String sql = """
                SELECT COUNT(*) FROM refresh_tokens;
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    /**
     * Retrieves a paginated list of TokenEntity objects by querying the database.
     * The result includes user and token details, with the query limited by the specified
     * offset and size for pagination purposes.
     *
     * @param offset the starting point in the result set for pagination
     * @param size   the maximum number of records to retrieve in the page
     * @return a list of TokenEntity objects corresponding to the specified offset and size
     */
    public List<TokenEntity> getPage(final int offset, final int size) {
        String sql = """ 
                SELECT u.*, t.*
                FROM users u
                JOIN refresh_tokens t ON u.user_id = t.user_id
                LIMIT ?, ?;
                """;
        return jdbcTemplate.query(sql, new TokenMapper(), offset, size);
    }

    /**
     * Retrieves a paginated and sorted list of valid TokenEntity objects from the database.
     * This method fetches tokens that are considered valid based on the query logic,
     * which may involve expiration date or other criteria defined in the SQL query.
     *
     * @param offset the starting point in the result set for pagination
     * @param size the maximum number of records to retrieve in the page
     * @param sortByParam the column name by which the results should be sorted
     * @param sortOrderParam the sorting order, either "ASC" for ascending or "DESC" for descending
     * @return a list of TokenEntity objects corresponding to the specified parameters
     */
    public List<TokenEntity> findValid(final int offset, final int size, final String sortByParam, final String sortOrderParam) {
        String sql = buildQueryForValidTokensPage(sortByParam, sortOrderParam);
        return jdbcTemplate.query(sql, new TokenMapper(),
                offset, size);
    }

    /**
     * Builds a SQL query for fetching a paginated and sorted list of valid refresh tokens
     * and their associated user details from the database. The method validates the sorting
     * parameters before constructing the query string.
     *
     * @param sortBy the column name by which the result set should be sorted. Must be a valid
     *               column included in the allowed set of sort columns.
     * @param sortOrder the sorting order, either "ASC" for ascending or "DESC" for descending.
     *                  Must be included in the allowed set of sort orders.
     * @return a SQL query string for selecting valid refresh tokens and their associated user
     *         details. The string includes placeholders for pagination parameters.
     * @throws IllegalArgumentException if the provided sortBy or sortOrder parameter is invalid.
     */
    private static String buildQueryForValidTokensPage(final String sortBy, final String sortOrder) {
        ValidatorUtils.validateSortParameters(sortBy, sortOrder, ALLOWED_SORT_COLUMNS, ALLOWED_SORT_ORDERS);
        String normalizedSortOrder = sortOrder.toUpperCase(Locale.ROOT);
        SortParameters sortParameters = new SortParameters(sortBy, normalizedSortOrder);
        return String.format("""
                SELECT u.*, t.*
                FROM users u
                JOIN refresh_tokens t ON u.user_id = t.user_id
                WHERE expiration_date > NOW()
                ORDER BY %s %s
                LIMIT ?, ?;
            """.formatted(sortParameters.sortBy(), sortParameters.sortOrder()));
    }


    /**
     * Retrieves a TokenEntity object wrapped in an Optional that matches the specified token ID.
     *
     * @param tokenId the unique identifier of the token to be retrieved
     * @return an Optional containing the matching TokenEntity if found, or an empty Optional if no match is found
     */
    public Optional<TokenEntity> findById(final long tokenId) {
        String sql = """
                SELECT u.*, t.*
                FROM users u
                JOIN refresh_tokens t ON u.user_id = t.user_id
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
     * Saves a new refresh token to the database.
     * Inserts the provided token details into the `refresh_tokens` table along with an expiration date.
     *
     * @param tokenId the unique identifier for the token being saved
     * @param userId the unique identifier of the user associated with the token
     * @param token the token string to be stored
     * @return the number of rows affected by the insert operation; returns 0 if the token already exists
     */
    public int save(final long tokenId, final long userId, final String token) {
        String sql = """ 
                INSERT INTO refresh_tokens (token_id, user_id, token, expiration_date)
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
     * Deletes a refresh token entry from the database identified by the specified token ID
     * and user ID.
     *
     * @param tokenId the unique identifier of the token to be deleted
     * @param userId the unique identifier of the user associated with the token
     * @return the number of rows affected by the delete operation; returns 0 if no matching entry was found
     */
    public int deleteById(final long tokenId, final long userId) {
        String sql = """ 
                DELETE FROM refresh_tokens
                WHERE token_id = ? AND user_id = ?;
                """;
        return jdbcTemplate.update(sql, tokenId, userId);
    }

    /**
     * Retrieves the next available token ID by querying the database for the
     * maximum current token ID and incrementing it by one.
     *
     * @return the next unique token ID as a Long, or null if the query result is empty.
     */
    public Long getNextId() {
        String idSql = """
                SELECT MAX(token_id) + 1
                FROM refresh_tokens
                """;
        return jdbcTemplate.queryForObject(idSql, Long.class);
    }

    /**
     * Retrieves a paginated and sorted list of TokenEntity objects, filtered by the specified username
     * and email criteria. The results are ordered according to the provided sorting parameters and limited
     * to a specific page size and offset for pagination.
     *
     * @param offset        the starting point in the result set for pagination
     * @param size          the maximum number of records to retrieve in the page
     * @param sortByParam   the column name by which the results should be sorted
     * @param sortOrderParam the sorting order, either "ASC" for ascending or "DESC" for descending
     * @param username      the username filter to be applied, allowing partial matches
     * @param email         the email filter to be applied, allowing partial matches
     * @return a list of TokenEntity objects that match the filters, sorted and paginated according to the
     *         specified parameters
     */
    public List<TokenEntity> getSortedPageWithFilters(final int offset, final int size, final String sortByParam, final String sortOrderParam, final String username, final String email) {
        String sql = buildQueryForSortedPage(sortByParam, sortOrderParam);
        return jdbcTemplate.query(sql, new TokenMapper(),
                getSqlLikeParam(username), getSqlLikeParam(email),
                offset, size);
    }

    /**
     * Builds a SQL query string for fetching a paginated and sorted list of users and their associated
     * refresh tokens based on the specified sorting parameters. The method validates the provided sorting
     * parameters before constructing the query.
     *
     * @param sortBy the column name by which the results should be sorted. This parameter must be a valid
     *               column included in the set of allowed sort columns.
     * @param sortOrder the sorting order, either "ASC" for ascending or "DESC" for descending, which must
     *                  be included in the set of allowed sorting orders.
     * @return a SQL query string for selecting and sorting users and their associated tokens, which
     *         includes placeholders for pagination and filtering parameters.
     * @throws IllegalArgumentException if the provided sortBy parameter is not valid or if the
     *                                  sortOrder parameter is not included in the allowed orders.
     */
    private static String buildQueryForSortedPage(final String sortBy, final String sortOrder) {
        ValidatorUtils.validateSortParameters(sortBy, sortOrder, ALLOWED_SORT_COLUMNS, ALLOWED_SORT_ORDERS);
        String normalizedSortOrder = sortOrder.toUpperCase(Locale.ROOT);
        SortParameters sortParameters = new SortParameters(sortBy, normalizedSortOrder);
        return String.format("""
                SELECT u.*, t.*
                FROM users u
                JOIN refresh_tokens t ON u.user_id = t.user_id
                WHERE u.username LIKE ?
                AND u.email LIKE ?
                ORDER BY %s %s
                LIMIT ?, ?;
            """.formatted(sortParameters.sortBy(), sortParameters.sortOrder()));
    }

    /**
     * Retrieves the count of users and their associated JWT refresh tokens
     * from the database that match the given username and email filters.
     *
     * @param username the username filter to be applied, allowing partial matches
     * @param email    the email filter to be applied, allowing partial matches
     * @return the total count of matching users and their associated tokens as a Long
     */
    public Long getFiltersCount(final String username, final String email) {
        String sql = """ 
                SELECT COUNT(*)
                FROM users u
                JOIN refresh_tokens t ON u.user_id = t.user_id
                WHERE u.username LIKE ?
                AND u.email LIKE ?;
                """;
        return jdbcTemplate.queryForObject(sql, Long.class, getSqlLikeParam(username), getSqlLikeParam(email));
    }

    /**
     * Retrieves the count of valid JWT refresh tokens from the database.
     * A token is considered valid if its expiration date is later than the current time.
     *
     * @return the total count of valid JWT refresh tokens as a Long
     */
    public Long getValidCount() {
        String sql = """
                SELECT COUNT(*)
                FROM refresh_tokens t
                WHERE expiration_date > NOW();
                """;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

}
