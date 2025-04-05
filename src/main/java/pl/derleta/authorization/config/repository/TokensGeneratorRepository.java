package pl.derleta.authorization.config.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pl.derleta.authorization.config.model.UserRolesSecurityEntity;
import pl.derleta.authorization.config.model.UserSecurity;
import pl.derleta.authorization.config.model.UserSecurityMapper;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.entity.UserEntity;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Repository class that facilitates database operations for token generation
 * and user security details retrieval. It uses JdbcTemplate to execute SQL
 * queries and maps database result sets to the application's user and role
 * entities.
 */
@Repository
public class TokensGeneratorRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructs a new instance of TokensGeneratorRepository and initializes it
     * with a JdbcTemplate object using the provided DataSource.
     *
     * @param dataSource the DataSource to be used for database interactions
     */
    @Autowired
    public TokensGeneratorRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Retrieves a user along with their associated roles from the database based on the provided login identifier.
     *
     * @param login the username of the user for whom the details are being retrieved
     * @return an Optional containing the UserSecurity object if a user with the given login exists, or an empty Optional otherwise
     */
    public Optional<UserSecurity> findByLogin(final String login) {
        String sql = """
                   SELECT u.*, r.*
                   FROM users u
                   JOIN user_roles ur ON u.user_id = ur.user_id
                   JOIN roles r ON ur.role_id = r.role_id
                   WHERE u.username = ?;
                """;
        List<UserRolesSecurityEntity> resultSet = jdbcTemplate.query(sql, new UserRolesSecurityMapper(), login);
        return getUserWithRoles(resultSet);
    }

    /**
     * Retrieves a user along with their associated roles from the database based on the provided email.
     *
     * @param email the email of the user for whom the details are being retrieved
     * @return an Optional containing the UserSecurity object if a user with the given email exists, or an empty Optional otherwise
     */
    public Optional<UserSecurity> findByEmail(final String email) {
        String sql = """
                   SELECT u.*, r.*
                   FROM users u
                   JOIN user_roles ur ON u.user_id = ur.user_id
                   JOIN roles r ON ur.role_id = r.role_id
                   WHERE u.email = ?;
                """;
        List<UserRolesSecurityEntity> resultSet = jdbcTemplate.query(sql, new UserRolesSecurityMapper(), email);
        return getUserWithRoles(resultSet);
    }

    /**
     * Retrieves a user along with their associated roles from a list of user-role entities.
     *
     * @param usersRolesEntityList a list of UserRolesSecurityEntity objects, each containing user and role information
     * @return an Optional containing the UserSecurity object with user details and roles if a valid user is found, or an empty Optional if no valid user exists
     */
    private Optional<UserSecurity> getUserWithRoles(List<UserRolesSecurityEntity> usersRolesEntityList) {
        UserEntity user = usersRolesEntityList.stream()
                .map(UserRolesSecurityEntity::getUserEntity)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        if (user != null) {
            Set<RoleEntity> roles = usersRolesEntityList.stream()
                    .map(UserRolesSecurityEntity::getRoleEntity)
                    .collect(Collectors.toSet());
            UserSecurity userSecurity = UserSecurityMapper.toUserSecurity(user, roles);
            return Optional.of(userSecurity);
        }
        return Optional.empty();
    }

    /**
     * UserRolesSecurityMapper is an implementation of the RowMapper interface that maps the rows of a ResultSet
     * to a UserRolesSecurityEntity object. It is used to construct an instance of UserRolesSecurityEntity by
     * extracting user and role data from the ResultSet.
     * <p>
     * The mapper retrieves fields related to the UserEntity, such as user ID, username, email, and password,
     * as well as fields related to the RoleEntity, such as role ID and role name.
     * <p>
     * The extracted data is then used to create instances of UserEntity and RoleEntity, which are subsequently
     * combined into a single UserRolesSecurityEntity object.
     */
    private static class UserRolesSecurityMapper implements RowMapper<UserRolesSecurityEntity> {
        @Override
        public UserRolesSecurityEntity mapRow(ResultSet resultSet, int rowNum) throws SQLException {

            long userId = resultSet.getLong("u.user_id");
            String username = resultSet.getString("u.username");
            String email = resultSet.getString("u.email");
            String password = resultSet.getString("u.password");
            UserEntity userEntity = new UserEntity(userId, username, email, password);

            int roleId = resultSet.getInt("role_id");
            String roleName = resultSet.getString("role_name");
            RoleEntity roleEntity = new RoleEntity(roleId, roleName);

            return new UserRolesSecurityEntity(userEntity, roleEntity);
        }
    }

}
