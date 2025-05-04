package pl.derleta.authorization.service.accounts.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.controller.mapper.UserRolesApiMapper;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.entity.UserEntityDecrypted;
import pl.derleta.authorization.domain.entity.UserRoleEntity;
import pl.derleta.authorization.domain.entity.token.ConfirmationTokenEntity;
import pl.derleta.authorization.domain.model.UserRoles;
import pl.derleta.authorization.domain.request.*;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.types.AccountProcessType;
import pl.derleta.authorization.domain.types.AccountResponseType;
import pl.derleta.authorization.repository.impl.UserRepository;
import pl.derleta.authorization.repository.impl.UserRoleRepository;
import pl.derleta.authorization.repository.impl.UserRolesRepository;
import pl.derleta.authorization.repository.impl.token.ConfirmationTokenRepository;
import pl.derleta.authorization.service.accounts.AccountProcess;
import pl.derleta.authorization.service.accounts.AccountProcessFactory;
import pl.derleta.authorization.service.accounts.process.ChangePasswordProcess;
import pl.derleta.authorization.service.accounts.process.ResetPasswordProcess;
import pl.derleta.authorization.service.accounts.process.UnlockAccountProcess;
import pl.derleta.authorization.service.accounts.process.UserRegistrationProcess;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class AccountsServiceImplTest {

    @Mock
    private AccountProcessFactory accountProcessFactory;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserRolesRepository userRolesRepository;

    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;

    @Mock
    private EmailService emailService;

    private AccountsServiceImpl accountsService;

    @BeforeEach
    void setUp() {
        accountsService = new AccountsServiceImpl(accountProcessFactory);
        accountsService.setUserRepository(userRepository);
        accountsService.setUserRoleRepository(userRoleRepository);
        accountsService.setUserRolesRepository(userRolesRepository);
        accountsService.setConfirmationTokenRepository(confirmationTokenRepository);
        accountsService.setEmailService(emailService);
    }

    @Test
    void registerUser_withValidRequest_shouldSucceed() {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest("username", "password", "email@test.com");
        AccountResponse accountResponse = new AccountResponse(true, AccountResponseType.VERIFICATION_MAIL_FROM_REGISTRATION);
        UserEntity userEntity = new UserEntity(1L, "username", "email@test.com", "password");
        ConfirmationTokenEntity token = new ConfirmationTokenEntity();
        UserRegistrationProcess userRegistrationProcess = mock(UserRegistrationProcess.class);

        when(accountProcessFactory.create(eq(AccountProcessType.USER_REGISTRATION), anySet(), eq(emailService)))
                .thenReturn(userRegistrationProcess);
        when(userRegistrationProcess.check(request)).thenReturn(accountResponse);
        when(userRegistrationProcess.save(request)).thenReturn(userEntity);
        when(userRegistrationProcess.getToken(userEntity)).thenReturn(token);
        when(userRegistrationProcess.sendEmail(userEntity, token))
                .thenReturn(new AccountResponse(true, AccountResponseType.VERIFICATION_MAIL_FROM_REGISTRATION));

        // Act
        AccountResponse result = accountsService.register(request);

        // Assert
        assertTrue(result.isSuccess(), "User registration should succeed with a valid request");
        verify(userRegistrationProcess, times(1)).check(request);
        verify(userRegistrationProcess, times(1)).save(request);
        verify(userRegistrationProcess, times(1)).getToken(userEntity);
        verify(userRegistrationProcess, times(1)).sendEmail(userEntity, token);
    }

    @Test
    void registerUser_withInvalidCheck_shouldFail() {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest("username", "password", "email@test.com");
        AccountResponse accountResponse = new AccountResponse(false, AccountResponseType.BAD_REGISTRATION_PROCESS_INSTANCE);
        UserRegistrationProcess userRegistrationProcess = mock(UserRegistrationProcess.class);

        when(accountProcessFactory.create(eq(AccountProcessType.USER_REGISTRATION), anySet(), eq(emailService)))
                .thenReturn(userRegistrationProcess);
        when(userRegistrationProcess.check(request)).thenReturn(accountResponse);

        // Act
        AccountResponse result = accountsService.register(request);

        // Assert
        assertFalse(result.isSuccess(), "User registration should fail when check fails");
        assertEquals(AccountResponseType.BAD_REGISTRATION_PROCESS_INSTANCE, result.getType(),
                "Response type should indicate a bad registration process instance");
        verify(userRegistrationProcess, times(1)).check(request);
        verify(userRegistrationProcess, never()).save(request);
        verify(userRegistrationProcess, never()).getToken(any());
        verify(userRegistrationProcess, never()).sendEmail(any(), any());
    }

    @Test
    void confirmUser_withValidToken_shouldSucceed() {
        // Arrange
        UserConfirmationRequest request = new UserConfirmationRequest(123L, "confirmationToken");
        AccountResponse successResponse = new AccountResponse(true, AccountResponseType.ACCOUNT_CONFIRMED);
        AccountProcess confirmationProcess = mock(AccountProcess.class);

        when(accountProcessFactory.create(eq(AccountProcessType.CONFIRMATION_TOKEN), anySet(), eq(emailService)))
                .thenReturn(confirmationProcess);
        when(confirmationProcess.check(request)).thenReturn(successResponse);
        when(confirmationProcess.update(request)).thenReturn(successResponse);

        // Act
        AccountResponse result = accountsService.confirm(request);

        // Assert
        assertTrue(result.isSuccess(), "The account confirmation process should succeed");
        assertEquals(AccountResponseType.ACCOUNT_CONFIRMED, result.getType(),
                "The account confirmation response type should indicate success");
        verify(confirmationProcess, times(1)).check(request);
        verify(confirmationProcess, times(1)).update(request);
    }

    @Test
    void confirmUser_withInvalidToken_shouldFail() {
        // Arrange
        UserConfirmationRequest request = new UserConfirmationRequest(123L, "invalidToken");
        AccountResponse failedResponse = new AccountResponse(false, AccountResponseType.BAD_CONFIRMATION_REQUEST_TYPE);
        AccountProcess confirmationProcess = mock(AccountProcess.class);

        when(accountProcessFactory.create(eq(AccountProcessType.CONFIRMATION_TOKEN), anySet(), eq(emailService)))
                .thenReturn(confirmationProcess);
        when(confirmationProcess.check(request)).thenReturn(failedResponse);

        // Act
        AccountResponse result = accountsService.confirm(request);

        // Assert
        assertFalse(result.isSuccess(), "The account confirmation process should fail for an invalid token");
        assertEquals(AccountResponseType.BAD_CONFIRMATION_REQUEST_TYPE, result.getType(),
                "The account confirmation response type should indicate an invalid token");
        verify(confirmationProcess, times(1)).check(request);
        verify(confirmationProcess, never()).update(request);
    }

    @Test
    void unlockAccount_withValidRequest_shouldSucceed() {
        // Arrange
        UserUnlockRequest request = new UserUnlockRequest(123L);
        AccountResponse successResponse = new AccountResponse(true, AccountResponseType.VERIFICATION_MAIL_FROM_UNLOCK);
        UserEntity userEntity = new UserEntity(1L, "username", "email@test.com", "password");
        ConfirmationTokenEntity token = new ConfirmationTokenEntity();
        UnlockAccountProcess process = mock(UnlockAccountProcess.class);

        when(accountProcessFactory.create(eq(AccountProcessType.UNLOCK_ACCOUNT), anySet(), eq(emailService)))
                .thenReturn(process);
        when(process.check(request)).thenReturn(successResponse);
        when(process.save(request)).thenReturn(userEntity);
        when(process.getToken(userEntity)).thenReturn(token);
        when(process.sendEmail(userEntity, token)).thenReturn(successResponse);

        // Act
        AccountResponse result = accountsService.unlock(request);

        // Assert
        assertTrue(result.isSuccess(), "Unlock account should succeed");
        assertEquals(AccountResponseType.VERIFICATION_MAIL_FROM_UNLOCK, result.getType(),
                "Response type should indicate verification mail sent for unlock");
        verify(process, times(1)).check(request);
        verify(process, times(1)).save(request);
        verify(process, times(1)).getToken(userEntity);
        verify(process, times(1)).sendEmail(userEntity, token);
    }

    @Test
    void unlockAccount_withInvalidRequest_shouldFail() {
        // Arrange
        UserUnlockRequest request = new UserUnlockRequest(123L);
        AccountResponse failedResponse = new AccountResponse(false, AccountResponseType.BAD_UNLOCK_REQUEST_TYPE);
        UnlockAccountProcess process = mock(UnlockAccountProcess.class);

        when(accountProcessFactory.create(eq(AccountProcessType.UNLOCK_ACCOUNT), anySet(), eq(emailService)))
                .thenReturn(process);
        when(process.check(request)).thenReturn(failedResponse);

        // Act
        AccountResponse result = accountsService.unlock(request);

        // Assert
        assertFalse(result.isSuccess(), "Unlock account should fail for invalid request");
        assertEquals(AccountResponseType.BAD_UNLOCK_REQUEST_TYPE, result.getType(),
                "Response type should indicate a bad unlock account request");
        verify(process, times(1)).check(request);
        verify(process, never()).save(any());
        verify(process, never()).sendEmail(any(), any());
    }

    @Test
    void resetPassword_withValidRequest_shouldSucceed() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com");
        AccountResponse successResponse = new AccountResponse(true, AccountResponseType.ACCOUNT_IS_BLOCKED_RESET_PASSWD);
        UserEntityDecrypted decryptedUser = mock(UserEntityDecrypted.class);
        ResetPasswordProcess resetPasswordProcess = mock(ResetPasswordProcess.class);

        when(accountProcessFactory.create(eq(AccountProcessType.RESET_PASSWORD), anySet(), eq(emailService)))
                .thenReturn(resetPasswordProcess);
        when(resetPasswordProcess.check(request)).thenReturn(successResponse);
        when(resetPasswordProcess.save(request)).thenReturn(decryptedUser);
        when(resetPasswordProcess.sendMail(decryptedUser)).thenReturn(successResponse);

        // Act
        AccountResponse result = accountsService.resetPassword(request);

        // Assert
        assertTrue(result.isSuccess(), "Result should be successful");
        assertEquals(AccountResponseType.ACCOUNT_IS_BLOCKED_RESET_PASSWD, result.getType(),
                "Response type should indicate password reset due to account being blocked");
        verify(resetPasswordProcess, times(1)).check(request);
        verify(resetPasswordProcess, times(1)).save(request);
        verify(resetPasswordProcess, times(1)).sendMail(decryptedUser);
    }

    @Test
    void resetPassword_withInvalidRequest_shouldFail() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com");
        AccountResponse failedResponse = new AccountResponse(false, AccountResponseType.BAD_RESET_PASSWD_REQUEST_TYPE);
        ResetPasswordProcess resetPasswordProcess = mock(ResetPasswordProcess.class);

        when(accountProcessFactory.create(eq(AccountProcessType.RESET_PASSWORD), anySet(), eq(emailService)))
                .thenReturn(resetPasswordProcess);
        when(resetPasswordProcess.check(request)).thenReturn(failedResponse);

        // Act
        AccountResponse result = accountsService.resetPassword(request);

        // Assert
        assertFalse(result.isSuccess(), "Result should not be successful");
        assertEquals(AccountResponseType.BAD_RESET_PASSWD_REQUEST_TYPE, result.getType(),
                "Response type should indicate invalid password reset request");
        verify(resetPasswordProcess, times(1)).check(request);
        verify(resetPasswordProcess, never()).save(any());
        verify(resetPasswordProcess, never()).sendMail(any());
    }

    @Test
    void updatePassword_withValidRequest_shouldSucceedAndSendMail() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest(123L, "email@test.com", "currentPassword", "newPassword");
        AccountResponse successResponse = new AccountResponse(true, AccountResponseType.PASSWORD_CHANGED);
        ChangePasswordProcess changePasswordProcess = mock(ChangePasswordProcess.class);

        when(accountProcessFactory.create(eq(AccountProcessType.CHANGE_PASSWORD), anySet(), eq(emailService)))
                .thenReturn(changePasswordProcess);
        when(changePasswordProcess.check(request)).thenReturn(successResponse);
        when(changePasswordProcess.update(request)).thenReturn(successResponse);
        when(changePasswordProcess.sendMail(request.email())).thenReturn(successResponse);

        // Act
        AccountResponse result = accountsService.updatePassword(request);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(AccountResponseType.PASSWORD_CHANGED, result.getType());
        verify(changePasswordProcess, times(1)).check(request);
        verify(changePasswordProcess, times(1)).update(request);
        verify(changePasswordProcess, times(1)).sendMail(request.email());
    }

    @Test
    void updatePassword_withInvalidRequest_shouldFail() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest(123L, "email@test.com", "currentPassword", "newPassword");
        AccountResponse failedResponse = new AccountResponse(false, AccountResponseType.PASSWORD_NOT_CHANGED);
        ChangePasswordProcess changePasswordProcess = mock(ChangePasswordProcess.class);

        when(accountProcessFactory.create(eq(AccountProcessType.CHANGE_PASSWORD), anySet(), eq(emailService)))
                .thenReturn(changePasswordProcess);
        when(changePasswordProcess.check(request)).thenReturn(failedResponse);

        // Act
        AccountResponse result = accountsService.updatePassword(request);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals(AccountResponseType.PASSWORD_NOT_CHANGED, result.getType());
        verify(changePasswordProcess, times(1)).check(request);
        verify(changePasswordProcess, never()).update(any());
        verify(changePasswordProcess, never()).sendMail(any());
    }

    @Test
    void getUserRoles_withValidUser_shouldReturnCorrectRoles() {
        // arrange
        String username = "username";
        String email = "email@test.com";

        UserEntity user = new UserEntity(1L, "username", "email@test.com", "encryptedPassword");
        RoleEntity roleUser = new RoleEntity(1, "USER");
        RoleEntity roleAdmin = new RoleEntity(2, "ADMIN");

        List<UserRoleEntity> userRoles = List.of(
                new UserRoleEntity(1L, user, roleUser),
                new UserRoleEntity(2L, user, roleAdmin)
        );

        UserRoles expectedUserRoles = UserRolesApiMapper.toUserRoles(userRoles);

        when(userRolesRepository.get(username, email)).thenReturn(userRoles);

        // act
        UserRoles result = accountsService.get(username, email);

        // assert
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.roles().size(), "Result should contain 2 roles");
        assertEquals(expectedUserRoles, result, "Result should match expected roles object");
        verify(userRolesRepository, times(1)).get(username, email);
    }

    @Test
    void getUserRoles_withInvalidUser_shouldReturnNull() {
        // arrange
        String username = "invalidUser";
        String email = "invalidEmail@test.com";

        when(userRolesRepository.get(username, email)).thenReturn(null);

        // act
        UserRoles result = accountsService.get(username, email);

        // assert
        assertNull(result, "Result should be null for invalid user input");
        verify(userRolesRepository, times(1)).get(username, email);
    }

}
