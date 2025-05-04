package pl.derleta.authorization.domain.builder.impl;

import pl.derleta.authorization.domain.builder.ConfirmationTokenBuilder;
import pl.derleta.authorization.domain.model.ConfirmationToken;
import pl.derleta.authorization.domain.model.User;

import java.sql.Timestamp;

/**
 * Implementation of the {@link ConfirmationTokenBuilder} interface.
 * This class provides a concrete builder for constructing instances of {@link ConfirmationToken}.
 * It allows setting various properties of the confirmation token and constructing a fully initialized object.
 */
public class ConfirmationTokenBuilderImpl implements ConfirmationTokenBuilder {

    private long tokenId;
    private User user;
    private String token;
    private Timestamp expirationDate;

    @Override
    public ConfirmationTokenBuilder tokenId(long tokenId) {
        this.tokenId = tokenId;
        return this;
    }

    @Override
    public ConfirmationTokenBuilder user(User user) {
        this.user = user;
        return this;
    }

    @Override
    public ConfirmationTokenBuilder token(String token) {
        this.token = token;
        return this;
    }

    @Override
    public ConfirmationTokenBuilder expirationDate(Timestamp expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    @Override
    public ConfirmationToken build() {
        return new ConfirmationToken(tokenId, token, user, expirationDate);
    }

}
