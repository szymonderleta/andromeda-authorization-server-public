package pl.derleta.authorization.config.model;

import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.entity.UserEntity;

import java.util.Objects;

public class UserRolesSecurityEntity {

    UserEntity userEntity;
    RoleEntity roleEntity;

    public UserRolesSecurityEntity(UserEntity userEntity, RoleEntity roleEntity) {
        this.userEntity = userEntity;
        this.roleEntity = roleEntity;
    }

    public UserRolesSecurityEntity() {
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    public RoleEntity getRoleEntity() {
        return roleEntity;
    }

    public void setRoleEntity(RoleEntity roleEntity) {
        this.roleEntity = roleEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRolesSecurityEntity that)) return false;

        if (!Objects.equals(userEntity, that.userEntity)) return false;
        return Objects.equals(roleEntity, that.roleEntity);
    }

    @Override
    public int hashCode() {
        int result = userEntity != null ? userEntity.hashCode() : 0;
        result = 31 * result + (roleEntity != null ? roleEntity.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserRolesSecurityEntity{" +
                "userEntity=" + userEntity +
                ", roleEntity=" + roleEntity +
                '}';
    }

}
