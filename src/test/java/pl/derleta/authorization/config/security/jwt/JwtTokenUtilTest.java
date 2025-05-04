package pl.derleta.authorization.config.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.derleta.authorization.config.model.RoleSecurity;
import pl.derleta.authorization.config.model.UserSecurity;

import java.sql.Date;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
public class JwtTokenUtilTest {

    @Value("${app.jwt.expiration.access}")
    public Integer JWT_ACCESS_EXPIRATION;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Test
    public void generateAccessToken_withValidUser_shouldReturnValidToken() {
        // Arrange
        UserSecurity mockUser = Mockito.mock(UserSecurity.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getEmail()).thenReturn("test@test.com");
        when(mockUser.getRoles()).thenReturn(new HashSet<>(Collections.singletonList(new RoleSecurity(1, "ROLE_USER"))));

        // Act
        String accessToken = jwtTokenUtil.generateAccessToken(mockUser);

        // Assert
        assertNotNull(accessToken);
        assertTrue(jwtTokenUtil.validateJWTToken(accessToken));
    }

    @Test
    public void generateAccessToken_withValidUser_shouldContainValidClaims() {
        // Arrange
        UserSecurity mockUser = Mockito.mock(UserSecurity.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getEmail()).thenReturn("test@test.com");
        when(mockUser.getRoles()).thenReturn(new HashSet<>(Collections.singletonList(new RoleSecurity(1, "ROLE_USER"))));

        // Act
        String accessToken = jwtTokenUtil.generateAccessToken(mockUser);
        Claims claims = jwtTokenUtil.parseClaims(accessToken);

        // Assert
        assertNotNull(accessToken);
        assertNotNull(claims);
        assertEquals("1,test@test.com", claims.getSubject());
        assertEquals("DbConnectionApp", claims.getIssuer());
        assertNotNull(claims.get("roles"));
    }

    @Test
    public void generateRefreshToken_withValidUser_shouldReturnValidToken() {
        // Arrange
        UserSecurity mockUser = Mockito.mock(UserSecurity.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getEmail()).thenReturn("test@test.com");
        when(mockUser.getRoles()).thenReturn(new HashSet<>(Collections.singletonList(new RoleSecurity(1, "ROLE_USER"))));

        // Act
        String refreshToken = jwtTokenUtil.generateRefreshToken(mockUser);

        // Assert
        assertNotNull(refreshToken);
        assertTrue(jwtTokenUtil.validateJWTToken(refreshToken));
    }

    @Test
    public void generateRefreshToken_withValidUser_shouldContainValidClaims() {
        // Arrange
        UserSecurity mockUser = Mockito.mock(UserSecurity.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getEmail()).thenReturn("test@test.com");
        when(mockUser.getRoles()).thenReturn(new HashSet<>(Collections.singletonList(new RoleSecurity(1, "ROLE_USER"))));

        // Act
        String refreshToken = jwtTokenUtil.generateRefreshToken(mockUser);
        Claims claims = jwtTokenUtil.parseClaims(refreshToken);

        // Assert
        assertNotNull(refreshToken);
        assertNotNull(claims);
        assertEquals("1,test@test.com", claims.getSubject());
        assertEquals("DbConnectionApp", claims.getIssuer());
        assertNotNull(claims.get("roles"));
    }

    @Test
    public void validateJWTToken_withValidToken_shouldReturnTrue() {
        // Arrange
        UserSecurity mockUser = Mockito.mock(UserSecurity.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getEmail()).thenReturn("test@test.com");
        when(mockUser.getRoles()).thenReturn(new HashSet<>(Collections.singletonList(new RoleSecurity(1, "ROLE_USER"))));
        String token = jwtTokenUtil.generateAccessToken(mockUser);

        // Act
        boolean isValid = jwtTokenUtil.validateJWTToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidateJWTToken_ExpiredToken_ReturnsFalse() {
        // Arrange
        String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcwNTE5NDgsImV4cCI6MTczNzA1NTU0OH0.iJR3QwCVF1tucpL6tONwQw6djy9iGHkJzoTIBRPere2klO-lFqjpwmVT5m0Rx9E1FCLAElHywm2II8OX_DC1Pw";

        // Act
        boolean isValid = jwtTokenUtil.validateJWTToken(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void validateJWTToken_withInvalidToken_shouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid_token_structure";

        // Act
        boolean isValid = jwtTokenUtil.validateJWTToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void validateJWTToken_withNullOrEmptyToken_shouldReturnFalse() {
        // Act & Assert
        assertFalse(jwtTokenUtil.validateJWTToken(null));
        assertFalse(jwtTokenUtil.validateJWTToken(""));
        assertFalse(jwtTokenUtil.validateJWTToken("   "));
    }

    @Test
    public void parseClaims_withValidToken_shouldReturnCorrectClaims() {
        // Arrange
        UserSecurity mockUser = Mockito.mock(UserSecurity.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getEmail()).thenReturn("test@test.com");
        when(mockUser.getRoles()).thenReturn(new HashSet<>(Collections.singletonList(new RoleSecurity(1, "ROLE_USER"))));

        String token = jwtTokenUtil.generateAccessToken(mockUser);

        // Act
        Claims claims = jwtTokenUtil.parseClaims(token);

        // Assert
        assertNotNull(claims);
        assertEquals("1,test@test.com", claims.getSubject());
        assertEquals("DbConnectionApp", claims.getIssuer());
        assertNotNull(claims.get("roles"));
    }

    @Test
    public void parseClaims_withInvalidSignature_shouldThrowSignatureException() {
        // Arrange
        String invalidToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdW99999xMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcwNTE5NDgsImV4cCI6MTczNzA1NTU0OH0.iJR3QwCVF1tucpL6tONwQw6djy9iGHkJzoTIBRPere2klO-lFqjpwmVT5m0Rx9E1FCLAElHywm2II8OX_DC1Pw";

        // Act & Assert
        assertThrows(SignatureException.class, () -> jwtTokenUtil.parseClaims(invalidToken));
    }

    @Test
    public void parseClaims_withExpiredToken_shouldThrowExpiredJwtException() {
        // Arrange
        String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcwNTE5NDgsImV4cCI6MTczNzA1NTU0OH0.iJR3QwCVF1tucpL6tONwQw6djy9iGHkJzoTIBRPere2klO-lFqjpwmVT5m0Rx9E1FCLAElHywm2II8OX_DC1Pw";

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> jwtTokenUtil.parseClaims(expiredToken));
    }

    @Test
    public void getTokenExpiration_withValidToken_shouldReturnCorrectExpiration() {
        // Arrange
        UserSecurity mockUser = Mockito.mock(UserSecurity.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getEmail()).thenReturn("test@test.com");
        when(mockUser.getRoles()).thenReturn(new HashSet<>(Collections.singletonList(new RoleSecurity(1, "ROLE_USER"))));

        String token = jwtTokenUtil.generateAccessToken(mockUser);

        // Act
        Date expirationDate = jwtTokenUtil.getTokenExpiration(token);

        // Assert
        assertNotNull(expirationDate);
        long expectedExpiration = System.currentTimeMillis() + JWT_ACCESS_EXPIRATION;
        assertTrue(Math.abs(expirationDate.getTime() - expectedExpiration) < 1000); // Allowable timing difference
    }

    @Test
    public void getTokenExpiration_withInvalidToken_shouldThrowSignatureException() {
        // Arrange
        String invalidToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdW99999xMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcwNTE5NDgsImV4cCI6MTczNzA1NTU0OH0.iJR3QwCVF1tucpL6tONwQw6djy9iGHkJzoTIBRPere2klO-lFqjpwmVT5m0Rx9E1FCLAElHywm2II8OX_DC1Pw";

        // Act & Assert
        assertThrows(SignatureException.class, () -> jwtTokenUtil.getTokenExpiration(invalidToken));
    }

    @Test
    public void getTokenExpiration_withExpiredJwtToken_shouldThrowExpiredJwtException() {
        // Arrange
        String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMSx4YnNtdnV6ZmF5eXpqeGR4YWtAY2twdHIuY29tIiwiaXNzIjoiRGJDb25uZWN0aW9uQXBwIiwicm9sZXMiOlt7ImlkIjoxLCJuYW1lIjoiUk9MRV9VU0VSIn1dLCJpYXQiOjE3MzcwNTE5NDgsImV4cCI6MTczNzA1NTU0OH0.iJR3QwCVF1tucpL6tONwQw6djy9iGHkJzoTIBRPere2klO-lFqjpwmVT5m0Rx9E1FCLAElHywm2II8OX_DC1Pw";

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> jwtTokenUtil.getTokenExpiration(expiredToken));
    }

    @Test
    void getUserId_validToken_returnsUserId() {
        // Arrange
        RoleSecurity role1 = new RoleSecurity(1, "ROLE_USER");
        RoleSecurity role2 = new RoleSecurity(2, "ROLE_ADMIN");
        Set<RoleSecurity> roles = Set.of(role1, role2);
        final Long userId = 1L;
        final String email = "test@example.com";

        UserSecurity user = new UserSecurity();
        user.setId(userId);
        user.setEmail(email);
        user.setRoles(roles);

        // Act
        String validToken = jwtTokenUtil.generateAccessToken(user);
        Long extractedUserId = jwtTokenUtil.getUserId(validToken);

        // Assert
        assertEquals(userId, extractedUserId);
    }

}
