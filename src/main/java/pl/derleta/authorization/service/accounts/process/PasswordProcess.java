package pl.derleta.authorization.service.accounts.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.domain.request.Request;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.repository.RepositoryClass;
import pl.derleta.authorization.repository.impl.UserRepository;
import pl.derleta.authorization.service.accounts.AccountProcess;

import java.util.Set;

/**
 * The PasswordProcess class is an abstract sealed class that provides the foundation for processing
 * account password-related operations such as password change or reset. It implements the AccountProcess
 * interface and can be extended only by permitted subclasses such as ChangePasswordProcess and
 * ResetPasswordProcess.
 * <p>
 * This class defines common dependencies such as the UserRepository for accessing user-related data
 * and EmailService for sending email notifications. It provides mechanisms for setting these dependencies
 * and ensures subclasses have access to them.
 * <p>
 * Certain operations, including account request validation (`check` method), are intentionally left
 * unsupported within this class, requiring specific implementations in permissible subclasses.
 */
@Service
public abstract sealed class PasswordProcess implements AccountProcess permits ChangePasswordProcess, ResetPasswordProcess {

    protected EmailService emailService;

    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    protected UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Constructs a new instance of the PasswordProcess class with the specified repository list and email service.
     * The constructor initializes the user repository by iterating through the provided repository list
     * and identifying an instance of UserRepository. It also sets the email service for the process.
     *
     * @param repositoryList a set of repository objects that will be iterated to locate an instance of UserRepository
     * @param emailService   the email service used for sending notifications or handling email-related operations
     */
    @Autowired
    public PasswordProcess(Set<RepositoryClass> repositoryList, EmailService emailService) {
        for (RepositoryClass item : repositoryList) {
            if (item instanceof UserRepository instance) this.userRepository = instance;
        }
        this.setEmailService(emailService);
    }

    /**
     * Checks the status or validity of the provided account-related request.
     * This method is not supported in the abstract `PasswordProcess` class and
     * throws an `UnsupportedOperationException` if invoked.
     *
     * @param request the request object to be checked, typically containing details
     *                necessary for account verification or processing.
     * @return does not return a value since this method is not supported. It always
     * throws an `UnsupportedOperationException`.
     * @throws UnsupportedOperationException always thrown, as this method is not
     *                                       implemented in the abstract class.
     */
    @Override
    public AccountResponse check(Request request) {
        throw new UnsupportedOperationException("Update operation not supported");
    }

}
