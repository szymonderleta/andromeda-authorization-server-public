package pl.derleta.authorization.service.accounts.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.service.accounts.AccountProcess;
import pl.derleta.authorization.service.accounts.AccountProcessFactory;
import pl.derleta.authorization.domain.types.AccountProcessType;
import pl.derleta.authorization.service.accounts.process.*;

import java.util.Set;

/**
 * Implementation of the {@link AccountProcessFactory} interface for creating various account-related
 * processes based on the provided {@link AccountProcessType}.
 * <p>
 * The factory utilizes a switch statement to instantiate the appropriate {@link AccountProcess}
 * implementation. Each process type requires specific dependencies, which are injected through
 * the method parameters.
 * <p>
 * Supported {@link AccountProcessType} values and their corresponding {@link AccountProcess} implementations:
 * - CONFIRMATION_TOKEN: Creates an instance of {@link ConfirmationTokenProcess}.
 * - USER_REGISTRATION: Creates an instance of {@link UserRegistrationProcess}.
 * - UNLOCK_ACCOUNT: Creates an instance of {@link UnlockAccountProcess}.
 * - RESET_PASSWORD: Creates an instance of {@link ResetPasswordProcess}.
 * - CHANGE_PASSWORD: Creates an instance of {@link ChangePasswordProcess}.
 * <p>
 * Dependencies:
 * - repositoryList: A set of repositories required by the respective {@link AccountProcess} implementations.
 * - emailService: An email service used for sending emails in processes that require notification functionality.
 */
@Component
public class AccountProcessFactoryImpl implements AccountProcessFactory {

    private final ApplicationContext applicationContext;

    @Autowired
    public AccountProcessFactoryImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    /**
     * Creates a new instance of a specific {@link AccountProcess} based on the provided {@link AccountProcessType}.
     * The method utilizes a switch statement to dynamically generate the appropriate process implementation
     * required for various account-related operations, such as user registration, token confirmation, account unlocking, etc.
     *
     * @param process        the type of account process to be created. Must be a non-null {@link AccountProcessType}.
     * @param repositoryList the set of repository instances required for the respective process implementation.
     *                       These repositories include dependencies such as for handling user data or tokens.
     * @param emailService   the email service used for sending emails in processes requiring notification functionality.
     *                       This parameter is required for certain process types such as user registration and account unlocking.
     * @return a concrete implementation of {@link AccountProcess} depending on the specified process type.
     * Returns an instance of one of the following process implementations:
     * - {@link ConfirmationTokenProcess} for {@link AccountProcessType#CONFIRMATION_TOKEN}.
     * - {@link UserRegistrationProcess} for {@link AccountProcessType#USER_REGISTRATION}.
     * - {@link UnlockAccountProcess} for {@link AccountProcessType#UNLOCK_ACCOUNT}.
     * - {@link ResetPasswordProcess} for {@link AccountProcessType#RESET_PASSWORD}.
     * - {@link ChangePasswordProcess} for {@link AccountProcessType#CHANGE_PASSWORD}.
     */
    @Override
    public AccountProcess create(AccountProcessType process, Set<RepositoryClass> repositoryList, EmailService emailService) {
        return switch (process) {
            case CONFIRMATION_TOKEN -> new ConfirmationTokenProcess(repositoryList);
            case USER_REGISTRATION -> new UserRegistrationProcess(repositoryList, emailService);
            case UNLOCK_ACCOUNT -> new UnlockAccountProcess(repositoryList, emailService);
            case RESET_PASSWORD -> new ResetPasswordProcess(repositoryList, emailService);
            case CHANGE_PASSWORD ->
                    applicationContext.getBean(ChangePasswordProcess.class, repositoryList, emailService);
        };
    }

}
