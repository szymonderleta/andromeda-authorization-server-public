package pl.derleta.authorization.domain.response;

import org.springframework.hateoas.RepresentationModel;

import java.util.Set;

public class UserRolesResponse extends RepresentationModel<UserRolesResponse> {

    private UserResponse user;
    private Set<RoleResponse> roles;

    public UserRolesResponse() {
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public Set<RoleResponse> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleResponse> roles) {
        this.roles = roles;
    }

}
