package pl.derleta.authorization.service.accounts.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.request.Request;
import pl.derleta.authorization.domain.request.UserUnlockRequest;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.types.AccountResponseType;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.repository.impl.UserRepository;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnlockAccountProcessTest {

    @Mock
    UserRepository userRepository;

    @Mock
    EmailService emailService;

    private UnlockAccountProcess process;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Set<RepositoryClass> repositoryList = new HashSet<>();
        repositoryList.add(userRepository);

        process = new UnlockAccountProcess(repositoryList, emailService);
    }

    @Test
    void check_withNonExistentUser_shouldReturnAccountNotExist() {
        // arrange
        long userId = 1L;
        UserUnlockRequest request = new UserUnlockRequest(userId);
        when(userRepository.findById(userId)).thenReturn(null);

        // act
        AccountResponse response = process.check(request);

        // assert
        assertFalse(response.isSuccess());
        assertEquals(AccountResponseType.ACCOUNT_NOT_EXIST_UNLOCK_ACCOUNT, response.getType());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void check_withNonMatchingUserId_shouldReturnAccountNotExist() {
        // arrange
        long userId = 1L;
        UserUnlockRequest request = new UserUnlockRequest(userId);
        UserEntity entity = new UserEntity();
        entity.setUserId(2L);
        when(userRepository.findById(userId)).thenReturn(entity);

        // act
        AccountResponse response = process.check(request);

        // assert
        assertFalse(response.isSuccess());
        assertEquals(AccountResponseType.ACCOUNT_NOT_EXIST_UNLOCK_ACCOUNT, response.getType());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void check_withVerifiedAndNonBlockedAccount_shouldReturnAccountVerifiedAndNotBlocked() {
        // arrange
        long userId = 1L;
        UserUnlockRequest request = new UserUnlockRequest(userId);
        UserEntity entity = new UserEntity();
        entity.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(entity);
        when(userRepository.isVerified(userId)).thenReturn(true);
        when(userRepository.isBlocked(userId)).thenReturn(false);

        // act
        AccountResponse response = process.check(request);

        // assert
        assertFalse(response.isSuccess());
        assertEquals(AccountResponseType.ACCOUNT_VERIFIED_AND_NOT_BLOCKED, response.getType());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).isVerified(userId);
        verify(userRepository, times(1)).isBlocked(userId);
    }

    @Test
    void check_withBlockedOrUnverifiedAccount_shouldReturnAccountCanBeUnlocked() {
        // arrange
        long userId = 1L;
        UserUnlockRequest request = new UserUnlockRequest(userId);
        UserEntity entity = new UserEntity();
        entity.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(entity);
        when(userRepository.isVerified(userId)).thenReturn(false);
        when(userRepository.isBlocked(userId)).thenReturn(true);

        // act
        AccountResponse response = process.check(request);

        // assert
        assertTrue(response.isSuccess());
        assertEquals(AccountResponseType.ACCOUNT_CAN_BE_UNLOCKED, response.getType());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).isVerified(userId);
        verify(userRepository, times(1)).isBlocked(userId);
    }

    @Test
    void check_withInvalidRequestType_shouldReturnBadUnlockRequestType() {
        // arrange
        Request request = mock(Request.class);

        // act
        AccountResponse response = process.check(request);

        // assert
        assertFalse(response.isSuccess());
        assertEquals(AccountResponseType.BAD_UNLOCK_REQUEST_TYPE, response.getType());
        verifyNoInteractions(userRepository);
    }

    @Test
    void save_withValidRequest_shouldUpdateUserStatusAndReturnUpdatedUser() {
        // arrange
        long userId = 1L;
        UserUnlockRequest request = new UserUnlockRequest(userId);
        UserEntity updatedEntity = new UserEntity();
        updatedEntity.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(updatedEntity);

        // act
        UserEntity result = process.save(request);

        // assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(userRepository, times(1)).updateStatus(userId, false, false);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void save_withInvalidRequestType_shouldReturnNull() {
        // arrange
        Request invalidRequest = mock(Request.class);

        // act
        UserEntity result = process.save(invalidRequest);

        // assert
        assertNull(result);
        verifyNoInteractions(userRepository);
    }

}
