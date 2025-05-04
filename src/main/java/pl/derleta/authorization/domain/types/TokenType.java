package pl.derleta.authorization.domain.types;

public enum TokenType {

    CONFIRMATION(1, "Confirmation Token"),
    ACCESS(2, "Access Token"),
    REFRESH(3, "Refresh Token");

    private final int id;
    private final String name;

    TokenType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
