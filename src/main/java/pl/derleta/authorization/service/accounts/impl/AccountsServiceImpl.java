package pl.derleta.authorization.service.accounts.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.controller.mapper.UserRolesApiMapper;
import pl.derleta.authorization.domain.entity.UserRoleEntity;
import pl.derleta.authorization.domain.entity.token.ConfirmationTokenEntity;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.entity.UserEntityDecrypted;
import pl.derleta.authorization.domain.request.*;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.model.UserRoles;
import pl.derleta.authorization.domain.types.AccountProcessType;
import pl.derleta.authorization.domain.types.AccountResponseType;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.repository.impl.token.ConfirmationTokenRepository;
import pl.derleta.authorization.repository.impl.UserRepository;
import pl.derleta.authorization.repository.impl.UserRoleRepository;
import pl.derleta.authorization.repository.impl.UserRolesRepository;
import pl.derleta.authorization.service.accounts.*;
import pl.derleta.authorization.service.accounts.process.ChangePasswordProcess;
import pl.derleta.authorization.service.accounts.process.CreateConfirmationProcess;
import pl.derleta.authorization.service.accounts.process.ResetPasswordProcess;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service implementation for handling account-related operations.
 * <p>
 * This class provides implementations for operations including user registration,
 * account confirmation, user role retrieval, account unlocking, password reset,
 * and password update. Dependencies are injected into this class using the `@Autowired`
 * annotation to follow the dependency injection pattern.
 */
@Service
public class AccountsServiceImpl implements AccountsService {

    private final AccountProcessFactory accountProcessFactory;

    @Autowired
    public AccountsServiceImpl(AccountProcessFactory accountProcessFactory) {
        this.accountProcessFactory = accountProcessFactory;
    }

    private EmailService emailService;

    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserRoleRepository userRoleRepository;

    @Autowired
    public void setUserRoleRepository(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    private UserRolesRepository userRolesRepository;

    @Autowired
    public void setUserRolesRepository(UserRolesRepository userRolesRepository) {
        this.userRolesRepository = userRolesRepository;
    }

    private ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    public void setConfirmationTokenRepository(ConfirmationTokenRepository confirmationTokenRepository) {
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    /**
     * Registers a user based on the provided registration request.
     * Validates the request, processes the registration, and optionally sends a confirmation email depending on the process type.
     *
     * @param request the user registration request containing the necessary data for registration
     * @return an AccountResponse indicating the success or failure of the registration process, along with the appropriate response type
     */
    @Override
    public AccountResponse register(final UserRegistrationRequest request) {
        final Set<RepositoryClass> repositories = new HashSet<>(Set.of(userRepository, userRoleRepository, confirmationTokenRepository));
        AccountProcess accountProcess = accountProcessFactory.create(AccountProcessType.USER_REGISTRATION, repositories, emailService);
        AccountResponse status = accountProcess.check(request);
        if (!status.isSuccess()) return status;
        UserEntity userEntity = accountProcess.save(request);
        if (accountProcess instanceof CreateConfirmationProcess instance) {
            ConfirmationTokenEntity confirmationTokenEntity = instance.getToken(userEntity);
            return instance.sendEmail(userEntity, confirmationTokenEntity);
        } else {
            return new AccountResponse(false, AccountResponseType.BAD_REGISTRATION_PROCESS_INSTANCE);
        }
    }

    /**
     * Retrieves the user roles based on the provided username and email.
     *
     * @param username the username of the user whose roles are to be retrieved
     * @param email the email of the user whose roles are to be retrieved
     * @return an instance of UserRoles corresponding to the provided username and email,
     *         or null if no roles are found
     */
    @Override
    public UserRoles get(final String username, final String email) {
        List<UserRoleEntity> userRoleEntity =  userRolesRepository.get(username, email);
        if (userRoleEntity == null || userRoleEntity.isEmpty()) return null;
        return UserRolesApiMapper.toUserRoles(userRoleEntity);
    }

    /**
     * Confirms the user based on the provided confirmation request.
     *
     * @param request the user confirmation request containing necessary data for the confirmation process
     * @return the response of the account confirmation process, including success status and additional details
     */
    @Override
    public AccountResponse confirm(final UserConfirmationRequest request) {
        final Set<RepositoryClass> repositories = new HashSet<>(Set.of(userRepository, userRoleRepository, confirmationTokenRepository));
        AccountProcess accountProcess = accountProcessFactory.create(AccountProcessType.CONFIRMATION_TOKEN, repositories, emailService);
        AccountResponse status = accountProcess.check(request);
        if (!status.isSuccess()) return status;
        return accountProcess.update(request);
    }

    /**
     * Unlocks a user account based on the provided unlock request.
     *
     * @param request the user unlock request containing necessary details for account unlocking
     * @return an AccountResponse indicating the success or failure of the unlock process
     */
    @Override
    public AccountResponse unlock(final UserUnlockRequest request) {
        final Set<RepositoryClass> repositories = new HashSet<>(Set.of(userRepository, userRoleRepository, confirmationTokenRepository));
        AccountProcess accountProcess = accountProcessFactory.create(AccountProcessType.UNLOCK_ACCOUNT, repositories, emailService);
        AccountResponse status = accountProcess.check(request);
        if (!status.isSuccess()) return status;
        if (accountProcess instanceof CreateConfirmationProcess instance) {
            UserEntity userEntity = accountProcess.save(request);
            ConfirmationTokenEntity confirmationTokenEntity = instance.getToken(userEntity);
            return instance.sendEmail(userEntity, confirmationTokenEntity);
        }
        return new AccountResponse(false, AccountResponseType.BAD_UNLOCK_PROCESS_INSTANCE);
    }

    /**
     * Handles the reset password process for a user.
     * <p>
     * This method validates the input reset password request, processes it by using
     * the appropriate account process, updates the user's password if the validation
     * is successful, and sends a confirmation email.
     *
     * @param request the ResetPasswordRequest object containing the details necessary
     *                for initiating a password reset process such as the email associated
     *                with the account and the new password.
     * @return an AccountResponse object indicating the outcome of the operation. The response
     * contains a success flag and a response type to specify the status of the operation.
     */
    @Override
    public AccountResponse resetPassword(final ResetPasswordRequest request) {
        final Set<RepositoryClass> repositories = new HashSet<>(Set.of(userRepository, userRoleRepository, confirmationTokenRepository));
        AccountProcess accountProcess = accountProcessFactory.create(AccountProcessType.RESET_PASSWORD, repositories, emailService);
        AccountResponse status = accountProcess.check(request);
        if (!status.isSuccess()) return status;
        if (accountProcess instanceof ResetPasswordProcess instance) {
            UserEntity userEntity = instance.save(request);
            if (userEntity instanceof UserEntityDecrypted decrypted) return instance.sendMail(decrypted);
            else return new AccountResponse(false, AccountResponseType.BAD_USER_ENTITY_INSTANCE);
        }
        return new AccountResponse(false, AccountResponseType.BAD_RESET_PASSWD_PROCESS_INSTANCE);
    }

    /**
     * Updates the password for an account based on the provided request.
     *
     * @param request the request object containing information necessary for changing the password,
     *                such as the user's email, old password, and new password.
     * @return an AccountResponse object indicating the success or failure of the operation,
     * and if applicable, the reason for failure (e.g., invalid request, mail not sent).
     */
    public AccountResponse updatePassword(final ChangePasswordRequest request) {
        final Set<RepositoryClass> repositories = new HashSet<>(Set.of(userRepository));
        AccountProcess accountProcess = accountProcessFactory.create(AccountProcessType.CHANGE_PASSWORD, repositories, emailService);
        AccountResponse status = accountProcess.check(request);
        if (!status.isSuccess()) return status;
        if (accountProcess instanceof ChangePasswordProcess instance) {
            var result = instance.update(request);
            if (result.isSuccess()) {
                var mailResult = instance.sendMail(request.email());
                if (mailResult.isSuccess()) return result;
            }
            return new AccountResponse(false, AccountResponseType.PASSWORD_CHANGED_BUT_MAIL_NOT_SEND);
        }
        return new AccountResponse(false, AccountResponseType.BAD_CHANGE_PASSWD_REQUEST_TYPE);
    }

}
