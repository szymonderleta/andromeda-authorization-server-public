package pl.derleta.authorization.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.entity.UserRoleEntity;
import pl.derleta.authorization.domain.model.UserRole;
import pl.derleta.authorization.repository.impl.UserRoleRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserRoleServiceTest {

    @Autowired
    private UserRoleService userRoleService;

    @MockBean
    private UserRoleRepository userRoleRepository;

    @Test
    public void getPage_withValidFiltersAndAscendingOrder_shouldReturnPagedUserRoles() {
        // Arrange
        int page = 0;
        int size = 5;
        String sortBy = "username";
        String sortOrder = "asc";
        String usernameFilter = "john";
        String emailFilter = "john@example.com";
        String roleNameFilter = "ADMIN";

        List<UserRoleEntity> mockedEntities = List.of(
                new UserRoleEntity(1L, new UserEntity(1L, "john", "john@example.com", "passwd"), new RoleEntity(1, "ADMIN")),
                new UserRoleEntity(2L, new UserEntity(2L, "jane", "jane@example.com", "passwd"), new RoleEntity(2, "USER"))
        );

        when(userRoleRepository.getSortedPageWithFilters(
                page * size, size, "u.username", "ASC", usernameFilter, emailFilter, roleNameFilter
        )).thenReturn(mockedEntities);

        when(userRoleRepository.getFiltersCount(usernameFilter, emailFilter, roleNameFilter)).thenReturn(2L);

        // Act
        Page<UserRole> result = userRoleService.getPage(page, size, sortBy, sortOrder, usernameFilter, emailFilter, roleNameFilter);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertEquals(1L, result.getContent().getFirst().userRoleId());
        assertEquals("john", result.getContent().getFirst().user().username());
        assertEquals("ADMIN", result.getContent().getFirst().role().roleName());

        // Verify interactions
        verify(userRoleRepository, times(1)).getSortedPageWithFilters(
                page * size, size, "u.username", "ASC", usernameFilter, emailFilter, roleNameFilter
        );
        verify(userRoleRepository, times(1)).getFiltersCount(usernameFilter, emailFilter, roleNameFilter);
    }

    @Test
    public void getPage_withNoMatchingResults_shouldReturnEmptyPage() {
        // Arrange
        int page = 0;
        int size = 5;
        String sortBy = "email";
        String sortOrder = "desc";
        String usernameFilter = "nonexistent";
        String emailFilter = "nonexistent@example.com";
        String roleNameFilter = "UNKNOWN";

        List<UserRoleEntity> mockedEntities = List.of();

        when(userRoleRepository.getSortedPageWithFilters(
                page * size, size, "u.email", "DESC", usernameFilter, emailFilter, roleNameFilter
        )).thenReturn(mockedEntities);

        when(userRoleRepository.getFiltersCount(usernameFilter, emailFilter, roleNameFilter)).thenReturn(0L);

        // Act
        Page<UserRole> result = userRoleService.getPage(page, size, sortBy, sortOrder, usernameFilter, emailFilter, roleNameFilter);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());

        // Verify interactions
        verify(userRoleRepository, times(1)).getSortedPageWithFilters(
                page * size, size, "u.email", "DESC", usernameFilter, emailFilter, roleNameFilter
        );
        verify(userRoleRepository, times(1)).getFiltersCount(usernameFilter, emailFilter, roleNameFilter);
    }

    @Test
    public void get_withValidId_shouldReturnUserRole() {
        // Arrange
        long id = 1L;
        UserRoleEntity entity = new UserRoleEntity(1L, new UserEntity(1L, "john", "john@example.com", "passwd"), new RoleEntity(1, "ADMIN"));
        when(userRoleRepository.findById(id)).thenReturn(entity);

        // Act
        UserRole result = userRoleService.get(id);

        // Assert
        assertEquals(1L, result.userRoleId());
        assertEquals("john", result.user().username());
        assertEquals("ADMIN", result.role().roleName());

        // Verify interactions
        verify(userRoleRepository, times(1)).findById(id);
    }

    @Test
    public void get_withNonExistentId_shouldReturnNull() {
        // Arrange
        long id = 999L;
        when(userRoleRepository.findById(id)).thenReturn(null);

        // Act
        UserRole result = userRoleService.get(id);

        // Assert
        assertNull(result);

        // Verify interactions
        verify(userRoleRepository, times(1)).findById(id);
    }

    @Test
    public void save_withValidData_shouldCreateUserRoleSuccessfully() {
        // Arrange
        long userId = 1L;
        int roleId = 1;
        long userRoleId = 100L;

        UserRoleEntity entity = new UserRoleEntity(userRoleId, new UserEntity(userId, "john", "john@example.com", "passwd"), new RoleEntity(roleId, "ADMIN"));

        when(userRoleRepository.getNextId()).thenReturn(userRoleId);
        doNothing().when(userRoleRepository).save(userRoleId, userId, roleId);
        when(userRoleRepository.findById(userRoleId)).thenReturn(entity);

        // Act
        UserRole result = userRoleService.save(userId, roleId);

        // Assert
        assertEquals(userRoleId, result.userRoleId());
        assertEquals("john", result.user().username());
        assertEquals("ADMIN", result.role().roleName());

        // Verify interactions
        verify(userRoleRepository, times(1)).getNextId();
        verify(userRoleRepository, times(1)).save(userRoleId, userId, roleId);
        verify(userRoleRepository, times(1)).findById(userRoleId);
    }

    @Test
    public void save_withSaveFailure_shouldReturnNull() {
        // Arrange
        long userId = 2L;
        int roleId = 2;
        long userRoleId = 101L;

        when(userRoleRepository.getNextId()).thenReturn(userRoleId);
        doNothing().when(userRoleRepository).save(userRoleId, userId, roleId);
        when(userRoleRepository.findById(userRoleId)).thenReturn(null);

        // Act
        UserRole result = userRoleService.save(userId, roleId);

        // Assert
        assertNull(result);

        // Verify interactions
        verify(userRoleRepository, times(1)).getNextId();
        verify(userRoleRepository, times(1)).save(userRoleId, userId, roleId);
        verify(userRoleRepository, times(1)).findById(userRoleId);
    }

    @Test
    public void delete_withExistingUserRole_shouldReturnTrue() {
        // Arrange
        long userId = 1L;
        int roleId = 1;

        UserRoleEntity entity = new UserRoleEntity(100L, new UserEntity(userId, "john", "john@example.com", "passwd"), new RoleEntity(roleId, "ADMIN"));
        when(userRoleRepository.findByIds(userId, roleId)).thenReturn(entity);
        doNothing().when(userRoleRepository).deleteById(userId, roleId);

        // Act
        boolean result = userRoleService.delete(userId, roleId);

        // Assert
        assertTrue(result);

        // Verify interactions
        verify(userRoleRepository, times(1)).findByIds(userId, roleId);
        verify(userRoleRepository, times(1)).deleteById(userId, roleId);
    }

    @Test
    public void delete_withNonExistentUserRole_shouldReturnFalse() {
        // Arrange
        long userId = 999L;
        int roleId = 2;

        when(userRoleRepository.findByIds(userId, roleId)).thenReturn(null);

        // Act
        boolean result = userRoleService.delete(userId, roleId);

        // Assert
        assertFalse(result);

        // Verify interactions
        verify(userRoleRepository, times(1)).findByIds(userId, roleId);
        verify(userRoleRepository, times(0)).deleteById(userId, roleId);
    }

}
