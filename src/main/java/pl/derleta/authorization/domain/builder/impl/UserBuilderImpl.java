package pl.derleta.authorization.domain.builder.impl;

import pl.derleta.authorization.domain.builder.UserBuilder;
import pl.derleta.authorization.domain.model.User;

/**
 * Implementation of the {@link UserBuilder} interface.
 * This class provides a concrete implementation for building {@link User} objects.
 * It supports the fluent builder pattern, allowing for the step-by-step creation
 * of a {@link User} instance by setting its properties.
 */
public class UserBuilderImpl implements UserBuilder {

    private long userId;
    private String username;
    private String password;
    private String email;

    @Override
    public UserBuilder userId(long userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public UserBuilder username(String username) {
        this.username = username;
        return this;
    }

    @Override
    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }

    @Override
    public UserBuilder password(String password) {
        this.password = password;
        return this;
    }

    @Override
    public User build() {
        return new User(userId, username, password, email);
    }

}
