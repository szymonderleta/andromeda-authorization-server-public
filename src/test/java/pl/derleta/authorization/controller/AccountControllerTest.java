package pl.derleta.authorization.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.derleta.authorization.config.model.RoleSecurity;
import pl.derleta.authorization.config.model.UserSecurity;
import pl.derleta.authorization.config.security.jwt.JwtTokenUtil;
import pl.derleta.authorization.controller.assembler.UserRolesModelAssembler;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.model.UserRoles;
import pl.derleta.authorization.domain.request.*;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.response.UserRolesResponse;
import pl.derleta.authorization.domain.types.AccountResponseType;
import pl.derleta.authorization.service.accounts.impl.AccountsServiceImpl;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    private final String requestingAppHeader = "nebula_rest_api";

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private UserRolesModelAssembler userRolesModelAssembler;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountsServiceImpl service;

    private UserSecurity createUserWithRoles(Set<RoleSecurity> roles) {
        return new UserSecurity(
                1L,
                "Admin User",
                "admin@example.com",
                "password123",
                roles
        );
    }

    private String generateTokenForUser(UserSecurity user) {
        return jwtTokenUtil.generateAccessToken(user);
    }


    @Test
    void register_withValidData_shouldReturnSuccessfully() throws Exception {
        // Arrange
        AccountResponse response = new AccountResponse(true, AccountResponseType.VERIFICATION_MAIL_FROM_REGISTRATION);

        when(service.register(any(UserRegistrationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/public/account/register")
                        .header("X-Requesting-App", requestingAppHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "testuser",
                                    "password": "password123",
                                    "email": "testuser@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(content().json("""
                        {
                            "success": true,
                            "type": "VERIFICATION_MAIL_FROM_REGISTRATION"
                        }
                        """));
    }

    @Test
    void register_withInvalidAppHeader_shouldReturnForbidden() throws Exception {
        // Arrange
        AccountResponse response = new AccountResponse(true, AccountResponseType.VERIFICATION_MAIL_FROM_REGISTRATION);

        when(service.register(any(UserRegistrationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/public/account/register")
                        .header("X-Requesting-App", "nebula-rest-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "testuser",
                                    "password": "password123",
                                    "email": "testuser@example.com"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void register_failToRegisterUser_shouldReturnBadRequest() throws Exception {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest("testuser", "password123", "testuser@example.com");
        AccountResponse response = new AccountResponse(false, AccountResponseType.BAD_REGISTRATION_PROCESS_INSTANCE);

        when(service.register(any(UserRegistrationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/public/account/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Requesting-App", requestingAppHeader)
                        .content("""
                                {
                                    "username": "testuser",
                                    "password": "password123",
                                    "email": "testuser@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(content().json("""
                        {
                            "success": false,
                            "type": "BAD_REGISTRATION_PROCESS_INSTANCE"
                        }
                        """));
    }

    @Test
    void get_withValidInputs_shouldReturnUserRolesResponse() throws Exception {
        // Arrange
        long userId = 1L;
        String username = "testuser";
        String password = "password";
        String email = "testuser@example.com";

        User user = new User(userId, username, password, email);
        Role role = new Role(1, "ROLE_USER");
        Set<Role> roles = new java.util.HashSet<>();
        roles.add(role);
        UserRoles userRoles = new UserRoles(user, roles);

        UserRolesResponse response = new UserRolesResponse();

        when(service.get(username, email)).thenReturn(userRoles);
        when(userRolesModelAssembler.toModel(userRoles)).thenReturn(response);


        // Act & Assert
        mockMvc.perform(get("/api/v1/public/account")
                        .header("X-Requesting-App", requestingAppHeader)
                        .param("username", username)
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"));
    }

    @Test
    void get_withInvalidInputs_shouldReturnInternalServerError() throws Exception {
        // Arrange
        String username = "invaliduser";
        String email = "invalid@example.com";

        when(service.get(username, email)).thenThrow(new RuntimeException("Service failure"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/public/account")
                        .header("X-Requesting-App", requestingAppHeader)
                        .param("username", username)
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void get_withMissingParameters_shouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/public/account")
                        .header("X-Requesting-App", requestingAppHeader)
                        .param("username", "testuser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirm_withValidData_shouldReturnSuccessfully() throws Exception {
        // Arrange
        AccountResponse response = new AccountResponse(true, AccountResponseType.ACCOUNT_CONFIRMED);

        when(service.confirm(any(UserConfirmationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/public/account/confirm")
                        .header("X-Requesting-App", requestingAppHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tokenId": 123,
                                    "token": "validToken"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(content().json("""
                        {
                            "success": true,
                            "type": "ACCOUNT_CONFIRMED"
                        }
                        """));
    }

    @Test
    void confirm_withInvalidData_shouldReturnBadRequestError() throws Exception {
        // Arrange
        AccountResponse response = new AccountResponse(false, AccountResponseType.BAD_CONFIRMATION_REQUEST_TYPE);

        when(service.confirm(any(UserConfirmationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/public/account/confirm")
                        .header("X-Requesting-App", requestingAppHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tokenId": 123,
                                    "token": "invalidToken"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(content().json("""
                        {
                            "success": false,
                            "type": "BAD_CONFIRMATION_REQUEST_TYPE"
                        }
                        """));
    }

    @Test
    void unlock_withValidId_shouldReturnSuccessfully() throws Exception {
        // Arrange
        AccountResponse response = new AccountResponse(true, AccountResponseType.VERIFICATION_MAIL_FROM_UNLOCK);

        when(service.unlock(any(UserUnlockRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/public/account/unlock/{id}", 123)
                        .header("X-Requesting-App", requestingAppHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(content().json("""
                        {
                            "success": true,
                            "type": "VERIFICATION_MAIL_FROM_UNLOCK"
                        }
                        """));
    }

    @Test
    void unlock_withInvalidId_shouldReturnBadRequest() throws Exception {
        // Arrange
        AccountResponse response = new AccountResponse(false, AccountResponseType.BAD_UNLOCK_REQUEST_TYPE);

        when(service.unlock(any(UserUnlockRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/public/account/unlock/{id}", -1)
                        .header("X-Requesting-App", requestingAppHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(content().json("""
                        {
                            "success": false,
                            "type": "BAD_UNLOCK_REQUEST_TYPE"
                        }
                        """));
    }

    @Test
    void unlock_withNonExistingId_shouldReturnBadRequest() throws Exception {
        // Arrange
        AccountResponse response = new AccountResponse(false, AccountResponseType.BAD_UNLOCK_REQUEST_TYPE);

        when(service.unlock(any(UserUnlockRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/public/account/unlock/{id}", 9999)
                        .header("X-Requesting-App", requestingAppHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(content().json("""
                        {
                            "success": false,
                            "type": "BAD_UNLOCK_REQUEST_TYPE"
                        }
                        """));
    }

    @Test
    void resetPassword_validEmail_shouldReturnSuccessfully() throws Exception {
        // Arrange
        String email = "valid@example.com";
        AccountResponse response = new AccountResponse(true, AccountResponseType.MAIL_NEW_PASSWD_SENT);

        when(service.resetPassword(any(ResetPasswordRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/public/account/reset-password/{email}", email)
                        .header("X-Requesting-App", requestingAppHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(content().json("""
                        {
                            "success": true,
                            "type": "MAIL_NEW_PASSWD_SENT"
                        }
                        """));
    }

    @Test
    void resetPassword_invalidEmail_shouldReturnBadRequest() throws Exception {
        // Arrange
        String email = "invalid@example";
        AccountResponse response = new AccountResponse(false, AccountResponseType.BAD_RESET_PASSWD_REQUEST_TYPE);

        when(service.resetPassword(any(ResetPasswordRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/public/account/reset-password/{email}", email)
                        .header("X-Requesting-App", requestingAppHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(content().json("""
                        {
                            "success": false,
                            "type": "BAD_RESET_PASSWD_REQUEST_TYPE"
                        }
                        """));
    }

    @Test
    void updatePassword_withValidDataWithAdminRole_shouldReturnSuccessfully() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_ADMIN")
        ));

        String token = generateTokenForUser(user);

        AccountResponse response = new AccountResponse(true, AccountResponseType.PASSWORD_CHANGED);

        when(service.updatePassword(any(ChangePasswordRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/account/change-password")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Requesting-App", requestingAppHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 123,
                                    "email": "user@example.com",
                                    "actualPassword": "oldPassword123",
                                    "newPassword": "newPassword123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(content().json("""
                        {
                            "success": true,
                            "type": "PASSWORD_CHANGED"
                        }
                        """));
    }

    @Test
    void updatePassword_withValidDataWithUserRole_shouldReturnSuccessfully() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        AccountResponse response = new AccountResponse(true, AccountResponseType.PASSWORD_CHANGED);

        when(service.updatePassword(any(ChangePasswordRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/account/change-password")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Requesting-App", requestingAppHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 123,
                                    "email": "user@example.com",
                                    "actualPassword": "oldPassword123",
                                    "newPassword": "newPassword123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(content().json("""
                        {
                            "success": true,
                            "type": "PASSWORD_CHANGED"
                        }
                        """));
    }

    @Test
    void updatePassword_withValidDataWithNonExistRole_shouldReturnForbidden() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_NON_EXIST_USER")
        ));

        String token = generateTokenForUser(user);

        AccountResponse response = new AccountResponse(true, AccountResponseType.PASSWORD_CHANGED);

        when(service.updatePassword(any(ChangePasswordRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/account/change-password")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Requesting-App", requestingAppHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 123,
                                    "email": "user@example.com",
                                    "actualPassword": "oldPassword123",
                                    "newPassword": "newPassword123"
                                }
                                """))
                .andExpect(status().isForbidden());
    }


    @Test
    void updatePassword_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Arrange
        UserSecurity user = createUserWithRoles(Set.of(
                new RoleSecurity(1, "ROLE_USER")
        ));

        String token = generateTokenForUser(user);

        AccountResponse response = new AccountResponse(false, AccountResponseType.BAD_CHANGE_PASSWD_REQUEST_TYPE);

        when(service.updatePassword(any(ChangePasswordRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/account/change-password")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Requesting-App", requestingAppHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 123,
                                    "email": "user@example.com",
                                    "actualPassword": "wrongPassword",
                                    "newPassword": "newPassword123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(content().json("""
                        {
                            "success": false,
                            "type": "BAD_CHANGE_PASSWD_REQUEST_TYPE"
                        }
                        """));
    }

}
