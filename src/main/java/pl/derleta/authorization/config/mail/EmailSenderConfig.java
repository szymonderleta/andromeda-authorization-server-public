package pl.derleta.authorization.config.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Configuration class for setting up the email sender using Spring's {@link JavaMailSender}.
 * This class loads SMTP-related configuration properties from the application's properties file,
 * such as host, port, username, and password. It also defines a bean for {@link JavaMailSender},
 * which can be used throughout the application for sending emails.
 * <p>
 * Properties configured:
 * - spring.mail.host: The SMTP server host.
 * - spring.mail.port: The SMTP server port.
 * - spring.mail.username: The username for authentication with the SMTP server.
 * - spring.mail.password: The password for authentication with the SMTP server.
 */
@Configuration
public class EmailSenderConfig {

    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.port}")
    private Integer port;
    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;

    /**
     * Configures and provides a {@link JavaMailSender} bean with SMTP settings such as host, port,
     * username, password, and other mail properties for sending emails.
     *
     * @return an instance of {@link JavaMailSender} configured for SMTP email sending.
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

}
