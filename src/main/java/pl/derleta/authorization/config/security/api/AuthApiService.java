package pl.derleta.authorization.config.security.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.derleta.authorization.config.model.UserSecurity;
import pl.derleta.authorization.config.model.UserSecurityMapper;
import pl.derleta.authorization.config.security.jwt.JwtTokenUtil;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.repository.impl.UserRepository;
import pl.derleta.authorization.repository.impl.UserRolesRepository;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Service class for managing authentication-related operations.
 * This service interacts with the {@code AuthApiRepository} to perform
 * operations such as saving tokens and validating them against the stored data.
 */
@Service
public class AuthApiService {

    private JwtTokenUtil jwtTokenUtil;
    private UserRepository userRepository;
    private UserRolesRepository userRolesRepository;

    private AuthApiRepository repository;

    @Autowired
    public void setRepository(AuthApiRepository repository, JwtTokenUtil jwtTokenUtil, UserRepository userRepository, UserRolesRepository userRolesRepository) {
        this.repository = repository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
        this.userRolesRepository = userRolesRepository;
    }


    /**
     * Saves an access token for a specified user with an associated expiration date.
     * This method generates a unique token ID, stores the token in the repository,
     * and validates that the token was saved successfully. If the token is not
     * successfully saved, an exception is thrown.
     *
     * @param userId         the unique identifier of the user for whom the access token will be saved
     * @param token          the access token string to be stored
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
     * @param userId         the unique identifier of the user for whom the refresh token will be saved
     * @param token          the refresh token string to be stored
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
     * @param tokenId  the unique identifier of the access token to validate
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
     * @param tokenId  the unique identifier of the refresh token to validate
     * @param expected the expected value of the refresh token
     * @return true if the refresh token matches the expected value (case-insensitive), false otherwise
     */
    public boolean isValidRefreshToken(final long tokenId, final String expected) {
        String found = repository.findRefreshTokenById(tokenId).orElse(null);
        if (found == null) return false;
        return found.equalsIgnoreCase(expected);
    }

    /**
     * Updates the access token for a given user by generating a new JWT and saving it to the database.
     * <p>
     * This method performs the following steps:
     * <ul>
     *   <li>Validates the user ID is not null.</li>
     *   <li>Fetches the user entity from the database.</li>
     *   <li>Retrieves the user's roles and maps them to a {@link UserSecurity} object.</li>
     *   <li>Generates a new access token using the {@link JwtTokenUtil}.</li>
     *   <li>Retrieves the token expiration date.</li>
     *   <li>Gets the next token ID and stores the token in the database.</li>
     * </ul>
     *
     * @param userId the ID of the user for whom to update the access token
     * @return the newly generated access token as a {@link String}, or {@code null} if {@code userId} is null
     */
    public String updateAccessToken(final Long userId) {
        if (userId == null) return null;
        UserEntity userEntity = userRepository.findById(userId);
        List<RoleEntity> roles = userRolesRepository.getRoles(userId);
        UserSecurity userSecurity = UserSecurityMapper.toUserSecurity(userEntity, new HashSet<>(roles));

        String accessToken = jwtTokenUtil.generateAccessToken(userSecurity);
        Date expirationDate = jwtTokenUtil.getTokenExpiration(accessToken);
        long nextTokenId = repository.getAccessTokenNextId();
        repository.saveAccessToken(nextTokenId, userId, accessToken, expirationDate);
        return accessToken;
    }

}



