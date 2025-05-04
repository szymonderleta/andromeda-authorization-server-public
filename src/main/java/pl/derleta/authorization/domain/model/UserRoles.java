package pl.derleta.authorization.domain.model;

import java.io.Serializable;
import java.util.Set;

public record UserRoles(User user, Set<Role> roles) implements Serializable {

}
