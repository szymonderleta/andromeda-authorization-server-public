package pl.derleta.authorization.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.derleta.authorization.controller.mapper.UserRolesApiMapper;
import pl.derleta.authorization.domain.model.UserRoles;
import pl.derleta.authorization.repository.impl.UserRolesRepository;

/**
 * This service class handles operations related to retrieving and managing user roles.
 * It interacts with the {@link UserRolesRepository} to fetch data and defines logic
 * for filtering, sorting, and mapping user roles to the appropriate domain model.
 */
@Service
public class UserRolesService {

    private UserRolesRepository repository;

    @Autowired
    public void setRepository(UserRolesRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves a {@link UserRoles} object containing user and roles data based on the provided parameters.
     * It fetches user roles from the repository and applies sorting and filtering criteria.
     *
     * @param userId the ID of the user whose roles are to be retrieved
     * @param sortBy the parameter used to specify the sorting column (e.g., role name or role ID)
     * @param sortOrder the order of sorting, either "ASC" for ascending or "DESC" for descending
     * @param roleNameFilter a filter string used to match role names (supports partial matching)
     * @return a {@link UserRoles} object containing the user and their associated roles
     */
    public UserRoles get(final Long userId, final String sortBy, final String sortOrder, final String roleNameFilter) {
        String sortByValue = getSortByParam(sortBy);
        String sortOrderParam = sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        return UserRolesApiMapper.toUserRoles(
                repository.get(userId, sortByValue, sortOrderParam, roleNameFilter)
        );
    }

    /**
     * Translates the provided sortBy parameter to a database column reference.
     * If the input matches "roleName" (case-insensitive), it returns "r.role_name".
     * Otherwise, it defaults to "r.role_id".
     *
     * @param sortBy the sorting parameter provided as input, typically used
     *               to specify the sorting column for querying the database
     * @return the database column name corresponding to the provided sortBy parameter
     */
    private String getSortByParam(String sortBy) {
        if ("roleName".equalsIgnoreCase(sortBy)) return "r.role_name";
        return "r.role_id";
    }

}
