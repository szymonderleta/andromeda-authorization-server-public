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
import pl.derleta.authorization.controller.assembler.UserModelAssembler;
import pl.derleta.authorization.domain.model.User;
import pl.derleta.authorization.domain.response.UserResponse;
import pl.derleta.authorization.service.UserService;

/**
 * Controller responsible for managing user-related operations,
 * such as retrieving, creating, updating, and deleting users.
 * <p>
 * This controller provides RESTful endpoints for interacting with the user data, allowing for
 * filtering, paging, and sorting. It uses HATEOAS principles for responses and enforces access control
 * using role-based authorization annotations.
 */
@RestController
@RequestMapping("/api/v1")
public class UserController {

    public static final String DEFAULT_PATH = "table/users";
    private final UserService service;
    private final UserModelAssembler userModelAssembler;
    private final PagedResourcesAssembler<User> pagedResourcesAssembler;

    @Autowired
    public UserController(UserService service, UserModelAssembler userModelAssembler, PagedResourcesAssembler<User> pagedResourcesAssembler) {
        this.service = service;
        this.userModelAssembler = userModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    /**
     * Retrieves a paginated list of users with optional filtering and sorting.
     *
     * @param page           the page number to retrieve (default is 0)
     * @param size           the number of users per page (default is the application's default page size)
     * @param sortBy         the field by which the users should be sorted (default is "userId")
     * @param sortOrder      the order of sorting, either "asc" for ascending or "desc" for descending (default is "asc")
     * @param usernameFilter an optional filter for users by username (default is an empty string, meaning no filter)
     * @param emailFilter    an optional filter for users by email (default is an empty string, meaning no filter)
     * @return a ResponseEntity containing a PagedModel of UserResponse objects representing the fetched page of users
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER') or hasRole('ROLE_MODERATOR') or hasRole('ROLE_USER')")
    @GetMapping(value = "/" + DEFAULT_PATH, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<PagedModel<UserResponse>> getPage(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "" + AndromedaAuthorizationServerApplication.DEFAULT_PAGE_SIZE) int size,
                                                            @RequestParam(defaultValue = "userId") String sortBy,
                                                            @RequestParam(defaultValue = "asc") String sortOrder,
                                                            @RequestParam(defaultValue = "") String usernameFilter,
                                                            @RequestParam(defaultValue = "") String emailFilter) {
        Page<User> usersPage = service.getPage(page, size, sortBy, sortOrder, usernameFilter, emailFilter);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(usersPage, userModelAssembler));
    }

    /**
     * Retrieves a user resource by its unique identifier.
     *
     * @param id the unique identifier of the user to be retrieved
     * @return a ResponseEntity containing the user resource wrapped in a UserResponse object and an HTTP status code
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER') or hasRole('ROLE_MODERATOR') or hasRole('ROLE_USER')")
    @GetMapping(value = "/" + DEFAULT_PATH + "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<UserResponse> get(@PathVariable Integer id) {
        User user = service.get(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        UserResponse response = userModelAssembler.toModel(user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Adds a new user to the system. The user details are provided in the request body.
     * This operation can be performed by users with the roles "ROLE_ADMIN", "ROLE_MODERATOR", "ROLE_TESTER", or "ROLE_USER".
     *
     * @param user the details of the user to be added
     * @return a {@code ResponseEntity} containing the created {@code UserResponse} and a status of {@code HttpStatus.CREATED}
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER') or hasRole('ROLE_MODERATOR') or hasRole('ROLE_USER')")
    @PostMapping("/" + DEFAULT_PATH)
    public ResponseEntity<UserResponse> add(@RequestBody User user) {
        UserResponse response = userModelAssembler.toModel(
                service.save(user)
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing user with the specified ID using the provided user details.
     * This operation is restricted to users with the "ROLE_MODERATOR" or "ROLE_ADMIN" roles.
     *
     * @param id   the ID of the user to be updated
     * @param user the new details of the user to update
     * @return a {@code ResponseEntity} containing the updated user details wrapped in {@code UserResponse} and a status of {@code HttpStatus.OK}
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MODERATOR')")
    @PutMapping("/" + DEFAULT_PATH + "/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody User user) {
        User existingUser = service.update(id, user);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }
        UserResponse response = userModelAssembler.toModel(existingUser);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Deletes a user with the specified ID.
     * This operation is only allowed for users with the "ROLE_ADMIN" role.
     *
     * @param id the ID of the user to be deleted
     * @return a {@code ResponseEntity} with {@code HttpStatus.OK} if the user was successfully deleted,
     * or {@code HttpStatus.NOT_FOUND} if the user was not found
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/" + DEFAULT_PATH + "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean success = service.delete(id);
        return success ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
