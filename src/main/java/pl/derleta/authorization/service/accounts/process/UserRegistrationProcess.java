package pl.derleta.authorization.service.accounts.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.controller.mapper.UserApiMapper;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.request.Request;
import pl.derleta.authorization.domain.request.UserRegistrationRequest;
import pl.derleta.authorization.domain.types.AccountResponseType;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.repository.impl.token.ConfirmationTokenRepository;
import pl.derleta.authorization.repository.impl.UserRepository;
import pl.derleta.authorization.repository.impl.UserRoleRepository;
import pl.derleta.authorization.service.accounts.AccountProcess;

import java.util.Set;

/**
 * The UserRegistrationProcess class handles the process of user registration,
 * including validation of registration data, storing user information, and assigning roles to users.
 * This class extends CreateConfirmationProcess and implements AccountProcess.
 */
@Service
public final class UserRegistrationProcess extends CreateConfirmationProcess implements AccountProcess {

    private static final int USER_ROLE_ID = 1;

    private UserRepository userRepository;
    private UserRoleRepository userRoleRepository;

    /**
     * Constructs a new UserRegistrationProcess with the provided repositories and email service.
     * Initializes the appropriate repository fields based on the types of items in the repository list
     * and sets the email service for further use in the process.
     *
     * @param repositoryList the set of repository objects containing implementations for UserRepository,
     *                       UserRoleRepository, and ConfirmationTokenRepository
     * @param emailService   the email service used to handle email-related operations
     */
    @Autowired
    public UserRegistrationProcess(Set<RepositoryClass> repositoryList, EmailService emailService) {
        for (RepositoryClass item : repositoryList) {
            if (item instanceof UserRepository instance) this.userRepository = instance;
            if (item instanceof UserRoleRepository instance) this.userRoleRepository = instance;
            if (item instanceof ConfirmationTokenRepository instance) this.setConfirmationTokenRepository(instance);
        }
        this.setEmailService(emailService);
    }

    /**
     * Validates the provided registration request to ensure the uniqueness of the email and username.
     * Determines whether the registration request is valid or not based on specific criteria.
     *
     * @param request the request object containing user registration details; must be an instance of UserRegistrationRequest
     * @return an AccountResponse object indicating success or failure, along with the appropriate response type:
     * UNIQUE_LOGIN_AND_EMAIL if the email and username are both unique,
     * EMAIL_IS_NOT_UNIQUE if the email is already in use,
     * LOGIN_IS_NOT_UNIQUE if the username is already in use,
     * BAD_REGISTRATION_REQUEST_TYPE if the request is not a valid UserRegistrationRequest.
     */
    @Override
    public AccountResponse check(Request request) {
        if (request instanceof UserRegistrationRequest instance) {
            try {
                boolean emailStatus = userRepository.isEmailExist(instance.email());
                if (emailStatus) return new AccountResponse(false, AccountResponseType.EMAIL_IS_NOT_UNIQUE);
                boolean loginStatus = userRepository.isLoginExist(instance.username());
                if (loginStatus) return new AccountResponse(false, AccountResponseType.LOGIN_IS_NOT_UNIQUE);
                return new AccountResponse(true, AccountResponseType.UNIQUE_LOGIN_AND_EMAIL);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        return new AccountResponse(false, AccountResponseType.BAD_REGISTRATION_REQUEST_TYPE);
    }

    /**
     * Saves a new user based on the provided registration request by assigning a unique ID
     * and storing the user and their associated role in the database.
     *
     * @param request the request object containing user registration details; must be an instance of UserRegistrationRequest
     * @return the saved UserEntity corresponding to the newly created user
     * @throws RuntimeException if the provided request is not an instance of UserRegistrationRequest
     */
    @Override
    public UserEntity save(Request request) {
        if (request instanceof UserRegistrationRequest instance) {
            long userId = userRepository.getNextUserId();
            User user = UserApiMapper.toUser(userId, instance);
            userRepository.save(userId, user);
            saveUserRoleToDatabase(userId);
            return userRepository.findById(userId);
        }
        throw new RuntimeException("Bad instance of Request parameter");
    }

    /**
     * Associates a user with a predefined role by saving this relationship
     * into the `user_roles` table in the database.
     *
     * @param userId the unique identifier of the user to whom the role will be assigned
     */
    private void saveUserRoleToDatabase(Long userId) {
        long pk = userRoleRepository.getNextId();
        userRoleRepository.save(pk, userId, USER_ROLE_ID);
    }

}
