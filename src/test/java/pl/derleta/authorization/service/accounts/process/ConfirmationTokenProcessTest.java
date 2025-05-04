package pl.derleta.authorization.service.accounts.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.entity.token.ConfirmationTokenEntity;
import pl.derleta.authorization.domain.request.Request;
import pl.derleta.authorization.domain.request.UserConfirmationRequest;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.types.AccountResponseType;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.repository.impl.UserRepository;
import pl.derleta.authorization.repository.impl.token.ConfirmationTokenRepository;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConfirmationTokenProcessTest {

    @Mock
    UserRepository userRepository;

    @Mock
    ConfirmationTokenRepository confirmationTokenRepository;

    private ConfirmationTokenProcess process;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Set<RepositoryClass> repositoryList = new HashSet<>();
        repositoryList.add(userRepository);
        repositoryList.add(confirmationTokenRepository);

        process = new ConfirmationTokenProcess(repositoryList);
    }

    @Test
    void check_withNonExistentToken_shouldReturnTokenNotFound() {
        // arrange
        long tokenId = 1L;
        String token = "someTokenValue";

        when(confirmationTokenRepository.findById(tokenId)).thenReturn(Optional.empty());

        // act
        AccountResponse response = process.check(new UserConfirmationRequest(tokenId, token));

        // assert
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isSuccess(), "Response success flag should be false");
        assertEquals(AccountResponseType.TOKEN_NOT_FOUND, response.getType(), "Response type should be TOKEN_NOT_FOUND");
    }

    @Test
    void check_withInvalidTokenValue_shouldReturnInvalidToken() {
        // arrange
        long tokenId = 1L;
        String invalidToken = "invalidTokenValue";
        ConfirmationTokenEntity tokenEntity = new ConfirmationTokenEntity();
        tokenEntity.setToken("validTokenValue");

        when(confirmationTokenRepository.findById(tokenId)).thenReturn(Optional.of(tokenEntity));

        // act
        AccountResponse response = process.check(new UserConfirmationRequest(tokenId, invalidToken));

        // assert
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isSuccess(), "Response success flag should be false");
        assertEquals(AccountResponseType.INVALID_TOKEN_VALUE, response.getType(), "Response type should be INVALID_TOKEN_VALUE");
    }

    @Test
    void check_withExpiredToken_shouldReturnTokenExpired() {
        // arrange
        long tokenId = 1L;
        String token = "validTokenValue";
        ConfirmationTokenEntity tokenEntity = new ConfirmationTokenEntity();
        tokenEntity.setToken(token);
        tokenEntity.setExpirationDate(new Timestamp(System.currentTimeMillis() - 1000)); // Token expired

        when(confirmationTokenRepository.findById(tokenId)).thenReturn(Optional.of(tokenEntity));

        // act
        AccountResponse response = process.check(new UserConfirmationRequest(tokenId, token));

        // assert
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isSuccess(), "Response success flag should be false");
        assertEquals(AccountResponseType.TOKEN_EXPIRED, response.getType(), "Response type should be TOKEN_EXPIRED");
    }

    @Test
    void check_withValidToken_shouldReturnTokenIsValid() {
        // arrange
        long tokenId = 1L;
        String token = "validTokenValue";
        ConfirmationTokenEntity tokenEntity = new ConfirmationTokenEntity();
        tokenEntity.setToken(token);
        tokenEntity.setExpirationDate(new Timestamp(System.currentTimeMillis() + 1000)); // Token still valid

        when(confirmationTokenRepository.findById(tokenId)).thenReturn(Optional.of(tokenEntity));

        // act
        AccountResponse response = process.check(new UserConfirmationRequest(tokenId, token));

        // assert
        assertNotNull(response, "Response should not be null");
        assertTrue(response.isSuccess(), "Response success flag should be true");
        assertEquals(AccountResponseType.TOKEN_IS_VALID, response.getType(), "Response type should be TOKEN_IS_VALID");
    }

    @Test
    void check_withInvalidRequestType_shouldReturnBadConfirmationRequestType() {
        // arrange
        Request invalidRequest = mock(Request.class);

        // act
        AccountResponse response = process.check(invalidRequest);

        // assert
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isSuccess(), "Response success flag should be false");
        assertEquals(AccountResponseType.BAD_CONFIRMATION_REQUEST_TYPE, response.getType(),
                "Response type should be BAD_CONFIRMATION_REQUEST_TYPE");
    }

    @Test
    void update_withNonExistentToken_shouldReturnTokenNotFound() {
        // arrange
        long tokenId = 1L;

        when(confirmationTokenRepository.findById(tokenId)).thenReturn(Optional.empty());

        // act
        AccountResponse response = process.update(new UserConfirmationRequest(tokenId, "someTokenValue"));

        // assert
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isSuccess(), "Response success flag should be false");
        assertEquals(AccountResponseType.TOKEN_NOT_FOUND, response.getType(), "Response type should be TOKEN_NOT_FOUND");

        verify(userRepository, never()).unlock(anyLong());
        verify(confirmationTokenRepository, never()).setExpired(anyLong());
    }

    @Test
    void update_withExistingToken_shouldUnlockUserAndSetTokenExpired() {
        // arrange
        long tokenId = 1L;
        long userId = 100L;

        UserEntity user = new UserEntity();
        user.setUserId(userId);

        ConfirmationTokenEntity tokenEntity = new ConfirmationTokenEntity();
        tokenEntity.setTokenId(tokenId);
        tokenEntity.setUser(user);

        when(confirmationTokenRepository.findById(tokenId)).thenReturn(Optional.of(tokenEntity));

        // act
        AccountResponse response = process.update(new UserConfirmationRequest(tokenId, "someTokenValue"));

        // assert
        assertNotNull(response, "Response should not be null");
        assertTrue(response.isSuccess(), "Response success flag should be true");
        assertEquals(AccountResponseType.ACCOUNT_CONFIRMED, response.getType(),
                "Response type should be ACCOUNT_CONFIRMED");

        verify(userRepository).unlock(userId);
        verify(confirmationTokenRepository).setExpired(tokenId);
    }

    @Test
    void update_withInvalidRequestType_shouldReturnBadConfirmationRequestType() {
        // arrange
        Request invalidRequest = mock(Request.class);

        // act
        AccountResponse response = process.update(invalidRequest);

        // assert
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isSuccess(), "Response success flag should be false");
        assertEquals(AccountResponseType.BAD_CONFIRMATION_REQUEST_TYPE, response.getType(),
                "Response type should be BAD_CONFIRMATION_REQUEST_TYPE");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(confirmationTokenRepository);
    }

}
