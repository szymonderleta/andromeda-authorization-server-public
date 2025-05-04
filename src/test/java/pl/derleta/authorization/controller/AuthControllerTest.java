package pl.derleta.authorization.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import pl.derleta.authorization.config.model.RoleSecurity;
import pl.derleta.authorization.config.model.UserSecurity;
import pl.derleta.authorization.config.security.api.AuthApiService;
import pl.derleta.authorization.config.security.jwt.JwtTokenUtil;
import pl.derleta.authorization.domain.response.AccessResponse;
import pl.derleta.authorization.domain.types.AccessResponseType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authManager;

    @MockBean
    private JwtTokenUtil jwtUtil;

    @MockBean
    private AuthApiService authApiService;

    @Autowired
    private AuthController authController;

    @BeforeEach
    void setUp() throws Exception {
        Field accessExpirationField = JwtTokenUtil.class.getDeclaredField("JWT_ACCESS_EXPIRATION");
        accessExpirationField.setAccessible(true);
        accessExpirationField.set(jwtUtil, 36000);

        Field refreshExpirationField = JwtTokenUtil.class.getDeclaredField("JWT_REFRESH_EXPIRATION");
        refreshExpirationField.setAccessible(true);
        refreshExpirationField.set(jwtUtil, 360000L);

    }

    @Test
    void login_ValidCredentials_ShouldReturnOkResponseWithCookiesAndJson() throws Exception {
        // Arrange
        String login = "xbsmvuzfayyzjxdxak";
        String password = "examplepass123.";

        Set<RoleSecurity> roles = Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"));
        UserSecurity user = new UserSecurity(
                1L,
                login,
                login + "@ckptr.com",
                "password123",
                roles
        );

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        doReturn(accessToken).when(jwtUtil).generateAccessToken(any(UserSecurity.class));
        doReturn(refreshToken).when(jwtUtil).generateRefreshToken(any(UserSecurity.class));

        when(authApiService.saveAccessToken(anyLong(), eq(accessToken), any()))
                .thenReturn(true);
        when(authApiService.saveRefreshToken(anyLong(), eq(refreshToken), any()))
                .thenReturn(true);

        // Act
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Requesting-App", "nebula_rest_api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\", \"password\":\"" + password + "\"}"))

                // Assert
                .andExpect(status().isOk())
                .andExpect(cookie().value("accessToken", accessToken))
                .andExpect(cookie().value("refreshToken", refreshToken))
                .andExpect(jsonPath("$.username").value(login))
                .andExpect(jsonPath("$.email").value(login + "@ckptr.com"));
    }


    @Test
    void login_InvalidAppHeader_ShouldReturnForbidden() throws Exception {
        // Arrange
        String login = "xbsmvuzfayyzjxdxak";
        String password = "examplepass123.";

        Set<RoleSecurity> roles = Set.of(
                new RoleSecurity(1, "ROLE_ADMIN"));
        UserSecurity user = new UserSecurity(
                1L,
                login,
                login + "@ckptr.com",
                "password123",
                roles
        );

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        doReturn(accessToken).when(jwtUtil).generateAccessToken(any(UserSecurity.class));
        doReturn(refreshToken).when(jwtUtil).generateRefreshToken(any(UserSecurity.class));

        when(authApiService.saveAccessToken(anyLong(), eq(accessToken), any()))
                .thenReturn(true);
        when(authApiService.saveRefreshToken(anyLong(), eq(refreshToken), any()))
                .thenReturn(true);

        // Act
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Requesting-App", "nebula-rest-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\", \"password\":\"" + password + "\"}"))

                // Assert
                .andExpect(status().isForbidden());
    }

    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        String login = "notexistinguser";
        String password = "wrongpassword";

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Requesting-App", "nebula_rest_api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\", \"password\":\"" + password + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid login credentials"));
    }

    @Test
    void login_SaveTokenFails_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        String login = "xbsmvuzfayyzjxdxak";
        String password = "csamplepasswdl.";

        Set<RoleSecurity> roles = Set.of(new RoleSecurity(1, "ROLE_USER"));
        UserSecurity user = new UserSecurity(1L, login, login + "@test.com", "password123", roles);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        doReturn(accessToken).when(jwtUtil).generateAccessToken(any(UserSecurity.class));
        doReturn(refreshToken).when(jwtUtil).generateRefreshToken(any(UserSecurity.class));

        when(authApiService.saveAccessToken(anyLong(), eq(accessToken), any()))
                .thenReturn(false);
        when(authApiService.saveRefreshToken(anyLong(), eq(refreshToken), any()))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Requesting-App", "nebula_rest_api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\", \"password\":\"" + password + "\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Internal server error"));
    }

    @Test
    void login_MissingRequestHeader_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String login = "user";
        String password = "password";

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Requesting-App", "nebula_rest_api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\", \"password\":\"" + password + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_MissingLogin_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Requesting-App", "nebula_rest_api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"password\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_MissingPassword_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Requesting-App", "nebula_rest_api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"user\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_NullPrincipal_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        String login = "xbsmvuzfayyzjxdxak";
        String password = "examplepass123.";

        doThrow(new BadCredentialsException("Invalid login credentials"))
                .when(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Requesting-App", "nebula_rest_api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\", \"password\":\"" + password + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid login credentials"));
    }

    @Test
    void refreshAccess_invalidToken_shouldReturnUnauthorized() {
        // Arrange
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        String token = "invalid-token";
        when(jwtUtil.validateJWTToken(token)).thenReturn(false);
        AccessResponse expectedResponse = new AccessResponse(false, AccessResponseType.ACCESS_NOT_REFRESHED);

        // Act
        ResponseEntity<?> response = authController.refreshAccess(token, mockResponse);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(AccessResponse.class, response.getBody());
        assertEquals(expectedResponse, response.getBody());
        verify(jwtUtil, times(1)).validateJWTToken(token);
        verify(mockResponse, times(0)).addCookie(any());
    }

    @Test
    void refreshAccess_nullToken_shouldReturnUnauthorized() {
        // Arrange
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        when(jwtUtil.validateJWTToken(null)).thenReturn(false);
        AccessResponse expectedResponse = new AccessResponse(false, AccessResponseType.ACCESS_NOT_REFRESHED);

        // Act
        ResponseEntity<?> response = authController.refreshAccess(null, mockResponse);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(AccessResponse.class, response.getBody());
        assertEquals(expectedResponse, response.getBody());
        verify(jwtUtil, times(1)).validateJWTToken(null);
        verify(mockResponse, times(0)).addCookie(any());
    }

    @Test
    void refreshAccess_validToken_shouldRefreshAccessToken() {
        // Arrange
        String token = "valid-token";
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        String accessToken = "new-access-token";
        when(jwtUtil.validateJWTToken(token)).thenReturn(true);
        when(jwtUtil.getUserId(token)).thenReturn(1L);
        when(authApiService.updateAccessToken(1L)).thenReturn(accessToken);
        AccessResponse expectedResponse = new AccessResponse(true, AccessResponseType.ACCESS_REFRESHED);

        // Act
        ResponseEntity<?> response = authController.refreshAccess(token, mockResponse);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(AccessResponse.class, response.getBody());
        assertEquals(expectedResponse, response.getBody());
        verify(jwtUtil, times(1)).validateJWTToken(token);
        verify(mockResponse, times(2)).addCookie(any());
    }

}
