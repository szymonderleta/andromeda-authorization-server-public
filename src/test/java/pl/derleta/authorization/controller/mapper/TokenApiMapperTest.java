package pl.derleta.authorization.controller.mapper;

import org.junit.jupiter.api.Test;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.entity.token.TokenEntity;
import pl.derleta.authorization.domain.model.AccessToken;
import pl.derleta.authorization.domain.model.ConfirmationToken;
import pl.derleta.authorization.domain.model.RefreshToken;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenApiMapperTest {

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult_forEmptyTokenEntityListToConfirmationTokens() {
        // Arrange
        List<TokenEntity> emptyEntities = List.of();

        // Act
        List<ConfirmationToken> result = TokenApiMapper.toConfirmationTokens(emptyEntities);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult_forListOfTokenEntitiesToConfirmationTokens() {
        // Arrange
        TokenEntity mockEntity1 = mock(TokenEntity.class);
        TokenEntity mockEntity2 = mock(TokenEntity.class);

        when(mockEntity1.getTokenId()).thenReturn(1L);
        when(mockEntity1.getToken()).thenReturn("token-1");
        when(mockEntity1.getExpirationDate()).thenReturn(Timestamp.valueOf("2023-12-31 12:00:00"));
        UserEntity mockUser1 = mock(UserEntity.class);
        when(mockUser1.getUserId()).thenReturn(10L);
        when(mockUser1.getUsername()).thenReturn("user1");
        when(mockUser1.getPassword()).thenReturn("password1");
        when(mockUser1.getEmail()).thenReturn("user1@test.com");
        when(mockEntity1.getUser()).thenReturn(mockUser1);

        when(mockEntity2.getTokenId()).thenReturn(2L);
        when(mockEntity2.getToken()).thenReturn("token-2");
        when(mockEntity2.getExpirationDate()).thenReturn(Timestamp.valueOf("2024-01-01 12:00:00"));
        UserEntity mockUser2 = mock(UserEntity.class);
        when(mockUser2.getUserId()).thenReturn(20L);
        when(mockUser2.getUsername()).thenReturn("user2");
        when(mockUser2.getPassword()).thenReturn("password2");
        when(mockUser2.getEmail()).thenReturn("user2@test.com");
        when(mockEntity2.getUser()).thenReturn(mockUser2);

        List<TokenEntity> entities = List.of(mockEntity1, mockEntity2);

        // Act
        List<ConfirmationToken> result = TokenApiMapper.toConfirmationTokens(entities);

        // Assert
        assertEquals(2, result.size());

        ConfirmationToken token1 = result.getFirst();
        assertEquals(1L, token1.tokenId());
        assertEquals("token-1", token1.token());
        assertEquals(10L, token1.user().userId());
        assertEquals("user1", token1.user().username());
        assertEquals("password1", token1.user().password());
        assertEquals("user1@test.com", token1.user().email());
        assertEquals(Timestamp.valueOf("2023-12-31 12:00:00"), token1.expirationDate());

        ConfirmationToken token2 = result.get(1);
        assertEquals(2L, token2.tokenId());
        assertEquals("token-2", token2.token());
        assertEquals(20L, token2.user().userId());
        assertEquals("user2", token2.user().username());
        assertEquals("password2", token2.user().password());
        assertEquals("user2@test.com", token2.user().email());
        assertEquals(Timestamp.valueOf("2024-01-01 12:00:00"), token2.expirationDate());
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult_forTokenEntityToConfirmationToken() {
        // Arrange
        TokenEntity mockEntity = mock(TokenEntity.class);

        when(mockEntity.getTokenId()).thenReturn(1L);
        when(mockEntity.getToken()).thenReturn("valid-token");
        when(mockEntity.getExpirationDate()).thenReturn(Timestamp.valueOf("2023-12-31 12:00:00"));

        UserEntity mockUser = mock(UserEntity.class);
        when(mockUser.getUserId()).thenReturn(1L);
        when(mockUser.getUsername()).thenReturn("user123");
        when(mockUser.getPassword()).thenReturn("password123");
        when(mockUser.getEmail()).thenReturn("user@test.com");
        when(mockEntity.getUser()).thenReturn(mockUser);

        // Act
        ConfirmationToken confirmationToken = TokenApiMapper.toConfirmationToken(mockEntity);

        // Assert
        assertEquals(1L, confirmationToken.tokenId());
        assertEquals("valid-token", confirmationToken.token());
        assertEquals(1L, confirmationToken.user().userId());
        assertEquals("user123", confirmationToken.user().username());
        assertEquals("password123", confirmationToken.user().password());
        assertEquals("user@test.com", confirmationToken.user().email());
        assertEquals(Timestamp.valueOf("2023-12-31 12:00:00"), confirmationToken.expirationDate());
    }

    @Test
    void getPage_withNullFields_shouldReturnCorrectResult_forTokenEntityToConfirmationToken() {
        // Arrange
        TokenEntity mockEntity = mock(TokenEntity.class);
        when(mockEntity.getTokenId()).thenReturn(0L);
        when(mockEntity.getToken()).thenReturn(null);
        when(mockEntity.getExpirationDate()).thenReturn(null);
        when(mockEntity.getUser()).thenReturn(null);

        // Act
        ConfirmationToken confirmationToken = TokenApiMapper.toConfirmationToken(mockEntity);

        // Assert
        assertEquals(0L, confirmationToken.tokenId());
        assertNull(confirmationToken.token());
        assertNull(confirmationToken.user());
        assertNull(confirmationToken.expirationDate());
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult_forEmptyTokenEntityListToAccessTokens() {
        // Arrange
        List<TokenEntity> emptyEntities = List.of();

        // Act
        List<AccessToken> result = TokenApiMapper.toAccessTokens(emptyEntities);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult_forListOfTokenEntitiesToAccessTokens() {
        // Arrange
        TokenEntity mockEntity1 = mock(TokenEntity.class);
        TokenEntity mockEntity2 = mock(TokenEntity.class);

        when(mockEntity1.getTokenId()).thenReturn(1L);
        when(mockEntity1.getToken()).thenReturn("token-1");
        when(mockEntity1.getExpirationDate()).thenReturn(Timestamp.valueOf("2023-12-31 12:00:00"));
        UserEntity mockUser1 = mock(UserEntity.class);
        when(mockUser1.getUserId()).thenReturn(10L);
        when(mockUser1.getUsername()).thenReturn("user1");
        when(mockUser1.getPassword()).thenReturn("password1");
        when(mockUser1.getEmail()).thenReturn("user1@test.com");
        when(mockEntity1.getUser()).thenReturn(mockUser1);

        when(mockEntity2.getTokenId()).thenReturn(2L);
        when(mockEntity2.getToken()).thenReturn("token-2");
        when(mockEntity2.getExpirationDate()).thenReturn(Timestamp.valueOf("2024-01-01 12:00:00"));
        UserEntity mockUser2 = mock(UserEntity.class);
        when(mockUser2.getUserId()).thenReturn(20L);
        when(mockUser2.getUsername()).thenReturn("user2");
        when(mockUser2.getPassword()).thenReturn("password2");
        when(mockUser2.getEmail()).thenReturn("user2@test.com");
        when(mockEntity2.getUser()).thenReturn(mockUser2);

        List<TokenEntity> entities = List.of(mockEntity1, mockEntity2);

        // Act
        List<AccessToken> result = TokenApiMapper.toAccessTokens(entities);

        // Assert
        assertEquals(2, result.size());

        AccessToken token1 = result.getFirst();
        assertEquals(1L, token1.tokenId());
        assertEquals("token-1", token1.token());
        assertEquals(10L, token1.user().userId());
        assertEquals("user1", token1.user().username());
        assertEquals("password1", token1.user().password());
        assertEquals("user1@test.com", token1.user().email());
        assertEquals(Timestamp.valueOf("2023-12-31 12:00:00"), token1.expirationDate());

        AccessToken token2 = result.get(1);
        assertEquals(2L, token2.tokenId());
        assertEquals("token-2", token2.token());
        assertEquals(20L, token2.user().userId());
        assertEquals("user2", token2.user().username());
        assertEquals("password2", token2.user().password());
        assertEquals("user2@test.com", token2.user().email());
        assertEquals(Timestamp.valueOf("2024-01-01 12:00:00"), token2.expirationDate());
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult_forTokenEntityToAccessToken() {
        // Arrange
        TokenEntity mockEntity = mock(TokenEntity.class);

        when(mockEntity.getTokenId()).thenReturn(1L);
        when(mockEntity.getToken()).thenReturn("valid-token");
        when(mockEntity.getExpirationDate()).thenReturn(Timestamp.valueOf("2023-12-31 12:00:00"));

        UserEntity mockUser = mock(UserEntity.class);
        when(mockUser.getUserId()).thenReturn(1L);
        when(mockUser.getUsername()).thenReturn("user123");
        when(mockUser.getPassword()).thenReturn("password123");
        when(mockUser.getEmail()).thenReturn("user@test.com");
        when(mockEntity.getUser()).thenReturn(mockUser);

        // Act
        AccessToken accessToken = TokenApiMapper.toAccessToken(mockEntity);

        // Assert
        assertEquals(1L, accessToken.tokenId());
        assertEquals("valid-token", accessToken.token());
        assertEquals(1L, accessToken.user().userId());
        assertEquals("user123", accessToken.user().username());
        assertEquals("password123", accessToken.user().password());
        assertEquals("user@test.com", accessToken.user().email());
        assertEquals(Timestamp.valueOf("2023-12-31 12:00:00"), accessToken.expirationDate());
    }

    @Test
    void getPage_withNullFields_shouldReturnCorrectResult_forTokenEntityToAccessToken() {
        // Arrange
        TokenEntity mockEntity = mock(TokenEntity.class);
        when(mockEntity.getTokenId()).thenReturn(0L);
        when(mockEntity.getToken()).thenReturn(null);
        when(mockEntity.getExpirationDate()).thenReturn(null);
        when(mockEntity.getUser()).thenReturn(null);

        // Act
        AccessToken accessToken = TokenApiMapper.toAccessToken(mockEntity);

        // Assert
        assertEquals(0L, accessToken.tokenId());
        assertNull(accessToken.token());
        assertNull(accessToken.user());
        assertNull(accessToken.expirationDate());
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult_forEmptyTokenEntityListToRefreshTokens() {
        // Arrange
        List<TokenEntity> emptyEntities = List.of();

        // Act
        List<RefreshToken> result = TokenApiMapper.toRefreshTokens(emptyEntities);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult_forListOfTokenEntitiesToRefreshTokens() {
        // Arrange
        TokenEntity mockEntity1 = mock(TokenEntity.class);
        TokenEntity mockEntity2 = mock(TokenEntity.class);

        when(mockEntity1.getTokenId()).thenReturn(1L);
        when(mockEntity1.getToken()).thenReturn("refresh-token-1");
        when(mockEntity1.getExpirationDate()).thenReturn(Timestamp.valueOf("2023-12-31 12:00:00"));
        UserEntity mockUser1 = mock(UserEntity.class);
        when(mockUser1.getUserId()).thenReturn(10L);
        when(mockUser1.getUsername()).thenReturn("user1");
        when(mockUser1.getPassword()).thenReturn("password1");
        when(mockUser1.getEmail()).thenReturn("user1@test.com");
        when(mockEntity1.getUser()).thenReturn(mockUser1);

        when(mockEntity2.getTokenId()).thenReturn(2L);
        when(mockEntity2.getToken()).thenReturn("refresh-token-2");
        when(mockEntity2.getExpirationDate()).thenReturn(Timestamp.valueOf("2024-01-01 12:00:00"));
        UserEntity mockUser2 = mock(UserEntity.class);
        when(mockUser2.getUserId()).thenReturn(20L);
        when(mockUser2.getUsername()).thenReturn("user2");
        when(mockUser2.getPassword()).thenReturn("password2");
        when(mockUser2.getEmail()).thenReturn("user2@test.com");
        when(mockEntity2.getUser()).thenReturn(mockUser2);

        List<TokenEntity> entities = List.of(mockEntity1, mockEntity2);

        // Act
        List<RefreshToken> result = TokenApiMapper.toRefreshTokens(entities);

        // Assert
        assertEquals(2, result.size());

        RefreshToken token1 = result.getFirst();
        assertEquals(1L, token1.tokenId());
        assertEquals("refresh-token-1", token1.token());
        assertEquals(10L, token1.user().userId());
        assertEquals("user1", token1.user().username());
        assertEquals("password1", token1.user().password());
        assertEquals("user1@test.com", token1.user().email());
        assertEquals(Timestamp.valueOf("2023-12-31 12:00:00"), token1.expirationDate());

        RefreshToken token2 = result.get(1);
        assertEquals(2L, token2.tokenId());
        assertEquals("refresh-token-2", token2.token());
        assertEquals(20L, token2.user().userId());
        assertEquals("user2", token2.user().username());
        assertEquals("password2", token2.user().password());
        assertEquals("user2@test.com", token2.user().email());
        assertEquals(Timestamp.valueOf("2024-01-01 12:00:00"), token2.expirationDate());
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult_forTokenEntityToRefreshToken() {
        // Arrange
        TokenEntity mockEntity = mock(TokenEntity.class);

        when(mockEntity.getTokenId()).thenReturn(1L);
        when(mockEntity.getToken()).thenReturn("valid-refresh-token");
        when(mockEntity.getExpirationDate()).thenReturn(Timestamp.valueOf("2023-12-31 12:00:00"));

        UserEntity mockUser = mock(UserEntity.class);
        when(mockUser.getUserId()).thenReturn(1L);
        when(mockUser.getUsername()).thenReturn("user123");
        when(mockUser.getPassword()).thenReturn("password123");
        when(mockUser.getEmail()).thenReturn("user@test.com");
        when(mockEntity.getUser()).thenReturn(mockUser);

        // Act
        RefreshToken refreshToken = TokenApiMapper.toRefreshToken(mockEntity);

        // Assert
        assertEquals(1L, refreshToken.tokenId());
        assertEquals("valid-refresh-token", refreshToken.token());
        assertEquals(1L, refreshToken.user().userId());
        assertEquals("user123", refreshToken.user().username());
        assertEquals("password123", refreshToken.user().password());
        assertEquals("user@test.com", refreshToken.user().email());
        assertEquals(Timestamp.valueOf("2023-12-31 12:00:00"), refreshToken.expirationDate());
    }

    @Test
    void getPage_withNullFields_shouldReturnCorrectResult_forTokenEntityToRefreshToken() {
        // Arrange
        TokenEntity mockEntity = mock(TokenEntity.class);
        when(mockEntity.getTokenId()).thenReturn(0L);
        when(mockEntity.getToken()).thenReturn(null);
        when(mockEntity.getExpirationDate()).thenReturn(null);
        when(mockEntity.getUser()).thenReturn(null);

        // Act
        RefreshToken refreshToken = TokenApiMapper.toRefreshToken(mockEntity);

        // Assert
        assertEquals(0L, refreshToken.tokenId());
        assertNull(refreshToken.token());
        assertNull(refreshToken.user());
        assertNull(refreshToken.expirationDate());
    }

}
