package pl.derleta.authorization.repository.impl.token;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.derleta.authorization.domain.entity.token.ConfirmationTokenEntity;
import pl.derleta.authorization.domain.entity.token.TokenEntity;

import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
class ConfirmationTokenRepositoryTest {

    @Autowired
    private ConfirmationTokenRepository repository;

    @Test
    void getSize_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        final int size = 17;

        // Act
        var count = repository.getSize();

        // Assert
        assertEquals(size, count);
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        List<TokenEntity> expectedResults = List.of(
                new ConfirmationTokenEntity(0L, null, "h8hUUyOFej7T7FKaphvOpnTgKKZVMsCAXV2T5zwWCRRDMW6zHOVqKWA2SdXzmtfNzAixtG4pv8AqUnTeQJgs1uuiEDAUOMNhZykj", null),
                new ConfirmationTokenEntity(1L, null, "tKZ6YmOKts5QBXvRx636wBTV8MBDg4TBEKfjyqXzA0v8bTUEZxT8x4hpMYPCCfwlOaIaX7swEPjsCPj8pa0NPcbx1DYIcYgA5JA7", null)
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
        String firstValid = "tKZ6YmOKts5QBXvRx636wBTV8MBDg4TBEKfjyqXzA0v8bTUEZxT8x4hpMYPCCfwlOaIaX7swEPjsCPj8pa0NPcbx1DYIcYgA5JA7";
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
    void findValid_sortedByExpirationASC_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        String firstValid = "NBEvr2p4PNdZn6RRXA4LTwWzTjLC4u0fxTt97Wx4EQ3tSNGdjoNgUjV4Yu0qGSIVfbACsL9VT6oGXZcs2mbw8ATln33XfImz7qWI";
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
    void findValid_withInvalidOrderBy_shouldReturnException() {
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
    void findValid_withInvalidOrderDirection_shouldReturnException() {
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
        String expectedToken = "dwIL4Fba1caNYbTbtsmIcRmy7YPwboBRAxH8kan4EUO9nrnct0Mb5U2iwg0fgveGeGimHTSBELVQqXHPj2UCFIvtxtGFhx7jmbSX";
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
    void saveThenDeleteToken_withValidParameters_shouldWorkCorrectly() {
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
    void saveExistingTokenId_shouldReturnZeroAsNoSaveHappened() {
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
    void getNextId_shouldReturnCorrectResult() {
        // Arrange
        final int expectedId = 17;

        // Act
        long result = repository.getNextId();

        // Assert
        assertEquals(expectedId, result);
    }

    @Test
    void getSortedPageWithFilters_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        String firstValid = "NBEvr2p4PNdZn6RRXA4LTwWzTjLC4u0fxTt97Wx4EQ3tSNGdjoNgUjV4Yu0qGSIVfbACsL9VT6oGXZcs2mbw8ATln33XfImz7qWI";
        int exceptedItems = 3;
        String orderBy = "t.token_id";
        String orderDirection = "ASC";
        String username = "test001";
        String email = ".com";

        // Act
        List<? extends TokenEntity> returnedResult = repository.getSortedPageWithFilters(0, exceptedItems, orderBy, orderDirection, username, email);

        // Assert
        assertEquals(exceptedItems, returnedResult.size());
        assertEquals(firstValid, returnedResult.getFirst().getToken());
    }

    @Test
    void getSortedPageWithFilters_withEmailAsNull_shouldReturnCorrectResult() {
        // Arrange
        String firstValid = "NBEvr2p4PNdZn6RRXA4LTwWzTjLC4u0fxTt97Wx4EQ3tSNGdjoNgUjV4Yu0qGSIVfbACsL9VT6oGXZcs2mbw8ATln33XfImz7qWI";
        int exceptedItems = 3;
        String orderBy = "t.token_id";
        String orderDirection = "ASC";
        String username = "test001";
        String email = null;

        // Act
        List<? extends TokenEntity> returnedResult = repository.getSortedPageWithFilters(0, exceptedItems, orderBy, orderDirection, username, email);

        // Assert
        assertEquals(exceptedItems, returnedResult.size());
        assertEquals(firstValid, returnedResult.getFirst().getToken());
    }

    @Test
    void getSortedPageWithFilters_withEmailAsEmptyString_shouldReturnCorrectResult() {
        // Arrange
        String firstValid = "NBEvr2p4PNdZn6RRXA4LTwWzTjLC4u0fxTt97Wx4EQ3tSNGdjoNgUjV4Yu0qGSIVfbACsL9VT6oGXZcs2mbw8ATln33XfImz7qWI";
        int exceptedItems = 3;
        String orderBy = "t.token_id";
        String orderDirection = "ASC";
        String username = "test001";
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
        int exceptedItems = 17;
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
        int exceptedItems = 17;
        int exceptedTokenId = 16;
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
        int exceptedTokenId = 1;
        String exceptedTokenValue = "tKZ6YmOKts5QBXvRx636wBTV8MBDg4TBEKfjyqXzA0v8bTUEZxT8x4hpMYPCCfwlOaIaX7swEPjsCPj8pa0NPcbx1DYIcYgA5JA7";
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
    void getSortedPageWithFilters_badOrderBy_shouldReturnException() {
        // Arrange
        int size = 2;
        String orderBy = "t.invocation_date";
        String orderDirection = "ASC";
        String username = "test";
        String email = "@test";

        // Assert
        assertThatThrownBy(() ->
                // Act
                repository.getSortedPageWithFilters(0, size, orderBy, orderDirection, username, email)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sortBy parameter");
    }

    @Test
    void getSortedPageWithFilters_withInvalidOrderDirection_shouldThrowException() {
        // Arrange
        int size = 2;
        String orderBy = "t.invocation_date";
        String orderDirection = "ESC";
        String username = "test";
        String email = "@test";

        // Assert
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
        long excepted = 3;
        String username = "test";
        String email = "@bcooq.com";

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getFiltersCount_withNoMatches_shouldReturnZero() {
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
        long excepted = 17;
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
        long excepted = 17;
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
        long excepted = 17;
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
        long excepted = 17;
        String username = null;
        String email = ".com";

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getValidCount_shouldReturnCorrectResult() {
        // Arrange
        long excepted = 3;

        // Act
        long founded = repository.getValidCount();

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void setExpiredTokenNow_withExistingTokenId_shouldSetExpirationDateCorrectly() {
        // Arrange
        long id = 8;
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // Act
        int result = repository.setExpired(id);
        TokenEntity shouldBeExpired = repository.findById(id).orElse(null);

        // Assert
        assertEquals(1, result);
        assertNotNull(shouldBeExpired);
        assertEquals(
                now.toInstant().truncatedTo(ChronoUnit.SECONDS),
                shouldBeExpired.getExpirationDate().toInstant().truncatedTo(ChronoUnit.SECONDS)
        );
    }

    @Test
    void setExpiredTokenNow_withNonExistingTokenId_shouldNotSetExpirationDate() {
        // Arrange
        long id = 888;

        // Act
        int result = repository.setExpired(id);
        TokenEntity shouldBeExpired = repository.findById(id).orElse(null);

        // Assert
        assertEquals(0, result);
        assertNull(shouldBeExpired);
    }

}
