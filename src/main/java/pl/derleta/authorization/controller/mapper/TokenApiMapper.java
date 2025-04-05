package pl.derleta.authorization.controller.mapper;

import pl.derleta.authorization.domain.builder.impl.AccessTokenBuilderImpl;
import pl.derleta.authorization.domain.builder.impl.ConfirmationTokenBuilderImpl;
import pl.derleta.authorization.domain.builder.impl.RefreshTokenBuilderImpl;
import pl.derleta.authorization.domain.entity.token.TokenEntity;
import pl.derleta.authorization.domain.model.AccessToken;
import pl.derleta.authorization.domain.model.ConfirmationToken;
import pl.derleta.authorization.domain.model.RefreshToken;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Utility class for mapping {@link TokenEntity} objects to various token representations such as
 * {@link ConfirmationToken}, {@link AccessToken}, and {@link RefreshToken}.
 * This class provides static methods to handle the conversion process, ensuring consistent mapping
 * across the application.
 * <p>
 * This class is not instantiable.
 */
public final class TokenApiMapper {

    private TokenApiMapper() {
    }

    /**
     * Converts a list of {@link TokenEntity} objects into a list of {@link ConfirmationToken} objects.
     * This method maps each {@link TokenEntity} in the input list to its corresponding {@link ConfirmationToken}
     * using the {@link TokenApiMapper#toConfirmationToken(TokenEntity)} method.
     *
     * @param entities the list of {@link TokenEntity} objects to be converted
     * @return a list of {@link ConfirmationToken} objects corresponding to the input list of {@link TokenEntity} objects
     */
    public static List<ConfirmationToken> toConfirmationTokens(final List<? extends TokenEntity> entities) {
        return entities.stream().map(TokenApiMapper::toConfirmationToken).collect(Collectors.toList());
    }

    /**
     * Converts a {@link TokenEntity} object into a {@link ConfirmationToken} object.
     * This method uses a builder pattern to map the properties from the given {@link TokenEntity}
     * to the corresponding fields of a {@link ConfirmationToken}.
     *
     * @param entity the {@link TokenEntity} to be converted, containing details such as token ID, user, token string,
     *               and expiration date
     * @return a {@link ConfirmationToken} object populated with the mapped data from the provided {@link TokenEntity}
     */
    public static ConfirmationToken toConfirmationToken(final TokenEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ConfirmationTokenBuilderImpl()
                .tokenId(entity.getTokenId())
                .user(UserApiMapper.toUser(entity.getUser()))
                .token(entity.getToken())
                .expirationDate(entity.getExpirationDate())
                .build();
    }

    /**
     * Converts a list of {@link TokenEntity} objects into a list of {@link AccessToken} objects.
     * Each {@link TokenEntity} in the input list is transformed into an {@link AccessToken}
     * using the {@link TokenApiMapper#toAccessToken(TokenEntity)} method.
     *
     * @param entities the list of {@link TokenEntity} objects to be converted
     * @return a list of {@link AccessToken} objects corresponding to the input list of {@link TokenEntity} objects
     */
    public static List<AccessToken> toAccessTokens(final List<? extends TokenEntity> entities) {
        return entities.stream().map(TokenApiMapper::toAccessToken).collect(Collectors.toList());
    }

    /**
     * Converts a {@link TokenEntity} object into an {@link AccessToken} object.
     * This method uses a builder pattern to map the properties from the provided {@link TokenEntity}
     * to the corresponding fields of an {@link AccessToken}.
     *
     * @param entity the {@link TokenEntity} object containing token details such as token ID, user, token string,
     *               and expiration date, which are used to construct an {@link AccessToken} instance
     * @return an {@link AccessToken} object populated with data from the provided {@link TokenEntity}
     */
    public static AccessToken toAccessToken(final TokenEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AccessTokenBuilderImpl()
                .tokenId(entity.getTokenId())
                .user(UserApiMapper.toUser(entity.getUser()))
                .token(entity.getToken())
                .expirationDate(entity.getExpirationDate())
                .build();
    }

    /**
     * Converts a list of {@link TokenEntity} objects into a list of {@link RefreshToken} objects.
     * Each {@link TokenEntity} in the input list is transformed into a {@link RefreshToken}
     * using the {@link TokenApiMapper#toRefreshToken(TokenEntity)} method.
     *
     * @param entities the list of {@link TokenEntity} objects to be converted
     * @return a list of {@link RefreshToken} objects corresponding to the input list of {@link TokenEntity} objects
     */
    public static List<RefreshToken> toRefreshTokens(final List<? extends TokenEntity> entities) {
        return entities.stream().map(TokenApiMapper::toRefreshToken).collect(Collectors.toList());
    }

    /**
     * Converts a {@link TokenEntity} object into a {@link RefreshToken} object.
     * This method utilizes a builder pattern for the creation of the {@link RefreshToken}.
     * It maps the properties from the provided {@link TokenEntity} to the corresponding fields in the {@link RefreshToken}.
     *
     * @param entity the {@link TokenEntity} object containing token details, such as token ID, user, token,
     *               and expiration date, to be mapped to a {@link RefreshToken}
     * @return a {@link RefreshToken} object populated with data from the given {@link TokenEntity}
     */
    public static RefreshToken toRefreshToken(final TokenEntity entity) {
        if (entity == null) {
            return null;
        }
        return new RefreshTokenBuilderImpl()
                .tokenId(entity.getTokenId())
                .user(UserApiMapper.toUser(entity.getUser()))
                .token(entity.getToken())
                .expirationDate(entity.getExpirationDate())
                .build();
    }

}
