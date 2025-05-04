package pl.derleta.authorization.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.repository.mapper.RoleMapper;
import pl.derleta.authorization.repository.sort.SortParameters;
import pl.derleta.authorization.utils.ValidatorUtils;

import javax.sql.DataSource;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * RoleRepository is responsible for managing CRUD operations and queries related to roles in the "roles" table.
 * It provides methods to interact with the database, including retrieving, saving, updating,
 * and deleting role entities, as well as applying filters and pagination. The interactions
 * with the database are facilitated using JdbcTemplate.
 */
@Repository
public class RoleRepository implements RepositoryClass {

    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("role_id", "role_name");
    private static final Set<String> ALLOWED_SORT_ORDERS = Set.of("ASC", "DESC");


    private final JdbcTemplate jdbcTemplate;

    /**
     * Initializes the RoleRepository with a given DataSource.
     * Creates a JdbcTemplate instance using the provided DataSource to enable interaction with the database.
     *
     * @param dataSource the DataSource object used to configure the JdbcTemplate for database operations
     */
    @Autowired
    public RoleRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Retrieves the total number of roles in the "roles" table.
     *
     * @return the total count of roles as an Integer.
     */
    public Integer getSize() {
        String sql = """
                SELECT COUNT(*) FROM roles;
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    /**
     * Retrieves a paginated list of roles from the "roles" table.
     *
     * @param offset the starting position of the records to retrieve
     * @param size   the maximum number of records to retrieve
     * @return a list of RoleEntity objects representing the retrieved roles
     */
    public List<RoleEntity> getPage(final int offset, final int size) {
        String sql = """ 
                SELECT role_id, role_name
                FROM roles
                LIMIT ?, ?;
                """;
        return jdbcTemplate.query(sql, new RoleMapper(), offset, size);
    }

    /**
     * Retrieves a list of roles from the "roles" table that match the specified role name filter.
     *
     * @param roleName the partial role name to filter by; supports partial matching using "LIKE".
     * @return a list of RoleEntity objects matching the specified role name filter.
     */
    public List<RoleEntity> findAll(final String roleName) {
        String sql = """
                    SELECT role_id, role_name
                    FROM roles
                    WHERE role_name LIKE ?;
                """;
        String roleNameParam = "%" + roleName + "%";
        return jdbcTemplate.query(sql, new RoleMapper(), roleNameParam);
    }

    /**
     * Retrieves a role from the database based on the provided role ID.
     * Queries the "roles" table and maps the result to a {@link RoleEntity} object.
     * If no matching record is found, the method returns null.
     *
     * @param roleId the ID of the role to retrieve
     * @return the corresponding {@link RoleEntity} object if found; null otherwise
     */
    public RoleEntity findById(final int roleId) {
        String sql = """
                  SELECT role_id, role_name
                  FROM roles
                  WHERE role_id = ?;
                """;
        try {
            return jdbcTemplate.queryForObject(sql, new RoleMapper(), roleId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Saves a new role in the database.
     * <p>
     * Inserts a row into the "roles" table with the provided role ID and role details.
     * If a duplicate key exception occurs (e.g., a role with the same ID already exists),
     * the method returns 0 without performing the insertion.
     *
     * @param role_id the ID of the role to be saved
     * @param role    the Role object containing the details of the role to be saved
     * @return the number of rows affected (typically 1 if successful, 0 if a duplicate key exception occurs)
     */
    public int save(final int role_id, final Role role) {
        String sql = """ 
                    INSERT INTO roles (role_id, role_name)
                    VALUES (?, ?);
                """;
        try {
            return jdbcTemplate.update(sql,
                    role_id,
                    role.roleName());
        } catch (DuplicateKeyException e) {
            return 0;
        }
    }

    /**
     * Updates the details of a role in the database based on the given role ID.
     * The method updates the role name for the specified role ID in the "roles" table.
     *
     * @param roleId the ID of the role to be updated
     * @param role   the Role object containing the updated details of the role
     * @return the number of rows affected by the update operation
     */
    public int update(final int roleId, final Role role) {
        String sql = """
                  UPDATE roles SET
                  role_name = ?
                  WHERE role_id = ?;
                """;
        return jdbcTemplate.update(sql,
                role.roleName(),
                roleId);
    }

    /**
     * Deletes a role from the "roles" table based on the provided role ID.
     *
     * @param roleId the ID of the role to be deleted
     * @return the number of rows affected by the delete operation
     */
    public int deleteById(final int roleId) {
        String sql = """ 
                    DELETE FROM roles
                    WHERE role_id = ?;
                """;
        return jdbcTemplate.update(sql, roleId);
    }


    /**
     * Retrieves the next available role ID by finding the maximum role ID in the "roles" table
     * and incrementing it by 1.
     *
     * @return the next available role ID as an Integer; if the "roles" table is empty, it may return null
     */
    public Integer getNextRoleId() {
        String idSql = "SELECT MAX(role_id) + 1 FROM roles";
        return jdbcTemplate.queryForObject(idSql, Integer.class);
    }

    /**
     * Retrieves a sorted and paginated set of roles from the database with an optional role name filter.
     * <p>
     * This method queries the database for roles matching the specified role name filter,
     * sorted by the given column and order, and returns a distinct set of roles in the requested page range.
     *
     * @param offset         the starting position of the records to retrieve
     * @param size           the maximum number of records to retrieve
     * @param sortByParam    the column name to sort the results by
     * @param sortOrderParam the sorting order (e.g., "ASC" or "DESC")
     * @param roleName       the partial role name to filter by; supports partial matching using "LIKE"
     * @return a LinkedHashSet of RoleEntity objects representing the filtered, sorted, and paginated roles
     */
    public Set<RoleEntity> getSortedPageWithFilters(final int offset, final int size, final String sortByParam, final String sortOrderParam, final String roleName) {
        String sql = buildQueryForSortedPage(sortByParam, sortOrderParam);
        String roleNameParam = "%" + roleName + "%";

        List<RoleEntity> roles = jdbcTemplate.query(sql, new RoleMapper(), roleNameParam, offset, size);
        return new LinkedHashSet<>(roles);
    }

    /**
     * Builds a SQL query string for retrieving a sorted and paginated list of roles from the "roles" table.
     * Validates the provided sorting parameters before constructing the query.
     *
     * @param sortBy    the column name to sort the results by; must be one of the allowed sort columns
     * @param sortOrder the sorting order (e.g., "ASC" or "DESC"); must be one of the allowed sorting orders
     * @return the constructed SQL query string for retrieving the sorted and paginated roles
     */
    private static String buildQueryForSortedPage(final String sortBy, final String sortOrder) {
        ValidatorUtils.validateSortParameters(sortBy, sortOrder, ALLOWED_SORT_COLUMNS, ALLOWED_SORT_ORDERS);
        String normalizedSortOrder = sortOrder.toUpperCase(Locale.ROOT);
        SortParameters sortParameters = new SortParameters(sortBy, normalizedSortOrder);
        return String.format("""
                SELECT role_id, role_name
                FROM roles
                WHERE role_name LIKE ?
                ORDER BY %s %s
                LIMIT ?, ?;
                """.formatted(sortParameters.sortBy(), sortParameters.sortOrder()));
    }

    /**
     * Retrieves the count of roles that match the provided role name filter.
     *
     * @param roleName the role name filter to search for; supports partial matching
     * @return the count of roles that match the specified filter
     */
    public Integer getFiltersCount(final String roleName) {
        String sql = """ 
                SELECT COUNT(*)
                FROM roles
                WHERE role_name LIKE ?
                """;
        String roleNameParam = "%" + roleName + "%";
        return jdbcTemplate.queryForObject(sql, Integer.class, roleNameParam);
    }

}
