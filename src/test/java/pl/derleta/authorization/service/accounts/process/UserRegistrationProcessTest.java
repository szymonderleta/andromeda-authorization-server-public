package pl.derleta.authorization.service.accounts.process;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.request.Request;
import pl.derleta.authorization.domain.request.UserRegistrationRequest;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.types.AccountResponseType;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.repository.impl.UserRepository;
import pl.derleta.authorization.repository.impl.UserRoleRepository;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserRegistrationProcessTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    EmailService emailService;

    private UserRegistrationProcess process;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Set<RepositoryClass> repositoryList = new HashSet<>();
        repositoryList.add(userRepository);
        repositoryList.add(userRoleRepository);

        process = new UserRegistrationProcess(repositoryList, emailService);
    }

    @Test
    void check_withExistingEmail_shouldReturnEmailIsNotUnique() {
        // arrange
        UserRegistrationRequest request = new UserRegistrationRequest("test@example.com", "testuser", "password");
        when(userRepository.isEmailExist(request.email())).thenReturn(true);

        // act
        AccountResponse response = process.check(request);

        // assert
        assertFalse(response.isSuccess());
        assertEquals(AccountResponseType.EMAIL_IS_NOT_UNIQUE, response.getType());
    }

    @Test
    void check_withExistingUsername_shouldReturnLoginIsNotUnique() {
        // arrange
        UserRegistrationRequest request = new UserRegistrationRequest("test@example.com", "testuser", "password");
        when(userRepository.isEmailExist(request.email())).thenReturn(false);
        when(userRepository.isLoginExist(request.username())).thenReturn(true);

        // act
        AccountResponse response = process.check(request);

        // assert
        assertFalse(response.isSuccess());
        assertEquals(AccountResponseType.LOGIN_IS_NOT_UNIQUE, response.getType());
    }

    @Test
    void check_withUniqueLoginAndEmail_shouldReturnCorrectResult() {
        // arrange
        UserRegistrationRequest request = new UserRegistrationRequest("test@example.com", "testuser", "password");
        when(userRepository.isEmailExist(request.email())).thenReturn(false);
        when(userRepository.isLoginExist(request.username())).thenReturn(false);

        // act
        AccountResponse response = process.check(request);

        // assert
        assertTrue(response.isSuccess());
        assertEquals(AccountResponseType.UNIQUE_LOGIN_AND_EMAIL, response.getType());
    }

    @Test
    void check_withInvalidRequest_shouldReturnBadRequestType() {
        // arrange
        Request invalidRequest = new Request() {
        };

        // act
        AccountResponse response = process.check(invalidRequest);

        // assert
        assertFalse(response.isSuccess());
        assertEquals(AccountResponseType.BAD_REGISTRATION_REQUEST_TYPE, response.getType());
    }

    @Test
    void save_withValidRequest_shouldSaveUserSuccessfully() {
        // arrange
        UserRegistrationRequest validRequest = new UserRegistrationRequest("test@example.com", "tester", "password");

        long expectedUserId = 1L;
        when(userRepository.getNextUserId()).thenReturn(expectedUserId);
        when(userRepository.findById(expectedUserId)).thenReturn(new UserEntity(expectedUserId, "tester", "test@example.com", "password"));

        // act
        UserEntity savedEntity = process.save(validRequest);

        // assert
        assertEquals(expectedUserId, savedEntity.getUserId());
        assertEquals("tester", savedEntity.getUsername());
        assertEquals("test@example.com", savedEntity.getEmail());
        assertEquals("password", savedEntity.getPassword());
    }

    @Test
    void save_withValidRequest_shouldSaveUserRole() {
        // arrange
        UserRegistrationRequest validRequest = new UserRegistrationRequest("test@example.com", "testuser", "password");

        long expectedUserId = 1L;
        long expectedRolePk = 2L;
        when(userRepository.getNextUserId()).thenReturn(expectedUserId);
        when(userRepository.findById(expectedUserId)).thenReturn(new UserEntity(expectedUserId, "testuser", "test@example.com", "password"));
        when(userRoleRepository.getNextId()).thenReturn(expectedRolePk);

        // act
        process.save(validRequest);

        // assert
        when(userRoleRepository.getNextId()).thenReturn(expectedRolePk);
        userRoleRepository.save(expectedRolePk, expectedUserId, 1);
    }

    @Test
    void save_withInvalidRequest_shouldThrowException() {
        // arrange
        Request invalidRequest = new Request() {
        };

        // act & assert
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> process.save(invalidRequest));
        assertEquals("Bad instance of Request parameter", exception.getMessage());
    }

}
