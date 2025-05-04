package pl.derleta.authorization.service.accounts.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.derleta.authorization.domain.entity.token.ConfirmationTokenEntity;
import pl.derleta.authorization.domain.entity.token.TokenEntity;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.request.Request;
import pl.derleta.authorization.domain.request.UserConfirmationRequest;
import pl.derleta.authorization.domain.types.AccountResponseType;
import pl.derleta.authorization.repository.impl.token.ConfirmationTokenRepository;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.repository.impl.UserRepository;
import pl.derleta.authorization.service.accounts.AccountProcess;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of the {@link AccountProcess} interface to handle user account confirmations
 * using confirmation tokens. This class manages the validation and processing of confirmation
 * requests, including checking token validity and updating user accounts upon successful confirmation.
 */
@Service
public final class ConfirmationTokenProcess implements AccountProcess {

    private ConfirmationTokenRepository confirmationTokenRepository;
    private UserRepository userRepository;

    /**
     * Constructor for the {@code ConfirmationTokenProcess} class.
     * It initializes the required repositories for handling user confirmations and tokens.
     *
     * @param repositoryList a set of repository implementations.
     *                       The constructor assigns instances of {@link UserRepository} and {@link ConfirmationTokenRepository}
     *                       from this set to the respective fields if they are present.
     */
    @Autowired
    public ConfirmationTokenProcess(Set<RepositoryClass> repositoryList) {
        for (RepositoryClass item : repositoryList) {
            if (item instanceof UserRepository instance) this.userRepository = instance;
            if (item instanceof ConfirmationTokenRepository instance) this.confirmationTokenRepository = instance;
        }
    }

    /**
     * Validates a user confirmation request by checking the provided token's existence, value, and expiration status.
     * Returns an appropriate response indicating whether the token is valid or describes the issue encountered.
     *
     * @param request the request object containing the user confirmation details.
     *                Must be an instance of {@link UserConfirmationRequest}.
     * @return an {@link AccountResponse} object containing the success status and response type.
     * If the provided token is valid, a successful response is returned.
     * Otherwise, an error response is returned indicating the specific failure reason,
     * such as token not found, invalid token value, expired token, or an invalid request type.
     */
    @Override
    public AccountResponse check(Request request) {
        if (request instanceof UserConfirmationRequest(Long tokenId, String token)) {
            ConfirmationTokenEntity tokenEntity = findById(tokenId);

            if (tokenEntity == null) return new AccountResponse(false, AccountResponseType.TOKEN_NOT_FOUND);
            if (!token.equals(tokenEntity.getToken()))
                return new AccountResponse(false, AccountResponseType.INVALID_TOKEN_VALUE);

            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (tokenEntity.getExpirationDate().before(now))
                return new AccountResponse(false, AccountResponseType.TOKEN_EXPIRED);
            return new AccountResponse(true, AccountResponseType.TOKEN_IS_VALID);
        }
        return new AccountResponse(false, AccountResponseType.BAD_CONFIRMATION_REQUEST_TYPE);
    }

    /**
     * Updates and confirms the account based on the provided request.
     * This method unlocks the associated user account, marks the confirmation token as expired,
     * and returns the appropriate response indicating the success or failure of the update operation.
     *
     * @param request the request object containing information needed for the update.
     *                Must be an instance of {@link UserConfirmationRequest}.
     * @return an {@link AccountResponse} object containing the success status and response type.
     * Returns a successful response if the requested token exists and is processed correctly,
     * or returns a failure response if the request type is invalid.
     */
    @Override
    public AccountResponse update(Request request) {
        if (request instanceof UserConfirmationRequest instance) {
            ConfirmationTokenEntity tokenEntity = findById(instance.tokenId());
            if (tokenEntity == null) return new AccountResponse(false, AccountResponseType.TOKEN_NOT_FOUND);
            userRepository.unlock(tokenEntity.getUser().getUserId());
            confirmationTokenRepository.setExpired(tokenEntity.getTokenId());
            return new AccountResponse(true, AccountResponseType.ACCOUNT_CONFIRMED);
        }
        return new AccountResponse(false, AccountResponseType.BAD_CONFIRMATION_REQUEST_TYPE);
    }

    /**
     * Retrieves a confirmation token entity by its unique identifier.
     *
     * @param tokenId the unique identifier of the token to be retrieved.
     * @return the {@link ConfirmationTokenEntity} if the token exists and matches the expected type,
     * or null if no such token is found.
     */
    private ConfirmationTokenEntity findById(Long tokenId) {
        Optional<TokenEntity> tokenEntity = confirmationTokenRepository.findById(tokenId);
        return tokenEntity
                .filter(ConfirmationTokenEntity.class::isInstance)
                .map(ConfirmationTokenEntity.class::cast)
                .orElse(null);

    }

}
