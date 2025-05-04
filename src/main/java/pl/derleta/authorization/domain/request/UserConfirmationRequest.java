package pl.derleta.authorization.domain.request;

public record UserConfirmationRequest(Long tokenId, String token) implements Request {

}
