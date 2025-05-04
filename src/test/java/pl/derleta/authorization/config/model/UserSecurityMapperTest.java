package pl.derleta.authorization.config.model;

import org.junit.jupiter.api.Test;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.entity.UserEntity;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserSecurityMapperTest {

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        UserEntity userEntity = new UserEntity(1L, "testUser", "test@mail.com", "password123");
        RoleEntity role1 = new RoleEntity(101, "ROLE_USER");
        RoleEntity role2 = new RoleEntity(102, "ROLE_ADMIN");
        Set<RoleEntity> roles = Set.of(role1, role2);

        // Act
        UserSecurity result = UserSecurityMapper.toUserSecurity(userEntity, roles);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testUser", result.getName());
        assertEquals("test@mail.com", result.getEmail());
        assertEquals("password123", result.getPassword());
        assertNotNull(result.getRoles());
        assertEquals(2, result.getRoles().size());
        assertTrue(result.getRoles().contains(new RoleSecurity(101, "ROLE_USER")));
        assertTrue(result.getRoles().contains(new RoleSecurity(102, "ROLE_ADMIN")));
    }

    @Test
    void getPage_withEmptyRoles_shouldReturnUserSecurityWithNoRoles() {
        // Arrange
        UserEntity userEntity = new UserEntity(2L, "anotherUser", "another@mail.com", "securePass");
        Set<RoleEntity> roles = Set.of();

        // Act
        UserSecurity result = UserSecurityMapper.toUserSecurity(userEntity, roles);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("anotherUser", result.getName());
        assertEquals("another@mail.com", result.getEmail());
        assertEquals("securePass", result.getPassword());
        assertNotNull(result.getRoles());
        assertTrue(result.getRoles().isEmpty());
    }

    @Test
    void getPage_withNullRoles_shouldHandleNullGracefully() {
        // Arrange
        UserEntity userEntity = new UserEntity(3L, "nullRolesUser", "nullroles@mail.com", "nullPassword");
        Set<RoleEntity> roles = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> UserSecurityMapper.toUserSecurity(userEntity, roles));
    }

    @Test
    void getPage_withNullUser_shouldThrowException() {
        // Arrange
        UserEntity userEntity = null;
        RoleEntity role = new RoleEntity(201, "ROLE_TEST");
        Set<RoleEntity> roles = Set.of(role);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> UserSecurityMapper.toUserSecurity(userEntity, roles));
    }

}
