package pl.derleta.authorization.domain.model;

import java.io.Serializable;

public record User(long userId, String username, String password, String email) implements Serializable {

}
