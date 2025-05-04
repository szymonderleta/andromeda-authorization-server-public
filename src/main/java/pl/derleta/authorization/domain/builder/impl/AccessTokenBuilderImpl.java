package pl.derleta.authorization.domain.builder.impl;

import pl.derleta.authorization.domain.builder.AccessTokenBuilder;
import pl.derleta.authorization.domain.model.AccessToken;
import pl.derleta.authorization.domain.model.User;

import java.sql.Timestamp;

/**
 * Implementation of the {@link AccessTokenBuilder} interface.
 * This class provides a concrete implementation for building {@link AccessToken} objects.
 * It supports the fluent builder pattern, enabling the step-by-step creation of a {@link AccessToken} instance
 * by setting its properties such as the token ID, user, token, and expiration date.
 * <p>
 * This builder implementation ensures that a fully initialized instance of {@link AccessToken} can be created
 * by chaining method calls to set desired properties before invoking the build method.
 */
public class AccessTokenBuilderImpl implements AccessTokenBuilder {

    private long tokenId;
    private User user;
    private String token;
    private Timestamp expirationDate;

    @Override
    public AccessTokenBuilder tokenId(long tokenId) {
        this.tokenId = tokenId;
        return this;
    }

    @Override
    public AccessTokenBuilder user(User user) {
        this.user = user;
        return this;
    }

    @Override
    public AccessTokenBuilder token(String token) {
        this.token = token;
        return this;
    }

    @Override
    public AccessTokenBuilder expirationDate(Timestamp expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    @Override
    public AccessToken build() {
        return new AccessToken(tokenId, token, user, expirationDate);
    }

}
