package pl.derleta.authorization.domain.builder.impl;

import pl.derleta.authorization.domain.builder.UserRolesBuilder;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.model.UserRole;

/**
 * Implementation of the {@link UserRolesBuilder} interface.
 * This class provides a concrete builder for constructing instances of {@link UserRole}.
 * It uses the builder pattern to enable the step-by-step creation of a {@link UserRole} instance.
 * It allows setting the user role ID, user, and role before creating a fully initialized object.
 */
public class UserRolesBuilderImpl implements UserRolesBuilder {

    private long userRoleId;
    private User user;
    private Role role;

    @Override
    public UserRolesBuilder userRoleId(long userRoleId) {
        this.userRoleId = userRoleId;
        return this;
    }

    @Override
    public UserRolesBuilder user(User user) {
        this.user = user;
        return this;
    }

    @Override
    public UserRolesBuilder role(Role role) {
        this.role = role;
        return this;
    }

    @Override
    public UserRole build() {
        return new UserRole(userRoleId, user, role);
    }

}
