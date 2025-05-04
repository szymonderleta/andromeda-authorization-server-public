package pl.derleta.authorization.config.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


/**
 * Service for sending emails using an SMTP server.
 * This class provides a method to send emails with a recipient, subject, and body content.
 */
@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String username;

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * Sends an email using the provided recipient address, subject, and content.
     *
     * @param to      the email address of the recipient
     * @param subject the subject of the email
     * @param text    the body content of the email
     */
    public void sendEmail(String to, String subject, String text) {
        validateEmailParameters(to, subject, text);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(username);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    /**
     * Validates the parameters required for sending an email.
     *
     * @param to      the recipient's email address; must not be null, empty, or invalid in format
     * @param subject the subject of the email; must not be null or empty
     * @param text    the body content of the email; must not be null or empty
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    private void validateEmailParameters(String to, String subject, String text) {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email address cannot be null or empty");
        }

        if (!to.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new IllegalArgumentException("Invalid email address format: " + to);
        }

        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Email subject cannot be null or empty");
        }

        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Email body cannot be null or empty");
        }
    }

}
