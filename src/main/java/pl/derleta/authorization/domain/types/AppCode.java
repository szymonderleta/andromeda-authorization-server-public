package pl.derleta.authorization.domain.types;

/**
 * AppCode is an enumeration that represents application-specific codes
 * used for identifying various components within a system. Each code is
 * associated with a unique identifier and a descriptive name to facilitate
 * its identification and usage.
 * <p>
 * Enum Constants:
 * - ANDROMEDA_AUTH_SERVER: Represents the Andromeda Authorization Server.
 * <p>
 * Fields:
 * - id: A unique integer identifier assigned to the enumeration constant.
 * - name: A descriptive name associated with the enumeration constant.
 * <p>
 * Constructor:
 * - AppCode(int id, String name): Initializes the enumeration constant with
 * the specified id and name.
 */
public enum AppCode {

    ANDROMEDA_AUTH_SERVER(1, "Andromeda Authorization Server");

    final int id;
    final String name;

    AppCode(int id, String name) {
        this.id = id;
        this.name = name;
    }

}
