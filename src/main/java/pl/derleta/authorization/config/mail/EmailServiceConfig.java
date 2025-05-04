package pl.derleta.authorization.config.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Configuration class for setting up the {@link EmailService} bean.
 * This class provides integration with the {@link JavaMailSender} instance to enable email-sending functionalities.
 * The {@link EmailService} bean is created and configured using the {@link JavaMailSender} provided as a dependency.
 */
@Component
public class EmailServiceConfig {
    private final JavaMailSender javaMailSender;

    /**
     * Constructor for the {@link EmailServiceConfig} class.
     * This constructor initializes the configuration with the provided {@link JavaMailSender} instance,
     * which enables the creation and configuration of the {@link EmailService} bean for email-sending functionalities.
     *
     * @param javaMailSender the {@link JavaMailSender} instance used for sending emails
     */
    @Autowired
    public EmailServiceConfig(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * Creates and configures a bean for the {@link EmailService}, which provides functionality
     * to send emails using the configured {@link JavaMailSender}.
     *
     * @return an instance of {@link EmailService} initialized with the {@link JavaMailSender}.
     */
    @Bean
    public EmailService emailService() {
        return new EmailService(javaMailSender);
    }

}
