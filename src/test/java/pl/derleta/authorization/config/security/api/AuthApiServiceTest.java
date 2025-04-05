package pl.derleta.authorization.config.security.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class AuthApiServiceTest {

    @Autowired
    private AuthApiService authApiService;

    @MockBean
    private AuthApiRepository authApiRepository;

    @Test
    void saveAccessToken_withValidParameters_shouldSaveSuccessfully() {
        // Arrange
        long userId = 1L;
        String token = "testAccessToken";
        Date expirationDate = Date.valueOf(LocalDate.now().plusDays(7));
        long tokenId = 42L;

        when(authApiRepository.getAccessTokenNextId()).thenReturn(tokenId);
        doNothing().when(authApiRepository).saveAccessToken(tokenId, userId, token, expirationDate);
        when(authApiRepository.findAccessTokenById(tokenId)).thenReturn(Optional.of(token));

        // Act
        boolean result = authApiService.saveAccessToken(userId, token, expirationDate);

        // Assert
        assertTrue(result);
        verify(authApiRepository, times(1)).getAccessTokenNextId();
        verify(authApiRepository, times(1)).saveAccessToken(tokenId, userId, token, expirationDate);
        verify(authApiRepository, times(1)).findAccessTokenById(tokenId);
    }

    @Test
    void saveAccessToken_withInvalidRepositoryState_shouldThrowException() {
        // Arrange
        long userId = 1L;
        String token = "testAccessToken";
        Date expirationDate = Date.valueOf(LocalDate.now().plusDays(7));
        long tokenId = 42L;

        when(authApiRepository.getAccessTokenNextId()).thenReturn(tokenId);
        doNothing().when(authApiRepository).saveAccessToken(tokenId, userId, token, expirationDate);
        when(authApiRepository.findAccessTokenById(tokenId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ObjectNotSavedException.class, () ->
                authApiService.saveAccessToken(userId, token, expirationDate));

        verify(authApiRepository, times(1)).getAccessTokenNextId();
        verify(authApiRepository, times(1)).saveAccessToken(tokenId, userId, token, expirationDate);
        verify(authApiRepository, times(1)).findAccessTokenById(tokenId);
    }

    @Test
    void saveRefreshToken_withValidParameters_shouldSaveSuccessfully() {
        // Arrange
        long userId = 1L;
        String token = "testRefreshToken";
        Date expirationDate = Date.valueOf(LocalDate.now().plusDays(7));
        long tokenId = 42L;

        when(authApiRepository.getRefreshTokenNextId()).thenReturn(tokenId);
        doNothing().when(authApiRepository).saveRefreshToken(tokenId, userId, token, expirationDate);
        when(authApiRepository.findRefreshTokenById(tokenId)).thenReturn(Optional.of(token));

        // Act
        boolean result = authApiService.saveRefreshToken(userId, token, expirationDate);

        // Assert
        assertTrue(result);
        verify(authApiRepository, times(1)).getRefreshTokenNextId();
        verify(authApiRepository, times(1)).saveRefreshToken(tokenId, userId, token, expirationDate);
        verify(authApiRepository, times(1)).findRefreshTokenById(tokenId);
    }

    @Test
    void saveRefreshToken_withInvalidRepositoryState_shouldThrowException() {
        // Arrange
        long userId = 1L;
        String token = "testRefreshToken";
        Date expirationDate = Date.valueOf(LocalDate.now().plusDays(7));
        long tokenId = 42L;

        when(authApiRepository.getRefreshTokenNextId()).thenReturn(tokenId);
        doNothing().when(authApiRepository).saveRefreshToken(tokenId, userId, token, expirationDate);
        when(authApiRepository.findRefreshTokenById(tokenId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ObjectNotSavedException.class, () ->
                authApiService.saveRefreshToken(userId, token, expirationDate));

        verify(authApiRepository, times(1)).getRefreshTokenNextId();
        verify(authApiRepository, times(1)).saveRefreshToken(tokenId, userId, token, expirationDate);
        verify(authApiRepository, times(1)).findRefreshTokenById(tokenId);
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResultTrueForValidAccessToken() {
        // Arrange
        long tokenId = 42L;
        String token = "testAccessToken";

        when(authApiRepository.findAccessTokenById(tokenId)).thenReturn(Optional.of("TESTACCESSTOKEN"));

        // Act
        boolean result = authApiService.isValidAccessToken(tokenId, token);

        // Assert
        assertTrue(result);
        verify(authApiRepository, times(1)).findAccessTokenById(tokenId);
    }

    @Test
    void getPage_withInvalidParameters_shouldReturnCorrectResultFalseForInvalidAccessToken() {
        // Arrange
        long tokenId = 42L;
        String token = "testAccessToken";

        // Scenario 1: Token not found
        when(authApiRepository.findAccessTokenById(tokenId)).thenReturn(Optional.empty());

        // Act
        boolean resultNotFound = authApiService.isValidAccessToken(tokenId, token);

        // Assert
        assertFalse(resultNotFound);
        verify(authApiRepository, times(1)).findAccessTokenById(tokenId);

        // Reset mocks for the next scenario
        reset(authApiRepository);

        // Arrange for Scenario 2: Token does not match
        when(authApiRepository.findAccessTokenById(tokenId)).thenReturn(Optional.of("DifferentToken"));

        // Act
        boolean resultMismatch = authApiService.isValidAccessToken(tokenId, token);

        // Assert
        assertFalse(resultMismatch);
        verify(authApiRepository, times(1)).findAccessTokenById(tokenId);
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResultTrueForRefreshToken() {
        // Arrange
        long tokenId = 42L;
        String token = "testRefreshToken";

        when(authApiRepository.findRefreshTokenById(tokenId)).thenReturn(Optional.of("TESTREFRESHTOKEN"));

        // Act
        boolean result = authApiService.isValidRefreshToken(tokenId, token);

        // Assert
        assertTrue(result);
        verify(authApiRepository, times(1)).findRefreshTokenById(tokenId);
    }

    @Test
    void getPage_withInvalidParameters_shouldReturnCorrectResultFalseForRefreshToken() {
        // Arrange
        long tokenId = 42L;
        String token = "testRefreshToken";

        // Scenario 1: Token not found
        when(authApiRepository.findRefreshTokenById(tokenId)).thenReturn(Optional.empty());

        // Act
        boolean resultNotFound = authApiService.isValidRefreshToken(tokenId, token);

        // Assert
        assertFalse(resultNotFound);
        verify(authApiRepository, times(1)).findRefreshTokenById(tokenId);

        // Reset mocks for the next scenario
        reset(authApiRepository);

        // Arrange for Scenario 2: Token does not match
        when(authApiRepository.findRefreshTokenById(tokenId)).thenReturn(Optional.of("DifferentToken"));

        // Act
        boolean resultMismatch = authApiService.isValidRefreshToken(tokenId, token);

        // Assert
        assertFalse(resultMismatch);
        verify(authApiRepository, times(1)).findRefreshTokenById(tokenId);
    }

}
