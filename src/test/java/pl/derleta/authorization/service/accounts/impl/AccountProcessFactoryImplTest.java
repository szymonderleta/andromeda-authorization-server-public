package pl.derleta.authorization.service.accounts.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.domain.types.AccountProcessType;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.service.accounts.AccountProcess;
import pl.derleta.authorization.service.accounts.process.*;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountProcessFactoryImplTest {

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private AccountProcessFactoryImpl factory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createConfirmationTokenProcess_withValidParameters_shouldReturnConfirmationTokenProcess() {
        // Arrange
        Set<RepositoryClass> repositoryList = Collections.singleton(mock(RepositoryClass.class));

        // Act
        AccountProcess result = factory.create(AccountProcessType.CONFIRMATION_TOKEN, repositoryList, null);

        // Assert
        assertThat(result).isInstanceOf(ConfirmationTokenProcess.class);
    }

    @Test
    void createUserRegistrationProcess_withValidParameters_shouldReturnUserRegistrationProcess() {
        // Arrange
        Set<RepositoryClass> repositoryList = Collections.singleton(mock(RepositoryClass.class));
        EmailService emailService = mock(EmailService.class);

        // Act
        AccountProcess result = factory.create(AccountProcessType.USER_REGISTRATION, repositoryList, emailService);

        // Assert
        assertThat(result).isInstanceOf(UserRegistrationProcess.class);
    }

    @Test
    void createUnlockAccountProcess_withValidParameters_shouldReturnUnlockAccountProcess() {
        // Arrange
        Set<RepositoryClass> repositoryList = Collections.singleton(mock(RepositoryClass.class));
        EmailService emailService = mock(EmailService.class);

        // Act
        AccountProcess result = factory.create(AccountProcessType.UNLOCK_ACCOUNT, repositoryList, emailService);

        // Assert
        assertThat(result).isInstanceOf(UnlockAccountProcess.class);
    }

    @Test
    void createResetPasswordProcess_withValidParameters_shouldReturnResetPasswordProcess() {
        // Arrange
        Set<RepositoryClass> repositoryList = Collections.singleton(mock(RepositoryClass.class));
        EmailService emailService = mock(EmailService.class);

        // Act
        AccountProcess result = factory.create(AccountProcessType.RESET_PASSWORD, repositoryList, emailService);

        // Assert
        assertThat(result).isInstanceOf(ResetPasswordProcess.class);
    }

    @Test
    void createChangePasswordProcess_withValidParameters_shouldReturnChangePasswordProcess() {
        // Arrange
        Set<RepositoryClass> repositoryList = Collections.singleton(mock(RepositoryClass.class));
        EmailService emailService = mock(EmailService.class);

        ChangePasswordProcess changePasswordProcessMock = mock(ChangePasswordProcess.class);

        when(applicationContext.getBean(ChangePasswordProcess.class, repositoryList, emailService))
                .thenReturn(changePasswordProcessMock);

        // Act
        AccountProcess result = factory.create(AccountProcessType.CHANGE_PASSWORD, repositoryList, emailService);

        // Assert
        assertThat(result).isInstanceOf(ChangePasswordProcess.class);
    }

}
