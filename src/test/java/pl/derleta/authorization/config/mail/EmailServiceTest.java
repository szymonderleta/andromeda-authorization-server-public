package pl.derleta.authorization.config.mail;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    void sendEmail_withValidParameters_shouldSendEmail() {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Test Email Body";
        Mockito.doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendEmail(to, subject, text);

        // Assert
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_withEmptySubject_shouldThrowIllegalArgumentException() {
        // Arrange
        String to = "recipient@example.com";
        String subject = "";
        String text = "Test Email Body";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> emailService.sendEmail(to, subject, text));

        // Assert
        verify(javaMailSender, times(0)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_withEmptyMessageBody_shouldThrowIllegalArgumentException() {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String text = "";
        Mockito.doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> emailService.sendEmail(to, subject, text));

        // Assert
        verify(javaMailSender, times(0)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_withInvalidRecipient_shouldThrowIllegalArgumentException() {
        // Arrange
        String to = "invalid-email";
        String subject = "Test Subject";
        String text = "Test Email Body";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> emailService.sendEmail(to, subject, text));

        // Assert
        verify(javaMailSender, times(0)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_withNullRecipient_shouldThrowIllegalArgumentException() {
        // Arrange
        String subject = "Test Subject";
        String text = "Test Body";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> emailService.sendEmail(null, subject, text));

        // Assert
        verify(javaMailSender, times(0)).send(any(SimpleMailMessage.class));
    }
    
}
