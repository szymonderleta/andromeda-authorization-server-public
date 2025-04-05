package pl.derleta.authorization.service.accounts.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.entity.UserEntityDecrypted;
import pl.derleta.authorization.domain.request.ResetPasswordRequest;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.request.Request;
import pl.derleta.authorization.domain.types.AccountResponseType;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.utils.MailGenerator;
import pl.derleta.authorization.utils.PasswordGenerator;

import java.util.Set;

/**
 * The ResetPasswordProcess class is responsible for handling operations related to resetting user passwords.
 * This includes validating reset password requests, saving updated user password details, and sending
 * confirmation emails with temporary passwords. It extends the PasswordProcess class to provide specific
 * functionality for password reset workflows.
 */
@Service
public final class ResetPasswordProcess extends PasswordProcess {

    /**
     * Constructs a new ResetPasswordProcess instance with the specified components for handling operations
     * related to password reset workflows. This constructor initializes the required repositories and
     * email service for processing and sending reset password-related emails.
     *
     * @param repositoryList a set of repositories used to interact with the database for user-related operations.
     * @param emailService   an email service used for sending password reset emails to users.
     */
    @Autowired
    public ResetPasswordProcess(Set<RepositoryClass> repositoryList, EmailService emailService) {
        super(repositoryList, emailService);
    }

    /**
     * Sends an email to the specified user with a message containing their password.
     * The email includes a subject and a message body generated using the user's information
     * and decrypted password.
     *
     * @param userEntityDecrypted a `UserEntityDecrypted` object containing the user's information
     *                            and decrypted password to be included in the email.
     * @return an `AccountResponse` indicating the success of the email-sending operation
     * along with the response type `MAIL_NEW_PASSWD_SENT`.
     */
    public AccountResponse sendMail(final UserEntityDecrypted userEntityDecrypted) {
        UserEntity entity = userRepository.findById(userEntityDecrypted.getUserId());
        MailGenerator mailGenerator = new MailGenerator();
        String password = userEntityDecrypted.getDecryptedPassword();
        String text = mailGenerator.generatePasswordMailText(entity, password);
        String subject = MailGenerator.getPasswordSubject();
        emailService.sendEmail(entity.getEmail(), subject, text);
        return new AccountResponse(true, AccountResponseType.MAIL_NEW_PASSWD_SENT);
    }

    /**
     * Validates and processes the given request to determine the status and type of account-related action.
     * Specifically handles requests of type ResetPasswordRequest and verifies the account's existence,
     * verification status, and block status.
     *
     * @param request the request object to validate and process. Should be an instance of ResetPasswordRequest
     *                containing the email of the user whose account is being checked.
     * @return an AccountResponse indicating the success or failure of the validation, along with the corresponding
     * account-related response type.
     */
    @Override
    public AccountResponse check(Request request) {
        if (request instanceof ResetPasswordRequest(String email)) {
            UserEntity entity = userRepository.findByEmail(email);
            if (entity == null || !entity.getEmail().equalsIgnoreCase(email))
                return new AccountResponse(false, AccountResponseType.ACCOUNT_NOT_EXIST_RESET_PASSWD);
            if (userRepository.isBlocked(entity.getUserId()))
                return new AccountResponse(false, AccountResponseType.ACCOUNT_IS_BLOCKED_RESET_PASSWD);
            if (!userRepository.isVerified(entity.getUserId()))
                return new AccountResponse(false, AccountResponseType.ACCOUNT_IS_NOT_VERIFIED);
            else return new AccountResponse(true, AccountResponseType.PASSWORD_CAN_BE_GENERATED);
        }
        return new AccountResponse(false, AccountResponseType.BAD_RESET_PASSWD_REQUEST_TYPE);
    }

    /**
     * Saves the changes associated with the provided request, such as resetting a user's password.
     * If the request is an instance of ResetPasswordRequest, this method generates a strong password,
     * encrypts it, updates the user's password in the repository, and returns a UserEntity instance
     * containing the new password in decrypted form.
     *
     * @param request the request object containing the details of the action to be performed.
     *                Must be an instance of ResetPasswordRequest to reset a user's password.
     * @return a UserEntity object containing the updated user details, including the decrypted new password,
     * or null if the request type is not supported.
     */
    @Override
    public UserEntity save(Request request) {
        if (request instanceof ResetPasswordRequest(String email)) {
            UserEntity entity = userRepository.findByEmail(email);
            final long userId = entity.getUserId();
            String newPassword = PasswordGenerator.generateStrongPassword();
            String encrypted = new BCryptPasswordEncoder().encode(newPassword);
            userRepository.updatePassword(userId, encrypted);
            UserEntity userEntity = userRepository.findById(userId);
            return new UserEntityDecrypted(userEntity, newPassword);
        }
        return null;
    }

}
