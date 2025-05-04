package pl.derleta.authorization.config.security.api;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public class AuthLoginRequest implements AuthRequest {

    @NotNull
    @Length(min = 5, max = 50)
    private String login;

    @NotNull
    @Length(min = 5, max = 64)
    private String password;

    public AuthLoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public AuthLoginRequest() {
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public String getPassword() {
        return password;
    }

}
