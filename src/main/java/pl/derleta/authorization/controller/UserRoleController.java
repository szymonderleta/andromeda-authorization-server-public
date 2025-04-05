package pl.derleta.authorization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.derleta.authorization.AndromedaAuthorizationServerApplication;
import pl.derleta.authorization.controller.assembler.UserRoleModelAssembler;
import pl.derleta.authorization.domain.model.UserRole;
import pl.derleta.authorization.domain.response.UserRoleResponse;
import pl.derleta.authorization.service.UserRoleService;

/**
 * The UserRoleController class provides REST endpoints for managing user-role associations.
 * It allows retrieving, creating, and deleting user-role mappings while enforcing role-based access control.
 */
@RestController
@RequestMapping("/api/v1")
public class UserRoleController {

    public static final String DEFAULT_PATH = "table/user-role";

    private final UserRoleService service;
    private final UserRoleModelAssembler userRoleModelAssembler;
    private final PagedResourcesAssembler<UserRole> pagedResourcesAssembler;

    @Autowired
    public UserRoleController(UserRoleService service, UserRoleModelAssembler userRoleModelAssembler, PagedResourcesAssembler<UserRole> pagedResourcesAssembler) {
        this.service = service;
        this.userRoleModelAssembler = userRoleModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    /**
     * Retrieves a paginated list of user roles based on the provided filtering, sorting, and pagination parameters.
     *
     * @param page           the page number to retrieve, starts from 0. Defaults to 0 if not specified.
     * @param size           the number of records per page. Defaults to the application's default page size if not specified.
     * @param sortBy         the field to sort the results by. Defaults to "userId" if not specified.
     * @param sortOrder      the order of sorting: "asc" for ascending, "desc" for descending. Defaults to "asc" if not specified.
     * @param usernameFilter an optional filter for the username field. Defaults to an empty string if not specified.
     * @param emailFilter    an optional filter for the email field. Defaults to an empty string if not specified.
     * @param roleNameFilter an optional filter for the role name field. Defaults to an empty string if not specified.
     * @return a ResponseEntity containing a PagedModel of UserRoleResponse objects.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER') or hasRole('ROLE_MODERATOR') or hasRole('ROLE_USER')")
    @GetMapping(value = "/" + DEFAULT_PATH, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<PagedModel<UserRoleResponse>> getPage(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "" + AndromedaAuthorizationServerApplication.DEFAULT_PAGE_SIZE) int size,
                                                                @RequestParam(defaultValue = "userId") String sortBy,
                                                                @RequestParam(defaultValue = "asc") String sortOrder,
                                                                @RequestParam(defaultValue = "") String usernameFilter,
                                                                @RequestParam(defaultValue = "") String emailFilter,
                                                                @RequestParam(defaultValue = "") String roleNameFilter) {
        Page<UserRole> userRolesPage = service.getPage(page, size, sortBy, sortOrder, usernameFilter, emailFilter, roleNameFilter);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(userRolesPage, userRoleModelAssembler));
    }

    /**
     * Retrieves a user role by its unique identifier.
     *
     * @param id the unique identifier of the user role to retrieve
     * @return ResponseEntity containing the user role data if found, or a 404 status if not found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER') or hasRole('ROLE_MODERATOR') or hasRole('ROLE_USER')")
    @GetMapping(value = "/" + DEFAULT_PATH + "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<UserRoleResponse> get(@PathVariable Integer id) {
        UserRole userRole = service.get(id);
        if (userRole == null) {
            return ResponseEntity.notFound().build();
        }
        UserRoleResponse response = userRoleModelAssembler.toModel(userRole);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Creates an association between a user and a role using the provided user ID and role ID.
     * Only accessible by users with the "ROLE_ADMIN" role.
     *
     * @param userId the ID of the user to be associated with the role
     * @param roleId the ID of the role to be associated with the user
     * @return a {@link ResponseEntity} containing the created {@link UserRoleResponse} and a status code of 201 (CREATED)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/" + DEFAULT_PATH + "/{userId}/{roleId}")
    public ResponseEntity<UserRoleResponse> add(@PathVariable Long userId, @PathVariable Integer roleId) {
        UserRole userRole = service.save(userId, roleId);
        if (userRole == null) {
            return ResponseEntity.notFound().build();
        }
        UserRoleResponse response = userRoleModelAssembler.toModel(userRole);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Deletes the association between a user and a role identified by the provided user ID and role ID.
     * Only accessible by users with the "ROLE_ADMIN" role.
     *
     * @param userId the ID of the user whose role association is to be deleted
     * @param roleId the ID of the role associated with the user to be deleted
     * @return a {@link ResponseEntity} with status code 200 (OK) if the deletion was successful,
     * or 404 (NOT FOUND) if no such association exists
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/" + DEFAULT_PATH + "/{userId}/{roleId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId, @PathVariable Integer roleId) {
        boolean success = service.delete(userId, roleId);
        return success ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
