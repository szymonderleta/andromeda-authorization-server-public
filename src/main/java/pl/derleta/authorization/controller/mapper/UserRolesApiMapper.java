package pl.derleta.authorization.controller.mapper;

import pl.derleta.authorization.domain.entity.UserRoleEntity;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.model.UserRoles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for mapping between user roles-related entities and models.
 * This class provides methods for converting {@link UserRoleEntity} objects
 * into corresponding API-level models like {@link UserRoles}.
 */
public final class UserRolesApiMapper {

    private UserRolesApiMapper() {
    }

    /**
     * Converts a list of {@link UserRoleEntity} instances to a {@link UserRoles} object.
     * Extracts the {@link User} from the first encountered {@link UserRoleEntity}
     * and collects all unique {@link Role} instances associated with the entities.
     *
     * @param userRolesEntities a list of {@link UserRoleEntity} objects containing user and role information
     * @return a {@link UserRoles} object containing the mapped {@link User} and a set of distinct {@link Role} objects
     */
    public static UserRoles toUserRoles(final List<UserRoleEntity> userRolesEntities) {
        User user = null;
        Set<Role> roles = new HashSet<>();
        for (var item : userRolesEntities) {
            if (user == null) user = UserApiMapper.toUser(item.getUserEntity());
            if (item.getRoleEntity() != null) roles.add(RoleApiMapper.toRole(item.getRoleEntity()));
        }
        return new UserRoles(user, roles);
    }

}
