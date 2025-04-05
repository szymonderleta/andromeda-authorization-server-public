package pl.derleta.authorization.controller.mapper;

import pl.derleta.authorization.domain.builder.impl.UserBuilderImpl;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.request.UserRegistrationRequest;
import pl.derleta.authorization.domain.response.UserResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class that provides methods for mapping between various User-related models and entities.
 * This class contains static methods for converting between {@link UserEntity}, {@link User},
 * {@link UserResponse}, and handling user registration data.
 */
public final class UserApiMapper {

    private UserApiMapper() {
    }

    /**
     * Converts a list of {@link UserEntity} objects to a list of {@link User} objects.
     *
     * @param users the list of {@link UserEntity} objects to be converted
     * @return a list of {@link User} objects corresponding to the input entities
     */
    public static List<User> toUsers(final List<UserEntity> users) {
        if (users == null) return List.of();
        return users.stream().map(UserApiMapper::toUser).collect(Collectors.toList());
    }

    /**
     * Converts a {@link UserEntity} object to a {@link User} object.
     *
     * @param entity the {@link UserEntity} object to be converted
     * @return a {@link User} object containing the data mapped from the given {@link UserEntity}
     */
    public static User toUser(final UserEntity entity) {
        if (entity == null) return null;
        return new UserBuilderImpl()
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .password(entity.getPassword())
                .email(entity.getEmail())
                .build();
    }

    /**
     * Converts a {@link User} object to a {@link UserResponse} object.
     *
     * @param user the {@link User} object to be converted
     * @return a {@link UserResponse} object containing the mapped data from the given {@link User}
     */
    public static UserResponse toUserResponse(final User user) {
        if (user == null) return null;
        return new UserResponse(
                user.userId(),
                user.username(),
                user.email()
        );
    }

    /**
     * Converts the given user ID and registration request data into a {@link User} object.
     *
     * @param userId           the ID of the user to be assigned
     * @param userRegistration the registration request containing the user's username, password, and email
     * @return a {@link User} instance with the provided user ID and data from the registration request
     */
    public static User toUser(final long userId, UserRegistrationRequest userRegistration) {
        if (userRegistration == null) return null;
        return new UserBuilderImpl().userId(userId)
                .email(userRegistration.email())
                .password(userRegistration.password())
                .username(userRegistration.username())
                .build();
    }

}
