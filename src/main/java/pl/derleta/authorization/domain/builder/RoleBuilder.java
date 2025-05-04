package pl.derleta.authorization.domain.builder;

import pl.derleta.authorization.domain.model.Role;

/**
 * A builder interface for creating instances of {@link Role}.
 * This interface provides methods for setting the attributes of a Role object
 * and building a fully initialized Role instance via method chaining.
 */
public interface RoleBuilder {

    RoleBuilder roleId(int roleId);

    RoleBuilder roleName(String roleName);

    Role build();

}
