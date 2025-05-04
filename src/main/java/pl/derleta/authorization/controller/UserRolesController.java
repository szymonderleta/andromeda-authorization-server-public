package pl.derleta.authorization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.derleta.authorization.controller.assembler.UserRolesModelAssembler;
import pl.derleta.authorization.domain.model.UserRoles;
import pl.derleta.authorization.domain.response.UserRolesResponse;
import pl.derleta.authorization.service.UserRolesService;

/**
 * REST controller for managing and retrieving user roles.
 * This controller provides an API endpoint to retrieve roles associated with a specific user,
 * with optional filtering and sorting capabilities.
 */
@RestController
@RequestMapping("/api/v1")
public class UserRolesController {

    public static final String DEFAULT_PATH = "table/user-roles";
    private final UserRolesService service;
    private final UserRolesModelAssembler userRolesModelAssembler;

    @Autowired
    public UserRolesController(UserRolesService service, UserRolesModelAssembler userRolesModelAssembler) {
        this.service = service;
        this.userRolesModelAssembler = userRolesModelAssembler;
    }

    /**
     * Retrieves the user roles for the specified user with optional sorting and filtering parameters.
     *
     * @param userId         the ID of the user whose roles are to be retrieved
     * @param roleNameFilter an optional filter string used to filter roles by name; defaults to an empty string
     * @param sortBy         the attribute by which the roles should be sorted; defaults to "roleId"
     * @param sortOrder      the order in which roles should be sorted, either "asc" for ascending or "desc" for descending; defaults to "asc"
     * @return a {@link ResponseEntity} containing a {@link UserRolesResponse} with the user's roles data
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER') or hasRole('ROLE_MODERATOR') or hasRole('ROLE_USER')")
    @GetMapping(value = "/" + DEFAULT_PATH + "/{userId}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<UserRolesResponse> get(@PathVariable Long userId,
                                                 @RequestParam(defaultValue = "") String roleNameFilter,
                                                 @RequestParam(defaultValue = "roleId") String sortBy,
                                                 @RequestParam(defaultValue = "asc") String sortOrder) {
        UserRoles userRoles = service.get(userId, sortBy, sortOrder, roleNameFilter);
        if (userRoles == null) {
            return ResponseEntity.notFound().build();
        }
        UserRolesResponse response = userRolesModelAssembler.toModel(userRoles);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
