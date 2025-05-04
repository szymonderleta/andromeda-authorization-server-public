package pl.derleta.authorization.service.accounts.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.request.ChangePasswordRequest;
import pl.derleta.authorization.domain.request.Request;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.types.AccountResponseType;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.repository.impl.UserRepository;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ChangePasswordProcessTest {

    @Mock
    PasswordEncoder encoder;

    @Mock
    UserRepository userRepository;

    @Mock
    EmailService emailService;

    private ChangePasswordProcess changePasswordProcess;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Set<RepositoryClass> repositoryList = new HashSet<>();
        repositoryList.add(userRepository);

        changePasswordProcess = new ChangePasswordProcess(repositoryList, emailService, encoder);
    }

    @Test
    void check_withNonexistentEmail_shouldReturnEmailNotExist() {
        // arrange
        String email = "nonexistent@example.com";
        ChangePasswordRequest request = new ChangePasswordRequest(1L, email, "actualPassword", "newPassword");

        when(userRepository.findByEmail(email)).thenReturn(null);

        // act
        AccountResponse response = changePasswordProcess.check(request);

        // assert
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isSuccess(), "Response success flag should be false");
        assertEquals(AccountResponseType.EMAIL_NOT_EXIST_CHANGE_PASSWD, response.getType(),
                "Response type should be EMAIL_NOT_EXIST_CHANGE_PASSWD");
    }

    @Test
    void check_withBlockedAccount_shouldReturnAccountBlocked() {
        // arrange
        String email = "blocked@example.com";
        UserEntity entity = new UserEntity();
        entity.setUserId(1L);
        entity.setEmail(email);

        ChangePasswordRequest request = new ChangePasswordRequest(1L, email, "actualPassword", "newPassword");

        when(userRepository.findByEmail(email)).thenReturn(entity);
        when(userRepository.isBlocked(entity.getUserId())).thenReturn(true);

        // act
        AccountResponse response = changePasswordProcess.check(request);

        // assert
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isSuccess(), "Response success flag should be false");
        assertEquals(AccountResponseType.ACCOUNT_IS_BLOCKED_CHANGE_PASSWD, response.getType(),
                "Response type should be ACCOUNT_IS_BLOCKED_CHANGE_PASSWD");
    }

    @Test
    void check_withMismatchedPasswords_shouldReturnBadActualPassword() {
        // arrange
        String email = "user@example.com";
        UserEntity entity = new UserEntity();
        entity.setUserId(1L);
        entity.setEmail(email);
        entity.setPassword("encodedPassword");

        ChangePasswordRequest request = new ChangePasswordRequest(1L, email, "wrongPassword", "newPassword");

        when(userRepository.findByEmail(email)).thenReturn(entity);
        when(userRepository.isBlocked(entity.getUserId())).thenReturn(false);
        when(encoder.matches(request.actualPassword(), entity.getPassword())).thenReturn(false);

        // act
        AccountResponse response = changePasswordProcess.check(request);

        // assert
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isSuccess(), "Response success flag should be false");
        assertEquals(AccountResponseType.BAD_ACTUAL_PASSWORD_CHANGE_PASSWD, response.getType(),
                "Response type should be BAD_ACTUAL_PASSWORD_CHANGE_PASSWD");
    }

    @Test
    void check_withValidConditions_shouldReturnPasswordCanBeChanged() {
        // arrange
        String email = "user@example.com";
        UserEntity entity = new UserEntity();
        entity.setUserId(1L);
        entity.setEmail(email);
        entity.setPassword("encodedPassword");

        ChangePasswordRequest request = new ChangePasswordRequest(1L, email, "actualPassword", "newPassword");
        when(userRepository.findByEmail(email)).thenReturn(entity);
        when(userRepository.isBlocked(entity.getUserId())).thenReturn(false);
        when(encoder.matches(request.actualPassword(), entity.getPassword())).thenReturn(true);

        // act
        AccountResponse response = changePasswordProcess.check(request);

        // assert
        assertNotNull(response, "Response should not be null");
        assertTrue(response.isSuccess(), "Response success flag should be true");
        assertEquals(AccountResponseType.PASSWORD_CAN_BE_CHANGED, response.getType(),
                "Response type should be PASSWORD_CAN_BE_CHANGED");
    }

    @Test
    void check_withInvalidRequestType_shouldReturnBadResetPasswordRequestType() {
        // arrange
        Request invalidRequest = mock(Request.class);

        // act
        AccountResponse response = changePasswordProcess.check(invalidRequest);

        // assert
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isSuccess(), "Response success flag should be false");
        assertEquals(AccountResponseType.BAD_RESET_PASSWD_REQUEST_TYPE, response.getType(),
                "Response type should be BAD_RESET_PASSWD_REQUEST_TYPE");
    }

    @Test
    void update_withValidRequest_shouldUpdatePassword() {
        // arrange
        String email = "user@example.com";
        String newPassword = "newPassword123";
        String hashedPassword = "hashedPassword123";

        ChangePasswordRequest request = new ChangePasswordRequest(
                1L, email,
                "existingPassword",
                newPassword
        );

        UserEntity entity = new UserEntity();
        entity.setUserId(1L);
        entity.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(entity);
        when(encoder.encode(newPassword)).thenReturn(hashedPassword);

        // act
        AccountResponse response = changePasswordProcess.update(request);

        // assert
        assertNotNull(response, "Response should not be null");
        assertTrue(response.isSuccess(), "Response success flag should be true");
        assertEquals(AccountResponseType.PASSWORD_CHANGED, response.getType(),
                "Response type should be PASSWORD_CHANGED");

        verify(userRepository).findByEmail(email);
        verify(encoder).encode(newPassword);
        verify(userRepository).updatePassword(1L, hashedPassword);
        verifyNoMoreInteractions(userRepository, encoder);
    }

    @Test
    void update_withInvalidRequest_shouldReturnPasswordNotChanged() {
        // arrange
        Request invalidRequest = mock(Request.class);

        // act
        AccountResponse response = changePasswordProcess.update(invalidRequest);

        // assert
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isSuccess(), "Response success flag should be false");
        assertEquals(AccountResponseType.PASSWORD_NOT_CHANGED, response.getType(),
                "Response type should be PASSWORD_NOT_CHANGED");
    }

    @Test
    void sendMail_withValidRequest_shouldSendEmail() {
        // arrange
        String emailAddress = "n60962851@gmail.com";
        String expectedText = """
                Hello,\s
                this is information mail only,
                password was changed if it wasn't you, please restore your password in nebula immediately.""";
        String expectedSubject = "New password message";

        // act
        AccountResponse response = changePasswordProcess.sendMail(emailAddress);

        // assert
        verify(emailService).sendEmail(emailAddress, expectedSubject, expectedText); // Weryfikacja wysłania e-maila

        assertNotNull(response, "Response should not be null");
        assertTrue(response.isSuccess(), "Response success flag should be true");
        assertEquals(AccountResponseType.MAIL_NEW_PASSWD_SENT, response.getType(),
                "Response type should be MAIL_NEW_PASSWD_SENT");
    }

}
