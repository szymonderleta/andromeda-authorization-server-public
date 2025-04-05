package pl.derleta.authorization.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import pl.derleta.authorization.config.model.UserSecurity;
import pl.derleta.authorization.config.repository.TokensGeneratorRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Test
    void authenticate_withValidUser_shouldSucceed() {
        // Arrange
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken("xbsmvuzfayyzjxdxak@ckptr.com", "cStbkrsp}B3VxD.");

        // Act
        Authentication authResult = authenticationManager.authenticate(authRequest);

        // Assert
        assertNotNull(authResult);
        assertTrue(authResult.isAuthenticated());
    }

    @Test
    void authenticate_withInvalidUser_shouldThrowException() {
        // Arrange
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken("invalidUser@example.com", "wrongPassword");

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authenticationManager.authenticate(authRequest));
    }

    @Test
    void getUserByEmail_withExistingEmail_shouldReturnUser() {
        // Arrange
        String email = "test@example.com";

        UserSecurity user = new UserSecurity();
        user.setEmail(email);

        TokensGeneratorRepository mockedRepo = mock(TokensGeneratorRepository.class);
        when(mockedRepo.findByEmail(email)).thenReturn(Optional.of(user));

        SecurityConfig config = new SecurityConfig();
        config.setUserRepo(mockedRepo);

        UserDetailsService userDetailsService = config.userDetailsService();

        // Act & Assert
        assertEquals(user, userDetailsService.loadUserByUsername(email));

        // Assert
        verify(mockedRepo, times(1)).findByEmail(email);
        verify(mockedRepo, never()).findByLogin(anyString());
    }

    @Test
    void getUserByLogin_withExistingLogin_shouldReturnUser() {
        // Arrange
        String login = "testUser";

        UserSecurity user = new UserSecurity();
        user.setName(login);

        TokensGeneratorRepository mockedRepo = mock(TokensGeneratorRepository.class);
        when(mockedRepo.findByEmail(login)).thenReturn(Optional.empty());
        when(mockedRepo.findByLogin(login)).thenReturn(Optional.of(user));

        SecurityConfig config = new SecurityConfig();
        config.setUserRepo(mockedRepo);

        UserDetailsService userDetailsService = config.userDetailsService();

        // Act & Assert
        assertEquals(user, userDetailsService.loadUserByUsername(login));

        // Assert
        verify(mockedRepo, times(1)).findByEmail(login);
        verify(mockedRepo, times(1)).findByLogin(login);
    }

    @Test
    void getUserByUsername_withNonexistentUser_shouldThrowException() {
        // Arrange
        String username = "nonexistent";

        TokensGeneratorRepository mockedRepo = mock(TokensGeneratorRepository.class);
        when(mockedRepo.findByEmail(username)).thenReturn(Optional.empty());
        when(mockedRepo.findByLogin(username)).thenReturn(Optional.empty());

        SecurityConfig config = new SecurityConfig();
        config.setUserRepo(mockedRepo);

        UserDetailsService userDetailsService = config.userDetailsService();

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(username));

        // Assert
        verify(mockedRepo, times(1)).findByEmail(username);
        verify(mockedRepo, times(1)).findByLogin(username);
    }

}
