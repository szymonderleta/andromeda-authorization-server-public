package pl.derleta.authorization.repository.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.model.Role;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository repository;

    @Test
    void getSize_withExpectedValue_shouldReturnCorrectCount() {
        // Arrange
        final int size = 6;

        // Act
        var count = repository.getSize();

        // Assert
        assertEquals(size, count);
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        List<RoleEntity> expectedUsers = List.of(
                new RoleEntity(1, "ROLE_USER"),
                new RoleEntity(2, "ROLE_TESTER")
        );

        // Act
        List<RoleEntity> returnedUsers = repository.getPage(0, 2);

        // Assert
        assertEquals(expectedUsers.size(), returnedUsers.size());
        assertThat(returnedUsers).isEqualTo(expectedUsers);
    }

    @Test
    void findAll_withSearchUsers_shouldReturnMatchingRoles() {
        // Arrange
        String search = "_USER";
        List<RoleEntity> expectedUsers = List.of(
                new RoleEntity(1, "ROLE_USER"),
                new RoleEntity(10001, "ROLE_USER_DUPLICATED")
        );

        // Act
        List<RoleEntity> returnedUsers = repository.findAll(search);

        // Assert
        assertEquals(expectedUsers.size(), returnedUsers.size());
        assertThat(returnedUsers).isEqualTo(expectedUsers);
    }

    @Test
    void findAll_withSearchString_shouldReturnAllMatchingRoles() {
        // Arrange
        String search = "OLE_";

        // Act
        List<RoleEntity> returnedUsers = repository.findAll(search);

        // Assert
        assertEquals(6, returnedUsers.size());
    }

    @Test
    void findAll_withInvalidName_shouldReturnEmptyList() {
        // Arrange
        String search = "RULE_";

        // Act
        List<RoleEntity> returnedUsers = repository.findAll(search);

        // Assert
        assertEquals(0, returnedUsers.size());
    }

    @Test
    void findById_withValidId_shouldReturnRoleEntity() {
        // Arrange
        final int id = 10001;
        RoleEntity roleEntity = new RoleEntity(id, "ROLE_USER_DUPLICATED");

        // Act
        RoleEntity result = repository.findById(id);

        // Assert
        assertEquals(roleEntity, result);
    }

    @Test
    void findById_withNonExistentId_shouldReturnNull() {
        // Arrange
        final int id = 777;

        // Act
        RoleEntity result = repository.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void saveThenUpdateThenDeleteUser_withValidRole_shouldWorkCorrectly() {
        // Arrange
        int tempRoleId = 9999;
        Role temporarySaveRole = new Role(tempRoleId, "temporaryRole");
        Role temporaryUpdateRole = new Role(tempRoleId, "updatedRole");

        // Act
        int saveResult = repository.save(tempRoleId, temporarySaveRole);
        RoleEntity savedRole = repository.findById(tempRoleId);
        int updateResult = repository.update(tempRoleId, temporaryUpdateRole);
        RoleEntity updatedRole = repository.findById(tempRoleId);
        int deleteResult = repository.deleteById(tempRoleId);
        RoleEntity deletedRole = repository.findById(tempRoleId);

        // Assert
        assertEquals(1, saveResult);
        assertEquals(1, updateResult);
        assertEquals(1, deleteResult);
        assertEquals(savedRole.getRoleName(), temporarySaveRole.roleName());
        assertEquals(updatedRole.getRoleName(), temporaryUpdateRole.roleName());
        assertNull(deletedRole);
    }

    @Test
    void saveExistedRole_withInvalidRoleId_shouldNotSave() {
        // Arrange
        int tempRoleId = 10001;
        Role temporarySaveRole = new Role(tempRoleId, "temporaryRole");

        // Act
        int result = repository.save(tempRoleId, temporarySaveRole);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void updateNonExistedRole_withInvalidRoleId_shouldNotUpdate() {
        // Arrange
        int tempRoleId = 1999;
        Role temporarySaveRole = new Role(tempRoleId, "temporaryRole");

        // Act
        int result = repository.update(tempRoleId, temporarySaveRole);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void deleteById_withNonExistentRoleId_shouldReturnZero() {
        // Arrange
        int tempRoleId = 1999;

        // Act
        int result = repository.deleteById(tempRoleId);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void getNextRoleId_withExistingRoles_shouldReturnNextId() {
        // Arrange
        int excepted = 10002;

        // Act
        int nextRoleId = repository.getNextRoleId();

        // Assert
        assertEquals(excepted, nextRoleId);
    }

    @Test
    void getSortedPage_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        int offset = 0;
        int size = 5;
        String sortByParam = "role_name";
        String sortOrderParam = "ASC";
        String roleName = "ROLE";

        // Act
        Set<RoleEntity> roleEntitySet = repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, roleName);

        // Assert
        assertEquals(5, roleEntitySet.size());
        assertTrue(roleEntitySet.stream().findFirst().isPresent());
        assertEquals("ROLE_ADMIN", roleEntitySet.stream().findFirst().get().getRoleName());
    }

    @Test
    void getSortedPageWithFilters_withValidParameters_shouldReturnTwoRoles() {
        // Arrange
        int offset = 0;
        int size = 5;
        String sortByParam = "role_name";
        String sortOrderParam = "ASC";
        String roleName = "ROLE_U";

        // Act
        Set<RoleEntity> roleEntitySet = repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, roleName);

        // Assert
        assertEquals(2, roleEntitySet.size());
        assertTrue(roleEntitySet.stream().findFirst().isPresent());
        assertEquals("ROLE_USER", roleEntitySet.stream().findFirst().get().getRoleName());
    }

    @Test
    void getSortedPageWithFilters_withValidParameters_shouldReturnZeroRoles() {
        // Arrange
        int offset = 0;
        int size = 5;
        String sortByParam = "role_name";
        String sortOrderParam = "ASC";
        String roleName = "ROLE_UR";

        // Act
        Set<RoleEntity> roleEntitySet = repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, roleName);

        // Assert
        assertEquals(0, roleEntitySet.size());
    }

    @Test
    public void getSortedPageWithFilters_withInvalidSortByParameter_shouldThrowException() {
        // Arrange
        int offset = 0;
        int size = 5;
        String sortByParam = "rule_name";
        String sortOrderParam = "ASC";
        String roleName = "ROLE";

        // Assert
        assertThatThrownBy(() ->
                // Act
                repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, roleName)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sortBy parameter");
    }

    @Test
    public void getSortedPageWithFilters_withInvalidSortOrder_shouldThrowException() {
        // Arrange
        int offset = 0;
        int size = 5;
        String sortByParam = "role_name";
        String sortOrderParam = "ASIC";
        String roleName = "ROLE";

        // Assert
        assertThatThrownBy(() ->
                // Act
                repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, roleName)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sortOrder parameter");
    }

    @Test
    void getFiltersCount_withValidFilter_shouldReturnCorrectCount() {
        // Arrange
        long excepted = 2;
        String roleName = "USER";

        // Act
        long founded = repository.getFiltersCount(roleName);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getFiltersCount_withNoMatchingFilter_shouldReturnZero() {
        // Arrange
        long excepted = 0;
        String roleName = "ADAM";

        // Act
        long founded = repository.getFiltersCount(roleName);

        // Assert
        assertEquals(excepted, founded);
    }

}