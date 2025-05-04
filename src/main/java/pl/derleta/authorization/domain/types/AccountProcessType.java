package pl.derleta.authorization.domain.types;

/**
 * AccountProcessType is an enumeration that represents various processes
 * related to user account management. Each process type is associated with
 * a unique identifier and a name to facilitate identification and categorization
 * of account-related operations.
 * <p>
 * Enum Constants:
 * - USER_REGISTRATION: Represents the user registration process.
 * - UNLOCK_ACCOUNT: Represents the account unlocking process.
 * - CONFIRMATION_TOKEN: Represents actions related to confirmation tokens.
 * - RESET_PASSWORD: Represents the password reset process.
 * - CHANGE_PASSWORD: Represents the password change process.
 * <p>
 * Methods:
 * - getId: Returns the unique identifier for the process type.
 * - getName: Returns the name of the process type.
 * - toString: Provides a string representation of the enumeration, including its
 * id and name.
 */
public enum AccountProcessType {

    USER_REGISTRATION(1, "User Registration"),
    UNLOCK_ACCOUNT(2, "Unlock Account"),
    CONFIRMATION_TOKEN(3, "Confirmation Token"),
    RESET_PASSWORD(4, "Reset Password"),
    CHANGE_PASSWORD(5, "Change Password");

    final int id;
    final String name;

    AccountProcessType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "AccountProcessType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

}
