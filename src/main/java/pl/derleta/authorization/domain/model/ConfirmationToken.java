package pl.derleta.authorization.domain.model;

import java.sql.Timestamp;

public record ConfirmationToken(long tokenId, String token, User user, Timestamp expirationDate) {

}
