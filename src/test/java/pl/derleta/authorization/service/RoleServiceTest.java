package pl.derleta.authorization.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.repository.impl.RoleRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class RoleServiceTest {

    @MockBean
    private RoleRepository roleRepository;

    @Test
    void getList_withMatchingFilter_shouldReturnRoles() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        String roleNameFilter = "admin";
        List<RoleEntity> mockRoles = new ArrayList<>();
        mockRoles.add(new RoleEntity(1, "Admin"));
        mockRoles.add(new RoleEntity(2, "Administrator"));

        when(roleRepository.findAll(anyString())).thenReturn(mockRoles);

        // Act
        Set<Role> result = roleService.getList(roleNameFilter);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).anyMatch(role -> role.roleName().equals("Admin"));
        assertThat(result).anyMatch(role -> role.roleName().equals("Administrator"));
    }

    @Test
    void getList_withNonMatchingFilter_shouldReturnEmptySet() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        String roleNameFilter = "nonexistent";
        when(roleRepository.findAll(anyString())).thenReturn(new ArrayList<>());

        // Act
        Set<Role> result = roleService.getList(roleNameFilter);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void getList_withProvidedFilter_shouldCallRepository() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        String roleNameFilter = "manager";
        when(roleRepository.findAll(anyString())).thenReturn(new ArrayList<>());

        // Act
        roleService.getList(roleNameFilter);

        // Assert
        verify(roleRepository, times(1)).findAll(roleNameFilter);
    }

    @Test
    void getPage_withFilterAndPagination_shouldReturnPaginatedRoles() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        String roleNameFilter = "developer";

        Set<RoleEntity> mockRoles = Set.of(
                new RoleEntity(1, "Developer"),
                new RoleEntity(2, "Senior Developer")
        );

        int page = 0;
        int size = 2;
        String sortBy = "roleName";
        String sortOrder = "asc";

        when(roleRepository.getSortedPageWithFilters(0, 2, "role_name", "ASC", roleNameFilter))
                .thenReturn(mockRoles);
        when(roleRepository.getFiltersCount(roleNameFilter)).thenReturn(2);

        // Act
        var result = roleService.getPage(page, size, sortBy, sortOrder, roleNameFilter);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getContent()).anyMatch(role -> role.roleName().equals("Developer"));
        assertThat(result.getContent()).anyMatch(role -> role.roleName().equals("Senior Developer"));
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getPage_withNoMatchingFilter_shouldReturnEmptyPage() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        String roleNameFilter = "nonexistent";
        int page = 0;
        int size = 2;
        String sortBy = "roleName";
        String sortOrder = "asc";

        when(roleRepository.getSortedPageWithFilters(0, 2, "role_name", "ASC", roleNameFilter))
                .thenReturn(new HashSet<>());
        when(roleRepository.getFiltersCount(roleNameFilter)).thenReturn(0);

        // Act
        var result = roleService.getPage(page, size, sortBy, sortOrder, roleNameFilter);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void getPage_withCorrectPaginationParameters_shouldCallRepository() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        String roleNameFilter = "manager";
        int page = 1;
        int size = 3;
        String sortBy = "roleName";
        String sortOrder = "desc";

        when(roleRepository.getSortedPageWithFilters(3, 3, "role_name", "DESC", roleNameFilter))
                .thenReturn(new HashSet<>());
        when(roleRepository.getFiltersCount(roleNameFilter)).thenReturn(0);

        // Act
        roleService.getPage(page, size, sortBy, sortOrder, roleNameFilter);

        // Assert
        verify(roleRepository, times(1))
                .getSortedPageWithFilters(3, 3, "role_name", "DESC", roleNameFilter);
        verify(roleRepository, times(1)).getFiltersCount(roleNameFilter);
    }

    @Test
    void get_withExistingRoleId_shouldReturnRole() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        int roleId = 1;
        RoleEntity mockRoleEntity = new RoleEntity(roleId, "Admin");
        when(roleRepository.findById(roleId)).thenReturn(mockRoleEntity);

        // Act
        Role result = roleService.get(roleId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.roleId()).isEqualTo(roleId);
        assertThat(result.roleName()).isEqualTo("Admin");
    }

    @Test
    void get_withNonExistingRoleId_shouldReturnNull() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        int roleId = 999;
        when(roleRepository.findById(roleId)).thenReturn(null);

        // Act
        Role result = roleService.get(roleId);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void get_withRoleId_shouldCallRepositoryOnce() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        int roleId = 1;
        when(roleRepository.findById(roleId)).thenReturn(new RoleEntity(roleId, "Admin"));

        // Act
        roleService.get(roleId);

        // Assert
        verify(roleRepository, times(1)).findById(roleId);
    }

    @Test
    void save_withValidRole_shouldReturnSavedRole() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        Role roleToSave = new Role(0, "New Role");
        when(roleRepository.getNextRoleId()).thenReturn(123);
        int roleId = roleRepository.getNextRoleId();

        RoleEntity savedEntity = new RoleEntity(roleId, roleToSave.roleName());

        when(roleRepository.save(eq(roleId), any(Role.class))).thenReturn(1);
        when(roleRepository.findById(roleId)).thenReturn(savedEntity);

        // Act
        Role result = roleService.save(roleToSave);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.roleId()).isEqualTo(roleId);
        assertThat(result.roleName()).isEqualTo("New Role");
    }

    @Test
    void save_withRole_shouldCallRepositoryMethods() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        when(roleRepository.getNextRoleId()).thenReturn(123);
        int roleId = roleRepository.getNextRoleId();

        Role role = new Role(roleId, "Admin");
        RoleEntity entity = new RoleEntity(roleId, "Admin");

        when(roleRepository.save(eq(roleId), any(Role.class))).thenReturn(1);
        when(roleRepository.findById(roleId)).thenReturn(entity);

        // Act
        Role result = roleService.save(role);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.roleId()).isEqualTo(roleId);
        verify(roleRepository, times(1)).save(roleId, role);
    }

    @Test
    void update_withExistingRole_shouldReturnUpdatedRole() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        int roleId = 1;
        Role updatedRole = new Role(roleId, "Updated Role");
        RoleEntity existingEntity = new RoleEntity(roleId, "Old Role");
        when(roleRepository.findById(roleId)).thenReturn(existingEntity);

        when(roleRepository.update(roleId, updatedRole)).thenReturn(1);
        when(roleRepository.findById(roleId)).thenReturn(new RoleEntity(roleId, "Updated Role"));

        // Act
        Role result = roleService.update(roleId, updatedRole);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.roleId()).isEqualTo(roleId);
        assertThat(result.roleName()).isEqualTo("Updated Role");
    }

    @Test
    void update_withNonExistingRole_shouldReturnNull() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        int roleId = 123;
        Role updatedRole = new Role(roleId, "Updated Role");
        when(roleRepository.findById(roleId)).thenReturn(null);

        // Act
        Role result = roleService.update(roleId, updatedRole);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void update_withExistingRole_shouldCallRepositoryUpdate() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        int roleId = 1;
        Role updatedRole = new Role(roleId, "Updated Role");
        RoleEntity existingEntity = new RoleEntity(roleId, "Old Role");
        when(roleRepository.findById(roleId)).thenReturn(existingEntity);

        // Act
        roleService.update(roleId, updatedRole);

        // Assert
        verify(roleRepository, Mockito.times(1)).update(roleId, updatedRole);
    }

    @Test
    void update_withNonExistingRole_shouldNotCallRepositoryUpdate() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        int roleId = 999;
        Role updatedRole = new Role(roleId, "Updated Role");
        when(roleRepository.findById(roleId)).thenReturn(null);

        // Act
        roleService.update(roleId, updatedRole);

        // Assert
        verify(roleRepository, never()).update(anyInt(), any(Role.class));
    }

    @Test
    void delete_withExistingRoleId_shouldDeleteRole() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        int roleId = 1;
        RoleEntity mockRoleEntity = new RoleEntity(roleId, "Admin");
        when(roleRepository.findById(roleId)).thenReturn(mockRoleEntity);

        // Act
        boolean result = roleService.delete(roleId);

        // Assert
        assertThat(result).isTrue();
        verify(roleRepository, times(1)).deleteById(roleId);
    }

    @Test
    void delete_withNonExistingRoleId_shouldNotDeleteRole() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        int roleId = 999;
        when(roleRepository.findById(roleId)).thenReturn(null);

        // Act
        boolean result = roleService.delete(roleId);

        // Assert
        assertThat(result).isFalse();
        verify(roleRepository, never()).deleteById(anyInt());
    }

    @Test
    void delete_withExistingRoleId_shouldCallRepositoryDelete() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        int roleId = 1;
        RoleEntity mockRoleEntity = new RoleEntity(roleId, "Admin");
        when(roleRepository.findById(roleId)).thenReturn(mockRoleEntity);

        // Act
        roleService.delete(roleId);

        // Assert
        verify(roleRepository, times(1)).deleteById(roleId);
    }

    @Test
    void delete_withNonExistingRoleId_shouldNotCallRepositoryDelete() {
        // Arrange
        RoleService roleService = new RoleService();
        roleService.setRepository(roleRepository);

        int roleId = 999;
        when(roleRepository.findById(roleId)).thenReturn(null);

        // Act
        roleService.delete(roleId);

        // Assert
        verify(roleRepository, never()).deleteById(anyInt());
    }

}
