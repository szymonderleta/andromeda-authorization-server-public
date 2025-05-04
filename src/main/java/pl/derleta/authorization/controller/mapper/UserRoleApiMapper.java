package pl.derleta.authorization.controller.mapper;

import pl.derleta.authorization.domain.builder.impl.UserRolesBuilderImpl;
import pl.derleta.authorization.domain.entity.UserRoleEntity;
import pl.derleta.authorization.domain.model.UserRole;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between UserRole-related entities and models.
 * This class provides static methods for converting between {@link UserRoleEntity},
 * {@link UserRole}, and supporting legacy methods for response conversion.
 */
public final class UserRoleApiMapper {

    private UserRoleApiMapper() {
    }

    /**
     * Converts a list of {@link UserRoleEntity} objects to a list of {@link UserRole} objects.
     *
     * @param userRolesEntities the list of {@link UserRoleEntity} objects to be converted
     * @return a list of {@link UserRole} objects corresponding to the input entities
     */
    public static List<UserRole> toUserRolesList(final List<UserRoleEntity> userRolesEntities) {
        if (userRolesEntities == null) return List.of();
        return userRolesEntities.stream()
                .filter(Objects::nonNull)
                .map(UserRoleApiMapper::toUserRoles)
                .collect(Collectors.toList());
    }

    /**
     * Converts a {@link UserRoleEntity} object to a {@link UserRole} object.
     *
     * @param entity the {@link UserRoleEntity} object to be converted
     * @return a {@link UserRole} object containing the data mapped from the given {@link UserRoleEntity}
     */
    public static UserRole toUserRoles(final UserRoleEntity entity) {
        if (entity == null) return null;
        return new UserRolesBuilderImpl()
                .userRoleId(entity.getUserRoleId())
                .user(UserApiMapper.toUser(entity.getUserEntity()))
                .role(RoleApiMapper.toRole(entity.getRoleEntity()))
                .build();
    }

}
