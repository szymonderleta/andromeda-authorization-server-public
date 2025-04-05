package pl.derleta.authorization.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import pl.derleta.authorization.controller.mapper.UserApiMapper;
import pl.derleta.authorization.domain.entity.UserEntity;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.repository.impl.UserRepository;

import java.util.List;

/**
 * Service class for managing user-related operations.
 * This class provides methods for retrieving, saving, updating, and deleting user entities,
 * as well as retrieving paginated subsets of user data with sorting and filtering capabilities.
 */
@Service
public class UserService {

    private UserRepository repository;

    @Autowired
    public void setRepository(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves a paginated and optionally filtered list of users.
     * The method applies sorting and filtering criteria to fetch the required subset of users.
     *
     * @param page           the page number to retrieve (0-based index)
     * @param size           the number of users per page
     * @param sortBy         the field by which to sort the users (e.g., "username", "email")
     * @param sortOrder      the sort order, either "asc" for ascending or "desc" for descending
     * @param usernameFilter a filter applied to the username field
     * @param emailFilter    a filter applied to the email field
     * @return a page containing a list of users matching the specified criteria
     */
    public Page<User> getPage(final int page, final int size, final String sortBy, final String sortOrder, final String usernameFilter, final String emailFilter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
        int offset = page * size;
        String sortByParam = getSortByParam(sortBy);
        String sortOrderParam = sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        List<User> collection = UserApiMapper.toUsers(
                repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, usernameFilter, emailFilter)
        );
        long filteredColSize = repository.getFiltersCount(usernameFilter, emailFilter);
        return PageableExecutionUtils.getPage(collection, pageable, () -> filteredColSize);
    }

    /**
     * Retrieves a User object based on the provided user ID.
     *
     * @param userId the unique identifier of the user to be retrieved
     * @return a User object corresponding to the provided user ID, or null if no such user exists
     */
    public User get(final long userId) {
        UserEntity entity = repository.findById(userId);
        if (entity == null || entity.getUserId() <= 0) {
            return null;
        }
        return UserApiMapper.toUser(entity);
    }

    /**
     * Saves a user to the repository and retrieves the saved user.
     * This method assigns a new unique ID to the user, persists the user in the repository,
     * and then retrieves the saved user details using the assigned ID.
     *
     * @param user the user object to be saved
     * @return the saved user object with assigned ID
     */
    public User save(User user) {
        long userId = repository.getNextUserId();
        repository.save(userId, user);
        return this.get(userId);
    }

    /**
     * Updates a user's details in the repository based on the provided user ID and user information.
     * If the user exists and has a valid ID, their details are updated and the updated user is returned.
     * If the user does not exist, null is returned.
     *
     * @param userId the ID of the user to be updated
     * @param user   the user information to be updated
     * @return the updated user object if successful, or null if the user was not found
     */
    public User update(final long userId, final User user) {
        UserEntity entity = repository.findById(userId);
        if (entity != null && entity.getUserId() > 0) {
            repository.update(userId, user);
            return this.get(userId);
        }
        return null;
    }

    /**
     * Deletes a user with the specified user ID from the repository.
     * If the user exists and has a valid ID, the user is removed.
     *
     * @param userId the ID of the user to be deleted
     * @return true if the user was successfully deleted, false if the user was not found or could not be deleted
     */
    public boolean delete(final long userId) {
        UserEntity entity = repository.findById(userId);
        if (entity != null && entity.getUserId() > 0) {
            repository.deleteById(userId);
            return true;
        }
        return false;
    }

    /**
     * Determines the appropriate parameter for sorting based on the provided sort key.
     * If the provided sort key matches "username" or "email" (case-insensitive),
     * it returns the corresponding value. Otherwise, it defaults to "user_id".
     *
     * @param sortBy the sorting key requested, such as "username", "email", or another value.
     * @return the corresponding parameter to be used for sorting; "username", "email", or "user_id".
     */
    private String getSortByParam(String sortBy) {
        if ("username".equalsIgnoreCase(sortBy)) return "username";
        else if ("email".equalsIgnoreCase(sortBy)) return "email";
        return "user_id";
    }

}
