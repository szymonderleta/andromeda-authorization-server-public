package pl.derleta.authorization.domain.builder;

import pl.derleta.authorization.domain.model.ConfirmationToken;
import pl.derleta.authorization.domain.model.User;

import java.sql.Timestamp;

/**
 * A builder interface for constructing instances of {@link ConfirmationToken}.
 * This interface provides methods to set properties of a confirmation token and
 * allows chaining of calls to build a fully initialized {@link ConfirmationToken} object.
 */
public interface ConfirmationTokenBuilder {

    ConfirmationTokenBuilder tokenId(long tokenId);

    ConfirmationTokenBuilder user(User user);

    ConfirmationTokenBuilder token(String token);

    ConfirmationTokenBuilder expirationDate(Timestamp expirationDate);

    ConfirmationToken build();

}
