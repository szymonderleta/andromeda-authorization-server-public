package pl.derleta.authorization.domain.request;

public class TokenRequest implements Request {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
