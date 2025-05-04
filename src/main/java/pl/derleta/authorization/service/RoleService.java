package pl.derleta.authorization.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import pl.derleta.authorization.controller.mapper.RoleApiMapper;
import pl.derleta.authorization.domain.entity.RoleEntity;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.repository.impl.RoleRepository;

import java.util.HashSet;
import java.util.Set;

/**
 * Service class for managing roles in the system. Provides methods for CRUD operations
 * and additional functionalities such as filtering, pagination, and sorting of roles.
 * This service interacts with the repository layer and handles the business logic for roles.
 */
@Service
public class RoleService {

    private RoleRepository repository;

    @Autowired
    public void setRepository(RoleRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves a set of roles filtered by the specified role name criteria using a SQL-based filter.
     *
     * @param roleNameFilter the filter criteria used to match role names.
     * @return a set of roles that match the given filter criteria.
     */
    public Set<Role> getList(final String roleNameFilter) {
        return RoleApiMapper.toRoles(
                new HashSet<>(repository.findAll(roleNameFilter))
        );
    }

    /**
     * Retrieves a paginated and filtered list of Role objects based on the specified parameters.
     *
     * @param page           the page number to retrieve, starting from 0
     * @param size           the number of elements per page
     * @param sortBy         the field to sort the results by
     * @param sortOrder      the order of sorting, either "asc" or "desc"
     * @param roleNameFilter a filter to apply on the role names
     * @return a {@code Page<Role>} object containing the filtered and paginated roles
     */
    public Page<Role> getPage(final int page, final int size, final String sortBy, final String sortOrder, final String roleNameFilter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortOrder), sortBy));
        int offset = page * size;
        String sortByParam = getSortByParam(sortBy);
        String sortOrderParam = sortOrder.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        Set<Role> collection = RoleApiMapper.toRoles(
                repository.getSortedPageWithFilters(offset, size, sortByParam, sortOrderParam, roleNameFilter)
        );
        int filteredColSize = repository.getFiltersCount(roleNameFilter);
        return PageableExecutionUtils.getPage(collection.stream().toList(), pageable, () -> filteredColSize);
    }

    /**
     * Converts the provided sort key to a database-compatible column name.
     *
     * @param sortBy the input sort key, typically provided as a string
     *               such as "roleName"
     * @return the corresponding database column name, such as "role_name",
     * or "role_id" as the default value
     */
    private String getSortByParam(String sortBy) {
        if ("roleName".equalsIgnoreCase(sortBy)) return "role_name";
        return "role_id";
    }

    /**
     * Retrieves a Role object corresponding to the given role ID.
     *
     * @param roleId the unique identifier of the role to retrieve
     * @return the Role object if found, or null if no role exists with the given ID
     */
    public Role get(final int roleId) {
        RoleEntity entity = repository.findById(roleId);
        if (entity == null) return null;
        return RoleApiMapper.toRole(entity);
    }

    /**
     * Saves a given Role object to the repository and assigns it a unique identifier.
     *
     * @param role the Role object to be saved
     * @return the saved Role object retrieved from the repository
     * @throws RuntimeException if saving the Role fails
     */
    public Role save(Role role) {
        int roleId = repository.getNextRoleId();
        int result = repository.save(roleId, role);
        if(result == 0) throw new RuntimeException("Failed to save role with id: " + roleId);
        return this.get(roleId);
    }

    /**
     * Updates an existing role identified by the specified role ID with new data.
     * The role must exist in the repository and have a valid role ID greater than 0
     * to be updated. If the update is successful, the updated Role is returned.
     * If the role does not exist, null is returned.
     *
     * @param roleId the unique identifier of the role to be updated
     * @param role   the new role data to update the identified role
     * @return the updated Role if the operation is successful, or null if the role does not exist
     */
    public Role update(int roleId, Role role) {
        RoleEntity entity = repository.findById(roleId);
        if (entity != null && entity.getRoleId() > 0) {
            repository.update(roleId, role);
            return this.get(roleId);
        }
        return null;
    }

    /**
     * Deletes a role identified by the specified role ID from the repository.
     *
     * @param roleId the unique identifier of the role to be deleted
     * @return true if the role is successfully deleted, false if the role does not exist or could not be deleted
     */
    public boolean delete(final int roleId) {
        RoleEntity entity = repository.findById(roleId);
        if (entity != null && entity.getRoleId() > 0) {
            repository.deleteById(roleId);
            return true;
        }
        return false;
    }

}
