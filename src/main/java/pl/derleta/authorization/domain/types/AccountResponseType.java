package pl.derleta.authorization.domain.types;

/**
 * AccountResponseType is an enumeration that defines various response types
 * and statuses for operations related to account management in the system.
 * Each response type is associated with a unique identifier, corresponding
 * application code, process type, and an informational message.
 * <p>
 * Enum Constants:
 * - BAD_REGISTRATION_PROCESS_INSTANCE: Indicates an internal server error with
 * a bad process instance during user registration.
 * - EMAIL_IS_NOT_UNIQUE: Indicates that the email address is already in use.
 * - LOGIN_IS_NOT_UNIQUE: Indicates a server error with a non-unique login during
 * registration.
 * - UNIQUE_LOGIN_AND_EMAIL, BAD_REGISTRATION_REQUEST_TYPE, VERIFICATION_MAIL_FROM_REGISTRATION:
 * Represents various specific flags and messages related to the registration process.
 * - TOKEN_NOT_FOUND, INVALID_TOKEN_VALUE, TOKEN_EXPIRED, TOKEN_IS_VALID: Represents
 * different states of a confirmation token process.
 * - ACCOUNT_CONFIRMED: Indicates that an account confirmation process was successful.
 * - BAD_UNLOCK_PROCESS_INSTANCE, VERIFICATION_MAIL_FROM_UNLOCK, ACCOUNT_NOT_EXIST_UNLOCK_ACCOUNT:
 * Handles various states of account unlocking.
 * - BAD_RESET_PASSWD_PROCESS_INSTANCE, ACCOUNT_NOT_EXIST_RESET_PASSWD, ACCOUNT_IS_BLOCKED_RESET_PASSWD:
 * Represents states for resetting account password operations.
 * - BAD_CHANGE_PASSWD_REQUEST_TYPE, PASSWORD_CAN_BE_CHANGED, PASSWORD_CHANGED: Represents
 * the process and outcome for changing passwords.
 * - NULL: Represents a default or uninitialized scenario.
 * <p>
 * Fields:
 * - id: A unique integer identifier for the response type.
 * - appCode: Corresponds to the application code associated with the response type.
 * - processType: Specifies the account process type (e.g., USER_REGISTRATION, RESET_PASSWORD).
 * - info: A description or message tied to the response type.
 * <p>
 * Constructor:
 * - AccountResponseType(int id, AppCode appCode, AccountProcessType processType, String info):
 * Initializes a response type with an identifier, application code, process type, and message.
 */
public enum AccountResponseType {

    BAD_REGISTRATION_PROCESS_INSTANCE(101, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.USER_REGISTRATION, "Internal Server Error, bad AccountProcess instance."),
    EMAIL_IS_NOT_UNIQUE(102, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.USER_REGISTRATION, "This email address is already in use."),
    LOGIN_IS_NOT_UNIQUE(103, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.USER_REGISTRATION, "Internal Server Error, bad AccountProcess instance."),
    UNIQUE_LOGIN_AND_EMAIL(104, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.USER_REGISTRATION, "Internal Server Error, bad AccountProcess instance."),
    BAD_REGISTRATION_REQUEST_TYPE(105, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.USER_REGISTRATION, "Bad request type."),
    VERIFICATION_MAIL_FROM_REGISTRATION(106, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.USER_REGISTRATION, "Verification mail was sent."),
    TOKEN_NOT_FOUND(201, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.CONFIRMATION_TOKEN, "Token not found."),
    INVALID_TOKEN_VALUE(202, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.CONFIRMATION_TOKEN, "Invalid token value."),
    TOKEN_EXPIRED(203, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.CONFIRMATION_TOKEN, "Token expired."),
    TOKEN_IS_VALID(204, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.CONFIRMATION_TOKEN, "Token is valid."),
    BAD_CONFIRMATION_REQUEST_TYPE(205, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.CONFIRMATION_TOKEN, "Bad request type."),
    ACCOUNT_CONFIRMED(206, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.CONFIRMATION_TOKEN, "Account confirmed."),
    BAD_UNLOCK_PROCESS_INSTANCE(301, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.UNLOCK_ACCOUNT, "Internal Server Error, bad AccountProcess instance."),
    VERIFICATION_MAIL_FROM_UNLOCK(302, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.UNLOCK_ACCOUNT, "Verification mail was sent."),
    ACCOUNT_NOT_EXIST_UNLOCK_ACCOUNT(303, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.UNLOCK_ACCOUNT, "Account not exist."),
    ACCOUNT_VERIFIED_AND_NOT_BLOCKED(304, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.UNLOCK_ACCOUNT, "Account is verified and not blocked."),
    ACCOUNT_CAN_BE_UNLOCKED(305, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.UNLOCK_ACCOUNT, "Account can be unlocked."),
    BAD_UNLOCK_REQUEST_TYPE(306, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.UNLOCK_ACCOUNT, "Bad request type."),
    BAD_RESET_PASSWD_PROCESS_INSTANCE(401, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.RESET_PASSWORD, "Internal Server Error, bad AccountProcess instance."),
    BAD_USER_ENTITY_INSTANCE(402, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.RESET_PASSWORD, "Password was not sent, not instance of UserEntityDecrypted."),
    BAD_RESET_PASSWD_REQUEST_TYPE(403, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.RESET_PASSWORD, "Bad request type."),
    MAIL_NEW_PASSWD_SENT(404, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.RESET_PASSWORD, "New password mail was sent."),
    ACCOUNT_NOT_EXIST_RESET_PASSWD(405, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.RESET_PASSWORD, "Account not exist."),
    ACCOUNT_IS_BLOCKED_RESET_PASSWD(406, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.RESET_PASSWORD, "Account is blocked, unlock it first."),
    ACCOUNT_IS_NOT_VERIFIED(407, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.RESET_PASSWORD, "Account is not verified, verify account first."),
    PASSWORD_CAN_BE_GENERATED(408, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.RESET_PASSWORD, "Password can be generated."),

    BAD_CHANGE_PASSWD_REQUEST_TYPE(501, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.CHANGE_PASSWORD, "Bad request type."),
    EMAIL_NOT_EXIST_CHANGE_PASSWD(502, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.CHANGE_PASSWORD, "Bad email address."),
    ACCOUNT_IS_BLOCKED_CHANGE_PASSWD(503, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.CHANGE_PASSWORD, "Account is blocked, unlock it first."),
    BAD_ACTUAL_PASSWORD_CHANGE_PASSWD(504, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.CHANGE_PASSWORD, "Bad actual password."),
    PASSWORD_CAN_BE_CHANGED(505, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.CHANGE_PASSWORD, "Password can be changed."),
    PASSWORD_CHANGED(506, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.CHANGE_PASSWORD, "Password  changed."),
    PASSWORD_NOT_CHANGED(507, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.CHANGE_PASSWORD, "Password not changed."),
    PASSWORD_CHANGED_BUT_MAIL_NOT_SEND(507, AppCode.ANDROMEDA_AUTH_SERVER,
            AccountProcessType.CHANGE_PASSWORD, "Password was changed but probably information mail wasn't send."),

    NULL(0, null, null, "null");

    final int id;
    final AppCode appCode;
    final AccountProcessType processType;
    final String info;

    AccountResponseType(int id, AppCode appCode, AccountProcessType processType, String info) {
        this.id = id;
        this.info = info;
        this.appCode = appCode;
        this.processType = processType;
    }

    @Override
    public String toString() {
        return "AccountResponseType{" +
                "id=" + id +
                ", appCode=" + appCode +
                ", processType=" + processType +
                ", info='" + info + '\'' +
                '}';
    }

}
