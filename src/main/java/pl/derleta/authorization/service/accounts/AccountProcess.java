package pl.derleta.authorization.service.accounts;

import org.springframework.stereotype.Service;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.response.AccountResponse;
import pl.derleta.authorization.domain.request.Request;

/**
 * Interface defining the process for account-related operations, allowing
 * various implementations for different use cases. It provides methods to
 * check, update, and save account requests, with default behavior for update
 * and save operations that are unsupported unless overridden.
 */
@Service
public interface AccountProcess {

    AccountResponse check(Request request);

    default AccountResponse update(Request request) {
        throw new UnsupportedOperationException("Update operation not supported");
    }

    default UserEntity save(Request request) {
        throw new UnsupportedOperationException("Save operation not supported");
    }

}
