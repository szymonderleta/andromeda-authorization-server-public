package pl.derleta.authorization.domain.response;

import org.springframework.hateoas.RepresentationModel;

public class RoleResponse extends RepresentationModel<RoleResponse> {

    private int roleId;
    private String roleName;

    public RoleResponse(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public RoleResponse() {
    }

    public int getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

}
