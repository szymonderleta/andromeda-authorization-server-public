package pl.derleta.authorization.domain.request;

public record UserRegistrationRequest(String username, String password, String email) implements Request {

}
