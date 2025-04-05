package pl.derleta.authorization.service.accounts;

import pl.derleta.authorization.config.mail.EmailService;
import pl.derleta.authorization.domain.types.AccountProcessType;
import pl.derleta.authorization.repository.RepositoryClass;

import java.util.Set;


/**
 * Factory interface for creating instances of {@link AccountProcess}.
 * This factory is responsible for instantiating specific account processing
 * logic based on the provided {@link AccountProcessType}.
 * <p>
 * Methods implementing this interface would typically use the provided
 * dependencies (repository list and email service) to construct the appropriate
 * {@link AccountProcess} instance.
 */
public interface AccountProcessFactory {

    AccountProcess create(AccountProcessType process, Set<RepositoryClass> repositoryList, EmailService emailService);

}
