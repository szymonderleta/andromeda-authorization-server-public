package pl.derleta.authorization.domain.entity;

import java.util.Objects;

public class UserRoleEntity {

    long userRoleId;
    UserEntity userEntity;
    RoleEntity roleEntity;

    public UserRoleEntity(long userRoleId, UserEntity userEntity, RoleEntity roleEntity) {
        this.userRoleId = userRoleId;
        this.userEntity = userEntity;
        this.roleEntity = roleEntity;
    }

    public UserRoleEntity() {
    }

    public long getUserRoleId() {
        return this.userRoleId;
    }

    public void setUserRoleId(long userRoleId) {
        this.userRoleId = userRoleId;
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
        if (!(o instanceof UserRoleEntity that)) return false;
        if (userRoleId != that.userRoleId) return false;
        if (!Objects.equals(userEntity, that.userEntity)) return false;
        return Objects.equals(roleEntity, that.roleEntity);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(userRoleId);
        result = 31 * result + (userEntity != null ? userEntity.hashCode() : 0);
        result = 31 * result + (roleEntity != null ? roleEntity.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserRolesEntity{" +
                "userRoleId=" + userRoleId +
                ", userEntity=" + userEntity +
                ", roleEntity=" + roleEntity +
                '}';
    }

}
