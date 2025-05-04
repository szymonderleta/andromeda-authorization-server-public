package pl.derleta.authorization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.derleta.authorization.AndromedaAuthorizationServerApplication;
import pl.derleta.authorization.controller.assembler.RoleModelAssembler;
import pl.derleta.authorization.domain.model.AccessToken;
import pl.derleta.authorization.domain.model.Role;
import pl.derleta.authorization.domain.response.AccessTokenResponse;
import pl.derleta.authorization.domain.response.RoleResponse;
import pl.derleta.authorization.service.RoleService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * RoleController is a REST controller that manages CRUD operations for roles.
 * It provides endpoints to create, retrieve, update, and delete roles, as well as
 * retrieve roles using SQL filters with pagination.
 * <p>
 * This controller uses HAL-compliant responses and supports role-based access control
 * for different endpoints.
 */
@RestController
@RequestMapping("/api/v1")
public class RoleController {

    public static final String DEFAULT_PATH = "table/roles";

    private final RoleService service;
    private final RoleModelAssembler roleModelAssembler;
    private final PagedResourcesAssembler<Role> pagedResourcesAssembler;

    @Autowired
    public RoleController(RoleService service, RoleModelAssembler roleModelAssembler, PagedResourcesAssembler<Role> pagedResourcesAssembler) {
        this.service = service;
        this.roleModelAssembler = roleModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }


    /**
     * Retrieves a list of roles filtered by the provided role name filter.
     * This endpoint is accessible to users with admin, tester, moderator, or user roles.
     *
     * @param roleNameFilter a string to filter roles by name; defaults to an empty string if not provided
     * @return a ResponseEntity containing a CollectionModel of RoleResponse objects representing the roles
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER') or hasRole('ROLE_MODERATOR') or hasRole('ROLE_USER')")
    @GetMapping(value = "/" + DEFAULT_PATH + "/list", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<CollectionModel<RoleResponse>> getList(@RequestParam(defaultValue = "") String roleNameFilter) {
        Set<Role> rolesList = service.getList(roleNameFilter);
        List<RoleResponse> collection = rolesList.stream().map(roleModelAssembler::toModel).collect(Collectors.toList());
        Link link = linkTo(RoleController.class).slash(DEFAULT_PATH + "/list").withSelfRel();
        CollectionModel<RoleResponse> result = CollectionModel.of(collection, link);
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves a paginated list of roles based on the provided filter criteria.
     *
     * @param page           the page number to retrieve, default is 0
     * @param size           the number of items per page, default is the application's default page size
     * @param sortBy         the field by which the results should be sorted, default is "roleId"
     * @param sortOrder      the sorting order, either "asc" for ascending or "desc" for descending, default is "asc"
     * @param roleNameFilter the optional filter to apply on role names, default is an empty string (no filter)
     * @return a {@link ResponseEntity} containing a {@link PagedModel} of {@link RoleResponse} objects
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER') or hasRole('ROLE_MODERATOR') or hasRole('ROLE_USER')")
    @GetMapping(value = "/" + DEFAULT_PATH, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<PagedModel<RoleResponse>> getPage(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "" + AndromedaAuthorizationServerApplication.DEFAULT_PAGE_SIZE) int size,
                                                            @RequestParam(defaultValue = "roleId") String sortBy,
                                                            @RequestParam(defaultValue = "asc") String sortOrder,
                                                            @RequestParam(defaultValue = "") String roleNameFilter) {
        Page<Role> rolesPage = service.getPage(page, size, sortBy, sortOrder, roleNameFilter);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(rolesPage, roleModelAssembler));
    }


    /**
     * Retrieves a role by its unique identifier.
     *
     * @param id the unique identifier of the role to retrieve
     * @return a {@link ResponseEntity} containing the role details as a {@link RoleResponse}
     *         object with an HTTP status of OK if found, or an HTTP status of NOT FOUND if the role does not exist
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TESTER') or hasRole('ROLE_MODERATOR') or hasRole('ROLE_USER')")
    @GetMapping(value = "/" + DEFAULT_PATH + "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<RoleResponse> get(@PathVariable Integer id) {
        Role role = service.get(id);
        if (role == null) {
            return ResponseEntity.notFound().build();
        }
        RoleResponse response = roleModelAssembler.toModel(role);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Adds a new role by processing the provided role entity.
     *
     * @param role The Role object to be added.
     * @return A ResponseEntity containing the RoleResponse object along with the HTTP status CREATED.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/" + DEFAULT_PATH)
    public ResponseEntity<RoleResponse> add(@RequestBody Role role) {
        RoleResponse response = roleModelAssembler.toModel(
                service.save(role)
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing role identified by its ID with the provided role details.
     *
     * @param id   the ID of the role to be updated
     * @param role the updated details of the role
     * @return a ResponseEntity containing the updated RoleResponse and HttpStatus.OK
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/" + DEFAULT_PATH + "/{id}")
    public ResponseEntity<RoleResponse> update(@PathVariable Integer id, @RequestBody Role role) {
        Role updatedRole = service.update(id, role);
        if (updatedRole == null) {
            return ResponseEntity.notFound().build();
        }
        RoleResponse response = roleModelAssembler.toModel(updatedRole);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Deletes a resource identified by the specified ID.
     * The operation is restricted to users with the "ROLE_ADMIN" role.
     *
     * @param id the unique identifier of the resource to be deleted
     * @return a ResponseEntity containing a status of OK if the resource was successfully deleted,
     * or NOT_FOUND if the resource does not exist
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/" + DEFAULT_PATH + "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        boolean success = service.delete(id);
        return success ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
