package pl.derleta.authorization.config.security.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;

/**
 * Service class for managing authentication-related operations.
 * This service interacts with the {@code AuthApiRepository} to perform
 * operations such as saving tokens and validating them against the stored data.
 */
@Service
public class AuthApiService {

    private AuthApiRepository repository;

    @Autowired
    public void setRepository(AuthApiRepository repository) {
        this.repository = repository;
    }


    /**
     * Saves an access token for a specified user with an associated expiration date.
     * This method generates a unique token ID, stores the token in the repository,
     * and validates that the token was saved successfully. If the token is not
     * successfully saved, an exception is thrown.
     *
     * @param userId the unique identifier of the user for whom the access token will be saved
     * @param token the access token string to be stored
     * @param expirationDate the expiration date of the access token
     * @return true if the access token is successfully saved and validated
     * @throws ObjectNotSavedException if the access token could not be saved in the database
     */
    public boolean saveAccessToken(final long userId, final String token, final Date expirationDate) {
        long tokenId = repository.getAccessTokenNextId();
        repository.saveAccessToken(tokenId, userId, token, expirationDate);
        boolean result = this.isValidAccessToken(tokenId, token);
        if (!result) throw new ObjectNotSavedException("Token not saved in database");
        return true;
    }

    /**
     * Saves a refresh token for a specified user with an associated expiration date.
     * This method generates a unique token ID, stores the token in the repository,
     * and validates that the token was saved successfully. If the token is not
     * successfully saved, an exception is thrown.
     *
     * @param userId the unique identifier of the user for whom the refresh token will be saved
     * @param token the refresh token string to be stored
     * @param expirationDate the expiration date of the refresh token
     * @return true if the refresh token is successfully saved and validated
     * @throws ObjectNotSavedException if the refresh token could not be saved in the database
     */
    public boolean saveRefreshToken(final long userId, final String token, final Date expirationDate) {
        long tokenId = repository.getRefreshTokenNextId();
        repository.saveRefreshToken(tokenId, userId, token, expirationDate);
        boolean result = this.isValidRefreshToken(tokenId, token);
        if (!result) throw new ObjectNotSavedException("Token not saved in database");
        return true;
    }

    /**
     * Validates whether an access token identified by its ID matches the expected value.
     * This method checks the access token stored in the database and compares it
     * with the provided value, ignoring case differences.
     *
     * @param tokenId the unique identifier of the access token to validate
     * @param expected the expected value of the access token
     * @return true if the access token matches the expected value (case-insensitive), false otherwise
     */
    public boolean isValidAccessToken(final long tokenId, final String expected) {
        String found = repository.findAccessTokenById(tokenId).orElse(null);
        if (found == null) return false;
        return found.equalsIgnoreCase(expected);
    }

    /**
     * Validates whether a refresh token identified by its ID matches the expected value.
     * This method retrieves the refresh token from the repository and compares it
     * with the provided value, ignoring case differences.
     *
     * @param tokenId the unique identifier of the refresh token to validate
     * @param expected the expected value of the refresh token
     * @return true if the refresh token matches the expected value (case-insensitive), false otherwise
     */
    public boolean isValidRefreshToken(final long tokenId, final String expected) {
        String found = repository.findRefreshTokenById(tokenId).orElse(null);
        if (found == null) return false;
        return found.equalsIgnoreCase(expected);
    }

}



