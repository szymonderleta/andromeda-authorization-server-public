package pl.derleta.authorization.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pl.derleta.authorization.domain.entity.RoleEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RoleRepository is responsible for managing CRUD operations and queries related to roles in the "roles" table.
 * It provides methods to interact with the database, including retrieving, saving, updating,
 * and deleting role entities, as well as applying filters and pagination. The interactions
 * with the database are facilitated using JdbcTemplate.
 */
@Repository
public class RoleMapper implements RowMapper<RoleEntity> {
    @Override
    public RoleEntity mapRow(ResultSet resultSet, int i) throws SQLException {
        int roleId = resultSet.getInt("role_id");
        String roleName = resultSet.getString("role_name");
        return new RoleEntity(roleId, roleName);
    }
}
