package pl.derleta.authorization.repository.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserRoleRepositoryTest {

    @Autowired
    private UserRoleRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getSize_withValidParameters_shouldReturnTotalUserRolesCount() {
        // Act
        var result = repository.getSize();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(20);
    }

    @Test
    public void getPage_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        int offset = 0;
        int size = 5;

        // Act
        var result = repository.getPage(offset, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(size);
        result.forEach(userRole -> {
            assertThat(userRole.getUserRoleId()).isNotNull();
            assertThat(userRole.getUserEntity()).isNotNull();
            assertThat(userRole.getRoleEntity()).isNotNull();
        });
    }

    @Test
    public void findByIds_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        long userId = 1L;
        int roleId = 1;

        // Act
        var result = repository.findByIds(userId, roleId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserRoleId()).isNotNull();
        assertThat(result.getUserEntity().getUserId()).isEqualTo(userId);
        assertThat(result.getRoleEntity().getRoleId()).isEqualTo(roleId);
    }

    @Test
    public void findUserRoleById_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        long userRoleId = 1L;

        // Act
        var result = repository.findById(userRoleId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserRoleId()).isEqualTo(userRoleId);
        assertThat(result.getUserEntity()).isNotNull();
        assertThat(result.getRoleEntity()).isNotNull();
    }


    @Test
    public void saveUserRole_withValidParameters_shouldSucceed() {
        // Arrange
        long userRoleId = 100L;
        long userId = 2L;
        int roleId = 3;

        // Act
        repository.save(userRoleId, userId, roleId);

        // Assert
        var savedUserRole = repository.findById(userRoleId);
        assertThat(savedUserRole).isNotNull();
        assertThat(savedUserRole.getUserRoleId()).isEqualTo(userRoleId);
        assertThat(savedUserRole.getUserEntity().getUserId()).isEqualTo(userId);
        assertThat(savedUserRole.getRoleEntity().getRoleId()).isEqualTo(roleId);
    }


    @Test
    public void getNextIdForUserRole_shouldSucceed() {
        // Act
        var nextId = repository.getNextId();

        // Assert
        assertThat(nextId).isNotNull();
        assertThat(nextId).isGreaterThan(0L);
    }

    @Test
    public void deleteUserRoleByUserIdAndRoleId_withValidParameters_shouldSucceed() {
        // Arrange
        long userId = 2L;
        int roleId = 3;

        // Act
        repository.deleteById(userId, roleId);

        // Assert
        var deletedUserRoleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_roles WHERE user_id = ? AND role_id = ?",
                Integer.class, userId, roleId
        );
        assertThat(deletedUserRoleCount).isEqualTo(0);
    }

    @Test
    public void getSortedPageWithFilters_withValidParameters_shouldReturnFilteredAndSortedPage() {
        // Arrange
        int offset = 0;
        int size = 10;
        String sortByParam = "u.username";
        String sortOrderParam = "ASC";
        String username = "tester";
        String email = "tester@test.com";
        String roleName = "ROLE_USER";

        // Act
        var result = repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, username, email, roleName);

        // Assert
        assertThat(result).isNotNull();
        result.forEach(userRole -> {
            assertThat(userRole.getUserEntity()).isNotNull();
            assertThat(userRole.getUserEntity().getUsername()).contains(username);
            assertThat(userRole.getUserEntity().getEmail()).contains(email);
            assertThat(userRole.getRoleEntity().getRoleName()).contains(roleName);
        });
    }

    @Test
    public void getFiltersCount_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        String username = "test";
        String email = "example.com";
        String roleName = "ROLE_USER";

        // Act
        var count = repository.getFiltersCount(username, email, roleName);

        // Assert
        assertThat(count).isNotNull();
        assertThat(count).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void getFiltersCount_withTestUserParameters_shouldReturnCorrectResult() {
        // Arrange
        String username = "tester";
        String email = "test@test.com";
        String roleName = "ROLE_USER";

        // Act
        var count = repository.getFiltersCount(username, email, roleName);

        // Assert
        assertThat(count).isNotNull();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void getFiltersCount_withNullValues_shouldReturnCorrectResult() {
        // Arrange
        String username = null;
        String email = null;
        String roleName = null;

        // Act
        var count = repository.getFiltersCount(username, email, roleName);

        // Assert
        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(0);
    }

}
