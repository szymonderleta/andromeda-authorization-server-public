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
import pl.derleta.authorization.domain.model.AccessToken;
import pl.derleta.authorization.repository.impl.UserRepository;
import pl.derleta.authorization.repository.impl.token.AccessTokenRepository;

import java.util.List;
import java.util.Optional;


/**
 * Service for managing access tokens. This class provides methods to retrieve,
 * save, and delete tokens, as well as to perform operations like filtering and
 * sorting token data.
 */
@Service
public class AccessTokenService {

    private AccessTokenRepository repository;
    private UserRepository userRepository;

    @Autowired
    public void setRepository(AccessTokenRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a paginated and sorted list of AccessToken objects filtered by specified criteria.
     *
     * @param page the page number to retrieve, starting from 0
     * @param size the number of items per page
     * @param sortBy the property by which to sort the records
     * @param sortOrder the order of sorting, either "asc" for ascending or "desc" for descending
     * @param usernameFilter a filter parameter to match against the username field
     * @param emailFilter a filter parameter to match against the email field
     * @return a paginated {@code Page} containing the filtered and sorted AccessToken objects
     */
    public Page<AccessToken> getPage(final int page, final int size, final String sortBy, final String sortOrder, final String usernameFilter, final String emailFilter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
        int offset = page * size;
        String sortByParam = getSortByParam(sortBy);
        String sortOrderParam = sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        List<AccessToken> collection = TokenApiMapper.toAccessTokens(
                repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, usernameFilter, emailFilter)
        );
        long filteredColSize = repository.getFiltersCount(usernameFilter, emailFilter);
        return PageableExecutionUtils.getPage(collection, pageable, () -> filteredColSize);
    }

    /**
     * Retrieves a paginated and sorted list of valid access tokens from the repository.
     *
     * @param page the zero-based page index to retrieve
     * @param size the number of records per page
     * @param sortBy the field to be used for sorting the results
     * @param sortOrder the order of sorting, either "asc" for ascending or "desc" for descending
     * @return a Page object containing the list of valid access tokens for the specified page and size
     */
    public Page<AccessToken> getValid(final int page, final int size, final String sortBy, final String sortOrder) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
        int offset = page * size;
        String sortByParam = getSortByParam(sortBy);
        String sortOrderParam = sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        List<AccessToken> collection = TokenApiMapper.toAccessTokens(
                repository.findValid(offset, size, sortByParam, sortOrderParam)
        );
        long filteredColSize = repository.getValidCount();
        return PageableExecutionUtils.getPage(collection, pageable, () -> filteredColSize);
    }

    /**
     * Retrieves an access token based on the provided token ID.
     *
     * @param tokenId the unique identifier of the token to retrieve
     * @return the AccessToken object corresponding to the given token ID, or null if no matching token is found
     */
    public AccessToken get(final long tokenId) {
        Optional<TokenEntity> entity = repository.findById(tokenId);
        return entity.map(TokenApiMapper::toAccessToken).orElse(null);
    }

    /**
     * Saves a new access token for a specified user and returns the saved token.
     *
     * @param userId the ID of the user the token is associated with
     * @param token the access token to be saved
     * @return the saved AccessToken object
     */
    public AccessToken save(final long userId, final String token) {
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
     * Deletes a token associated with the specified token ID and user ID.
     *
     * @param tokenId the unique identifier of the token to be deleted
     * @param userId the unique identifier of the user initiating the deletion
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
     * Maps a given sortBy parameter to the corresponding database column name.
     *
     * @param sortBy the parameter used for sorting, such as "username" or "email".
     * @return the corresponding database column name, e.g., "u.username", "u.email", or "u.user_id" by default.
     */
    private String getSortByParam(String sortBy) {
        if ("username".equalsIgnoreCase(sortBy)) return "u.username";
        else if ("email".equalsIgnoreCase(sortBy)) return "u.email";
        return "u.user_id";
    }

}
