package pl.derleta.authorization.domain.builder.impl;

import pl.derleta.authorization.domain.builder.RefreshTokenBuilder;
import pl.derleta.authorization.domain.model.RefreshToken;
import pl.derleta.authorization.domain.model.User;

import java.sql.Timestamp;


/**
 * Implementation of the {@link RefreshTokenBuilder} interface.
 * This class provides a concrete implementation for constructing {@link RefreshToken} objects.
 * It supports the fluent builder pattern, enabling the step-by-step creation of a {@link RefreshToken} instance
 * by setting its properties, such as the token ID, user, token, and expiration date.
 * <p>
 * The class ensures that a fully initialized {@link RefreshToken} instance can be created by chaining method calls
 * to set desired properties before invoking the build method.
 */
public class RefreshTokenBuilderImpl implements RefreshTokenBuilder {

    private long tokenId;
    private User user;
    private String token;
    private Timestamp expirationDate;

    @Override
    public RefreshTokenBuilder tokenId(long tokenId) {
        this.tokenId = tokenId;
        return this;
    }

    @Override
    public RefreshTokenBuilder user(User user) {
        this.user = user;
        return this;
    }

    @Override
    public RefreshTokenBuilder token(String token) {
        this.token = token;
        return this;
    }

    @Override
    public RefreshTokenBuilder expirationDate(Timestamp expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    @Override
    public RefreshToken build() {
        return new RefreshToken(tokenId, token, user, expirationDate);
    }

}
