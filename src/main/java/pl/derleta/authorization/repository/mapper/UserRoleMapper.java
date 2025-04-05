package pl.derleta.authorization.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.entity.UserRoleEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class implements the RowMapper interface to map rows of a ResultSet
 * to instances of the UserRoleEntity class. The mapping process involves
 * extracting data for a user, role, and their association, and constructing
 * corresponding UserEntity, RoleEntity, and UserRoleEntity objects.
 * <p>
 * The SQL query columns used for extraction are assumed to follow a specific naming convention:
 * - User-related fields are prefixed with "u.".
 * - Role-related fields are prefixed with "r.".
 * - User-role-related fields are prefixed with "ur.".
 * <p>
 * Responsibilities:
 * - Extract the user details from the ResultSet and create a UserEntity object.
 * - Extract the role details from the ResultSet and create a RoleEntity object.
 * - Combine the UserEntity and RoleEntity objects with the mapped user role ID
 *   to create a UserRoleEntity.
 * <p>
 * Expected Column Mapping in the ResultSet:
 * - "ur.user_role_id": User-Role ID associated with the mapping.
 * - "u.user_id": User's unique identifier.
 * - "u.username": Username of the associated user.
 * - "u.email": Email of the user.
 * - "u.password": Password of the user.
 * - "r.role_id": Role's unique identifier.
 * - "r.role_name": Name of the associated role.
 * <p>
 * Throws:
 * - SQLException: If an error occurs while accessing the ResultSet.
 */
public class UserRoleMapper implements RowMapper<UserRoleEntity> {
    @Override
    public UserRoleEntity mapRow(ResultSet resultSet, int rowNum) throws SQLException {

        long userRoleId = resultSet.getLong("ur.user_role_id");

        long userId = resultSet.getLong("u.user_id");
        String username = resultSet.getString("u.username");
        String email = resultSet.getString("u.email");
        String password = resultSet.getString("u.password");
        UserEntity userEntity = new UserEntity(userId, username, email, password);

        int roleId = resultSet.getInt("r.role_id");
        String roleName = resultSet.getString("r.role_name");
        RoleEntity roleEntity = new RoleEntity(roleId, roleName);

        return new UserRoleEntity(userRoleId, userEntity, roleEntity);
    }
}
