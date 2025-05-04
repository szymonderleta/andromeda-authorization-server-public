package pl.derleta.authorization.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.derleta.authorization.domain.entity.UserRoleEntity;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.repository.mapper.UserRoleMapper;
import pl.derleta.authorization.repository.sort.SortParameters;
import pl.derleta.authorization.utils.ValidatorUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Repository class responsible for managing database operations related
 * to user-role mappings in the "andromeda" schema. This includes fetching,
 * saving, deleting, and filtering user-role entries, as well as retrieving
 * related user and role details.
 * <p>
 * The class uses Spring's {@link JdbcTemplate} to perform SQL queries and updates.
 * It operates on the "user_roles" table and integrates with the "users" and "roles"
 * tables to fetch complete user-role information.
 */
@Repository
public class UserRoleRepository implements RepositoryClass {

    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("u.user_id", "u.username", "u.email", "r.role_id", "r.role_name");
    private static final Set<String> ALLOWED_SORT_ORDERS = Set.of("ASC", "DESC");

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructs an instance of UserRoleRepository and initializes the JdbcTemplate
     * with the provided DataSource for database operations.
     *
     * @param dataSource the DataSource to be used for initializing the JdbcTemplate
     */
    @Autowired
    public UserRoleRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Retrieves the total count of user-role mappings from the database.
     *
     * @return the total number of user-role mappings as an Integer
     */
    public Integer getSize() {
        String sql = """
                SELECT COUNT(*) FROM user_roles;
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    /**
     * Retrieves a paginated list of user-role mappings from the database.
     * This method fetches user-role records along with associated user and role details
     * and maps the result to a list of {@link UserRoleEntity} objects.
     *
     * @param offset the starting position for the records to retrieve
     * @param size   the number of records to retrieve
     * @return a list of {@link UserRoleEntity} objects containing user, role, and user-role details
     */
    public List<UserRoleEntity> getPage(final int offset, final int size) {
        String sql = """ 
                SELECT ur.user_role_id, u.*, r.*
                FROM users u
                JOIN user_roles ur ON u.user_id = ur.user_id
                JOIN roles r ON ur.role_id = r.role_id
                LIMIT ?, ?;
                """;
        return jdbcTemplate.query(sql, new UserRoleMapper(), offset, size);
    }

    /**
     * Retrieves a UserRoleEntity based on the specified user ID and role ID.
     * This method fetches a user-role mapping record from the database,
     * along with associated user and role details, and maps the result
     * to a UserRoleEntity object.
     *
     * @param userId the unique identifier for the user
     * @param roleId the unique identifier for the role
     * @return a UserRoleEntity object containing user, role, and user-role details;
     * throws an exception if no matching record is found
     */
    public UserRoleEntity findByIds(final long userId, final int roleId) {
        String sql = """
                  SELECT ur.user_role_id, u.*, r.*
                  FROM users u
                  JOIN user_roles ur ON u.user_id = ur.user_id
                  JOIN roles r ON ur.role_id = r.role_id
                  WHERE u.user_id = ?
                  AND r.role_id = ?;
                """;
        return jdbcTemplate.queryForObject(sql, new UserRoleMapper(), userId, roleId);
    }

    /**
     * Retrieves a UserRoleEntity based on the specified user role ID.
     * This method fetches a user role record from the database, along with associated
     * user and role details, and maps the result to a UserRoleEntity object.
     *
     * @param userRoleId the unique identifier for the user-role mapping to be retrieved
     * @return a UserRoleEntity object containing user, role, and user-role details;
     * or throws an exception if no matching record is found
     */
    public UserRoleEntity findById(final long userRoleId) {
        String sql = """
                  SELECT ur.user_role_id, u.*, r.*
                  FROM users u
                  JOIN user_roles ur ON u.user_id = ur.user_id
                  JOIN roles r ON ur.role_id = r.role_id
                  WHERE user_role_id = ?;
                """;
        return jdbcTemplate.queryForObject(sql, new UserRoleMapper(), userRoleId);
    }

    /**
     * Persists a mapping of user roles into the database.
     *
     * @param userRoleId the unique identifier for the user-role mapping
     * @param userId     the identifier of the user
     * @param roleId     the identifier of the role
     */
    public void save(final long userRoleId, final long userId, final int roleId) {
        String sql = """ 
                    INSERT INTO user_roles (user_role_id, user_id, role_id)
                    VALUES (?, ?, ?);
                """;
        jdbcTemplate.update(sql,
                userRoleId,
                userId,
                roleId);
    }

    /**
     * Retrieves the next available unique identifier for the user_roles table by querying the maximum
     * user_role_id and incrementing it by 1.
     *
     * @return the next unique user_role_id as a Long, or null if no records exist in the user_roles table
     */
    public Long getNextId() {
        String idSql = "SELECT MAX(user_role_id) + 1 FROM user_roles";
        return jdbcTemplate.queryForObject(idSql, Long.class);
    }

    /**
     * Deletes a user-role mapping from the database for the specified user and role IDs.
     *
     * @param userId the ID of the user whose user-role mapping is to be deleted
     * @param roleId the ID of the role whose user-role mapping is to be deleted
     */
    public void deleteById(final long userId, final int roleId) {
        String sql = """ 
                    DELETE FROM user_roles
                    WHERE user_id = ? AND role_id = ?;
                """;
        jdbcTemplate.update(sql, userId, roleId);
    }

    /**
     * Retrieves a sorted and paginated list of user-role mappings based on specified filter conditions.
     * This method filters by username, email, and role name, and sorts the results based on the given
     * sorting parameters.
     *
     * @param offset       the starting position for the records to retrieve
     * @param size         the number of records to retrieve
     * @param sortByParam  the column name to sort the results by
     * @param sortOrderParam the sorting order, either "ASC" for ascending or "DESC" for descending
     * @param username     the filter criteria for the username; supports partial matching using wildcards
     * @param email        the filter criteria for the email; supports partial matching using wildcards
     * @param roleName     the filter criteria for the role name; supports partial matching using wildcards
     * @return a list of {@link UserRoleEntity} objects containing user, role, and user-role details
     */
    public List<UserRoleEntity> getSortedPageWithFilters(final int offset, final int size, final String sortByParam, final String sortOrderParam, final String username, final String email, final String roleName) {
        String sql = buildQueryForSortedList(sortByParam, sortOrderParam);
        String usernameParam = "%" + username + "%";
        String emailParam = "%" + email + "%";
        String roleNameParam = "%" + roleName + "%";
        return jdbcTemplate.query(sql, new UserRoleMapper(),
                usernameParam, emailParam, roleNameParam,
                offset, size);
    }

    /**
     * Builds a SQL query string for retrieving a sorted list of user-role mappings based on
     * specified sorting criteria. Validates the provided sort parameters before constructing
     * the query to ensure they match the allowed sorting options.
     *
     * @param sortBy the column name to sort the results by
     * @param sortOrder the sorting order, either "ASC" for ascending or "DESC" for descending
     * @return a formatted SQL query string with the specified sorting conditions
     */
    private static String buildQueryForSortedList(final String sortBy, final String sortOrder) {
        ValidatorUtils.validateSortParameters(sortBy, sortOrder, ALLOWED_SORT_COLUMNS, ALLOWED_SORT_ORDERS);
        String normalizedSortOrder = sortOrder.toUpperCase(Locale.ROOT);
        SortParameters sortParameters = new SortParameters(sortBy, normalizedSortOrder);
        return String.format("""
                SELECT ur.user_role_id, u.*, r.*
                FROM users u
                JOIN user_roles ur ON u.user_id = ur.user_id
                JOIN roles r ON ur.role_id = r.role_id
                WHERE u.username LIKE ?
                AND u.email LIKE ?
                AND r.role_name LIKE ?
                ORDER BY %s %s
                LIMIT ?, ?;
            """.formatted(sortParameters.sortBy(), sortParameters.sortOrder()));
    }

    /**
     * Retrieves the count of user-role entries that match the given filters: username, email, and role name.
     *
     * @param username the username filter condition, a string to be matched (supports partial matching using wildcards)
     * @param email    the email filter condition, a string to be matched (supports partial matching using wildcards)
     * @param roleName the role name filter condition, a string to be matched (supports partial matching using wildcards)
     * @return the count of user-role entries that satisfy the specified filters, as a Long
     */
    public Long getFiltersCount(final String username, final String email, final String roleName) {
        String sql = """ 
                SELECT COUNT(*)
                FROM users u
                JOIN user_roles ur ON u.user_id = ur.user_id
                JOIN roles r ON ur.role_id = r.role_id
                WHERE u.username LIKE ?
                AND u.email LIKE ?
                AND r.role_name LIKE ?
                """;
        String usernameParam = "%" + username + "%";
        String emailParam = "%" + email + "%";
        String roleNameParam = "%" + roleName + "%";
        return jdbcTemplate.queryForObject(sql, Long.class, usernameParam, emailParam, roleNameParam);
    }

}
