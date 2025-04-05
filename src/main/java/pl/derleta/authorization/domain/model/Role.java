package pl.derleta.authorization.domain.model;

import java.io.Serializable;

public record Role(int roleId, String roleName) implements Serializable {

}
