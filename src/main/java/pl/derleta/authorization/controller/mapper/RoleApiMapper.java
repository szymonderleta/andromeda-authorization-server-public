package pl.derleta.authorization.controller.mapper;

import pl.derleta.authorization.domain.builder.impl.RoleBuilderImpl;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.domain.response.RoleResponse;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class providing methods for mapping Role-related entities and models.
 * This class contains methods for converting between {@link RoleEntity}, {@link Role},
 * and {@link RoleResponse}.
 */
public final class RoleApiMapper {

    private RoleApiMapper() {
    }

    /**
     * Converts a set of {@link RoleEntity} objects to a set of {@link Role} objects.
     *
     * @param roles the set of {@link RoleEntity} objects to be converted
     * @return a set of {@link Role} objects corresponding to the input entities
     */
    public static Set<Role> toRoles(final Set<RoleEntity> roles) {
        return roles.stream().map(RoleApiMapper::toRole).collect(Collectors.toSet());
    }

    /**
     * Converts a {@link RoleEntity} to a {@link Role}.
     *
     * @param entity the {@link RoleEntity} to be converted
     * @return a {@link Role} containing the data mapped from the given {@link RoleEntity}
     */
    public static Role toRole(final RoleEntity entity) {
        return new RoleBuilderImpl()
                .roleId(entity.getRoleId())
                .roleName(entity.getRoleName())
                .build();
    }

}
