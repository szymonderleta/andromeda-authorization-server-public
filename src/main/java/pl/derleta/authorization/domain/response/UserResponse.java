package pl.derleta.authorization.domain.response;

import org.springframework.hateoas.RepresentationModel;

public class UserResponse extends RepresentationModel<UserResponse> {

    private long userId;
    private String username;
    private String email;

    public UserResponse() {
    }

    public UserResponse(long userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
