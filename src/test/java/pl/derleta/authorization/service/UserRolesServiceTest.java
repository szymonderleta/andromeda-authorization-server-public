package pl.derleta.authorization.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.entity.UserRoleEntity;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.model.UserRoles;
import pl.derleta.authorization.repository.impl.UserRolesRepository;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserRolesServiceTest {

    @Autowired
    private UserRolesService userRolesService;

    @MockBean
    private UserRolesRepository userRolesRepository;

    @Test
    void getUserRoles_withValidParametersAndAdminRole_shouldReturnUserRoles() {
        long userId = 1L;
        String sortBy = "roleName";
        String sortOrder = "asc";
        String roleNameFilter = "admin";

        // Arrange
        User expectedUser = new User(userId, "testUser", "password", "test_user@test.com");
        Role expectedRoleAdmin = new Role(1, "admin");
        Role expectedRoleUser = new Role(2, "user");
        Set<Role> expectedRoles = Set.of(expectedRoleAdmin, expectedRoleUser);
        UserEntity userEntity = new UserEntity(userId, "testUser", "test_user@test.com", "password");
        UserRoleEntity userRoleEntityAdmin = new UserRoleEntity(userId, userEntity, new RoleEntity(1, "admin"));
        UserRoleEntity userRoleEntityUser = new UserRoleEntity(userId, userEntity, new RoleEntity(2, "user"));
        List<UserRoleEntity> mockUserRoleEntities = List.of(
                userRoleEntityAdmin, userRoleEntityUser
        );

        when(userRolesRepository.get(userId, "r.role_name", "ASC", roleNameFilter))
                .thenReturn(mockUserRoleEntities);

        // Act
        UserRoles result = userRolesService.get(userId, sortBy, sortOrder, roleNameFilter);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUser, result.user());
        assertEquals(expectedRoles, result.roles());
        verify(userRolesRepository, times(1)).get(userId, "r.role_name", "ASC", roleNameFilter);
    }

    @Test
    void getUserRoles_withSortOrderDescAndAdminRole_shouldReturnUserRoles() {
        long userId = 1L;
        String sortBy = "roleName";
        String sortOrder = "desc";
        String roleNameFilter = "user";

        // Arrange
        User expectedUser = new User(userId, "testUser", "password", "test_user@test.com");
        Role expectedRoleAdmin = new Role(1, "admin");
        Role expectedRoleUser = new Role(2, "user");
        Set<Role> expectedRoles = Set.of(expectedRoleAdmin, expectedRoleUser);
        UserEntity userEntity = new UserEntity(userId, "testUser", "test_user@test.com", "password");
        UserRoleEntity userRoleEntityAdmin = new UserRoleEntity(userId, userEntity, new RoleEntity(1, "admin"));
        UserRoleEntity userRoleEntityUser = new UserRoleEntity(userId, userEntity, new RoleEntity(2, "user"));
        List<UserRoleEntity> mockUserRoleEntities = List.of(
                userRoleEntityAdmin, userRoleEntityUser
        );

        when(userRolesRepository.get(userId, "r.role_name", "DESC", roleNameFilter))
                .thenReturn(mockUserRoleEntities);

        // Act
        UserRoles result = userRolesService.get(userId, sortBy, sortOrder, roleNameFilter);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUser, result.user());
        assertEquals(expectedRoles, result.roles());
        verify(userRolesRepository, times(1)).get(userId, "r.role_name", "DESC", roleNameFilter);
    }

    @Test
    void getUserRoles_withSortByRoleIdAndValidParameters_shouldReturnUserRoles() {
        long userId = 1L;
        String sortBy = "roleId";
        String sortOrder = "asc";
        String roleNameFilter = "editor";

        // Arrange
        User expectedUser = new User(userId, "testUser", "password", "test_user@test.com");
        Role expectedRoleAdmin = new Role(1, "admin");
        Role expectedRole = new Role(3, "editor");
        Set<Role> expectedRoles = Set.of(expectedRoleAdmin, expectedRole);
        UserEntity userEntity = new UserEntity(userId, "testUser", "test_user@test.com", "password");
        UserRoleEntity userRoleEntityAdmin = new UserRoleEntity(userId, userEntity, new RoleEntity(1, "admin"));
        UserRoleEntity userRoleEntityUser = new UserRoleEntity(userId, userEntity, new RoleEntity(3, "editor"));
        List<UserRoleEntity> mockUserRoleEntities = List.of(
                userRoleEntityAdmin, userRoleEntityUser
        );

        when(userRolesRepository.get(userId, "r.role_id", "ASC", roleNameFilter))
                .thenReturn(mockUserRoleEntities);

        // Act
        UserRoles result = userRolesService.get(userId, sortBy, sortOrder, roleNameFilter);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUser, result.user());
        assertEquals(expectedRoles, result.roles());
        verify(userRolesRepository, times(1)).get(userId, "r.role_id", "ASC", roleNameFilter);
    }

    @Test
    void getUserRoles_withEmptyRoleNameFilter_shouldHandleAndReturnUserRoles() {
        long userId = 1L;
        String sortBy = "roleName";
        String sortOrder = "asc";
        String roleNameFilter = "";

        // Arrange
        User expectedUser = new User(userId, "testUser", "password", "test_user@test.com");
        Role expectedRoleAdmin = new Role(1, "admin");
        Role expectedRole = new Role(4, "viewer");
        Set<Role> expectedRoles = Set.of(expectedRoleAdmin, expectedRole);
        UserEntity userEntity = new UserEntity(userId, "testUser", "test_user@test.com", "password");
        UserRoleEntity userRoleEntityAdmin = new UserRoleEntity(userId, userEntity, new RoleEntity(1, "admin"));
        UserRoleEntity userRoleEntityUser = new UserRoleEntity(userId, userEntity, new RoleEntity(4, "viewer"));
        List<UserRoleEntity> mockUserRoleEntities = List.of(
                userRoleEntityAdmin, userRoleEntityUser
        );

        when(userRolesRepository.get(userId, "r.role_name", "ASC", roleNameFilter))
                .thenReturn(mockUserRoleEntities);

        // Act
        UserRoles result = userRolesService.get(userId, sortBy, sortOrder, roleNameFilter);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUser, result.user());
        assertEquals(expectedRoles, result.roles());
        verify(userRolesRepository, times(1)).get(userId, "r.role_name", "ASC", roleNameFilter);
    }

}
