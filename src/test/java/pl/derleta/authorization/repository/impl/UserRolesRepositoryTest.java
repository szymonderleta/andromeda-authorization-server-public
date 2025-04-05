package pl.derleta.authorization.repository.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class UserRolesRepositoryTest {

    @Autowired
    private UserRolesRepository repository;

    @Test
    public void getPage_withValidParameters_shouldReturnCorrectResult() {
        // Arrange

        // Act
        var result = repository.get(1, "r.role_id", "ASC", "ROLE_USER");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        var userRole = result.getFirst();
        assertThat(userRole.getUserRoleId()).isEqualTo(1);

        var user = userRole.getUserEntity();
        assertThat(user.getUsername()).isEqualTo("tester");
        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getPassword()).startsWith("$2a");

        var role = userRole.getRoleEntity();
        assertThat(role.getRoleId()).isEqualTo(1);
        assertThat(role.getRoleName()).isEqualTo("ROLE_USER");
    }

    @Test
    public void getPage_withValidUserIdAndFiltersDESC_shouldReturnCorrectResult() {
        // Arrange

        // Act
        var result = repository.get(1, "r.role_id", "DESC", "ROLE_USER");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        var userRole = result.getFirst();
        assertThat(userRole.getUserRoleId()).isEqualTo(1);

        var user = userRole.getUserEntity();
        assertThat(user.getUsername()).isEqualTo("tester");
        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getPassword()).startsWith("$2a");

        var role = userRole.getRoleEntity();
        assertThat(role.getRoleId()).isEqualTo(1);
        assertThat(role.getRoleName()).isEqualTo("ROLE_USER");
    }

    @Test
    public void getPage_withInvalidSortBy_shouldThrowException() {
        // Arrange

        // Act & Assert
        assertThatThrownBy(() ->
                repository.get(1, "injection", "ASC", "ROLE_USER")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sortBy parameter");
    }

    @Test
    public void getPage_withInvalidSortOrder_shouldThrowException() {
        // Arrange

        // Act & Assert
        assertThatThrownBy(() ->
                repository.get(1, "r.role_id", "Injection", "ROLE_USER")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sortOrder parameter");
    }

    @Test
    public void getPage_withNonExistentRole_shouldNotReturnResults() {
        // Arrange

        // Act
        var result = repository.get(1, "r.role_id", "ASC", "ROLE_XYZ");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    public void getPage_withValidUsernameAndEmail_shouldReturnCorrectResult() {
        // Arrange

        // Act
        var result = repository.get("tester", "test@test.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);

        var userRole = result.getFirst();
        assertThat(userRole.getUserRoleId()).isEqualTo(1);

        var user = userRole.getUserEntity();
        assertThat(user.getUsername()).isEqualTo("tester");
        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getPassword()).startsWith("$2a");

        var role = userRole.getRoleEntity();
        assertThat(role.getRoleId()).isEqualTo(1);
        assertThat(role.getRoleName()).isEqualTo("ROLE_USER");
    }

    @Test
    public void getPage_withNonExistentUsernameAndEmail_shouldReturnEmptyList() {
        // Arrange

        // Act
        var result = repository.get("nonExistingUser", "nonExistingEmail@test.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    public void getPage_withNullEmail_shouldReturnEmptyList() {
        // Arrange

        // Act
        var result = repository.get("tester", null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    public void getPage_withNullUsername_shouldReturnEmptyList() {
        // Arrange

        // Act
        var result = repository.get(null, "test@test.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

}
