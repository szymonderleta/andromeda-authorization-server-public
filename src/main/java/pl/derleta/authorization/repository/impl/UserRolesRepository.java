package pl.derleta.authorization.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.entity.UserRoleEntity;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.repository.mapper.RoleMapper;
import pl.derleta.authorization.repository.mapper.UserRoleMapper;
import pl.derleta.authorization.repository.sort.SortParameters;
import pl.derleta.authorization.utils.ValidatorUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Locale;
import java.util.Set;


/**
 * UserRolesRepository is a Spring repository implementation used to manage user role data.
 * This class provides methods to retrieve user roles based on various filters and criteria.
 * It utilizes JdbcTemplate for executing SQL queries and dynamically maps result sets
 * to corresponding entities using a custom RowMapper.
 */
@Repository
public class UserRolesRepository implements RepositoryClass {

    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("u.user_id", "u.username", "u.email", "r.role_id", "r.role_name");
    private static final Set<String> ALLOWED_SORT_ORDERS = Set.of("ASC", "DESC");


    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructs a new instance of the UserRolesRepository using the provided data source.
     * Initializes a JdbcTemplate for executing database queries.
     *
     * @param dataSource the DataSource to be used for initializing the JdbcTemplate
     */
    @Autowired
    public UserRolesRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }


    /**
     * Retrieves a list of user roles for a given user ID, filtered by role name and sorted
     * by the specified parameters.
     *
     * @param userId the ID of the user for whom the roles are being retrieved
     * @param sortByParam the column name by which to sort the result set
     * @param sortOrderParam the sorting order, either "ASC" for ascending or "DESC" for descending
     * @param roleName a partial or full role name*/
    public List<UserRoleEntity> get(final long userId, final String sortByParam, final String sortOrderParam, final String roleName) {
        String sql = buildQueryForSortedList(sortByParam, sortOrderParam);
        String roleNameParam = "%" + roleName + "%";
        return jdbcTemplate.query(sql, new UserRoleMapper(),
                userId, roleNameParam);
    }

    /**
     * Builds an SQL query string for retrieving a sorted list of user roles based on the specified sorting parameters.
     * Validates the provided sorting parameters against a predefined set of allowed columns and orders.
     *
     * @param sortBy the column name by which to sort the result set
     * @param sortOrder the sorting order, either "ASC" for ascending or "DESC" for descending
     * @return an SQL query string for retrieving user roles, including sorting as specified
     * @throws IllegalArgumentException if the sorting parameters are invalid
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
                WHERE u.user_id = ?
                AND r.role_name LIKE ?
                ORDER BY %s %s;
            """.formatted(sortParameters.sortBy(), sortParameters.sortOrder()));
    }

    /**
     * Retrieves a list of user roles associated with the provided username and email.
     * The method performs a database query to fetch user roles from the `user_roles` table
     * matched with the corresponding user and role details.
     *
     * @param username the username to filter the user roles by
     * @param email    the email to filter the user roles by
     * @return a list of {@link UserRoleEntity} objects that match the specified username and email
     */
    public List<UserRoleEntity> get(final String username, final String email) {
        String sql = """ 
                SELECT ur.user_role_id, u.*, r.*
                FROM users u
                JOIN user_roles ur ON u.user_id = ur.user_id
                JOIN roles r ON ur.role_id = r.role_id
                WHERE u.username = ?
                AND u.email = ?;
                """;
        return jdbcTemplate.query(sql, new UserRoleMapper(), username, email);
    }

    /**
     * Retrieves a list of roles assigned to the specified user ID.
     * <p>
     * This method queries the database to find all roles associated with the user
     * by joining the {@code roles} and {@code user_roles} tables.
     *
     * @param userId the ID of the user for whom roles should be fetched
     * @return a list of {@link RoleEntity} objects representing the user's roles
     * @throws org.springframework.dao.DataAccessException if a data access error occurs
     */
    public List<RoleEntity> getRoles(final long userId) {
        String sql = """
                SELECT r.*
                FROM roles r
                JOIN user_roles ur ON r.role_id = ur.role_id
                WHERE ur.user_id = ?;
                """;
        return jdbcTemplate.query(sql, new RoleMapper(), userId);
    }

}
