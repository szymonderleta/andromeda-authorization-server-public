package pl.derleta.authorization.service.accounts.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.entity.UserEntityDecrypted;
import pl.derleta.authorization.domain.request.Request;
import pl.derleta.authorization.domain.request.ResetPasswordRequest;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.types.AccountResponseType;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.repository.impl.UserRepository;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class ResetPasswordProcessTest {

    @Mock
    UserRepository userRepository;

    @Mock
    EmailService emailService;

    private ResetPasswordProcess process;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Set<RepositoryClass> repositoryList = new HashSet<>();
        repositoryList.add(userRepository);
        repositoryList.add(userRepository);

        process = new ResetPasswordProcess(repositoryList, emailService);
    }

    @Test
    void sendMail_withValidUserEntity_shouldSendEmailSuccessfully() {
        // arrange
        long userId = 1L;
        String email = "test@example.com";
        String decryptedPassword = "plaintextPassword";

        UserEntity userEntity = new UserEntity(userId, "TestUser", email, "hashedPassword");
        UserEntityDecrypted userEntityDecrypted = new UserEntityDecrypted(userEntity, decryptedPassword);

        when(userRepository.findById(eq(userId))).thenReturn(userEntity);
        doNothing().when(emailService).sendEmail(eq(email), anyString(), anyString());

        // act
        AccountResponse response = process.sendMail(userEntityDecrypted);

        // assert
        assertTrue(response.isSuccess());
        assertEquals(AccountResponseType.MAIL_NEW_PASSWD_SENT, response.getType());

        verify(userRepository, times(1)).findById(eq(userId));
        verify(emailService, times(1)).sendEmail(eq(email), anyString(), anyString());
    }

    @Test
    void sendMail_withNonExistentUserEntity_shouldNotSendEmail() {
        // arrange
        long userId = 1L;
        String decryptedPassword = "plaintextPassword";

        UserEntityDecrypted userEntityDecrypted = new UserEntityDecrypted(
                new UserEntity(userId, "TestUser", "test@example.com", "hashedPassword"),
                decryptedPassword);

        when(userRepository.findById(eq(userId))).thenReturn(null);

        // act & assert
        try {
            process.sendMail(userEntityDecrypted);
        } catch (NullPointerException ignored) {
            // Ignored exception
        }

        verify(userRepository, times(1)).findById(eq(userId));
        verify(emailService, times(0)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendMail_withEmailServiceFailure_shouldThrowException() {
        // arrange
        long userId = 1L;
        String email = "test@example.com";
        String decryptedPassword = "plaintextPassword";

        UserEntity userEntity = new UserEntity(userId, "TestUser", email, "hashedPassword");
        UserEntityDecrypted userEntityDecrypted = new UserEntityDecrypted(userEntity, decryptedPassword);

        when(userRepository.findById(eq(userId))).thenReturn(userEntity);
        doThrow(new RuntimeException("Email service failure")).when(emailService).sendEmail(eq(email), anyString(), anyString());

        // act & assert
        try {
            process.sendMail(userEntityDecrypted);
        } catch (RuntimeException exception) {
            assertEquals("Email service failure", exception.getMessage());
        }

        verify(userRepository, times(1)).findById(eq(userId));
        verify(emailService, times(1)).sendEmail(eq(email), anyString(), anyString());
    }

    @Test
    void check_withNonExistentAccount_shouldReturn_ACCOUNT_NOT_EXIST_RESET_PASSWD() {
        // arrange
        String email = "test@example.com";
        ResetPasswordRequest request = new ResetPasswordRequest(email);

        when(userRepository.findByEmail(eq(email))).thenReturn(null);

        // act
        AccountResponse response = process.check(request);

        // assert
        assertFalse(response.isSuccess());
        assertEquals(AccountResponseType.ACCOUNT_NOT_EXIST_RESET_PASSWD, response.getType());

        verify(userRepository, times(1)).findByEmail(eq(email));
    }

    @Test
    void check_withBlockedAccount_shouldReturn_ACCOUNT_IS_BLOCKED_RESET_PASSWD() {
        // arrange
        String email = "blocked@example.com";
        long userId = 2L;

        UserEntity userEntity = new UserEntity(userId, "BlockedUser", email, "hashedPassword");
        ResetPasswordRequest request = new ResetPasswordRequest(email);

        when(userRepository.findByEmail(eq(email))).thenReturn(userEntity);
        when(userRepository.isBlocked(userId)).thenReturn(true);

        // act
        AccountResponse response = process.check(request);

        // assert
        assertFalse(response.isSuccess());
        assertEquals(AccountResponseType.ACCOUNT_IS_BLOCKED_RESET_PASSWD, response.getType());

        verify(userRepository, times(1)).findByEmail(eq(email));
        verify(userRepository, times(1)).isBlocked(userId);
    }

    @Test
    void check_withUnverifiedAccount_shouldReturn_ACCOUNT_IS_NOT_VERIFIED() {
        // arrange
        String email = "notverified@example.com";
        long userId = 3L;

        UserEntity userEntity = new UserEntity(userId, "NotVerifiedUser", email, "hashedPassword");
        ResetPasswordRequest request = new ResetPasswordRequest(email);

        when(userRepository.findByEmail(eq(email))).thenReturn(userEntity);
        when(userRepository.isBlocked(userId)).thenReturn(false);
        when(userRepository.isVerified(userId)).thenReturn(false);

        // act
        AccountResponse response = process.check(request);

        // assert
        assertFalse(response.isSuccess());
        assertEquals(AccountResponseType.ACCOUNT_IS_NOT_VERIFIED, response.getType());

        verify(userRepository, times(1)).findByEmail(eq(email));
        verify(userRepository, times(1)).isBlocked(userId);
        verify(userRepository, times(1)).isVerified(userId);
    }

    @Test
    void check_withValidAccount_shouldReturn_PASSWORD_CAN_BE_GENERATED() {
        // arrange
        String email = "test@example.com";
        long userId = 4L;

        UserEntity userEntity = new UserEntity(userId, "ValidUser", email, "hashedPassword");
        ResetPasswordRequest request = new ResetPasswordRequest(email);

        when(userRepository.findByEmail(eq(email))).thenReturn(userEntity);
        when(userRepository.isBlocked(userId)).thenReturn(false);
        when(userRepository.isVerified(userId)).thenReturn(true);

        // act
        AccountResponse response = process.check(request);

        // assert
        assertTrue(response.isSuccess());
        assertEquals(AccountResponseType.PASSWORD_CAN_BE_GENERATED, response.getType());

        verify(userRepository, times(1)).findByEmail(eq(email));
        verify(userRepository, times(1)).isBlocked(userId);
        verify(userRepository, times(1)).isVerified(userId);
    }

    @Test
    void check_withInvalidRequestType_shouldReturn_BAD_RESET_PASSWD_REQUEST_TYPE() {
        // arrange
        Request invalidRequest = mock(Request.class);

        // act
        AccountResponse response = process.check(invalidRequest);

        // assert
        assertFalse(response.isSuccess());
        assertEquals(AccountResponseType.BAD_RESET_PASSWD_REQUEST_TYPE, response.getType());
    }

    @Test
    void save_withValidRequest_shouldReturnUserEntityDecrypted() {
        // arrange
        final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+";
        String email = "valid@example.com";
        long userId = 5L;

        ResetPasswordRequest request = new ResetPasswordRequest(email);
        UserEntity userEntity = new UserEntity(userId, "ValidUser", email, "oldHashedPassword");

        when(userRepository.findByEmail(eq(email))).thenReturn(userEntity);
        when(userRepository.findById(eq(userId))).thenReturn(userEntity);
        doAnswer(invocation -> null).when(userRepository).updatePassword(eq(userId), anyString());

        // act
        UserEntityDecrypted result = (UserEntityDecrypted) process.save(request);

        // assert
        assertNotNull(result.getDecryptedPassword(), "Password should not be null");
        assertFalse(result.getDecryptedPassword().isEmpty(), "Password should not be empty");
        assertEquals(12, result.getDecryptedPassword().length(), "Password length does not match the expected length");
        assertTrue(
                result.getDecryptedPassword().chars().anyMatch(Character::isUpperCase),
                "Password must contain at least one uppercase letter"
        );
        assertTrue(
                result.getDecryptedPassword().chars().anyMatch(Character::isLowerCase),
                "Password must contain at least one lowercase letter"
        );
        assertTrue(
                result.getDecryptedPassword().chars().anyMatch(Character::isDigit),
                "Password must contain at least one digit"
        );
        assertTrue(
                result.getDecryptedPassword().chars()
                        .anyMatch(ch -> SPECIAL_CHARACTERS.indexOf(ch) >= 0),
                "Password must contain at least one special character from the set: " + SPECIAL_CHARACTERS
        );

        verify(userRepository, times(1)).findByEmail(eq(email));
        verify(userRepository, times(1)).updatePassword(eq(userId), anyString());
        verify(userRepository, times(1)).findById(eq(userId));
    }

    @Test
    void save_withInvalidRequest_shouldReturnNull() {
        // arrange
        Request invalidRequest = mock(Request.class);

        // act
        UserEntity result = process.save(invalidRequest);

        // assert
        assertNull(result, "Result should be null for an invalid request");
    }

    @Test
    void save_withRepositoryException_shouldThrowRuntimeException() {
        // arrange
        String email = "exception@example.com";
        ResetPasswordRequest request = new ResetPasswordRequest(email);

        when(userRepository.findByEmail(eq(email))).thenThrow(new RuntimeException("Repository failure"));

        // act & assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> process.save(request),
                "Expected process.save(request) to throw a RuntimeException"
        );

        assertEquals("Repository failure", exception.getMessage(), "Exception message should match expected message");

        verify(userRepository, times(1)).findByEmail(eq(email));
    }

}
