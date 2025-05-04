package pl.derleta.authorization.config.security.api;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import pl.derleta.authorization.config.model.UserSecurity;
import pl.derleta.authorization.config.model.UserSecurityMapper;
import pl.derleta.authorization.config.security.jwt.JwtTokenUtil;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.repository.impl.UserRepository;
import pl.derleta.authorization.repository.impl.UserRolesRepository;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class AuthApiServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserRolesRepository userRolesRepository;

    @Mock
    JwtTokenUtil jwtTokenUtil;

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


    @Test
    void saveRefreshToken_nullId_returnNull() {
        // Arrange
        Long invalidUserId = null;

        // Act
        String actualToken = authApiService.updateAccessToken(invalidUserId);

        // Assert
        assertNull(actualToken);
    }

    @Test
    void saveRefreshToken_validId_returnAccessToken() {
        // Arrange
        long userId = 1L;
        UserEntity mockUser = new UserEntity();
        List<RoleEntity> mockRoles = List.of(new RoleEntity());
        UserSecurity mockSecurity = new UserSecurity();
        mockSecurity.setId(userId);
        mockSecurity.setEmail("<EMAIL>");
        String exampleToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxLDxFTUFJTD4iLCJpc3MiOiJEYkNvbm5lY3Rpb25BcHAiLCJyb2xlcyI6W10sImlhdCI6MTc0NjIwNDcyOSwiZXhwIjoxNzQ2MjA4MzI5fQ.l1ZFQYwcjDN_BtKkVkYBf_tAwTgnSjZKCZAfbEIkm9gCGw6VGHPvNGqq_uiMwxMHAUbc293QW8ciIWzVYIeD9w";
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 60);

        when(userRepository.findById(userId)).thenReturn(mockUser);
        when(userRolesRepository.getRoles(userId)).thenReturn(mockRoles);
        when(jwtTokenUtil.generateAccessToken(any())).thenReturn(exampleToken);
        when(jwtTokenUtil.getTokenExpiration(exampleToken)).thenReturn(expiration);
        when(authApiRepository.getAccessTokenNextId()).thenReturn(42L);

        try (MockedStatic<UserSecurityMapper> mocked = mockStatic(UserSecurityMapper.class)) {
            mocked.when(() -> UserSecurityMapper.toUserSecurity(any(UserEntity.class), anySet()))
                    .thenReturn(mockSecurity);

            // Act
            String actualToken = authApiService.updateAccessToken(userId);
            String[] parts = actualToken.split("\\.");
            assertEquals(3, parts.length);
            String header = new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8);

            // Assert
            assertNotNull(actualToken);
            assertEquals(3, parts.length);
            assertTrue(header.contains("\"alg\":\"HS512\""));
            assertEquals(exampleToken.length(), actualToken.length());
        }
    }

}
