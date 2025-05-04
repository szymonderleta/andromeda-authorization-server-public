package pl.derleta.authorization.service.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import pl.derleta.authorization.controller.mapper.TokenApiMapper;
import pl.derleta.authorization.domain.entity.token.TokenEntity;
import pl.derleta.authorization.domain.model.ConfirmationToken;
import pl.derleta.authorization.repository.impl.UserRepository;
import pl.derleta.authorization.repository.impl.token.ConfirmationTokenRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing confirmation tokens.
 * Provides functionality for retrieving, saving, and deleting confirmation tokens,
 * as well as pagination and sorting capabilities.
 */
@Service
public class ConfirmationTokenService {

    private ConfirmationTokenRepository repository;
    private UserRepository userRepository;

    @Autowired
    public void setRepository(ConfirmationTokenRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a paginated list of ConfirmationTokens based on the specified filters and sorting options.
     *
     * @param page           The current page number, zero-based.
     * @param size           The number of items per page.
     * @param sortBy         The field to sort by.
     * @param sortOrder      The sorting order, either "asc" for ascending or "desc" for descending.
     * @param usernameFilter The filter for usernames, can be partial or empty.
     * @param emailFilter    The filter for email addresses, can be partial or empty.
     * @return A paginated list of ConfirmationTokens matching the specified criteria.
     */
    public Page<ConfirmationToken> getPage(final int page, final int size, final String sortBy, final String sortOrder, final String usernameFilter, final String emailFilter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
        int offset = page * size;
        String sortByParam = getSortByParam(sortBy);
        String sortOrderParam = sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        List<ConfirmationToken> collection = TokenApiMapper.toConfirmationTokens(
                repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, usernameFilter, emailFilter)
        );
        long filteredColSize = repository.getFiltersCount(usernameFilter, emailFilter);
        return PageableExecutionUtils.getPage(collection, pageable, () -> filteredColSize);
    }

    /**
     * Retrieves a paginated and sorted list of valid confirmation tokens.
     *
     * @param page      the page number to retrieve (zero-based index)
     * @param size      the number of items per page
     * @param sortBy    the field by which the results should be sorted
     * @param sortOrder the sort order, either "asc" for ascending or "desc" for descending
     * @return a page containing the valid confirmation tokens based on the specified pagination and sorting parameters
     */
    public Page<ConfirmationToken> getValid(final int page, final int size, final String sortBy, final String sortOrder) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
        int offset = page * size;
        String sortByParam = getSortByParam(sortBy);
        String sortOrderParam = sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        List<ConfirmationToken> collection = TokenApiMapper.toConfirmationTokens(
                repository.findValid(offset, size, sortByParam, sortOrderParam)
        );
        long filteredColSize = repository.getValidCount();
        return PageableExecutionUtils.getPage(collection, pageable, () -> filteredColSize);
    }

    /**
     * Retrieves a confirmation token by its token ID.
     *
     * @param tokenId the unique identifier of the token to retrieve
     * @return the confirmation token if found, or null if no token with the specified ID exists
     */
    public ConfirmationToken get(final long tokenId) {
        Optional<TokenEntity> tokenEntity = repository.findById(tokenId);
        return tokenEntity.map(TokenApiMapper::toConfirmationToken).orElse(null);
    }

    /**
     * Saves a confirmation token associated with a specific user ID and retrieves the saved token.
     *
     * @param userId the unique identifier of the user for whom the token is being created
     * @param token  the token string to be associated with the user
     * @return the saved confirmation token
     */
    public ConfirmationToken save(final long userId, final String token) {
        boolean userExist = userRepository.isValidId(userId);
        if (!userExist) {return null;}
        long tokenId = repository.getNextId();
        if (tokenId <= 0) {
            return null;
        }
        repository.save(tokenId, userId, token);
        return this.get(tokenId);
    }

    /**
     * Deletes a token identified by the given token ID associated with the specified user ID.
     *
     * @param tokenId the ID of the token to be deleted
     * @param userId the ID of the user associated with the token
     * @return true if the token was successfully deleted, false otherwise
     */
    public boolean delete(final long tokenId, final long userId) {
        Optional<TokenEntity> entity = repository.findById(tokenId);
        if (entity.isPresent() && entity.get().getTokenId() > 0) {
            repository.deleteById(tokenId, userId);
            return true;
        }
        return false;
    }

    /**
     * Maps the provided sort key to the corresponding database column name.
     *
     * @param sortBy the string representing the field by which the sorting should be performed (e.g., "username", "email").
     * @return the corresponding database column for the provided sort key. Defaults to "u.user_id" if no match is found.
     */
    private String getSortByParam(String sortBy) {
        if ("username".equalsIgnoreCase(sortBy)) return "u.username";
        else if ("email".equalsIgnoreCase(sortBy)) return "u.email";
        return "u.user_id";
    }

}
