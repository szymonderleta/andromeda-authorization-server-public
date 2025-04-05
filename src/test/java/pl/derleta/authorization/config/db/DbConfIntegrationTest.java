package pl.derleta.authorization.config.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DbConfIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldLoadJdbcTemplateFromContext_withValidConfiguration_shouldReturnNonNullJdbcTemplate() {
        // Arrange

        // Act
        JdbcTemplate jdbcTemplateInstance = jdbcTemplate;

        // Assert
        assertNotNull(jdbcTemplateInstance);
    }

}
