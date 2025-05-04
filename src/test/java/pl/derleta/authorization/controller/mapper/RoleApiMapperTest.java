package pl.derleta.authorization.controller.mapper;

import org.junit.jupiter.api.Test;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.model.Role;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RoleApiMapperTest {

    @Test
    void toRoles_withValidRoleEntities_shouldReturnCorrectRoles() {
        // Arrange
        Set<RoleEntity> roleEntities = Set.of(
                new RoleEntity(1, "ADMIN"),
                new RoleEntity(2, "USER")
        );

        // Act
        Set<Role> roles = RoleApiMapper.toRoles(roleEntities);

        // Assert
        assertEquals(2, roles.size());
        assertEquals(Set.of(
                new Role(1, "ADMIN"),
                new Role(2, "USER")
        ), roles);
    }

    @Test
    void toRoles_withEmptyRoleEntitySet_shouldReturnEmptyRoleSet() {
        // Arrange
        Set<RoleEntity> roleEntities = Set.of();

        // Act
        Set<Role> roles = RoleApiMapper.toRoles(roleEntities);

        // Assert
        assertEquals(0, roles.size());
    }

    @Test
    void toRoles_withNullRoleEntityFields_shouldHandleNullFieldsCorrectly() {
        // Arrange
        Set<RoleEntity> roleEntities = Set.of(
                new RoleEntity(0, null),
                new RoleEntity(3, "MANAGER")
        );

        // Act
        Set<Role> roles = RoleApiMapper.toRoles(roleEntities);

        // Assert
        assertEquals(2, roles.size());
        assertEquals(Set.of(
                new Role(0, null),
                new Role(3, "MANAGER")
        ), roles);
    }

    @Test
    void toRole_withValidRoleEntity_shouldReturnCorrectRole() {
        // Arrange
        RoleEntity roleEntity = new RoleEntity(1, "ADMIN");

        // Act
        Role actualRole = RoleApiMapper.toRole(roleEntity);

        // Assert
        assertEquals(1, actualRole.roleId());
        assertEquals("ADMIN", actualRole.roleName());
    }

    @Test
    void toRole_withNullRoleName_shouldReturnRoleWithNullName() {
        // Arrange
        RoleEntity roleEntity = new RoleEntity(2, null);

        // Act
        Role actualRole = RoleApiMapper.toRole(roleEntity);

        // Assert
        assertEquals(2, actualRole.roleId());
        assertNull(actualRole.roleName());
    }

    @Test
    void toRole_withZeroRoleId_shouldReturnRoleWithZeroId() {
        // Arrange
        RoleEntity roleEntity = new RoleEntity(0, "USER");

        // Act
        Role actualRole = RoleApiMapper.toRole(roleEntity);

        // Assert
        assertEquals(0, actualRole.roleId());
        assertEquals("USER", actualRole.roleName());
    }

}
