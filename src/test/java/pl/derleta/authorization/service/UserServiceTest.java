package pl.derleta.authorization.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.repository.impl.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void getPage_withValidParams_shouldReturnExpectedResults() {
        // Arrange
        int page = 0, size = 5;
        String sortBy = "username";
        String sortOrder = "asc";
        String usernameFilter = "testUser";
        String emailFilter = "test@example.com";

        List<UserEntity> mockEntities = List.of(
                new UserEntity(3L, "testUser3", "test3@example.com", "password3"),
                new UserEntity(4L, "testUser4", "test4@example.com", "password4")
        );

        when(userRepository.getSortedPageWithFilters(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockEntities);
        when(userRepository.getFiltersCount(anyString(), anyString())).thenReturn(10L);

        // Act
        Page<User> result = userService.getPage(page, size, sortBy, sortOrder, usernameFilter, emailFilter);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals("testUser3", result.getContent().get(0).username());
        assertEquals("testUser4", result.getContent().get(1).username());
        assertTrue(result.getContent().stream().allMatch(user -> user.email().contains("test")));
        assertEquals(mockEntities.size(), result.getTotalElements());

        verify(userRepository).getSortedPageWithFilters(0, size, "username", "ASC", usernameFilter, emailFilter);
        verify(userRepository).getFiltersCount(usernameFilter, emailFilter);
    }

    @Test
    void getPage_withEmptyResult_shouldReturnNoResults() {
        // Arrange
        int page = 0, size = 5;
        String sortBy = "username";
        String sortOrder = "asc";
        String usernameFilter = "nonexistent";
        String emailFilter = "nonexistent@example.com";

        when(userRepository.getSortedPageWithFilters(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        when(userRepository.getFiltersCount(anyString(), anyString())).thenReturn(0L);

        // Act
        Page<User> result = userService.getPage(page, size, sortBy, sortOrder, usernameFilter, emailFilter);

        // Assert
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(userRepository).getSortedPageWithFilters(0, size, "username", "ASC", usernameFilter, emailFilter);
        verify(userRepository).getFiltersCount(usernameFilter, emailFilter);
    }

    @Test
    void getPage_withDifferentSortOrder_shouldReturnResultsSortedByEmailDesc() {
        // Arrange
        int page = 0, size = 5;
        String sortBy = "email";
        String sortOrder = "desc";
        String usernameFilter = "testUser";
        String emailFilter = "test@example.com";

        List<UserEntity> mockEntities = List.of(
                new UserEntity(3L, "testUser3", "test3@example.com", "password3"),
                new UserEntity(4L, "testUser4", "test4@example.com", "password4")
        );

        when(userRepository.getSortedPageWithFilters(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockEntities);
        when(userRepository.getFiltersCount(anyString(), anyString())).thenReturn(10L);

        // Act
        Page<User> result = userService.getPage(page, size, sortBy, sortOrder, usernameFilter, emailFilter);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals("testUser3", result.getContent().get(0).username());
        assertEquals("testUser4", result.getContent().get(1).username());
        assertTrue(result.getContent().stream().allMatch(user -> user.email().contains("test")));
        assertEquals(mockEntities.size(), result.getTotalElements());

        verify(userRepository).getSortedPageWithFilters(0, size, "email", "DESC", usernameFilter, emailFilter);
        verify(userRepository).getFiltersCount(usernameFilter, emailFilter);
    }

    @Test
    void get_withValidUserId_shouldReturnCorrectUser() {
        // Arrange
        long userId = 1L;
        UserEntity mockEntity = new UserEntity(userId, "testUser1", "test1@example.com", "password1");

        when(userRepository.findById(userId)).thenReturn(mockEntity);

        // Act
        User result = userService.get(userId);

        // Assert
        assertEquals(userId, result.userId());
        assertEquals("testUser1", result.username());
        assertEquals("test1@example.com", result.email());

        verify(userRepository).findById(userId);
    }

    @Test
    void get_withInvalidUserId_shouldReturnNull() {
        // Arrange
        long userId = -1L;

        when(userRepository.findById(userId)).thenReturn(null);

        // Act
        User result = userService.get(userId);

        // Assert
        assertNull(result);

        verify(userRepository).findById(userId);
    }

    @Test
    void save_withValidUserDetails_shouldSaveAndReturnUser() {
        // Arrange
        String username = "newUser";
        String email = "newuser@example.com";
        String password = "password";

        User userToSave = new User(0L, username, password, email);
        UserEntity savedEntity = new UserEntity(100L, username, email, password);

        when(userRepository.getNextUserId()).thenReturn(100L);
        when(userRepository.save(anyLong(), any(User.class))).thenReturn(1);
        when(userRepository.findById(100L)).thenReturn(savedEntity);

        // Act
        User result = userService.save(userToSave);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.userId());
        assertEquals(username, result.username());
        assertEquals(email, result.email());

        verify(userRepository).getNextUserId();
        verify(userRepository).save(100L, userToSave);
        verify(userRepository).findById(100L);
    }

    @Test
    void save_withFailure_shouldThrowException() {
        // Arrange
        String username = "newUser";
        String email = "newuser@example.com";
        String password = "password";

        User userToSave = new User(0L, username, password, email);

        when(userRepository.getNextUserId()).thenReturn(200L);
        doThrow(new RuntimeException("Save failed"))
                .when(userRepository).save(Mockito.eq(200L), Mockito.any(User.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.save(userToSave));
        assertEquals("Save failed", exception.getMessage());

        verify(userRepository, Mockito.times(1)).getNextUserId();
        verify(userRepository, Mockito.times(1)).save(200L, userToSave);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void update_withValidUserId_shouldUpdateAndReturnUser() {
        // Arrange
        long userId = 1L;
        User updatedUser = new User(userId, "updatedUser", "newPassword", "updated@example.com");
        UserEntity existingEntity = new UserEntity(userId, "testUser1", "test1@example.com", "password1");
        UserEntity updatedEntity = new UserEntity(userId, "updatedUser", "updated@example.com", "newPassword");

        when(userRepository.findById(userId))
                .thenReturn(existingEntity)
                .thenReturn(updatedEntity);

        when(userRepository.update(userId, updatedUser)).thenReturn(1);

        // Act
        User result = userService.update(userId, updatedUser);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.userId());
        assertEquals("updatedUser", result.username());
        assertEquals("updated@example.com", result.email());

        // Verify interactions
        verify(userRepository, Mockito.times(2)).findById(userId);
        verify(userRepository, Mockito.times(1)).update(userId, updatedUser);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void update_withInvalidUserId_shouldNotUpdateAndReturnNull() {
        // Arrange
        long userId = -1L;
        User updatedUser = new User(userId, "updatedUser", "newPassword", "updated@example.com");

        when(userRepository.findById(userId)).thenReturn(null);

        // Act
        User result = userService.update(userId, updatedUser);

        // Assert
        assertNull(result);

        verify(userRepository, Mockito.times(1)).findById(userId);
        verify(userRepository, Mockito.never()).update(anyLong(), any(User.class));
    }

    @Test
    void update_withFailure_shouldThrowException() {
        // Arrange
        long userId = 1L;
        User updatedUser = new User(userId, "updatedUser", "newPassword", "updated@example.com");
        UserEntity existingEntity = new UserEntity(userId, "testUser1", "test1@example.com", "password1");

        when(userRepository.findById(userId)).thenReturn(existingEntity);
        doThrow(new RuntimeException("Update failed"))
                .when(userRepository).update(userId, updatedUser);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.update(userId, updatedUser));
        assertEquals("Update failed", exception.getMessage());

        verify(userRepository, Mockito.times(1)).findById(userId);
        verify(userRepository, Mockito.times(1)).update(userId, updatedUser);
    }

    @Test
    void delete_withValidUserId_shouldDeleteAndReturnTrue() {
        // Arrange
        long userId = 1L;
        UserEntity existingEntity = new UserEntity(userId, "testUser1", "test1@example.com", "password1");

        when(userRepository.findById(userId)).thenReturn(existingEntity);

        // Act
        boolean result = userService.delete(userId);

        // Assert
        assertTrue(result);
        verify(userRepository, Mockito.times(1)).findById(userId);
        verify(userRepository, Mockito.times(1)).deleteById(userId);
    }

    @Test
    void delete_withInvalidUserId_shouldNotDeleteAndReturnFalse() {
        // Arrange
        long userId = -1L;

        when(userRepository.findById(userId)).thenReturn(null);

        // Act
        boolean result = userService.delete(userId);

        // Assert
        assertFalse(result);
        verify(userRepository, Mockito.times(1)).findById(userId);
        verify(userRepository, Mockito.never()).deleteById(anyLong());
    }

    @Test
    void delete_withFailure_shouldThrowException() {
        // Arrange
        long userId = 1L;
        UserEntity existingEntity = new UserEntity(userId, "testUser1", "test1@example.com", "password1");

        when(userRepository.findById(userId)).thenReturn(existingEntity);
        doThrow(new RuntimeException("Delete failed"))
                .when(userRepository).deleteById(userId);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.delete(userId));
        assertEquals("Delete failed", exception.getMessage());

        // Verify interactions
        verify(userRepository, Mockito.times(1)).findById(userId);
        verify(userRepository, Mockito.times(1)).deleteById(userId);
    }

}
