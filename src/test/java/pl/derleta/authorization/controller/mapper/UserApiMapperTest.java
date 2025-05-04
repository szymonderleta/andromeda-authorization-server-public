package pl.derleta.authorization.controller.mapper;

import org.junit.jupiter.api.Test;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.request.UserRegistrationRequest;
import pl.derleta.authorization.domain.response.UserResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserApiMapperTest {

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        List<UserEntity> userEntities = List.of(
                new UserEntity(1L, "user1", "user1@example.com", "pass1"),
                new UserEntity(2L, "user2", "user2@example.com", "pass2")
        );

        // Act
        List<User> users = UserApiMapper.toUsers(userEntities);

        // Assert
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals(1L, users.getFirst().userId());
        assertEquals("user1", users.getFirst().username());
        assertEquals("user1@example.com", users.get(0).email());
        assertEquals("pass1", users.get(0).password());
        assertEquals(2L, users.get(1).userId());
        assertEquals("user2", users.get(1).username());
        assertEquals("user2@example.com", users.get(1).email());
        assertEquals("pass2", users.get(1).password());
    }

    @Test
    void getPage_withEmptyList_shouldReturnCorrectResult() {
        // Arrange
        List<UserEntity> userEntities = List.of();

        // Act
        List<User> users = UserApiMapper.toUsers(userEntities);

        // Assert
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void getPage_withNullList_shouldReturnCorrectResult() {
        // Act
        List<User> users = UserApiMapper.toUsers(null);

        // Assert
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void getPage_withEntitiesHavingNullFields_shouldReturnCorrectResult() {
        // Arrange
        List<UserEntity> userEntities = List.of(
                new UserEntity(0L, null, null, null),
                new UserEntity(2L, "user2", null, "pass2")
        );

        // Act
        List<User> users = UserApiMapper.toUsers(userEntities);

        // Assert
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals(0L, users.getFirst().userId());
        assertNull(users.getFirst().username());
        assertNull(users.get(0).email());
        assertNull(users.get(0).password());
        assertEquals(2L, users.get(1).userId());
        assertEquals("user2", users.get(1).username());
        assertNull(users.get(1).email());
        assertEquals("pass2", users.get(1).password());
    }

    @Test
    void getPage_withValidEntity_shouldReturnCorrectResult() {
        // Arrange
        UserEntity userEntity = new UserEntity(1L, "testUser", "testEmail@example.com", "testPassword");

        // Act
        User user = UserApiMapper.toUser(userEntity);

        // Assert
        assertNotNull(user);
        assertEquals(1L, user.userId());
        assertEquals("testUser", user.username());
        assertEquals("testEmail@example.com", user.email());
        assertEquals("testPassword", user.password());
    }

    @Test
    void getPage_withNullEntity_shouldReturnCorrectResult() {
        // Act
        User user = UserApiMapper.toUser(null);

        // Assert
        assertNull(user);
    }

    @Test
    void getPage_withEntityHavingEmptyFields_shouldReturnCorrectResult() {
        // Arrange
        UserEntity userEntity = new UserEntity(0L, "", "", "");

        // Act
        User user = UserApiMapper.toUser(userEntity);

        // Assert
        assertNotNull(user);
        assertEquals(0L, user.userId());
        assertEquals("", user.username());
        assertEquals("", user.email());
        assertEquals("", user.password());
    }


    @Test
    void toUser_withValidRegistrationRequest_shouldReturnCorrectResult() {
        // Arrange
        long userId = 10L;
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest("newUser", "newPassword", "newUser@example.com");

        // Act
        User user = UserApiMapper.toUser(userId, registrationRequest);

        // Assert
        assertNotNull(user);
        assertEquals(userId, user.userId());
        assertEquals("newUser", user.username());
        assertEquals("newPassword", user.password());
        assertEquals("newUser@example.com", user.email());
    }

    @Test
    void toUser_withNullRegistrationRequest_shouldReturnNull() {
        // Act
        User user = UserApiMapper.toUser(10L, null);

        // Assert
        assertNull(user);
    }

    @Test
    void toUser_withEmptyFieldsInRegistrationRequest_shouldReturnCorrectResult() {
        // Arrange
        long userId = 10L;
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest("", "", "");

        // Act
        User user = UserApiMapper.toUser(userId, registrationRequest);

        // Assert
        assertNotNull(user);
        assertEquals(userId, user.userId());
        assertEquals("", user.username());
        assertEquals("", user.password());
        assertEquals("", user.email());
    }

    @Test
    void getUserResponse_withValidUser_shouldReturnCorrectResult() {
        // Arrange
        User user = new User(1L, "testUser", "testPassword", "testEmail@example.com");

        // Act
        UserResponse userResponse = UserApiMapper.toUserResponse(user);

        // Assert
        assertNotNull(userResponse);
        assertEquals(1L, userResponse.getUserId());
        assertEquals("testUser", userResponse.getUsername());
        assertEquals("testEmail@example.com", userResponse.getEmail());
    }

    @Test
    void getUserResponse_withNullUser_shouldReturnNull() {
        // Act
        UserResponse userResponse = UserApiMapper.toUserResponse(null);

        // Assert
        assertNull(userResponse);
    }

    @Test
    void getUserResponse_withUserHavingEmptyFields_shouldReturnEmptyFields() {
        // Arrange
        User user = new User(0L, "", "", "");

        // Act
        UserResponse userResponse = UserApiMapper.toUserResponse(user);

        // Assert
        assertNotNull(userResponse);
        assertEquals(0L, userResponse.getUserId());
        assertEquals("", userResponse.getUsername());
        assertEquals("", userResponse.getEmail());
    }

}
