package pl.derleta.authorization.service.accounts.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.request.Request;
import pl.derleta.authorization.domain.request.UserUnlockRequest;
import pl.derleta.authorization.domain.types.AccountResponseType;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.repository.impl.token.ConfirmationTokenRepository;
import pl.derleta.authorization.repository.impl.UserRepository;
import pl.derleta.authorization.service.accounts.AccountProcess;

import java.util.Set;

/**
 * Service class that handles the process of unlocking a user's account.
 * Extends CreateConfirmationProcess and implements AccountProcess to provide
 * functionality for validating and processing unlock requests, as well as
 * updating user account status.
 * <p>
 * This class relies on UserRepository for user-related database operations
 * and EmailService for email notifications. Confirmation tokens are managed
 * using ConfirmationTokenRepository, inherited from CreateConfirmationProcess.
 */
@Service
public final class UnlockAccountProcess extends CreateConfirmationProcess implements AccountProcess {

    private UserRepository userRepository;

    /**
     * Constructor for the UnlockAccountProcess class. Initializes the userRepository
     * and confirmationTokenRepository instances from the provided repository list,
     * and sets up the EmailService for further email-related operations.
     *
     * @param repositoryList a set of Repository implementations, used to retrieve and
     *                       initialize the UserRepository and ConfirmationTokenRepository
     * @param emailService   an instance of EmailService used to handle email-related
     *                       tasks such as sending confirmation emails
     */
    @Autowired
    public UnlockAccountProcess(Set<RepositoryClass> repositoryList, EmailService emailService) {
        for (RepositoryClass item : repositoryList) {
            if (item instanceof UserRepository instance) this.userRepository = instance;
            if (item instanceof ConfirmationTokenRepository instance) this.setConfirmationTokenRepository(instance);

        }
        this.setEmailService(emailService);
    }

    /**
     * Validates and processes a user account unlock request.
     *
     * @param request the request object, must be an instance of UserUnlockRequest
     *                containing the user ID to check the account unlock eligibility
     * @return an AccountResponse encapsulating the result of the check and the
     * corresponding AccountResponseType to indicate the status
     */
    @Override
    public AccountResponse check(final Request request) {
        if (request instanceof UserUnlockRequest(Long userId)) {
            UserEntity entity = userRepository.findById(userId);
            if (entity == null || entity.getUserId() != userId)
                return new AccountResponse(false, AccountResponseType.ACCOUNT_NOT_EXIST_UNLOCK_ACCOUNT);
            boolean isVerified = userRepository.isVerified(userId);
            boolean isBlocked = userRepository.isBlocked(userId);
            if (isVerified && !isBlocked) {
                return new AccountResponse(false, AccountResponseType.ACCOUNT_VERIFIED_AND_NOT_BLOCKED);
            } else return new AccountResponse(true, AccountResponseType.ACCOUNT_CAN_BE_UNLOCKED);
        }
        return new AccountResponse(false, AccountResponseType.BAD_UNLOCK_REQUEST_TYPE);
    }


    /**
     * Updates the status of a user in the database and retrieves the updated user entity.
     *
     * @param request the request object, must be an instance of UserUnlockRequest
     *                containing the user ID to update and retrieve
     * @return the corresponding UserEntity object after the update if the request is valid,
     * otherwise null
     */
    @Override
    public UserEntity save(final Request request) {
        if (request instanceof UserUnlockRequest(Long userId)) {
            userRepository.updateStatus(userId, false, false);
            return userRepository.findById(userId);
        }
        return null;
    }

}
