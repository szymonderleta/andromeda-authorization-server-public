package pl.derleta.authorization.domain.response;

import org.springframework.hateoas.RepresentationModel;

public class UserRoleResponse extends RepresentationModel<UserRoleResponse> {

    private long userRoleId;
    private UserResponse user;
    private RoleResponse role;

    public UserRoleResponse() {
    }

    public long getUserRoleId() {
        return userRoleId;
    }

    public void setUserRoleId(long userRoleId) {
        this.userRoleId = userRoleId;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public RoleResponse getRole() {
        return role;
    }

    public void setRole(RoleResponse role) {
        this.role = role;
    }

}
