package pl.derleta.authorization.repository.impl.token;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.derleta.authorization.domain.entity.token.AccessTokenEntity;
import pl.derleta.authorization.domain.entity.token.TokenEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
class AccessTokenRepositoryTest {

    @Autowired
    private AccessTokenRepository repository;

    @Test
    void getSize_withValidData_shouldReturnCorrectResult() {
        // Arrange
        final int size = 10;

        // Act
        var count = repository.getSize();

        // Assert
        assertEquals(size, count);
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        List<TokenEntity> expectedResults = List.of(
                new AccessTokenEntity(1L, null, "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxLDUwQDkwLmNvbSIsImlzcyI6IkV4YW1wbGVBcHAiLCJyb2xlcyI6W3siaWQiOjEsIm5hbWUiOiJST0xFX1RFWFQifSx7ImlkIjoyLCJuYW1lIjoiUk9MRV9QT1dFUiJ9LHsiaWQiOjQsIm5hbWUiOiJST0xFX0FETUlOIn0seyJpZCI6MywibmFtZSI6IlJPTEVfQURNSU4ifV0sImlhdCI6MTcwNDkwMzg3NiwgImV4cCI6MTcwNDk5MDA3Nn0.ZxYDS_UQ12QA8MLWZxHi56LoaqK54OXdtemq_6RXyl3905y4Rpm4qNrqoTTFczdGONGzWmnxzeHYDtmjsJjAEA", null),
                new AccessTokenEntity(2L, null, "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxLDUwQDYwLnRlc3QiLCJpc3MiOiJEYk1vYmplY3RzVGVzdCIsInJvbGVzIjpbeyJpZCI6MSwibmFtZSI6IlJPTEVfVVNFUiJ9LHsiaWQiOjIsIm5hbWUiOiJST0xFX1VTRVIifSx7ImlkIjo1LCJuYW1lIjoiUk9MRV9BRE1JTiJ9LHsiaWQiOjQsIm5hbWUiOiJST0xFX01PREVSQUxFUiJ9XSwiaWF0IjoxNzA0OTAxODA0LCJleHAiOjE3MDQ5ODk1MDR9.2ZtXX3Q4az89eCVZ_X7D921dTam1GHV04j-i9PvUJn1Ej66tw89EBiGJu9zMejl3Kq1QWJqZYD83-mnkjoUuRQ", null)
        );

        // Act
        List<? extends TokenEntity> returnedResult = repository.getPage(0, 2);

        // Assert
        assertEquals(expectedResults.size(), returnedResult.size());
        assertEquals(expectedResults.getFirst().getTokenId(), returnedResult.getFirst().getTokenId());
        assertEquals(expectedResults.getFirst().getToken(), returnedResult.getFirst().getToken());
        assertEquals(expectedResults.getLast().getTokenId(), returnedResult.getLast().getTokenId());
        assertEquals(expectedResults.getLast().getToken(), returnedResult.getLast().getToken());
    }

    @Test
    void findValid_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        String firstValid = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxLDUwQDYwLnRlc3QiLCJpc3MiOiJEYk1vYmplY3RzVGVzdCIsInJvbGVzIjpbeyJpZCI6MSwibmFtZSI6IlJPTEVfVVNFUiJ9LHsiaWQiOjIsIm5hbWUiOiJST0xFX1VTRVIifSx7ImlkIjo1LCJuYW1lIjoiUk9MRV9BRE1JTiJ9LHsiaWQiOjQsIm5hbWUiOiJST0xFX01PREVSQUxFUiJ9XSwiaWF0IjoxNzA0OTAxODA0LCJleHAiOjE3MDQ5ODk1MDR9.2ZtXX3Q4az89eCVZ_X7D921dTam1GHV04j-i9PvUJn1Ej66tw89EBiGJu9zMejl3Kq1QWJqZYD83-mnkjoUuRQ";
        int validCount = 3;
        String orderBy = "t.token_id";
        String orderDirection = "ASC";

        // Act
        List<? extends TokenEntity> returnedResult = repository.findValid(0, validCount, orderBy, orderDirection);

        // Assert
        assertEquals(validCount, returnedResult.size());
        assertEquals(firstValid, returnedResult.getFirst().getToken());
    }

    @Test
    void getPage_sortedByExpirationASC_shouldReturnCorrectResult() {
        // Arrange
        String firstValid = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcwNTE5NTcsImV4cCI6MTczNzA1NTU1N30.dMHEGQQAP7Kgairw0jkhnr_ny7CU4mOr6YlVb7C90BCVyAktPm2_9F9yvK3DkLoXd9Kp5H-u7BDIHQbRT2pw9g";
        int validCount = 2;
        String orderBy = "t.expiration_date";
        String orderDirection = "ASC";

        // Act
        List<? extends TokenEntity> returnedResult = repository.findValid(0, validCount, orderBy, orderDirection);

        // Assert
        assertEquals(validCount, returnedResult.size());
        assertEquals(firstValid, returnedResult.getFirst().getToken());
    }

    @Test
    void getPage_withBadOrderBy_shouldThrowException() {
        // Arrange
        int size = 2;
        String orderBy = "t.invocation_date";
        String orderDirection = "ASC";

        // Assert
        assertThatThrownBy(() ->
                // Act
                repository.findValid(0, size, orderBy, orderDirection)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sortBy parameter");
    }

    @Test
    void getPage_withBadOrderDirection_shouldThrowException() {
        // Arrange
        int size = 2;
        String orderBy = "t.invocation_date";
        String orderDirection = "ESC";

        // Assert
        assertThatThrownBy(() ->
                // Act
                repository.findValid(0, size, orderBy, orderDirection)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sortOrder parameter");
    }

    @Test
    void findById_withValidTokenId_shouldReturnCorrectResult() {
        // Arrange
        String expectedToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcwNTE5OTgsImV4cCI6MTczNzA1NTU5OH0.X-KDtxgM8ohZQBz49o9DwBsRNoSM-8cq4haBT6kubFu6KV_-fjmS75RIoke-OYsUFjM9J953e7o8FIvQJ_49xg";
        int expectedTokenId = 10;

        // Act
        Optional<TokenEntity> tokenResult = repository.findById(expectedTokenId);

        // Assert
        assertTrue(tokenResult.isPresent());
        assertEquals(expectedToken, tokenResult.get().getToken());
        assertEquals(expectedTokenId, tokenResult.get().getTokenId());
    }

    @Test
    void findById_withNonExistingTokenId_shouldReturnOptionalEmpty() {
        // Arrange
        int expectedTokenId = 777;

        // Act
        Optional<TokenEntity> tokenResult = repository.findById(expectedTokenId);

        // Assert
        assertTrue(tokenResult.isEmpty());
    }

    @Test
    void saveThenDeleteToken_withValidParams_shouldBehaveAsExpected() {
        // Arrange
        final int saveTokenId = 789;
        final long userId = 3L;
        final String token = "token_value_to_save";

        // Act
        int saveResult = repository.save(saveTokenId, userId, token);
        TokenEntity savedTokenEntity = repository.findById(saveTokenId).orElse(null);
        int deleteResult = repository.deleteById(saveTokenId, userId);
        TokenEntity deletedTokenEntity = repository.findById(saveTokenId).orElse(null);

        // Assert
        assertEquals(1, saveResult);
        assertNotNull(savedTokenEntity);
        assertEquals(saveTokenId, savedTokenEntity.getTokenId());
        assertEquals(token, savedTokenEntity.getToken());
        assertEquals(1, deleteResult);
        assertNull(deletedTokenEntity);
    }

    @Test
    void save_withNullTokenValue_shouldThrowException() {
        // Arrange
        final int saveTokenId = 789;
        final long userId = 3L;

        // Assert
        assertThatThrownBy(() ->
                // Act
                repository.save(saveTokenId, userId, null)
        )
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Column 'token' cannot be null");
    }

    @Test
    void save_withExistingTokenId_shouldReturnZero() {
        // Arrange
        final int saveTokenId = 6;
        final long userId = 3L;
        final String token = "token_value_to_save";

        // Act
        int result = repository.save(saveTokenId, userId, token);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void deleteById_withNonExistentToken_shouldReturnZero() {
        // Arrange
        final int tokenId = 1999;
        final long userId = 1999L;

        // Act
        int result = repository.deleteById(tokenId, userId);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void getNextId_withValidRequest_shouldReturnCorrectResult() {
        // Arrange
        final int expectedId = 11;

        // Act
        long result = repository.getNextId();

        // Assert
        assertEquals(expectedId, result);
    }

    @Test
    void getPage_withValidFiltersAndSorting_shouldReturnCorrectResult() {
        // Arrange
        String firstValid = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcwNTE2OTksImV4cCI6MTczNzA1NTI5OX0.4ztN1ZpKR25Svo4UoJ9BH56zWsA3IVthQQJWqpeZTqpAABkKorz1n9SrLLFOSPA8y07AgozjMhBcBvnnpjDz-Q";
        int exceptedItems = 7;
        String orderBy = "t.token_id";
        String orderDirection = "ASC";
        String username = "jxdxak2";
        String email = "jxdxak2@";

        // Act
        List<? extends TokenEntity> returnedResult = repository.getSortedPageWithFilters(0, exceptedItems, orderBy, orderDirection, username, email);

        // Assert
        assertEquals(exceptedItems, returnedResult.size());
        assertEquals(firstValid, returnedResult.getFirst().getToken());
    }

    @Test
    void getPage_withValidUsernameAndNullEmail_shouldReturnCorrectResult() {
        // Arrange
        String firstValid = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcwNTE2OTksImV4cCI6MTczNzA1NTI5OX0.4ztN1ZpKR25Svo4UoJ9BH56zWsA3IVthQQJWqpeZTqpAABkKorz1n9SrLLFOSPA8y07AgozjMhBcBvnnpjDz-Q";
        int exceptedItems = 7;
        String orderBy = "t.token_id";
        String orderDirection = "ASC";
        String username = "jxdxak2";
        String email = null;

        // Act
        List<? extends TokenEntity> returnedResult = repository.getSortedPageWithFilters(0, exceptedItems, orderBy, orderDirection, username, email);

        // Assert
        assertEquals(exceptedItems, returnedResult.size());
        assertEquals(firstValid, returnedResult.getFirst().getToken());
    }

    @Test
    void getPage_withEmailEmptyString_shouldReturnCorrectResult() {
        // Arrange
        String firstValid = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcwNTE2OTksImV4cCI6MTczNzA1NTI5OX0.4ztN1ZpKR25Svo4UoJ9BH56zWsA3IVthQQJWqpeZTqpAABkKorz1n9SrLLFOSPA8y07AgozjMhBcBvnnpjDz-Q";
        int exceptedItems = 7;
        String orderBy = "t.token_id";
        String orderDirection = "ASC";
        String username = "jxdxak2";
        String email = "";

        // Act
        List<? extends TokenEntity> returnedResult = repository.getSortedPageWithFilters(0, exceptedItems, orderBy, orderDirection, username, email);

        // Assert
        assertEquals(exceptedItems, returnedResult.size());
        assertEquals(firstValid, returnedResult.getFirst().getToken());
    }

    @Test
    void getPage_withNulls_shouldReturnCorrectResult() {
        // Arrange
        int exceptedItems = 0;
        String orderBy = "t.token_id";
        String orderDirection = "ASC";
        String username = null;
        String email = null;

        // Act
        List<? extends TokenEntity> returnedResult = repository.getSortedPageWithFilters(0, exceptedItems, orderBy, orderDirection, username, email);

        // Assert
        assertEquals(exceptedItems, returnedResult.size());
    }

    @Test
    void getPage_withEmptyStrings_shouldReturnCorrectResult() {
        // Arrange
        int exceptedItems = 10;
        String orderBy = "t.token_id";
        String orderDirection = "ASC";
        String username = "";
        String email = "";

        // Act
        List<? extends TokenEntity> returnedResult = repository.getSortedPageWithFilters(0, exceptedItems, orderBy, orderDirection, username, email);

        // Assert
        assertEquals(exceptedItems, returnedResult.size());
    }

    @Test
    void getPage_withEmptyStringsSortedByTokenIdDESC_shouldReturnCorrectResult() {
        // Arrange
        int exceptedItems = 10;
        int exceptedTokenId = 10;
        String orderBy = "t.token_id";
        String orderDirection = "DESC";
        String username = "";
        String email = "";

        // Act
        List<? extends TokenEntity> returnedResult = repository.getSortedPageWithFilters(0, exceptedItems, orderBy, orderDirection, username, email);

        // Assert
        assertEquals(exceptedItems, returnedResult.size());
        assertEquals(exceptedTokenId, returnedResult.getFirst().getTokenId());
    }

    @Test
    void getPage_sortedByExpirationDESC_shouldReturnCorrectResult() {
        // Arrange
        int exceptedItems = 10;
        int exceptedTokenId = 2;
        String exceptedTokenValue = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxLDUwQDYwLnRlc3QiLCJpc3MiOiJEYk1vYmplY3RzVGVzdCIsInJvbGVzIjpbeyJpZCI6MSwibmFtZSI6IlJPTEVfVVNFUiJ9LHsiaWQiOjIsIm5hbWUiOiJST0xFX1VTRVIifSx7ImlkIjo1LCJuYW1lIjoiUk9MRV9BRE1JTiJ9LHsiaWQiOjQsIm5hbWUiOiJST0xFX01PREVSQUxFUiJ9XSwiaWF0IjoxNzA0OTAxODA0LCJleHAiOjE3MDQ5ODk1MDR9.2ZtXX3Q4az89eCVZ_X7D921dTam1GHV04j-i9PvUJn1Ej66tw89EBiGJu9zMejl3Kq1QWJqZYD83-mnkjoUuRQ";
        String orderBy = "t.expiration_date";
        String orderDirection = "DESC";
        String username = "";
        String email = "";

        // Act
        List<? extends TokenEntity> returnedResult = repository.getSortedPageWithFilters(0, exceptedItems, orderBy, orderDirection, username, email);

        // Assert
        assertEquals(exceptedItems, returnedResult.size());
        assertEquals(exceptedTokenId, returnedResult.getFirst().getTokenId());
        assertEquals(exceptedTokenValue, returnedResult.getFirst().getToken());
    }

    @Test
    void getPage_badOrderBy_shouldReturnException() {
        // Arrange
        int size = 2;
        String orderBy = "t.invocation_date";
        String orderDirection = "ASC";
        String username = "test";
        String email = "@test";

        // Act & Assert
        assertThatThrownBy(() ->
                repository.getSortedPageWithFilters(0, size, orderBy, orderDirection, username, email)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sortBy parameter");
    }

    @Test
    void getPage_badOrderDirection_shouldReturnException() {
        // Arrange
        int size = 2;
        String orderBy = "t.invocation_date";
        String orderDirection = "ESC";
        String username = "test";
        String email = "@test";

        // Act & Assert
        assertThatThrownBy(() ->
                repository.getSortedPageWithFilters(0, size, orderBy, orderDirection, username, email)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sortOrder parameter");
    }

    @Test
    void getFiltersCount_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        long excepted = 7;
        String username = "dxak2";
        String email = "dxak2@";

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getFiltersCount_noMatches_shouldReturnCorrectResult() {
        // Arrange
        long excepted = 0;
        String username = "tesiit";
        String email = "@bcooq.com";

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getFiltersCount_withNulls_shouldReturnCorrectCount() {
        // Arrange
        long excepted = 10;
        String username = null;
        String email = null;

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getFiltersCount_withEmptyStrings_shouldReturnCorrectCount() {
        // Arrange
        long excepted = 10;
        String username = "";
        String email = "";

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getFiltersCount_withEmptyUsername_shouldReturnCorrectCount() {
        // Arrange
        long excepted = 10;
        String username = "";
        String email = ".com";

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getFiltersCount_withNullUsername_shouldReturnCorrectCount() {
        // Arrange
        long excepted = 10;
        String username = null;
        String email = ".com";

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getValidCount_withValidParameters_shouldReturnCorrectCount() {
        // Arrange
        long excepted = 3;

        // Act
        long founded = repository.getValidCount();

        // Assert
        assertEquals(excepted, founded);
    }

}
