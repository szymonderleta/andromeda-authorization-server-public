package pl.derleta.authorization.controller.mapper;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.entity.UserRoleEntity;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.model.UserRole;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class UserRoleApiMapperTest {

    @Test
    void shouldReturnEmptyListWhenInputListIsEmpty_shouldReturnEmptyList() {
        // Arrange
        List<UserRoleEntity> entities = List.of();

        // Act
        List<UserRole> result = UserRoleApiMapper.toUserRolesList(entities);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void shouldHandleNullElementsInInputList_withNullElementsInInputList_shouldFilterOutNulls() {
        // Arrange
        UserRoleEntity validEntity = new UserRoleEntity(1L, mock(UserEntity.class), mock(RoleEntity.class));
        List<UserRoleEntity> entities = new ArrayList<>();
        entities.add(validEntity);
        entities.add(null);

        User mockUser = mock(User.class);
        Role mockRole = mock(Role.class);

        try (MockedStatic<UserApiMapper> userApiMapperMock = Mockito.mockStatic(UserApiMapper.class);
             MockedStatic<RoleApiMapper> roleApiMapperMock = Mockito.mockStatic(RoleApiMapper.class)) {

            userApiMapperMock.when(() -> UserApiMapper.toUser(validEntity.getUserEntity())).thenReturn(mockUser);
            roleApiMapperMock.when(() -> RoleApiMapper.toRole(validEntity.getRoleEntity())).thenReturn(mockRole);

            // Act
            List<UserRole> result = UserRoleApiMapper.toUserRolesList(entities);

            // Assert
            assertEquals(1, result.size());
            assertEquals(validEntity.getUserRoleId(), result.getFirst().userRoleId());
            assertEquals(mockUser, result.getFirst().user());
            assertEquals(mockRole, result.getFirst().role());
        }
    }

    @Test
    void shouldHandleUserRoleEntityWithNullUserOrRole_withNullUserOrRole_shouldProcessCorrectly() {
        // Arrange
        UserRoleEntity entityWithNullUser = new UserRoleEntity(1L, null, mock(RoleEntity.class));
        UserRoleEntity entityWithNullRole = new UserRoleEntity(2L, mock(UserEntity.class), null);
        List<UserRoleEntity> entities = List.of(entityWithNullUser, entityWithNullRole);

        Role mockRole = mock(Role.class);
        User mockUser = mock(User.class);

        try (MockedStatic<RoleApiMapper> roleApiMapperMock = Mockito.mockStatic(RoleApiMapper.class);
             MockedStatic<UserApiMapper> userApiMapperMock = Mockito.mockStatic(UserApiMapper.class)) {
            roleApiMapperMock.when(() -> RoleApiMapper.toRole(entityWithNullUser.getRoleEntity())).thenReturn(mockRole);
            userApiMapperMock.when(() -> UserApiMapper.toUser(entityWithNullRole.getUserEntity())).thenReturn(mockUser);

            // Act
            List<UserRole> result = UserRoleApiMapper.toUserRolesList(entities);

            // Assert
            assertEquals(2, result.size());
            assertEquals(mockRole, result.get(0).role());
            assertNull(result.get(0).user());
            assertEquals(mockUser, result.get(1).user());
            assertNull(result.get(1).role());
        }
    }

    @Test
    void shouldMapUserRoleEntityToUserRole_withValidEntity_shouldReturnMappedObject() {
        // Arrange
        long userRoleId = 1L;
        UserEntity mockUserEntity = mock(UserEntity.class);
        RoleEntity mockRoleEntity = mock(RoleEntity.class);
        UserRoleEntity entity = new UserRoleEntity(userRoleId, mockUserEntity, mockRoleEntity);

        User mockUser = Mockito.mock(User.class);
        Role mockRole = Mockito.mock(Role.class);

        try (MockedStatic<UserApiMapper> userApiMapperMock = Mockito.mockStatic(UserApiMapper.class);
             MockedStatic<RoleApiMapper> roleApiMapperMock = Mockito.mockStatic(RoleApiMapper.class)) {

            userApiMapperMock.when(() -> UserApiMapper.toUser(mockUserEntity)).thenReturn(mockUser);
            roleApiMapperMock.when(() -> RoleApiMapper.toRole(mockRoleEntity)).thenReturn(mockRole);

            // Act
            UserRole result = UserRoleApiMapper.toUserRoles(entity);

            // Assert
            assertEquals(userRoleId, result.userRoleId());
            assertEquals(mockUser, result.user());
            assertEquals(mockRole, result.role());
        }
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        long userRoleId1 = 1L;
        long userRoleId2 = 2L;

        UserEntity mockUserEntity1 = mock(UserEntity.class);
        RoleEntity mockRoleEntity1 = mock(RoleEntity.class);
        UserRoleEntity entity1 = new UserRoleEntity(userRoleId1, mockUserEntity1, mockRoleEntity1);

        UserEntity mockUserEntity2 = mock(UserEntity.class);
        RoleEntity mockRoleEntity2 = mock(RoleEntity.class);
        UserRoleEntity entity2 = new UserRoleEntity(userRoleId2, mockUserEntity2, mockRoleEntity2);

        User mockUser1 = mock(User.class);
        Role mockRole1 = mock(Role.class);

        User mockUser2 = mock(User.class);
        Role mockRole2 = mock(Role.class);

        List<UserRoleEntity> entities = List.of(entity1, entity2);

        try (MockedStatic<UserApiMapper> userApiMapperMock = Mockito.mockStatic(UserApiMapper.class);
             MockedStatic<RoleApiMapper> roleApiMapperMock = Mockito.mockStatic(RoleApiMapper.class)) {

            userApiMapperMock.when(() -> UserApiMapper.toUser(mockUserEntity1)).thenReturn(mockUser1);
            roleApiMapperMock.when(() -> RoleApiMapper.toRole(mockRoleEntity1)).thenReturn(mockRole1);

            userApiMapperMock.when(() -> UserApiMapper.toUser(mockUserEntity2)).thenReturn(mockUser2);
            roleApiMapperMock.when(() -> RoleApiMapper.toRole(mockRoleEntity2)).thenReturn(mockRole2);

            // Act
            List<UserRole> result = UserRoleApiMapper.toUserRolesList(entities);

            // Assert
            assertEquals(2, result.size());
            assertEquals(userRoleId1, result.getFirst().userRoleId());
            assertEquals(mockUser1, result.getFirst().user());
            assertEquals(mockRole1, result.getFirst().role());
            assertEquals(userRoleId2, result.get(1).userRoleId());
            assertEquals(mockUser2, result.get(1).user());
            assertEquals(mockRole2, result.get(1).role());
        }
    }

}
