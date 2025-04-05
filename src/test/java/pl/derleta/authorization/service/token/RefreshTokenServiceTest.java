package pl.derleta.authorization.service.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.entity.token.RefreshTokenEntity;
import pl.derleta.authorization.domain.entity.token.TokenEntity;
import pl.derleta.authorization.domain.model.RefreshToken;
import pl.derleta.authorization.repository.impl.UserRepository;
import pl.derleta.authorization.repository.impl.token.RefreshTokenRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class RefreshTokenServiceTest {

    private UserRepository mockUserRepository;
    private RefreshTokenRepository mockRepository;
    private RefreshTokenService tokenService;

    @BeforeEach
    void setUp() {
        mockUserRepository = mock(UserRepository.class);
        mockRepository = mock(RefreshTokenRepository.class);
        tokenService = new RefreshTokenService();
        tokenService.setRepository(mockRepository);
        tokenService.setUserRepository(mockUserRepository);
    }


    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "username";
        String sortOrder = "ASC";
        String usernameFilter = "testUser";
        String emailFilter = "testEmail";
        int offset = 0;

        List<TokenEntity> tokenEntities = List.of(
                new RefreshTokenEntity(1L, new UserEntity(), "token1", null),
                new RefreshTokenEntity(2L, new UserEntity(), "token2", null)
        );

        when(mockRepository.getSortedPageWithFilters(offset, size, "u.username", "ASC", usernameFilter, emailFilter))
                .thenReturn(tokenEntities);
        when(mockRepository.getFiltersCount(usernameFilter, emailFilter)).thenReturn(2L);

        // Act
        Page<RefreshToken> result = tokenService.getPage(page, size, sortBy, sortOrder, usernameFilter, emailFilter);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(mockRepository, times(1)).getSortedPageWithFilters(offset, size, "u.username", "ASC", usernameFilter, emailFilter);
    }

    @Test
    void getPage_withEmptyResult_shouldReturnEmptyPage() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "email";
        String sortOrder = "DESC";
        String usernameFilter = "unknownUser";
        String emailFilter = "unknownEmail";
        int offset = 0;

        when(mockRepository.getSortedPageWithFilters(offset, size, "u.email", "DESC", usernameFilter, emailFilter))
                .thenReturn(List.of());
        when(mockRepository.getFiltersCount(usernameFilter, emailFilter)).thenReturn(0L);

        // Act
        Page<RefreshToken> result = tokenService.getPage(page, size, sortBy, sortOrder, usernameFilter, emailFilter);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(mockRepository, times(1)).getSortedPageWithFilters(offset, size, "u.email", "DESC", usernameFilter, emailFilter);
    }

    @Test
    void getPage_withException_shouldThrowRuntimeException() {
        // Arrange
        int page = 1;
        int size = 10;
        String sortBy = "username";
        String sortOrder = "ASC";
        String usernameFilter = "testUser";
        String emailFilter = "testEmail";
        int offset = page * size;

        when(mockRepository.getSortedPageWithFilters(offset, size, "u.username", "ASC", usernameFilter, emailFilter))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> tokenService.getPage(page, size, sortBy, sortOrder, usernameFilter, emailFilter));
        verify(mockRepository, times(1)).getSortedPageWithFilters(offset, size, "u.username", "ASC", usernameFilter, emailFilter);
    }

    @Test
    void getValid_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "username";
        String sortOrder = "ASC";
        int offset = 0;

        List<TokenEntity> tokenEntities = List.of(
                new RefreshTokenEntity(1L, new UserEntity(), "token1", null),
                new RefreshTokenEntity(2L, new UserEntity(), "token2", null)
        );

        when(mockRepository.findValid(offset, size, "u.username", "ASC")).thenReturn(tokenEntities);
        when(mockRepository.getValidCount()).thenReturn(2L);

        // Act
        Page<RefreshToken> result = tokenService.getValid(page, size, sortBy, sortOrder);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1L, result.getContent().getFirst().tokenId());
        verify(mockRepository, times(1)).findValid(offset, size, "u.username", "ASC");
    }

    @Test
    void getValid_withEmptyResult_shouldReturnEmptyPage() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "username";
        String sortOrder = "ASC";
        int offset = 0;

        when(mockRepository.findValid(offset, size, "u.username", "ASC")).thenReturn(List.of());
        when(mockRepository.getValidCount()).thenReturn(0L);

        // Act
        Page<RefreshToken> result = tokenService.getValid(page, size, sortBy, sortOrder);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(mockRepository, times(1)).findValid(offset, size, "u.username", "ASC");
    }

    @Test
    void getValid_withException_shouldThrowRuntimeException() {
        // Arrange
        int page = 1;
        int size = 10;
        String sortBy = "username";
        String sortOrder = "DESC";
        int offset = page * size;

        when(mockRepository.findValid(offset, size, "u.username", "DESC")).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> tokenService.getValid(page, size, sortBy, sortOrder));
        verify(mockRepository, times(1)).findValid(offset, size, "u.username", "DESC");
    }

    @Test
    void get_withValidTokenId_shouldReturnRefreshToken() {
        // Arrange
        long tokenId = 123L;
        RefreshTokenEntity tokenEntity = new RefreshTokenEntity(tokenId, new UserEntity(), "validToken", null);
        when(mockRepository.findById(tokenId)).thenReturn(Optional.of(tokenEntity));

        // Act
        RefreshToken result = tokenService.get(tokenId);

        // Assert
        assertNotNull(result);
        assertEquals(tokenId, result.tokenId());
        assertEquals("validToken", result.token());
    }

    @Test
    void get_withInvalidTokenId_shouldReturnNull() {
        // Arrange
        long tokenId = 999L;
        when(mockRepository.findById(tokenId)).thenReturn(Optional.empty());

        // Act
        RefreshToken result = tokenService.get(tokenId);

        // Assert
        assertNull(result);
    }

    @Test
    void save_withValidData_shouldSaveTokenAndReturnCorrectResult() {
        // Arrange
        long userId = 1L;
        String token = "validTokenString";
        UserEntity userEntity = new UserEntity();
        long tokenId = 123L;

        when(mockUserRepository.isValidId(userId)).thenReturn(true);
        when(mockRepository.getNextId()).thenReturn(tokenId);
        when(mockRepository.findById(tokenId)).thenReturn(Optional.of(new RefreshTokenEntity(tokenId, userEntity, token, null)));

        // Act
        RefreshToken result = tokenService.save(userId, token);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(tokenId, result.tokenId(), "Token ID doesn't match");
        assertEquals(token, result.token(), "Token string doesn't match");

        verify(mockRepository, times(1)).save(eq(tokenId), eq(userId), eq(token));
        verify(mockRepository, times(1)).getNextId();
        verify(mockRepository, times(1)).findById(tokenId);
    }

    @Test
    void save_withMissingNextId_shouldReturnNull() {
        // Arrange
        long userId = 1L;
        String token = "validTokenString";

        when(mockRepository.getNextId()).thenReturn(0L);

        // Act
        RefreshToken result = tokenService.save(userId, token);

        // Assert
        assertNull(result);
        verify(mockRepository, never()).save(anyLong(), eq(userId), eq(token));
    }

    @Test
    void save_withNullToken_shouldReturnNull() {
        // Arrange
        long userId = 1L;

        // Act
        RefreshToken result = tokenService.save(userId, null);

        // Assert
        assertNull(result);
        verify(mockRepository, never()).save(anyLong(), eq(userId), eq(null));
    }


    @Test
    void delete_withValidTokenAndUserId_shouldDeleteToken() {
        // Arrange
        long tokenId = 1L;
        long userId = 1L;
        TokenEntity tokenEntity = new RefreshTokenEntity();
        tokenEntity.setTokenId(tokenId);

        when(mockRepository.findById(tokenId)).thenReturn(Optional.of(tokenEntity));

        // Act
        boolean result = tokenService.delete(tokenId, userId);

        // Assert
        assertTrue(result);
        verify(mockRepository, times(1)).deleteById(tokenId, userId);
    }

    @Test
    void delete_withInvalidTokenId_shouldNotDeleteToken() {
        // Arrange
        long tokenId = 200L;
        long userId = 1L;

        when(mockRepository.findById(tokenId)).thenReturn(Optional.empty());

        // Act
        boolean result = tokenService.delete(tokenId, userId);

        // Assert
        assertFalse(result);
        verify(mockRepository, never()).deleteById(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void delete_withNonPositiveTokenId_shouldNotDeleteToken() {
        // Arrange
        long tokenId = 1L;
        long userId = 1L;
        TokenEntity tokenEntity = new RefreshTokenEntity();
        tokenEntity.setTokenId(-1L);

        when(mockRepository.findById(tokenId)).thenReturn(Optional.of(tokenEntity));

        // Act
        boolean result = tokenService.delete(tokenId, userId);

        // Assert
        assertFalse(result);
        verify(mockRepository, never()).deleteById(Mockito.anyLong(), Mockito.anyLong());
    }

}
