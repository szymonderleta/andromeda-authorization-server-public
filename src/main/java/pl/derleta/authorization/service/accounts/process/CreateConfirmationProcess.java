package pl.derleta.authorization.service.accounts.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.domain.entity.token.ConfirmationTokenEntity;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.entity.token.TokenEntity;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.types.AccountResponseType;
import pl.derleta.authorization.repository.impl.token.ConfirmationTokenRepository;
import pl.derleta.authorization.utils.ConfigurationTokenGenerator;
import pl.derleta.authorization.utils.MailGenerator;

import java.util.Optional;

/**
 * Abstract class representing a process for creating confirmation tokens and sending emails.
 * This class is sealed and allows specific implementations to handle different types
 * of confirmation processes such as UnlockAccountProcess and UserRegistrationProcess.
 */
@Service
public abstract sealed class CreateConfirmationProcess permits UnlockAccountProcess, UserRegistrationProcess {

    private EmailService emailService;

    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    private ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    public void setConfirmationTokenRepository(ConfirmationTokenRepository confirmationTokenRepository) {
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    /**
     * Generates and retrieves a confirmation token entity for a specified user.
     *
     * @param userEntity the user entity for which the confirmation token is generated
     * @return the generated ConfirmationTokenEntity or null if the token cannot be found
     */
    public ConfirmationTokenEntity getToken(UserEntity userEntity) {
        String token = ConfigurationTokenGenerator.getToken();
        long tokenId = confirmationTokenRepository.getNextId();
        if (userEntity == null || tokenId < 0) return null;
        confirmationTokenRepository.save(tokenId, userEntity.getUserId(), token);

        Optional<TokenEntity> tokenEntity = confirmationTokenRepository.findById(tokenId);

        return tokenEntity
                .filter(ConfirmationTokenEntity.class::isInstance)
                .map(ConfirmationTokenEntity.class::cast)
                .orElse(null);
    }

    /**
     * Sends a verification email to the specified user using the provided confirmation token.
     *
     * @param userEntity              the user entity containing user details such as email
     * @param confirmationTokenEntity the confirmation token entity containing token details
     * @return an AccountResponse indicating the result of the email sending process
     */
    public AccountResponse sendEmail(UserEntity userEntity, ConfirmationTokenEntity confirmationTokenEntity) {
        MailGenerator mailGenerator = new MailGenerator();
        String text = mailGenerator.generateVerificationMailText(userEntity, confirmationTokenEntity);
        String subject = MailGenerator.getVerificationSubject();
        emailService.sendEmail(userEntity.getEmail(), subject, text);
        return getResponse(this);
    }

    /**
     * Generates an AccountResponse based on the type of CreateConfirmationProcess.
     *
     * @param createConfirmationProcess the process type that determines the response content
     * @return an AccountResponse object containing success status and response type
     */
    private AccountResponse getResponse(CreateConfirmationProcess createConfirmationProcess) {
        return switch (createConfirmationProcess) {
            case UserRegistrationProcess urp ->
                    new AccountResponse(true, AccountResponseType.VERIFICATION_MAIL_FROM_REGISTRATION);
            case UnlockAccountProcess uap ->
                    new AccountResponse(true, AccountResponseType.VERIFICATION_MAIL_FROM_UNLOCK);
        };
    }

}
