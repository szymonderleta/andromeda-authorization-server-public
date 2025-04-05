package pl.derleta.authorization.repository.impl.token;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.derleta.authorization.domain.entity.token.RefreshTokenEntity;
import pl.derleta.authorization.domain.entity.token.TokenEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository repository;

    @Test
    void getSize_withValidSetup_shouldReturnCorrectResult() {
        // Arrange
        final int size = 21;

        // Act
        var count = repository.getSize();

        // Assert
        assertEquals(size, count);
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        List<TokenEntity> expectedResults = List.of(
                new RefreshTokenEntity(1L, null, "eyJkbGwiOiJIUzUxNiJ9.eyJhdWIiOiIzLDQ1NkBkZW1vLmNvbSIsImlzc3VlciI6Ik15QXBwbGljYXRpb24iLCJyb2xlcyI6W3siaWQiOjIsIm5hbWUiOiJST0xFX0RFVkVMT1BFUiJ9LHsiaWQiOjQsIm5hbWUiOiJST0xFX01BTkdFUiJ9LHsiaWQiOjcsIm5hbWUiOiJST0xFX0FETUluIn1dLCJpYXQiOjE3MDQ5MDQzMjAsImV4cCI6MTcwNTAwMDcyMH0.KyZXD_WX18PQL9NTZxEj64PqbmR39ZP9emnv_4DFyr4503h7Ytn7ePrwuVHGamzJOUyLmnymaAXDksiäOiZRTR", null),
                new RefreshTokenEntity(2L, null, "eyJzbGEiOiJIUzMxMhJ9.eyJkbWIiOiIzLDgxNkBnZW5lLmNvbSIsImlzcyI6IkFwcGxpY2F0aW9uUHJvZHVjdCIsInJvbGVzIjpbeyJpZCI6MSwibmFtZSI6IlJPTEVfVVNFUiJ9LHsiaWQiOjMsIm5hbWUiOiJST0xFX1NQRUNJQUwifSx7ImlkIjo2LCJuYW1lIjoiUk9MRV9URVNUIn1dLCJpYXQiOjE3MDQ5MDk4MzAsImV4cCI6MTcwNTAwNjIzMH0.JuXPT_AY07QEJ2TWKzPy45AoYmJ25BXrppnp_1CZDu7581q2Oup8kNrqyRTUazfHYUpYrncfuaBADuqkrUeUEM", null)
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
        String firstValid = "eyJzbGEiOiJIUzMxMhJ9.eyJkbWIiOiIzLDgxNkBnZW5lLmNvbSIsImlzcyI6IkFwcGxpY2F0aW9uUHJvZHVjdCIsInJvbGVzIjpbeyJpZCI6MSwibmFtZSI6IlJPTEVfVVNFUiJ9LHsiaWQiOjMsIm5hbWUiOiJST0xFX1NQRUNJQUwifSx7ImlkIjo2LCJuYW1lIjoiUk9MRV9URVNUIn1dLCJpYXQiOjE3MDQ5MDk4MzAsImV4cCI6MTcwNTAwNjIzMH0.JuXPT_AY07QEJ2TWKzPy45AoYmJ25BXrppnp_1CZDu7581q2Oup8kNrqyRTUazfHYUpYrncfuaBADuqkrUeUEM";
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
    void findValid_sortedByExpirationASC_shouldReturnCorrectResult() {
        // Arrange
        String firstValid = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxNSxjbnk2ODI5MkBiY29vcS5jb20iLCJpc3MiOiJEYkNvbm5lY3Rpb25BcHAiLCJyb2xlcyI6W3siaWQiOjEsIm5hbWUiOiJST0xFX1VTRVIifV0sImlhdCI6MTc0MTE5NTg2NiwiZXhwIjoxNzQzNzg3ODY2fQ.sHKrvRGV3CDuLlx1oU1ic7KQ238uDIFQhswkEPTpyUPByG20TMiRW7XliS3bx9NYqhR3kiCJOdhvfSZP-vkwnQ";
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
    void findValid_withBadOrderByParameter_shouldThrowException() {
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
    void findValid_withBadOrderDirection_shouldThrowException() {
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
    void findById_withExistingToken_shouldReturnCorrectResult() {
        // Arrange
        String expectedToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxNSxjbnk2ODI5MkBiY29vcS5jb20iLCJpc3MiOiJEYkNvbm5lY3Rpb25BcHAiLCJyb2xlcyI6W3siaWQiOjEsIm5hbWUiOiJST0xFX1VTRVIifV0sImlhdCI6MTc0MTE5NTg1MiwiZXhwIjoxNzQzNzg3ODUyfQ.Ctx5gXN5dQVvarQ_StzNYP6qy_-DGyV3y6GrRvf_zKza3JYk0fdS3awzhjbEZBTuc_lQ3KM60qAylBtDtvVpsw";
        int expectedTokenId = 9;

        // Act
        Optional<TokenEntity> tokenResult = repository.findById(expectedTokenId);

        // Assert
        assertTrue(tokenResult.isPresent());
        assertEquals(expectedToken, tokenResult.get().getToken());
        assertEquals(expectedTokenId, tokenResult.get().getTokenId());
    }

    @Test
    void findById_withNonExistingToken_shouldReturnOptionalEmpty() {
        // Arrange
        int expectedTokenId = 777;

        // Act
        Optional<TokenEntity> tokenResult = repository.findById(expectedTokenId);

        // Assert
        assertTrue(tokenResult.isEmpty());
    }

    @Test
    void saveThenDeleteToken_withValidParameters_shouldReturnCorrectResult() {
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
    void save_withNullTokenValue_shouldThrowDataIntegrityViolationException() {
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
    void testSaveExistingTokenId_shouldReturnZeroAsNoSaveHappened() {
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
    void deleteNonExistedToken_shouldReturnZeroAsNoDeleteHappened() {
        // Arrange
        final int tokenId = 1999;
        final long userId = 1999L;

        // Act
        int result = repository.deleteById(tokenId, userId);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void getNextId_shouldReturnExpectedId() {
        // Arrange
        final int expectedId = 22;

        // Act
        long result = repository.getNextId();

        // Assert
        assertEquals(expectedId, result);
    }

    @Test
    void getSortedPageWithFilters_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        String firstValid = "eyJzbGEiOiJIUzMxMhJ9.eyJkbWIiOiIzLDgxNkBnZW5lLmNvbSIsImlzcyI6IkFwcGxpY2F0aW9uUHJvZHVjdCIsInJvbGVzIjpbeyJpZCI6MSwibmFtZSI6IlJPTEVfVVNFUiJ9LHsiaWQiOjMsIm5hbWUiOiJST0xFX1NQRUNJQUwifSx7ImlkIjo2LCJuYW1lIjoiUk9MRV9URVNUIn1dLCJpYXQiOjE3MDQ5MDk4MzAsImV4cCI6MTcwNTAwNjIzMH0.JuXPT_AY07QEJ2TWKzPy45AoYmJ25BXrppnp_1CZDu7581q2Oup8kNrqyRTUazfHYUpYrncfuaBADuqkrUeUEM";
        int exceptedItems = 8;
        String orderBy = "t.token_id";
        String orderDirection = "ASC";
        String username = "test";
        String email = "cny68292@";

        // Act
        List<? extends TokenEntity> returnedResult = repository.getSortedPageWithFilters(0, exceptedItems, orderBy, orderDirection, username, email);

        // Assert
        assertEquals(exceptedItems, returnedResult.size());
        assertEquals(firstValid, returnedResult.getFirst().getToken());
    }

    @Test
    void getSortedPageWithFilters_withEmailAsNull_shouldReturnCorrectResult() {
        // Arrange
        String firstValid = "eyJzbGEiOiJIUzMxMhJ9.eyJkbWIiOiIzLDgxNkBnZW5lLmNvbSIsImlzcyI6IkFwcGxpY2F0aW9uUHJvZHVjdCIsInJvbGVzIjpbeyJpZCI6MSwibmFtZSI6IlJPTEVfVVNFUiJ9LHsiaWQiOjMsIm5hbWUiOiJST0xFX1NQRUNJQUwifSx7ImlkIjo2LCJuYW1lIjoiUk9MRV9URVNUIn1dLCJpYXQiOjE3MDQ5MDk4MzAsImV4cCI6MTcwNTAwNjIzMH0.JuXPT_AY07QEJ2TWKzPy45AoYmJ25BXrppnp_1CZDu7581q2Oup8kNrqyRTUazfHYUpYrncfuaBADuqkrUeUEM";
        int exceptedItems = 8;
        String orderBy = "t.token_id";
        String orderDirection = "ASC";
        String username = "test";
        String email = null;

        // Act
        List<? extends TokenEntity> returnedResult = repository.getSortedPageWithFilters(0, exceptedItems, orderBy, orderDirection, username, email);

        // Assert
        assertEquals(exceptedItems, returnedResult.size());
        assertEquals(firstValid, returnedResult.getFirst().getToken());
    }

    @Test
    void getSortedPageWithFilters_withEmailEmptyString_shouldReturnCorrectResult() {
        // Arrange
        String firstValid = "eyJzbGEiOiJIUzMxMhJ9.eyJkbWIiOiIzLDgxNkBnZW5lLmNvbSIsImlzcyI6IkFwcGxpY2F0aW9uUHJvZHVjdCIsInJvbGVzIjpbeyJpZCI6MSwibmFtZSI6IlJPTEVfVVNFUiJ9LHsiaWQiOjMsIm5hbWUiOiJST0xFX1NQRUNJQUwifSx7ImlkIjo2LCJuYW1lIjoiUk9MRV9URVNUIn1dLCJpYXQiOjE3MDQ5MDk4MzAsImV4cCI6MTcwNTAwNjIzMH0.JuXPT_AY07QEJ2TWKzPy45AoYmJ25BXrppnp_1CZDu7581q2Oup8kNrqyRTUazfHYUpYrncfuaBADuqkrUeUEM";
        int exceptedItems = 8;
        String orderBy = "t.token_id";
        String orderDirection = "ASC";
        String username = "test";
        String email = "";

        // Act
        List<? extends TokenEntity> returnedResult = repository.getSortedPageWithFilters(0, exceptedItems, orderBy, orderDirection, username, email);

        // Assert
        assertEquals(exceptedItems, returnedResult.size());
        assertEquals(firstValid, returnedResult.getFirst().getToken());
    }

    @Test
    void getSortedPageWithFilters_withNullParameters_shouldReturnCorrectResult() {
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
    void getSortedPageWithFilters_withEmptyStrings_shouldReturnCorrectResult() {
        // Arrange
        int exceptedItems = 21;
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
    void getSortedPageWithFilters_withEmptyStringsSortedByTokenIdDESC_shouldReturnCorrectResult() {
        // Arrange
        int exceptedItems = 21;
        int exceptedTokenId = 21;
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
    void getSortedPageWithFilters_sortedPageByExpirationDESC_shouldReturnCorrectResult() {
        // Arrange
        int exceptedItems = 10;
        int exceptedTokenId = 2;
        String exceptedTokenValue = "eyJzbGEiOiJIUzMxMhJ9.eyJkbWIiOiIzLDgxNkBnZW5lLmNvbSIsImlzcyI6IkFwcGxpY2F0aW9uUHJvZHVjdCIsInJvbGVzIjpbeyJpZCI6MSwibmFtZSI6IlJPTEVfVVNFUiJ9LHsiaWQiOjMsIm5hbWUiOiJST0xFX1NQRUNJQUwifSx7ImlkIjo2LCJuYW1lIjoiUk9MRV9URVNUIn1dLCJpYXQiOjE3MDQ5MDk4MzAsImV4cCI6MTcwNTAwNjIzMH0.JuXPT_AY07QEJ2TWKzPy45AoYmJ25BXrppnp_1CZDu7581q2Oup8kNrqyRTUazfHYUpYrncfuaBADuqkrUeUEM";
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
    void getSortedPageWithFilters_withBadOrderBy_shouldReturnException() {
        // Arrange
        int size = 2;
        String orderBy = "t.invocation_date";
        String orderDirection = "ASC";
        String username = "test";
        String email = "@test";

        // Act & Assert
        assertThatThrownBy(() ->
                // Act
                repository.getSortedPageWithFilters(0, size, orderBy, orderDirection, username, email)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sortBy parameter");
    }

    @Test
    void getSortedPageWithFilters_withBadOrderDirection_shouldReturnException() {
        // Arrange
        int size = 2;
        String orderBy = "t.invocation_date";
        String orderDirection = "ESC";
        String username = "test";
        String email = "@test";

        // Act & Assert
        assertThatThrownBy(() ->
                // Act
                repository.getSortedPageWithFilters(0, size, orderBy, orderDirection, username, email)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sortOrder parameter");
    }

    @Test
    void getFiltersCount_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        long excepted = 8;
        String username = "test";
        String email = "@bcooq.com";

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getFiltersCount_withNoMatches_shouldReturnCorrectResult() {
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
    void getFiltersCount_withNullParameters_shouldReturnCorrectResult() {
        // Arrange
        long excepted = 21;
        String username = null;
        String email = null;

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getFiltersCount_withEmptyStrings_shouldReturnCorrectResult() {
        // Arrange
        long excepted = 21;
        String username = "";
        String email = "";

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getFiltersCount_withEmptyUsername_shouldReturnCorrectResult() {
        // Arrange
        long excepted = 21;
        String username = "";
        String email = ".com";

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getFiltersCount_withNullUsername_shouldReturnCorrectResult() {
        // Arrange
        long excepted = 21;
        String username = null;
        String email = ".com";

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getValidCount_withNoParameters_shouldReturnCorrectResult() {
        // Arrange
        long excepted = 3;

        // Act
        long founded = repository.getValidCount();

        // Assert
        assertEquals(excepted, founded);
    }

}
