package pl.derleta.authorization.config.security.api;

public class ObjectNotSavedException extends RuntimeException {

    public ObjectNotSavedException(String message) {
        super(message);
    }

}
