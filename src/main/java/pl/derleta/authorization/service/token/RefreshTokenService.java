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
import pl.derleta.authorization.domain.model.RefreshToken;
import pl.derleta.authorization.repository.impl.UserRepository;
import pl.derleta.authorization.repository.impl.token.RefreshTokenRepository;

import java.util.List;
import java.util.Optional;


/**
 * Service for managing RefreshToken entities. Provides functionality for
 * retrieving, saving, deleting, and managing refresh tokens with associated filters
 * and pagination support.
 * <p>
 * This service interacts with the {@code RefreshTokenRepository} to perform
 * persistence operations and supports sorting and filtering to accommodate
 * various business requirements.
 */
@Service
public class RefreshTokenService {

    private RefreshTokenRepository repository;
    private UserRepository userRepository;

    @Autowired
    public void setRepository(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a paginated and sorted collection of RefreshToken objects using the specified filters.
     *
     * @param page           the zero-based page index
     * @param size           the number of items per page
     * @param sortBy         the property used for sorting
     * @param sortOrder      the order of sorting, can be "asc" or "desc"
     * @param usernameFilter the username filter to apply to the result
     * @param emailFilter    the email filter to apply to the result
     * @return a paginated collection of RefreshToken objects matching the specified filters
     */
    public Page<RefreshToken> getPage(final int page, final int size, final String sortBy, final String sortOrder, final String usernameFilter, final String emailFilter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
        int offset = page * size;
        String sortByParam = getSortByParam(sortBy);
        String sortOrderParam = sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        List<RefreshToken> collection = TokenApiMapper.toRefreshTokens(
                repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, usernameFilter, emailFilter)
        );
        long filteredColSize = repository.getFiltersCount(usernameFilter, emailFilter);
        return PageableExecutionUtils.getPage(collection, pageable, () -> filteredColSize);
    }

    /**
     * Retrieves a paginated list of valid refresh tokens, sorted by the specified parameters.
     *
     * @param page      the page number to retrieve (zero-based index)
     * @param size      the number of items per page
     * @param sortBy    the attribute to sort the results by
     * @param sortOrder the order of sorting, either "asc" for ascending or "desc" for descending
     * @return a paginated list of valid refresh tokens
     */
    public Page<RefreshToken> getValid(final int page, final int size, final String sortBy, final String sortOrder) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
        int offset = page * size;
        String sortByParam = getSortByParam(sortBy);
        String sortOrderParam = sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        List<RefreshToken> collection = TokenApiMapper.toRefreshTokens(
                repository.findValid(offset, size, sortByParam, sortOrderParam)
        );
        long filteredColSize = repository.getValidCount();
        return PageableExecutionUtils.getPage(collection, pageable, () -> filteredColSize);
    }

    /**
     * Retrieves a refresh token associated with the given token ID.
     *
     * @param tokenId the ID of the token to retrieve
     * @return a RefreshToken object if the token is found, or null if not found
     */
    public RefreshToken get(final long tokenId) {
        Optional<TokenEntity> entity = repository.findById(tokenId);
        return entity.map(TokenApiMapper::toRefreshToken).orElse(null);
    }

    /**
     * Saves a new refresh token associated with the given user ID and token value.
     *
     * @param userId the unique identifier of the user associated with the token
     * @param token  the token string to be saved
     * @return the saved RefreshToken object
     */
    public RefreshToken save(final long userId, final String token) {
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
     * Deletes a token identified by its ID and user ID from the repository.
     *
     * @param tokenId the unique identifier of the token to be deleted
     * @param userId the unique identifier of the user associated with the token
     * @return {@code true} if the token is successfully found and deleted, {@code false} otherwise
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
     * Converts a given sort by parameter to its corresponding database column name.
     *
     * @param sortBy the sort parameter provided by the user, such as "username" or "email"
     * @return the corresponding database column name, e.g., "u.username" for "username",
     * or "u.email" for "email". Defaults to "u.user_id" if the provided parameter does not match.
     */
    private String getSortByParam(String sortBy) {
        if ("username".equalsIgnoreCase(sortBy)) return "u.username";
        else if ("email".equalsIgnoreCase(sortBy)) return "u.email";
        return "u.user_id";
    }

}
