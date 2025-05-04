package pl.derleta.authorization.config.model;

import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.entity.UserEntity;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * The UserSecurityMapper class provides utility methods for converting
 * UserEntity and associated RoleEntity objects into security-specific
 * representations used within the authentication and authorization process.
 * <p>
 * This class facilitates the mapping between domain-level entities and
 * their security counterparts, such as UserSecurity and RoleSecurity,
 * ensuring seamless integration with security frameworks.
 */
public class UserSecurityMapper {

    /**
     * Converts a UserEntity and a set of RoleEntity objects into a UserSecurity object.
     *
     * @param user  the UserEntity object containing user details, such as ID, username, email, and password
     * @param roles a set of RoleEntity objects representing the roles assigned to the user
     * @return a UserSecurity object containing the user's security-specific attributes, including roles
     */
    public static UserSecurity toUserSecurity(final UserEntity user, final Set<RoleEntity> roles) {
        Set<RoleSecurity> roleSecuritySet = toRolesSecurity(roles);
        return new UserSecurity(
                user.getUserId(),
                user.getUsername(), user.getEmail(), user.getPassword(),
                roleSecuritySet
        );
    }

    /**
     * Converts a set of RoleEntity objects into a set of RoleSecurity objects.
     * Each RoleEntity is transformed into a corresponding RoleSecurity using the
     * mapping method {@code UserSecurityMapper::toRoleSecurity}.
     *
     * @param roles a set of RoleEntity objects representing the roles within the application
     * @return a set of RoleSecurity objects corresponding to the input RoleEntity set
     */
    public static Set<RoleSecurity> toRolesSecurity(final Set<RoleEntity> roles) {
        return roles.stream().map(UserSecurityMapper::toRoleSecurity).collect(Collectors.toSet());
    }

    /**
     * Converts a RoleEntity object into a RoleSecurity object.
     *
     * @param role the RoleEntity object containing the role's ID and name
     * @return a RoleSecurity object with the corresponding ID and name from the RoleEntity
     */
    public static RoleSecurity toRoleSecurity(final RoleEntity role) {
        return new RoleSecurity(role.getRoleId(), role.getRoleName());
    }

}
