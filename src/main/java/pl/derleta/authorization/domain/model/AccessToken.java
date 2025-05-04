package pl.derleta.authorization.domain.model;

import java.sql.Timestamp;

public record AccessToken(long tokenId, String token, User user, Timestamp expirationDate) {

}
