package pl.derleta.authorization.controller.mapper;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.entity.UserRoleEntity;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.model.UserRoles;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserRolesApiMapperTest {

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        UserEntity mockUserEntity = Mockito.mock(UserEntity.class);
        RoleEntity mockRoleEntity1 = Mockito.mock(RoleEntity.class);
        RoleEntity mockRoleEntity2 = Mockito.mock(RoleEntity.class);

        User mockUser = new User(1L, "testUser", "test_password", "<EMAIL>");
        Role role1 = new Role(1, "ROLE_ADMIN");
        Role role2 = new Role(2, "ROLE_USER");

        try (MockedStatic<UserApiMapper> userApiMapperMock = Mockito.mockStatic(UserApiMapper.class);
             MockedStatic<RoleApiMapper> roleApiMapperMock = Mockito.mockStatic(RoleApiMapper.class)) {

            userApiMapperMock.when(() -> UserApiMapper.toUser(mockUserEntity)).thenReturn(mockUser);
            roleApiMapperMock.when(() -> RoleApiMapper.toRole(mockRoleEntity1)).thenReturn(role1);
            roleApiMapperMock.when(() -> RoleApiMapper.toRole(mockRoleEntity2)).thenReturn(role2);

            UserRoleEntity entity1 = new UserRoleEntity(1L, mockUserEntity, mockRoleEntity1);
            UserRoleEntity entity2 = new UserRoleEntity(2L, mockUserEntity, mockRoleEntity2);
            List<UserRoleEntity> userRolesEntities = List.of(entity1, entity2);

            // Act
            UserRoles result = UserRolesApiMapper.toUserRoles(userRolesEntities);

            // Assert
            assertEquals(mockUser, result.user());
            assertEquals(Set.of(role1, role2), result.roles());
        }
    }

    @Test
    void getPage_withNullRoleEntities_shouldReturnCorrectResult() {
        // Arrange
        UserEntity mockUserEntity = Mockito.mock(UserEntity.class);
        User mockUser = new User(1L, "testUser", "test_password", "<EMAIL>");
        try (MockedStatic<UserApiMapper> userApiMapperMock = Mockito.mockStatic(UserApiMapper.class)) {
            userApiMapperMock.when(() -> UserApiMapper.toUser(mockUserEntity)).thenReturn(mockUser);

            UserRoleEntity entity1 = new UserRoleEntity(1L, mockUserEntity, null);
            UserRoleEntity entity2 = new UserRoleEntity(2L, mockUserEntity, null);
            List<UserRoleEntity> userRolesEntities = List.of(entity1, entity2);

            // Act
            UserRoles result = UserRolesApiMapper.toUserRoles(userRolesEntities);

            // Assert
            assertEquals(mockUser, result.user());
            assertEquals(Set.of(), result.roles());
        }
    }

    @Test
    void getPage_withEmptyInput_shouldReturnEmptyUserRoles() {
        // Arrange
        List<UserRoleEntity> userRolesEntities = List.of();

        // Act
        UserRoles result = UserRolesApiMapper.toUserRoles(userRolesEntities);

        // Assert
        assertNull(result.user());
        assertEquals(Set.of(), result.roles());
    }

    @Test
    void getPage_withNullUserEntity_shouldHandleGracefully() {
        // Arrange
        RoleEntity mockRoleEntity = Mockito.mock(RoleEntity.class);
        Role role = new Role(1, "ROLE_ADMIN");
        UserRoleEntity entity = new UserRoleEntity(1L, null, mockRoleEntity);
        List<UserRoleEntity> userRolesEntities = List.of(entity);

        try (MockedStatic<RoleApiMapper> roleApiMapperMock = Mockito.mockStatic(RoleApiMapper.class)) {
            roleApiMapperMock.when(() -> RoleApiMapper.toRole(mockRoleEntity)).thenReturn(role);

            // Act
            UserRoles result = UserRolesApiMapper.toUserRoles(userRolesEntities);

            // Assert
            assertNull(result.user());
            assertEquals(Set.of(role), result.roles());
        }
    }

    @Test
    void getPage_withAllNullProperties_shouldReturnNullUserAndEmptyRoles() {
        // Arrange
        UserRoleEntity entity = new UserRoleEntity();
        List<UserRoleEntity> userRolesEntities = List.of(entity);

        // Act
        UserRoles result = UserRolesApiMapper.toUserRoles(userRolesEntities);

        // Assert
        assertNull(result.user());
        assertEquals(Set.of(), result.roles());
    }

}
