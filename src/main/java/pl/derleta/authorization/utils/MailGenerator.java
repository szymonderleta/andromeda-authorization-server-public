package pl.derleta.authorization.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.derleta.authorization.domain.entity.token.ConfirmationTokenEntity;
import pl.derleta.authorization.domain.entity.UserEntity;

/**
 * A utility class responsible for generating email content for various use cases such as
 * account verification, password resets, and informational notifications. It also provides
 * methods to retrieve appropriate email subject lines.
 */
@Component
public final class MailGenerator {

    private static String NEBULA_CONFIRMATION_MAIL_URL;

    @Value("${nebula.confirmation.mail.url}")
    public void setNEBULA_FRONT_APP_URL_CONFIRMATION(String value) {
        NEBULA_CONFIRMATION_MAIL_URL = value;
    }

    /**
     * Generates the text content for a verification email. The email includes a personalized
     * greeting and a link constructed using the confirmation token information.
     *
     * @param userEntity              the user entity containing the user's username to personalize the email
     * @param confirmationTokenEntity the confirmation token entity containing token details
     *                                for constructing the verification link
     * @return a string containing the text of the verification email
     */
    public String generateVerificationMailText(UserEntity userEntity, ConfirmationTokenEntity confirmationTokenEntity) {
        String link = NEBULA_CONFIRMATION_MAIL_URL + confirmationTokenEntity.getTokenId() + "/" + confirmationTokenEntity.getToken();
        return "Dear " + userEntity.getUsername() + ",\n" +
                "to complete please enter to link:\n" +
                link;
    }

    /**
     * Generates the text content for an email that provides a user with a new password.
     * The email includes a personalized greeting and the new password, along with a
     * recommendation to change the password after logging in.
     *
     * @param user     the user entity containing the username to personalize the email
     * @param password the newly generated password to be included in the email content
     * @return a string containing the text of the password notification email
     */
    public String generatePasswordMailText(final UserEntity user, final String password) {
        return "Dear " + user.getUsername() + ",\n" +
                "your new password is:\n" +
                password + " \n Please change your password after login, as soon as possible.";
    }

    /**
     * Generates the text content for an informational email regarding a password change.
     * The email notifies the recipient that their password has been changed and provides
     * instructions on what to do if the change was unauthorized.
     *
     * @return a string containing the text of the password change notification email
     */
    public String generateChangePasswdInfoMailText() {
        return """
                Hello,\s
                this is information mail only,
                password was changed if it wasn't you, please restore your password in nebula immediately.""";
    }

    /**
     * Retrieves the subject line for an email used for verification purposes.
     *
     * @return a string representing the subject of the verification email
     */
    public static String getVerificationSubject() {
        return "Confirmation message";
    }

    /**
     * Retrieves the subject line for an email related to password reset or generation.
     *
     * @return a string representing the subject of the password-related email
     */
    public static String getPasswordSubject() {
        return "New password message";
    }

}
