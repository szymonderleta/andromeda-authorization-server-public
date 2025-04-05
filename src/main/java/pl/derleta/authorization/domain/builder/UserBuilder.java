package pl.derleta.authorization.domain.builder;

import pl.derleta.authorization.domain.model.User;

/**
 * A builder interface for creating instances of {@link User}.
 * This interface provides methods to set the properties of a User object, enabling
 * the creation of a fully initialized {@link User} instance. The methods support
 * method chaining for simplified and fluent construction.
 */
public interface UserBuilder {

    UserBuilder userId(long userId);

    UserBuilder username(String username);

    UserBuilder email(String email);

    UserBuilder password(String password);

    User build();

}
