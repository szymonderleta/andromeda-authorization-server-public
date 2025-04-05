package pl.derleta.authorization.repository.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    void getSize_withExpectedSize_shouldReturnCorrectCount() {
        // Arrange
        final int size = 18;

        // Act
        var count = repository.getSize();

        // Assert
        assertEquals(size, count);
    }

    @Test
    void getPage_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        List<UserEntity> expectedUsers = List.of(
                new UserEntity(1L, "tester", "test@test.com", "$2a$10$j2qFOKGE74htRbuVEGBYQ.OPinr8fsHS2iuWsfnj1jch2W6JSHmcZjiZH9"),
                new UserEntity(2L, "carlos", "non_existed@gmail.com", "$2y$10$/Ej2qFOKka5m0VEJno81.vEBYQ.bZGXgVmk7brNI.uxAGNIzZM6JSH9eo6")
        );

        // Act
        List<UserEntity> returnedUsers = repository.getPage(0, 2);

        // Assert
        assertEquals(expectedUsers.size(), returnedUsers.size());
        assertThat(returnedUsers).isEqualTo(expectedUsers);
    }

    @Test
    void findById_withValidId_shouldReturnCorrectUser() {
        // Arrange
        UserEntity expectedUser = new UserEntity(1L, "tester", "test@test.com", "$2a$10$j2qFOKGE74htRbuVEGBYQ.OPinr8fsHS2iuWsfnj1jch2W6JSHmcZjiZH9");

        // Act
        UserEntity user = repository.findById(1L);

        // Assert
        assertNotNull(user);
        assertEquals(expectedUser, user);
    }

    @Test
    void findById_withInvalidId_shouldReturnNull() {
        // Arrange

        // Act
        UserEntity user = repository.findById(12345991L);

        // Assert
        assertNull(user);
    }

    @Test
    void findByEmail_withValidEmail_shouldReturnCorrectUser() {
        // Arrange
        UserEntity expectedUser = new UserEntity(2L, "carlos", "non_existed@gmail.com", "$2y$10$/Ej2qFOKka5m0VEJno81.vEBYQ.bZGXgVmk7brNI.uxAGNIzZM6JSH9eo6");

        // Act
        UserEntity user = repository.findByEmail("non_existed@gmail.com");

        // Assert
        assertNotNull(user);
        assertEquals(expectedUser, user);
    }

    @Test
    void findByEmail_withNullEmail_shouldReturnNull() {
        // Arrange

        // Act
        UserEntity user = repository.findByEmail(null);

        // Assert
        assertNull(user);
    }

    @Test
    void isBlocked_withExistentUser_shouldReturnTrue() {
        // Arrange

        // Act
        Boolean result = repository.isBlocked(8);

        // Assert
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    void isBlocked_withNonExistentUser_shouldReturnNull() {
        // Arrange

        // Act
        Boolean result = repository.isBlocked(-23);

        // Assert
        assertNull(result);
    }

    @Test
    void isVerified_withExistentUser_shouldReturnTrue() {
        // Arrange

        // Act
        Boolean result = repository.isVerified(8);

        // Assert
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    void isVerified_withNonExistentUser_shouldReturnNull() {
        // Act
        Boolean result = repository.isVerified(-23);

        // Assert
        assertNull(result);
    }

    @Test
    void saveUpdateAndDeleteUser_withValidUser_shouldPerformCRUDOperationsCorrectly() {
        // Arrange
        long tempUserId = 9999;
        User temporarySaveUser = new User(tempUserId, "temporaryUser", "temporaryPass", "temp@temporary.com");
        User temporaryUpdateUser = new User(tempUserId, "updatedUser", "updatedPass", "up@updated.com");

        // Act
        int saveResult = repository.save(tempUserId, temporarySaveUser);
        UserEntity savedUser = repository.findById(tempUserId);
        int updateResult = repository.update(tempUserId, temporaryUpdateUser);
        UserEntity updatedUser = repository.findById(tempUserId);
        int deleteResult = repository.deleteById(tempUserId);
        UserEntity deletedUser = repository.findById(tempUserId);

        // Assert
        assertEquals(1, saveResult);
        assertEquals(1, updateResult);
        assertEquals(1, deleteResult);
        assertTrue(savedUser.getUsername().equals(temporarySaveUser.username()) && savedUser.getEmail().equals(temporarySaveUser.email()) && savedUser.getPassword().equals(temporarySaveUser.password()));
        assertTrue(updatedUser.getUsername().equals(temporaryUpdateUser.username()) && updatedUser.getEmail().equals(temporaryUpdateUser.email()) && updatedUser.getPassword().equals(temporaryUpdateUser.password()));
        assertNull(deletedUser);
    }

    @Test
    void saveExistedUser_withExistentUser_shouldReturnZero() {
        // Arrange
        long tempUserId = 999L;
        User temporarySaveUser = new User(tempUserId, "temporaryUser", "temporaryPass", "temp@temporary.com");

        // Act
        int result = repository.save(tempUserId, temporarySaveUser);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void updateNonExistentUser_withNonExistentUser_shouldReturnZero() {
        // Arrange
        long tempUserId = 1999L;
        User temporarySaveUser = new User(tempUserId, "updateNonExistedUser", "updateNonExistedUser", "up@nonexist.com");

        // Act
        int result = repository.update(tempUserId, temporarySaveUser);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void deleteById_withNonExistentUser_shouldReturnZero() {
        // Arrange
        long tempUserId = 1999L;

        // Act
        int result = repository.deleteById(tempUserId);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void getNextUserId_withValidCall_shouldReturnCorrectId() {
        // Arrange
        long excepted = 123456790;

        // Act
        long nextUserId = repository.getNextUserId();

        // Assert
        assertEquals(excepted, nextUserId);
    }

    @Test
    void getSortedPage_withValidParameters_shouldReturnCorrectResult() {
        // Arrange
        int offset = 0;
        int size = 5;
        String sortByParam = "username";
        String sortOrderParam = "ASC";
        String username = "test";
        String email = "@bcooq.com";

        // Act
        List<UserEntity> resultUsers = repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, username, email);

        // Assert
        assertEquals(2, resultUsers.size());
        assertEquals("test001", resultUsers.get(0).getUsername());
        assertEquals("test002", resultUsers.get(1).getUsername());
    }

    @Test
    void getSortedPageWithFilters_withValidParameters_shouldReturnZeroUsers() {
        // Arrange
        int offset = 0;
        int size = 5;
        String sortByParam = "username";
        String sortOrderParam = "ASC";
        String username = "tester";
        String email = "@bcooq.com";

        // Act
        List<UserEntity> resultUsers = repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, username, email);

        // Assert
        assertEquals(0, resultUsers.size());
    }

    @Test
    public void getSortedPageWithFilters_withInvalidSortBy_shouldThrowException() {
        // Arrange
        int offset = 0;
        int size = 5;
        String sortByParam = "userename";
        String sortOrderParam = "ASC";
        String username = "test";
        String email = "@bcooq.com";

        // Assert
        assertThatThrownBy(() ->
                // Act
                repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, username, email)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sortBy parameter");
    }

    @Test
    public void shouldThrowException_whenInvalidSortOrderOrSortByProvided() {
        // Arrange
        int offset = 0;
        int size = 5;
        String sortByParam = "username";
        String sortOrderParam = "ASIC";
        String username = "test";
        String email = "@bcooq.com";

        // Assert
        assertThatThrownBy(() ->
                // Act
                repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, username, email)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sortOrder parameter");
    }

    @Test
    void getFiltersCount_withMatchingFilters_shouldReturnCorrectCount() {
        // Arrange
        long excepted = 2;
        String username = "test";
        String email = "@bcooq.com";

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void getFiltersCount_withNoMatchingFilters_shouldReturnZero() {
        // Arrange
        long excepted = 0;
        String username = "tesiit";
        String email = "@bcooq.com";

        // Act
        long founded = repository.getFiltersCount(username, email);

        // Assert
        assertEquals(excepted, founded);
    }

    @Test
    void isEmailExist_withValidEmail_shouldReturnTrue() {
        // Arrange
        String email = "test@test.com";

        // Act
        boolean result = repository.isEmailExist(email);

        // Assert
        assertTrue(result);
    }

    @Test
    void isEmailExist_withInvalidEmail_shouldReturnFalse() {
        // Arrange
        String email = "testuu@test.com";

        // Act
        boolean result = repository.isEmailExist(email);

        // Assert
        assertFalse(result);
    }

    @Test
    void isEmailExist_withHalfEmail_shouldReturnFalse() {
        // Arrange
        String email = "@test.com";

        // Act
        boolean result = repository.isEmailExist(email);

        // Assert
        assertFalse(result);
    }

    @Test
    void isLoginExist_withValidLogin_shouldReturnTrue() {
        // Arrange
        String login = "tester";

        // Act
        boolean result = repository.isLoginExist(login);

        // Assert
        assertTrue(result);
    }

    @Test
    void isLoginExist_withInvalidLogin_shouldReturnFalse() {
        // Arrange
        String login = "testuu";

        // Act
        boolean result = repository.isLoginExist(login);

        // Assert
        assertFalse(result);
    }

    @Test
    void isLoginExist_withPartialLogin_shouldReturnFalse() {
        // Arrange
        String login = "te";

        // Act
        boolean result = repository.isLoginExist(login);

        // Assert
        assertFalse(result);
    }

    @Test
    public void isValidId_withExistingId_shouldReturnTrue() {
        // Arrange
        Long id = 1L;

        // Act
        boolean exists = repository.isValidId(id);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    public void isValidId_withNonExistingId_shouldReturnFalse() {
        // Arrange
        Long id = 123445L;

        // Act
        boolean exists = repository.isValidId(id);

        // Assert
        assertThat(exists).isFalse();
    }

}