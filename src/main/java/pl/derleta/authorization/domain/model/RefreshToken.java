package pl.derleta.authorization.domain.model;

import java.sql.Timestamp;

public record RefreshToken(long tokenId, String token, User user, Timestamp expirationDate) {

}
