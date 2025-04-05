package pl.derleta.authorization.config.mail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = EmailSenderConfig.class)
class EmailSenderConfigTest {

    @Autowired
    private EmailSenderConfig emailSenderConfig;

    @Test
    void getJavaMailSender_withValidConfiguration_shouldReturnCorrectJavaMailSender() {
        // Arrange
        JavaMailSender mailSender = emailSenderConfig.getJavaMailSender();

        // Act
        assertThat(mailSender).isInstanceOf(JavaMailSenderImpl.class);
        JavaMailSenderImpl javaMailSender = (JavaMailSenderImpl) mailSender;
        assertThat(javaMailSender.getHost()).isNotNull();
        assertThat(javaMailSender.getPort()).isNotNull();
        assertThat(javaMailSender.getUsername()).isNotNull();
        assertThat(javaMailSender.getPassword()).isNotNull();

        // Assert
        Properties javaMailProperties = javaMailSender.getJavaMailProperties();
        assertThat(javaMailProperties.getProperty("mail.transport.protocol")).isEqualTo("smtp");
        assertThat(javaMailProperties.getProperty("mail.smtp.auth")).isEqualTo("true");
        assertThat(javaMailProperties.getProperty("mail.smtp.starttls.enable")).isEqualTo("true");
        assertThat(javaMailProperties.getProperty("mail.debug")).isEqualTo("true");
    }

}
