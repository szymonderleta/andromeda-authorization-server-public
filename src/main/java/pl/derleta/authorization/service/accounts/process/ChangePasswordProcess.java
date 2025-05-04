package pl.derleta.authorization.service.accounts.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.request.ChangePasswordRequest;
import pl.derleta.authorization.domain.request.Request;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.types.AccountResponseType;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.utils.MailGenerator;

import java.util.Set;

/**
 * The ChangePasswordProcess class is responsible for handling the logic and functionality
 * related to changing a user's password. This includes validating password change requests,
 * updating the user's password in the repository after encryption, and notifying the user
 * via email about the password update.
 * <p>
 * This class extends the PasswordProcess and provides implementation for methods to check
 * the validity of a password change request, update the password in storage, and send
 * notifications.
 */
@Service
public final class ChangePasswordProcess extends PasswordProcess {

    private final PasswordEncoder encoder;

    /**
     * Constructor for the ChangePasswordProcess class.
     *
     * @param repositoryList a set of repository instances required for handling
     *                       password change processes.
     * @param emailService   the service used for handling email notifications.
     * @param encoder        the password encoder utilized for securing passwords.
     */
    @Autowired
    public ChangePasswordProcess(Set<RepositoryClass> repositoryList, EmailService emailService, PasswordEncoder encoder) {
        super(repositoryList, emailService);
        this.encoder = encoder;
    }

    /**
     * Evaluates the provided request to determine whether an account modification
     * operation, such as password change, is permitted. This method specifically
     * handles requests of type ChangePasswordRequest and performs validation checks
     * related to the account’s email, actual password, and account status.
     *
     * @param request the request to be evaluated, typically an instance of ChangePasswordRequest
     * @return an AccountResponse indicating the result of the check, with specific
     * AccountResponseType values depending on the validation outcome:
     * - EMAIL_NOT_EXIST_CHANGE_PASSWD: if the email does not exist.
     * - ACCOUNT_IS_BLOCKED_CHANGE_PASSWD: if the account is blocked.
     * - BAD_ACTUAL_PASSWORD_CHANGE_PASSWD: if the actual password is incorrect.
     * - PASSWORD_CAN_BE_CHANGED: if all validation checks are successful.
     * - BAD_RESET_PASSWD_REQUEST_TYPE: if the request type is unsupported.
     */
    @Override
    public AccountResponse check(final Request request) {
        if (request instanceof ChangePasswordRequest instance) {
            final String email = instance.email();
            UserEntity entity = userRepository.findByEmail(email);
            if (entity == null || !entity.getEmail().equalsIgnoreCase(email))
                return new AccountResponse(false, AccountResponseType.EMAIL_NOT_EXIST_CHANGE_PASSWD);
            if (userRepository.isBlocked(entity.getUserId()))
                return new AccountResponse(false, AccountResponseType.ACCOUNT_IS_BLOCKED_CHANGE_PASSWD);
            if (!encoder.matches(instance.actualPassword(), entity.getPassword()))
                return new AccountResponse(false, AccountResponseType.BAD_ACTUAL_PASSWORD_CHANGE_PASSWD);
            else return new AccountResponse(true, AccountResponseType.PASSWORD_CAN_BE_CHANGED);
        }
        return new AccountResponse(false, AccountResponseType.BAD_RESET_PASSWD_REQUEST_TYPE);
    }

    /**
     * Updates the account information based on the provided request. Specifically,
     * if the request is an instance of ChangePasswordRequest, it updates the password
     * for the associated user account after encrypting it. If the update is successful,
     * an AccountResponse indicating the success of the operation is returned.
     *
     * @param request the request containing the new password information, must be an instance
     *                of ChangePasswordRequest
     * @return an AccountResponse indicating the result of the update operation. Returns
     * AccountResponseType.PASSWORD_CHANGED upon successful password change, or
     * AccountResponseType.PASSWORD_NOT_CHANGED if the update fails.
     */
    @Override
    public AccountResponse update(final Request request) {
        if (request instanceof ChangePasswordRequest instance) {
            final String email = instance.email();
            UserEntity entity = userRepository.findByEmail(email);
            final long userId = entity.getUserId();
            String encrypted = encoder.encode(instance.newPassword());
            userRepository.updatePassword(userId, encrypted);
            return new AccountResponse(true, AccountResponseType.PASSWORD_CHANGED);
        }
        return new AccountResponse(false, AccountResponseType.PASSWORD_NOT_CHANGED);
    }

    /**
     * Sends an email to the specified address containing information regarding password change.
     *
     * @param emailAddress the email address to which the email should be sent
     * @return an AccountResponse indicating the success of the email sending operation,
     * with the type set to AccountResponseType.MAIL_NEW_PASSWD_SENT
     */
    public AccountResponse sendMail(final String emailAddress) {
        MailGenerator mailGenerator = new MailGenerator();
        String text = mailGenerator.generateChangePasswdInfoMailText();
        String subject = MailGenerator.getPasswordSubject();
        emailService.sendEmail(emailAddress, subject, text);
        return new AccountResponse(true, AccountResponseType.MAIL_NEW_PASSWD_SENT);
    }

}
