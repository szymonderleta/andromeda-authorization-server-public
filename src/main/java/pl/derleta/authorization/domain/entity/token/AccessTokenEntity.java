package pl.derleta.authorization.domain.entity.token;

import pl.derleta.authorization.domain.entity.UserEntity;

import java.sql.Timestamp;

public class AccessTokenEntity extends TokenEntity {

    public AccessTokenEntity() {
    }

    public AccessTokenEntity(long tokenId, UserEntity user, String token, Timestamp expirationDate) {
        super(tokenId, user, token, expirationDate);
    }

}
