package pl.derleta.authorization.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.entity.token.ConfirmationTokenEntity;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MailGeneratorTest {

    @Value("${nebula.confirmation.mail.url}")
    private String confirmationMailUrl;

    @Test
    void generateVerificationMailText_withValidInput_shouldReturnExpectedMailText() {
        // Arrange
        MailGenerator mailGenerator = new MailGenerator();
        UserEntity userEntity = new UserEntity(1L, "testuser", "testuser@example.com", "password");
        ConfirmationTokenEntity confirmationTokenEntity = new ConfirmationTokenEntity(
                100L,
                userEntity,
                "sampleToken",
                Timestamp.valueOf("2024-12-31 23:59:59")
        );

        mailGenerator.setNEBULA_FRONT_APP_URL_CONFIRMATION(confirmationMailUrl);

        // Act
        String result = mailGenerator.generateVerificationMailText(userEntity, confirmationTokenEntity);

        // Assert
        String expectedLink = confirmationMailUrl + "100/sampleToken";
        assertThat(result).isEqualTo("Dear testuser,\n" +
                "to complete please enter to link:\n" +
                expectedLink);
    }

}
