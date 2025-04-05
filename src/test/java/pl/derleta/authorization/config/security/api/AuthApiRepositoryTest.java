package pl.derleta.authorization.config.security.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthApiRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @MockBean
    private AuthApiRepository repository;

    @BeforeEach
    void setUp() {
        repository = new AuthApiRepository(jdbcTemplate.getDataSource());
    }


    @Test
    void getAccessTokenNextId_withValidState_shouldReturnCorrectId() {
        // Arrange

        // Act
        var result = repository.getAccessTokenNextId();

        // Assert
        assertNotNull(result);
        assertEquals(11, result);
    }

    @Test
    void getRefreshTokenNextId_withValidState_shouldReturnCorrectId() {
        // Arrange

        // Act
        var result = repository.getRefreshTokenNextId();

        // Assert
        assertNotNull(result);
        assertEquals(22, result);
    }

    @Test
    void findAccessTokenById_withValidId_shouldReturnCorrectToken() {
        // Arrange
        String expectedToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxLDUwQDkwLmNvbSIsImlzcyI6IkV4YW1wbGVBcHAiLCJyb2xlcyI6W3siaWQiOjEsIm5hbWUiOiJST0xFX1RFWFQifSx7ImlkIjoyLCJuYW1lIjoiUk9MRV9QT1dFUiJ9LHsiaWQiOjQsIm5hbWUiOiJST0xFX0FETUlOIn0seyJpZCI6MywibmFtZSI6IlJPTEVfQURNSU4ifV0sImlhdCI6MTcwNDkwMzg3NiwgImV4cCI6MTcwNDk5MDA3Nn0.ZxYDS_UQ12QA8MLWZxHi56LoaqK54OXdtemq_6RXyl3905y4Rpm4qNrqoTTFczdGONGzWmnxzeHYDtmjsJjAEA";

        // Act
        var result = repository.findAccessTokenById(1L).orElse(null);

        // Assert
        assertNotNull(result);
        assertEquals(expectedToken, result);
    }

    @Test
    void findAccessTokenById_withNonExistentId_shouldReturnNull() {
        // Arrange

        // Act
        var result = repository.findAccessTokenById(123L).orElse(null);

        // Assert
        assertNull(result);
    }

    @Test
    void findRefreshTokenById_withValidId_shouldReturnCorrectToken() {
        // Arrange
        String expectedToken = "eyJkbGwiOiJIUzUxNiJ9.eyJhdWIiOiIzLDQ1NkBkZW1vLmNvbSIsImlzc3VlciI6Ik15QXBwbGljYXRpb24iLCJyb2xlcyI6W3siaWQiOjIsIm5hbWUiOiJST0xFX0RFVkVMT1BFUiJ9LHsiaWQiOjQsIm5hbWUiOiJST0xFX01BTkdFUiJ9LHsiaWQiOjcsIm5hbWUiOiJST0xFX0FETUluIn1dLCJpYXQiOjE3MDQ5MDQzMjAsImV4cCI6MTcwNTAwMDcyMH0.KyZXD_WX18PQL9NTZxEj64PqbmR39ZP9emnv_4DFyr4503h7Ytn7ePrwuVHGamzJOUyLmnymaAXDksiäOiZRTR";

        // Act
        var result = repository.findRefreshTokenById(1L).orElse(null);

        // Assert
        assertNotNull(result);
        assertEquals(expectedToken, result);
    }

    @Test
    void findRefreshTokenById_withNonExistentId_shouldReturnNull() {
        // Arrange

        // Act
        var result = repository.findRefreshTokenById(123L).orElse(null);

        // Assert
        assertNull(result);
    }

    @Test
    void saveAndDeleteAccessToken_withValidData_shouldPerformCorrectly() {
        // Arrange
        var nextId = repository.getAccessTokenNextId();
        final long userId = 123456789;
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzd8IiOiIxMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcwNTE5NDgsImV4cCI6MTczNzA1NTU0OH0.iJR3QwCVF1tucpL6tONwQw6djy9iGHkJzoTIBRPere2klO-lFqjpwmVT5m0Rx9E1FCLAElHywm2II8OX_DC1Pw";
        final Date expirationDate = new Date(System.currentTimeMillis() + 1000000);

        // Act
        repository.saveAccessToken(nextId, userId, token, expirationDate);
        var result = repository.findAccessTokenById(nextId).orElse(null);
        jdbcTemplate.update("DELETE FROM access_tokens WHERE token_id = ?", nextId);
        var resultAfterDelete = repository.findAccessTokenById(nextId).orElse(null);

        // Assert
        assertNotNull(result);
        assertEquals(token, result);
        assertNull(resultAfterDelete);
    }

    @Test
    void saveAndDeleteRefreshToken_withValidData_shouldPerformCorrectly() {
        // Arrange
        var nextId = repository.getRefreshTokenNextId();
        final long userId = 123456789;
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzd6IiOiIxMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcwNTE5NDgsImV4cCI6MTczNzA1NTU0OH0.iJR3QwCVF1tucpL6tONwQw6djy9iGHkJzoTIBRPere2klO-lFqjpwmVT5m0Rx9E1FCLAElHywm2II8OX_DC1Pw";
        final Date expirationDate = new Date(System.currentTimeMillis() + 1000000);

        // Act
        repository.saveRefreshToken(nextId, userId, token, expirationDate);
        var result = repository.findRefreshTokenById(nextId).orElse(null);
        jdbcTemplate.update("DELETE FROM refresh_tokens WHERE token_id = ?", nextId);
        var resultAfterDelete = repository.findRefreshTokenById(nextId).orElse(null);

        // Assert
        assertNotNull(result);
        assertEquals(token, result);
        assertNull(resultAfterDelete);
    }

}
