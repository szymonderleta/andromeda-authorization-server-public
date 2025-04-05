package pl.derleta.authorization.domain.response;

import org.springframework.hateoas.RepresentationModel;

import java.sql.Timestamp;

public class RefreshTokenResponse extends RepresentationModel<RefreshTokenResponse> {

    private long tokenId;
    private UserResponse user;
    private String token;
    private Timestamp expirationDate;

    public RefreshTokenResponse() {
    }

    public long getTokenId() {
        return tokenId;
    }

    public void setTokenId(long tokenId) {
        this.tokenId = tokenId;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Timestamp getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Timestamp expirationDate) {
        this.expirationDate = expirationDate;
    }

}
