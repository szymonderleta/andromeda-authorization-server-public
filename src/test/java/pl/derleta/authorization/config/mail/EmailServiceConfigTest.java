package pl.derleta.authorization.config.mail;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@SpringBootTest
class EmailServiceConfigTest {


    @Autowired
    private EmailServiceConfig emailServiceConfig;

    @Test
    void emailService_withValidConfig_shouldCreateEmailService() {
        // Arrange

        // Act
        EmailService emailService = emailServiceConfig.emailService();

        // Assert
        assertNotNull(emailService, "EmailService should not be null");
    }

    @Test
    void emailService_withMockedJavaMailSender_shouldNotInteractWithJavaMailSender() {
        // Arrange
        JavaMailSender mockJavaMailSender = Mockito.mock(JavaMailSender.class);

        // Act
        EmailService emailService = new EmailServiceConfig(mockJavaMailSender).emailService();

        // Assert
        assertNotNull(emailService, "EmailService should not be null");
        verify(mockJavaMailSender, Mockito.times(0)).createMimeMessage();
    }

}
