package pl.derleta.authorization.domain.builder.impl;

import pl.derleta.authorization.domain.builder.RoleBuilder;
import pl.derleta.authorization.domain.model.Role;

/**
 * Implementation of the {@link RoleBuilder} interface.
 * This class provides a concrete implementation for building {@link Role} objects.
 * It supports the fluent builder pattern, enabling the step-by-step creation of a {@link Role} instance
 * by setting its properties.
 */
public class RoleBuilderImpl implements RoleBuilder {

    private int roleId;
    private String roleName;

    @Override
    public RoleBuilder roleId(int roleId) {
        this.roleId = roleId;
        return this;
    }

    @Override
    public RoleBuilder roleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    @Override
    public Role build() {
        return new Role(roleId, roleName);
    }

}
