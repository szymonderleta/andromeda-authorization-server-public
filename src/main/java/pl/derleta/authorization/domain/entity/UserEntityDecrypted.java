package pl.derleta.authorization.domain.entity;

/**
 * The UserEntityDecrypted class extends the UserEntity class,
 * representing a version of the user entity that includes a
 * decrypted password value. This is useful in scenarios where
 * the decrypted password is required in addition to the standard
 * user information.
 * <p>
 * This class is immutable to ensure security and avoid accidental
 * modifications of the decrypted password.
 * <p>
 * This class is used only in operation to exchange decrypted password for generation new password process
 */
public final class UserEntityDecrypted extends UserEntity {

    private final String decryptedPassword;

    public UserEntityDecrypted(UserEntity userEntity, String decryptedPassword) {
        super(userEntity.getUserId(), userEntity.getUsername(), userEntity.getEmail(), userEntity.getPassword());
        this.decryptedPassword = decryptedPassword;
    }

    public UserEntityDecrypted(String decryptedPassword) {
        this.decryptedPassword = decryptedPassword;
    }

    public String getDecryptedPassword() {
        return decryptedPassword;
    }

}
