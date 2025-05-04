package pl.derleta.authorization.domain.builder;

import pl.derleta.authorization.domain.model.RefreshToken;
import pl.derleta.authorization.domain.model.User;

import java.sql.Timestamp;

/**
 * A builder interface for constructing instances of {@link RefreshToken}.
 * This interface provides methods to set the properties of a JWT token, enabling
 * the creation of a fully initialized {@link RefreshToken} object through method chaining.
 */
public interface RefreshTokenBuilder {

    RefreshTokenBuilder tokenId(long tokenId);

    RefreshTokenBuilder user(User user);

    RefreshTokenBuilder token(String token);

    RefreshTokenBuilder expirationDate(Timestamp expirationDate);

    RefreshToken build();

}
