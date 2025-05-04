package pl.derleta.authorization.domain.builder;

import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.model.UserRole;

/**
 * A builder interface for creating instances of {@link UserRole}.
 * This interface provides methods to set the properties of a UserRole object through method chaining,
 * enabling the creation of a fully configured {@link UserRole} instance.
 */
public interface UserRolesBuilder {

    UserRolesBuilder userRoleId(long userRoleId);

    UserRolesBuilder user(User user);

    UserRolesBuilder role(Role role);

    UserRole build();

}
