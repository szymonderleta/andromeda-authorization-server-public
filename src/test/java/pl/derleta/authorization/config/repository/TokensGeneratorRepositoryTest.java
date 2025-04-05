package pl.derleta.authorization.config.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class TokensGeneratorRepositoryTest {

    @MockBean
    private TokensGeneratorRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @BeforeEach
    void setUp() {
        repository = new TokensGeneratorRepository(jdbcTemplate.getDataSource());
    }

    @Test
    void getByLogin_withValidUsername_shouldReturnUserWithRole() {
        // Arrange

        // Act
        var result = repository.findByLogin("test_user");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("test_user");
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getPassword()).isEqualTo("secure_password");
        assertThat(result.get().getRoles()).hasSize(1);
        assertThat(result.get().getRoles().iterator().next().getName()).isEqualTo("ROLE_USER_DUPLICATED");
    }

    @Test
    void getByEmail_withValidEmail_shouldReturnUserWithRole() {
        // Arrange

        // Act
        var result = repository.findByEmail("test@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("test_user");
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getPassword()).isEqualTo("secure_password");
        assertThat(result.get().getRoles()).hasSize(1);
        assertThat(result.get().getRoles().iterator().next().getName()).isEqualTo("ROLE_USER_DUPLICATED");
    }

    @Test
    void getByLogin_withNullUsername_shouldReturnEmptyOptional() {
        // Arrange

        // Act
        var result = repository.findByLogin(null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getByEmail_withNullParameter_shouldReturnEmptyOptional() {
        // Arrange

        // Act
        var result = repository.findByEmail(null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getByLogin_withMultipleRoles_shouldReturnUserWithAllRoles() {
        // Arrange

        // Act
        var result = repository.findByLogin("tester");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("tester");
        assertThat(result.get().getEmail()).isEqualTo("test@test.com");
        assertThat(result.get().getRoles()).hasSize(4);
        assertThat(result.get().getRoles()).extracting("name").containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_TESTER", "ROLE_MODERATOR");
    }

    @Test
    void getByEmail_withMultipleRoles_shouldReturnUserWithAllRoles() {
        // Arrange

        // Act
        var result = repository.findByEmail("test@test.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("tester");
        assertThat(result.get().getEmail()).isEqualTo("test@test.com");
        assertThat(result.get().getRoles()).hasSize(4);
        assertThat(result.get().getRoles()).extracting("name").containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_TESTER", "ROLE_MODERATOR");
    }

}
