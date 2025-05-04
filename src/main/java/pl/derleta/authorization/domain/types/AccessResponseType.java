package pl.derleta.authorization.domain.types;

/**
 * Enum representing possible responses related to access token operations,
 * specifically focusing on refreshing access tokens using a refresh token.
 *
 * <p>This enum defines status codes, associated application codes, process types,
 * and informative messages for each access-related outcome.</p>
 *
 * <ul>
 *     <li>{@link #ACCESS_REFRESHED} - Indicates that the access token was successfully refreshed and sent to the client.</li>
 *     <li>{@link #ACCESS_NOT_REFRESHED} - Indicates that the access token could not be refreshed. The refresh token may be expired or invalid.</li>
 *     <li>{@link #NULL} - Represents a null or unknown response.</li>
 * </ul>
 */
public enum AccessResponseType {

    ACCESS_REFRESHED(601, AppCode.ANDROMEDA_AUTH_SERVER,
            AccessProcessType.REFRESH_ACCESS, "Access was refreshed, new access token was generated and sent to client."),
    ACCESS_NOT_REFRESHED(602, AppCode.ANDROMEDA_AUTH_SERVER,
            AccessProcessType.REFRESH_ACCESS, "Access was not refreshed, new access token was not generated. Refresh token might be expired or invalid. Please try to login again and generate a new access token."),

    NULL(0, null, null, "null");

    final int id;
    final AppCode appCode;
    final AccessProcessType processType;
    final String info;

    AccessResponseType(int id, AppCode appCode, AccessProcessType processType, String info) {
        this.id = id;
        this.info = info;
        this.appCode = appCode;
        this.processType = processType;
    }

    @Override
    public String toString() {
        return "AccessResponseType{" +
                "id=" + id +
                ", appCode=" + appCode +
                ", processType=" + processType +
                ", info='" + info + '\'' +
                '}';
    }

}
