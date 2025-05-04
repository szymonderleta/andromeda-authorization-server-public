package pl.derleta.authorization.domain.model;

import java.io.Serializable;

public record UserRole(long userRoleId, User user, Role role) implements Serializable {

}
