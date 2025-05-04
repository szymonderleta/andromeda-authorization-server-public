package pl.derleta.authorization.domain.response;

import pl.derleta.authorization.domain.types.AccountResponseType;

public class AccountResponse {

    boolean success;

    AccountResponseType type;

    public AccountResponse(boolean success, AccountResponseType type) {
        this.success = success;
        this.type = type;
    }

    public boolean isSuccess() {
        return success;
    }

    public AccountResponseType getType() {
        return type;
    }
}
