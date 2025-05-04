package pl.derleta.authorization.config.model;

import java.util.Objects;

public class RoleSecurity {

    private int id;
    private final String name;

    public RoleSecurity(String name) {
        this.name = name;
    }

    public RoleSecurity(int id, String name) {
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
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleSecurity that)) return false;
        if (id != that.id) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
