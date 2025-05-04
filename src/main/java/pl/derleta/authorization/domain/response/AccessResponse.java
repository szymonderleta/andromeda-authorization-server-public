package pl.derleta.authorization.domain.response;

import pl.derleta.authorization.domain.types.AccessResponseType;

import java.util.Objects;

public class AccessResponse {

    boolean success;

    AccessResponseType type;

    public AccessResponse(boolean success, AccessResponseType type) {
        this.success = success;
        this.type = type;
    }

    public boolean isSuccess() {
        return success;
    }

    public AccessResponseType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "AccessResponse{" +
                "success=" + success +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AccessResponse that = (AccessResponse) o;
        return success == that.success && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, type);
    }

}
