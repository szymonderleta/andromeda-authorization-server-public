package pl.derleta.authorization.domain.request;

public record ChangePasswordRequest(long userId, String email, String actualPassword,
                                    String newPassword) implements Request {

}
