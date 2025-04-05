package pl.derleta.authorization.config.db;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@SpringBootTest
public class DbConfTest {

    @Test
    void jdbcTemplateBeanCreation_withMockedDataSource_shouldReturnNonNullJdbcTemplate() {
        // Arrange
        DataSource mockDataSource = mock(DataSource.class);
        DbConf dbConf = new DbConf(mockDataSource);

        // Act
        JdbcTemplate jdbcTemplate = dbConf.jdbcTemplate();

        // Assert
        assertNotNull(jdbcTemplate, "JdbcTemplate bean should not be null");
    }

}
